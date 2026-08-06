package com.chiraggoswami.sduidemo.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

private const val TARGET_PACKAGE = "com.chiraggoswami.sduidemo"
private const val MAIN_ACTIVITY = "$TARGET_PACKAGE.MainActivity"

// Mirrors MainActivity.EXTRA_SCREEN_VARIANT / VARIANT_STATIC, duplicated as a literal —
// this module talks to the target app only via Intent/UiAutomator, never its code.
private const val EXTRA_SCREEN_VARIANT = "screen_variant"
private const val VARIANT_STATIC = "static"

/**
 * Cold-start comparison: static (hardcoded) vs sdui (JSON-rendered) rendering of the same
 * home screen, both in one app, one Activity. COLD mode force-stops the whole process before
 * every iteration, so launching either variant is still a genuine cold start for that path —
 * no separate app/flavor needed just to get that guarantee.
 */
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupStatic() = coldStartup(variant = VARIANT_STATIC)

    @Test
    fun coldStartupSdui() = coldStartup(variant = null)

    private fun coldStartup(variant: String?) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
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
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(TARGET_PACKAGE, MAIN_ACTIVITY)
            addCategory(Intent.CATEGORY_LAUNCHER)
            variant?.let { putExtra(EXTRA_SCREEN_VARIANT, it) }
        }
        startActivityAndWait(intent)
        // Confirms the header rendered — same "above the fold" bar both variants must clear.
        device.wait(Until.hasObject(By.textContains("Ahmedabad")), 5_000)
    }
}
