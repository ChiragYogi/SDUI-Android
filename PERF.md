# PERF.md

Status: **methodology only**. Numbers land after the measure → optimize →
re-measure pass (see `notes.md` for the running log as that happens).

## What's being compared

Two cold-start paths through the same `MainActivity`, same app, same
`benchmark` build variant (non-debuggable, release-shaped — see
`app/build.gradle.kts`; debug builds aren't representative, JIT/ART behave
differently):

- **SDUI** — default launch. `SduiScreen` reads `home_design.json` from
  assets, parses it, renders via the registry.
- **Static** — launch with the `screen_variant=static` string extra.
  `StaticHomeScreen` — hand-written Compose, no schema/registry/parsing
  involved. See `notes.md` (10:40) for why this is genuinely independent code
  and not catalog/ components fed literal props (that would inflate the
  static side's cost with registry-lookup/prop-decode work it shouldn't pay).

Both are reachable from one installed APK; `:macrobenchmark`'s
`StartupBenchmark` launches the same package with and without the intent
extra. See `notes.md` (10:48) for why this replaced an earlier two-app/two-
product-flavor setup — `StartupMode.COLD` force-stops the whole process
before every iteration regardless, so one app with two launch paths gets an
equally genuine cold start for both; two installable apps were never
required for that guarantee.

## Metrics and how each is actually measured

| Metric | Brief's definition | Implementation |
|---|---|---|
| TTR | Cold open → page fully rendered above the fold | `StartupTimingMetric()` via Macrobenchmark, `StartupMode.COLD`. "Above the fold" proxy: `device.wait(Until.hasObject(By.textContains("Ahmedabad")))` — the header's location text, present in both variants' first screenful, confirms real content (not just a loading spinner) reached the screen. |
| TTI | Cold open → page scrollable and tappable | Not separately instrumented yet — Macrobenchmark's `StartupTimingMetric` gives time-to-initial-display/fully-drawn signals; TTI specifically (first successful scroll/tap) needs either a `reportFullyDrawn()` call site or a UiAutomator scroll-then-measure step. Pending. |
| Full page time | Open → all sections rendered | Not instrumented yet. Candidate approach: a second `Until` wait on the footer's text (`"better drives"`) after a programmatic scroll-to-bottom, or a custom trace section around the last `RenderNode` call. |
| SDUI breakdown | JSON fetch/parse time vs. view-build time | Not instrumented yet. `AssetScreenRepository.loadScreen` already separates read (`Dispatchers.IO`) from parse (`Dispatchers.Default`) — candidate approach is wrapping each in a named trace section (`androidx.tracing.Trace`) and pulling both out of the same Perfetto capture Macrobenchmark already takes, rather than adding ad-hoc `System.currentTimeMillis()` calls to app code. |
| Scroll perf | Dropped frames / jank while scrolling the full page | Not instrumented yet. Candidate: `FrameTimingMetric` + a `flingGesture`/scroll `measureBlock`, separate from the cold-start test. **Caveat already found**: `FrameTimingMetric` was tried for the startup test on this dev's device and dropped — its Perfetto trace parser throws (`"Observed frame in trace missing id"`) against this ROM's SurfaceFlinger tracing, a Macrobenchmark/OEM incompatibility, not something fixable in this codebase. If the scroll-perf run hits the same wall, the same caveat applies and will be reported honestly rather than worked around silently. |

## Methodology decisions and why

- **`StartupMode.COLD`** — force-stops the target process before every
  iteration on its own; no separate shell command needed. This is what makes
  "cold open" mean the same thing for both variants sharing one app (see
  above).
- **`CompilationMode.None()`**, not the default `Partial` — `Partial` resets
  ART compilation via `cmd package compile`, which this dev device's OEM
  shell blocks/mangles (`"Failed to cpmpile !"`). `None()` skips that reset,
  so numbers reflect whatever compilation state the device already has, not
  a clean AOT baseline. This is a real methodology gap, disclosed here
  rather than silently worked around — numbers from this device are not a
  clean-AOT comparison, only a same-device relative comparison between the
  two variants.
- **`iterations = 10`** per variant — Macrobenchmark's own recommendation
  for stable percentile numbers on a single device.
- **Device**: TBD at measurement time — a physical device is preferred over
  the development emulator (emulators are noisier and not representative of
  real JIT/ART/thermal behavior); whichever is used will be named here, not
  left implicit, per the brief's own ask ("device used, methodology").

## Overhead %

Pending real numbers.

## What was tried to optimize, and what happened

Pending the first measurement pass.
