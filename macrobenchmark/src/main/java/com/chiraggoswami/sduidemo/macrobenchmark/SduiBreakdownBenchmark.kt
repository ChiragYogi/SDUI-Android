package com.chiraggoswami.sduidemo.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

// Mirrors AssetScreenRepository's SECTION_ASSET_READ/SECTION_JSON_PARSE and SduiScreen's
// SECTION_VIEW_BUILD (android.os.Trace.beginSection/endSection pairs there).
private const val SECTION_ASSET_READ = "sdui_asset_read"
private const val SECTION_JSON_PARSE = "sdui_json_parse"
private const val SECTION_VIEW_BUILD = "sdui_view_build"

/**
 * SDUI-only: asset read vs JSON parse vs initial Compose tree build, as three named trace
 * sections summed per cold start. No static-variant equivalent — that path has none of these
 * phases, which is the whole point of the comparison (see PERF.md's SDUI breakdown row).
 *
 * [SECTION_VIEW_BUILD] specifically is a lower bound, not a "fully rendered" time — see the
 * caveat on SduiScreen.kt's ScrollableRoot. It captures composition of the initially-visible
 * tree only: not layout, not draw, not lazy_row's off-screen items, not AsyncImage decode.
 */
@OptIn(ExperimentalMetricApi::class)
class SduiBreakdownBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun phaseBreakdown() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(
            // Sum, not First/Last: each section fires once per cold start in this codebase
            // today, but Sum stays correct (rather than silently wrong) if a future
            // recomposition ever fires SECTION_VIEW_BUILD more than once in the same trace.
            TraceSectionMetric(SECTION_ASSET_READ, TraceSectionMetric.Mode.Sum),
            TraceSectionMetric(SECTION_JSON_PARSE, TraceSectionMetric.Mode.Sum),
            TraceSectionMetric(SECTION_VIEW_BUILD, TraceSectionMetric.Mode.Sum),
        ),
        // 5, not Macrobenchmark's usual 10 — this device's cold-start iterations are slow
        // (~1min+ each with COLD force-stop) and a couple of connectedBenchmarkAndroidTest runs
        // have hit unrelated device-side flakiness (Perfetto failing to stop cleanly) on long
        // runs; 5 iterations still gives a usable median without pushing single-run duration
        // into territory where that flakiness shows up more. Bump back to 10 for a final,
        // submission-grade number once numbers are otherwise settled.
        iterations = 5,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.None(),
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait(launchIntent())
        device.wait(Until.hasObject(By.textContains("Ahmedabad")), 5_000)
    }
}
