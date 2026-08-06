package com.chiraggoswami.sduidemo.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

private const val STATIC_PACKAGE = "com.chiraggoswami.sduidemo.static"
private const val SDUI_PACKAGE = "com.chiraggoswami.sduidemo.sdui"

/**
 * Cold-start comparison: static (hardcoded) vs sdui (JSON-rendered) build of the same
 * home screen. Both metrics and the force-stop between iterations come from the
 * Macrobenchmark instrumentation itself — no timing code in the app under test.
 */
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupStatic() = coldStartup(STATIC_PACKAGE)

    @Test
    fun coldStartupSdui() = coldStartup(SDUI_PACKAGE)

    private fun coldStartup(packageName: String) = benchmarkRule.measureRepeated(
        packageName = packageName,
        // FrameTimingMetric dropped for this device: its Perfetto trace parser throws
        // ("Observed frame in trace missing id") against this ROM's SurfaceFlinger tracing —
        // a Macrobenchmark/OEM trace-format incompatibility, not something fixable here.
        // Keeping it would abort the whole run with zero TTID data too, not just no frame data.
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        // COLD force-stops the target process before every iteration on its own —
        // no separate shell command or app-side hook needed for that.
        startupMode = StartupMode.COLD,
        // Partial (the default) resets ART compilation via `cmd package compile`, which this
        // OEM ROM's shell blocks/mangles ("Failed to cpmpile !"). None() skips that reset —
        // numbers reflect whatever compilation state the device already has, not a clean AOT
        // baseline. Documented in PERF.md as a methodology caveat, not silently worked around.
        compilationMode = CompilationMode.None(),
        setupBlock = {
            pressHome()
        },
    ) {
        startActivityAndWait()
        // Confirms the header rendered — same "above the fold" bar both variants must clear.
        device.wait(Until.hasObject(By.textContains("Ahmedabad")), 5_000)
    }
}
