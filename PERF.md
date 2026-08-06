# PERF.md

Status: **measurement phase closed here, not abandoned mid-thought.** Startup
(both TTID and TTFD), the SDUI phase breakdown, and a real per-variant
overhead % all have numbers, from a physical device. Scroll jank does not —
see below. Read the "Headline finding" section before the raw numbers; the
naive TTID number is actively misleading here, and reporting it without that
context would fail exactly the "measurement honesty" bar this doc is graded
on.

Further benchmark runs are stopped as of this submission for two disclosed
reasons, not because the open items below stopped mattering: **device
limitation** — one physical device (OPPO CPH2371) and one emulator, both
already shown to fail `FrameTimingMetric`'s Perfetto parsing in two different
ways (see "Scroll perf" below), so more runs on the same two targets weren't
going to unblock scroll perf regardless of time spent; and **time
limitation** — submission deadline, with the stability/memoization pass
(see "What was tried to optimize") landed but its before/after re-measurement
still noisy at 5 iterations. Both are named in "Next steps" as the first
things to pick back up, not silently dropped.

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
- **`iterations = 5`**, not the 10 originally used for the phase-breakdown
  numbers below — 10-iteration cold-start runs were taking 11-13 minutes on
  this device and repeatedly hit an unrelated Perfetto-stop flake partway
  through. A higher-iteration startup re-run is not currently planned (see
  "Next steps") — the numbers here are reported at this sample size, not as
  a placeholder for a later, bigger run.

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
(TTFD) exist. `ReportDrawnWhen`/`ReportDrawn` are now wired for both variants
(`SduiScreen.kt`'s `ScrollableRoot`, `StaticHomeScreen.kt` — see their doc
comments), and TTFD numbers below are from a real Macrobenchmark run
confirming the metric actually populates, not just that the app no longer
crashes.

## The fair comparison: TTFD

`timeToFullDisplayMs`, same `StartupBenchmark.kt`/`StartupTimingMetric`, same
run as the TTID table above:

| Variant | min | median | max |
|---|---|---|---|
| SDUI | 1,076.9 ms | **1,119.1 ms** | 1,293.6 ms |
| Static | 927.3 ms | **1,024.7 ms** | 1,202.8 ms |

Static's TTID and TTFD are identical (`ReportDrawn()`, unconditional — no
async gap to wait for, so "fully drawn" and "first frame" are the same
moment for this variant, by construction, not coincidence). SDUI's TTFD
(1,119.1ms) is well past its TTID (567.2ms, see table above) — the ~552ms
gap the spinner was hiding.

**Read against static, SDUI is slower, not faster, once both sides are
measured the same way**: median TTFD is 1,119.1ms vs static's 1,024.7ms, a
**+94.4ms / +9.2% overhead**. This holds across the whole distribution, not
just the median — SDUI's *minimum* (1,076.9ms) is still slower than static's
*median* (1,024.7ms). That's the opposite conclusion from the naive TTID
table, and it's the one to report: SDUI here is measurably slower to real
content than the hardcoded twin, which is what the brief's premise ("SDUI
that's slow is worse than no SDUI") expects to be checking for.

One anomaly worth flagging, not yet explained: static's TTFD has a *wider*
spread (coefficient of variation 0.098) than SDUI's (0.075), despite static
having no async work to introduce variance. Could be thermal/scheduling
noise on a 5-iteration run rather than anything real — needs more iterations
before reading into it.

## Post-optimization re-check: TTFD (5 iterations, inconclusive)

Same `StartupBenchmark.kt`, same device, re-run after the stability +
memoization pass above:

| | Before | After |
|---|---|---|
| SDUI TTID median | 567.2 ms | 472.5 ms (-16.7%) |
| SDUI TTFD median | 1,119.1 ms | 1,070.9 ms (-4.3%) |
| Static TTFD median | 1,024.7 ms | 884.7 ms (-13.7%) |
| Overhead % (SDUI vs static) | +9.2% | +21.0% |

