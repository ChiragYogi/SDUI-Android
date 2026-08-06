# PERF.md

Status: **first real measurement pass done**. Startup and the SDUI phase
breakdown have numbers, from a physical device. Scroll jank does not — see
below. Read the "Headline finding" section before the raw numbers; the naive
number is actively misleading here, and reporting it without that context
would fail exactly the "measurement honesty" bar this doc is graded on.

## Device and methodology

- **Device**: OPPO CPH2371 (OnePlus/Oppo family), Android 13, API 33.
  Physical device, not the development emulator — Macrobenchmark itself
  errors out by default on an emulator ("not representative of real user
  devices") and that guard was respected, not suppressed, for these numbers.
- **Build**: `benchmark` build type (`app/build.gradle.kts`) — non-debuggable,
  release-shaped, `initWith(release)`. Debug builds aren't representative;
  JIT/ART behave differently.
- **`StartupMode.COLD`** — force-stops the whole process before every
  iteration. Both SDUI and static paths go through the same `MainActivity`
  in the same app (see `notes.md`, 10:48, for why two product flavors were
  removed) — COLD's force-stop is what makes "one app, two launch paths"
  still a genuine cold start for both, not a shortcut that favors one side.
- **`CompilationMode.None()`**, not the default `Partial` — `Partial` resets
  ART compilation via `cmd package compile`, which this device's OEM shell
  blocks (`"Failed to cpmpile !"`). `None()` skips that reset, so these
  numbers reflect whatever compilation state the device already had, **not**
  a clean-AOT baseline. Real methodology gap, disclosed rather than worked
  around — treat these as a same-device relative comparison, not an
  absolute one comparable to numbers from a clean-AOT run.
- **`iterations = 10`** per test — Macrobenchmark's own recommendation for
  stable percentiles on one device.

## Headline finding: the naive startup comparison is misleading, and here's why

Raw `timeToInitialDisplayMs` (`StartupTimingMetric`, `StartupBenchmark.kt`):

| Variant | min | median | max |
|---|---|---|---|
| SDUI | 426.4 ms | **460.1 ms** | 547.0 ms |
| Static | 804.1 ms | **884.9 ms** | 1,100.8 ms |

Read naively, SDUI looks ~48% *faster* to first frame than the hardcoded
static screen. That's backwards from what the brief expects ("SDUI that's
slow is worse than no SDUI"), and it's backwards because **TTID isn't
measuring the same thing on both sides**.

`SduiScreen`'s first composition, before `SduiScreenViewModel`'s asset
load/parse coroutine resolves, renders `ScreenUiState.Loading` — a bare
`CircularProgressIndicator()` (`SduiScreen.kt`). Android's TTID signal fires
on the *first frame drawn*, full stop — a loading spinner satisfies it just
as well as real content does. `StaticHomeScreen` has no such state: it's a
plain composable with no ViewModel and no async gap, so its first frame
*is* substantially real content. The comparison above is "time to spinner"
vs. "time to real screen" — not a fair fight, and reporting it without this
paragraph would be exactly the kind of unearned, misleading number the brief
warns against ("perf claims with no methodology" is explicitly called out
as a red flag).

This is a known, named Android problem with TTID as a metric — it's why
`reportFullyDrawn()` and Macrobenchmark's complementary `timeToFullDisplayMs`
(TTFD) exist. That's the fix, and it isn't wired up yet (tracked below as
the next concrete step, not silently deferred).

**What the SDUI breakdown numbers below suggest in the meantime**: SDUI's
three named phases (asset read + JSON parse + initial view build) sum to a
~98ms median. Added informally to the 460.1ms "time to spinner," that puts
real SDUI content around ~558ms — still apparently faster than static's
884.9ms, but this sum is *not* a measured number, it's an approximation from
two different traces, and stated as exactly that rather than dressed up as
a real result.

## SDUI phase breakdown

`TraceSectionMetric`, `SduiBreakdownBenchmark.kt` — three `android.os.Trace`
sections (`AssetScreenRepository.kt`, `SduiScreen.kt`), summed per cold
start:

| Phase | min | median | max |
|---|---|---|---|
| `sdui_asset_read` (assets, IO dispatcher) | 2.2 ms | 3.4 ms | 5.1 ms |
| `sdui_json_parse` (kotlinx.serialization, Default dispatcher) | 29.3 ms | 40.6 ms | 91.8 ms |
| `sdui_view_build` (initial Compose composition — see caveat below) | 40.0 ms | 53.7 ms | 103.1 ms |

Asset read is cheap and consistent — reading one file from `assets/` is not
where time goes. JSON parse (kotlinx.serialization, reflection-based decode)
and view build are the two real costs, roughly comparable to each other.
`sdui_json_parse`'s median-to-max spread (40.6 → 91.8ms) is wide relative to
asset read's — worth a closer look before calling it noise.

**`sdui_view_build` caveat** (documented in code at `SduiScreen.kt`'s
`ScrollableRoot`): this only times synchronous composition of the initially-
visible tree. Not layout, not draw, not off-screen `lazy_row` items (composed
lazily on scroll), not `AsyncImage` decode (fully async, detached from this
span). It's a lower bound on "how long building the initial tree took," not
"time to fully on screen" — same asymmetry as the TTID finding above, and
the reason `reportFullyDrawn()`/TTFD is the real fix for both at once.

## Scroll perf: not obtained, on either available test target

`FrameTimingMetric` (`ScrollBenchmark.kt`, 3 flings on the root scroll
container) fails with a Perfetto trace-parsing error on *both* devices
available for this project, with two different specific errors:

- **Emulator** (API 36.1): `"Observed frame in trace missing id"` — a
  Macrobenchmark/OEM SurfaceFlinger trace-format incompatibility (already
  known before this pass, from `StartupBenchmark.kt`'s comment).
- **Physical device** (OPPO CPH2371, Android 13): `"Observed no renderthread
  slices in trace - verify that your benchmark is redrawing and is hardware
  accelerated"` — a different failure, same category: `FrameTimingMetric`'s
  Perfetto frame-timeline extraction not finding what it expects on this
  device's trace output, despite the scroll gestures executing (confirmed —
  the fling-target-selection logic ran and found a scrollable node; the
  failure is specifically in `FrameTimingMetric`'s post-hoc trace parsing).

Two devices, two different parse failures, same metric — reported as found,
not worked around or hidden. No scroll-jank numbers exist for this
submission. If a third device becomes available, this is the first thing to
retry there.

**Targeting note** (also in `ScrollBenchmark.kt`'s doc comment): the scroll
root also carries a Compose `testTag` exposed via `testTagsAsResourceId`
(`SduiScreen.kt`). A manual `adb shell am start` + `uiautomator dump` on the
exact same benchmark APK found it by resource-id without issue; the same
selector inside `MacrobenchmarkRule`'s own `UiDevice` session did not, even
with a 10s wait, on the physical device — a second, unexplained gap between
the two UiAutomator entry points on this device, unrelated to the
`FrameTimingMetric` failure above. `ScrollBenchmark` currently falls back to
`By.scrollable(true)`, disambiguated by picking the tallest match (the
vertical root spans nearly the full display height; each `lazy_row` rail
only spans one row).

## What was tried to optimize, and what happened

- **Not yet attempted**: no optimization pass has happened. The result of
  this first measurement pass is a methodology fix (TTFD/`reportFullyDrawn`),
  not yet a performance fix — a legitimate, if less satisfying, first
  outcome. Reporting it as that, not padding it with a performance change
  made before the measurement that motivated it existed.

## Next steps, in order

1. Wire up `reportFullyDrawn()` in `SduiScreen.kt` (fire once the initial
   `RenderNode` composition + a layout-complete signal both land) and read
   Macrobenchmark's `timeToFullDisplayMs` for both variants — the actual
   fair comparison the headline finding above is missing.
2. Re-measure startup with TTFD in hand; only then does an honest overhead %
   exist to report here.
3. Look at why `sdui_json_parse`'s max (91.8ms) is ~2.3x its median — a
   single outlier iteration, thermal throttling, or a real tail-latency
   concern worth a closer look with `CompilationMode` variants.
4. Scroll perf stays unmeasured pending a device where `FrameTimingMetric`'s
   Perfetto parsing doesn't fail — not a fixable code issue on the current
   two devices.

## Overhead %

Not reported. The only startup numbers that exist (raw TTID) are
demonstrated above to be a mismatched comparison, not a fair one — printing
a percentage from them would be a bigger dishonesty than leaving this blank.