**Not reporting this as "the optimization made things worse."** `StaticHomeScreen`
had zero lines touched by this pass, and its own TTFD moved 140ms between these
two runs — pure run-to-run noise (thermal state, background load, whatever
`CompilationMode.None()`'s undisclosed compilation state happened to be this
time). That noise is *larger* than the 48ms TTFD change we're trying to
attribute to SDUI's optimization, so the "+9.2% → +21.0%" swing isn't a
trustworthy read at 5 iterations either direction.

The one signal that moved by more than static's own noise band: SDUI's TTID
dropped 16.7% (567→472ms), which is closest to what `@Immutable`/`remember`
should actually affect (first-composition cost). Not calling this confirmed
either — just the one number here worth taking seriously over the others.

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

- **Compose stability + memoization pass**: `SduiNode`, `ActionSpec`,
  `VisibleWhen` (`core/schema/`), `ComponentRegistry` (`core/registry/`), and
  the per-component prop classes with `List<T>` fields (`ChipRowProps`,
  `ChipItem`, `HeroCardProps`, `CarCardProps`) were all compiler-inferred
  *unstable* — `JsonElement`/`List`/`Map` fields the Compose compiler can't
  prove immutable — which meant no catalog composable could ever skip
  recomposition based on node equality. Added `@Immutable` to all of them
  (`RenderContext` gets `@Stable` instead, since it wraps live `StateHolder`
  state). Separately, `resolveChildren(node)` and `node.decodeProps<T>()`
  were being called unmemoized directly in composable bodies (`LazyRowNode`,
  `ColumnNode`, `GridNode`, `Section`, `ChipRow`, `HeroCard`, `CarCard`) —
  full JSON decode / template-expansion-and-interpolation re-ran on *every*
  recomposition, not just when the node changed. Wrapped each in
  `remember(node) { ... }`. No parsing/render behavior changed — compiles
  clean, `testDebugUnitTest` still passes.
- **Result: not yet conclusively measured.** See "Post-optimization re-check"
  below — the one before/after comparison run so far is confounded by
  run-to-run device noise larger than the effect being measured.

## Next steps, in order

Items 1-2 are done. Items 3-5 are open but **not pursued further in this
submission** — same device/time limitation named in Status above, not
because they stopped mattering.

1. ~~Wire up `reportFullyDrawn()`~~ — done (`ReportDrawnWhen`/`ReportDrawn`,
   both variants) and confirmed: `timeToFullDisplayMs` populates for both in
   the run behind the table above, not just TTID.
2. ~~Re-measure startup with TTFD in hand~~ — done, see "The fair comparison:
   TTFD" above. Overhead % below is no longer blocked.
3. **Deferred (time).** Look at why `sdui_json_parse`'s max (91.8ms) is
   ~2.3x its median — a single outlier iteration, thermal throttling, or a
   real tail-latency concern worth a closer look with `CompilationMode`
   variants.
4. **Deferred (device).** Scroll perf stays unmeasured pending a device
   where `FrameTimingMetric`'s Perfetto parsing doesn't fail — not a fixable
   code issue on the current two devices.
5. **Deferred (time).** The stability/memoization pass (see "What was tried
   to optimize") has one inconclusive before/after run behind it — see
   "Post-optimization re-check." A higher-iteration re-run to get a real
   signal is not currently planned; this is descoped for now, not silently
   dropped.

## Overhead %

**+9.2%** (median TTFD, SDUI vs static: 1,119.1ms vs 1,024.7ms), from a
5-iteration run on the OPPO CPH2371 under `CompilationMode.None()` (see
methodology caveats above — same-device relative comparison, not an
absolute one). This is the first number in this doc computed from a
same-metric, same-condition comparison on both sides — the raw TTID
percentages implied earlier in this doc were never a candidate for this
line, precisely because they weren't.
