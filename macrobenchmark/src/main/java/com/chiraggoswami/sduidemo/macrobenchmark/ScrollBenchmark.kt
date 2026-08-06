package com.chiraggoswami.sduidemo.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Scroll jank on the home screen's scroll root, 3 flings toward the bottom.
 *
 * The root is `Column` + `Modifier.verticalScroll`, not a `LazyColumn` (see
 * SduiScreen.kt/StaticHomeScreen.kt) — every section composes and lays out up front regardless
 * of viewport, so this measures draw/relayout cost during scroll, not incremental composition
 * (that only happens sideways, inside each `lazy_row`, as new items enter view — a 3-fling
 * vertical pass never reaches it).
 *
 * Targeting note: the root also carries a `testTag` exposed via `testTagsAsResourceId` (see
 * SduiScreen.kt) — `By.res(pkg, tag)` found it fine via a manual `adb shell am start` +
 * `uiautomator dump` on this exact benchmark APK, but consistently missed it from inside
 * MacrobenchmarkRule's own UiDevice session on this physical device (OnePlus/Oppo CPH2371,
 * Android 13), even with a 10s wait — a real, unresolved device-specific gap between the two
 * UiAutomator entry points, not an app bug (documented rather than silently worked around; see
 * notes.md). Falls back to `By.scrollable(true)`, disambiguated by picking the tallest match —
 * the vertical root spans nearly the full display height, every `lazy_row` rail only spans one
 * row's height, so height is a reliable, ambiguity-free discriminator between them.
 */
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollJank() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        // 5, not 10 — see SduiBreakdownBenchmark.kt for why. Bump back to 10 for a final,
        // submission-grade number once numbers are otherwise settled.
        iterations = 5,
        compilationMode = CompilationMode.None(),
        // No startupMode: this isn't a startup test — launch happens in setupBlock (not
        // measured) so cold-start cost never leaks into the FrameTimingMetric numbers below.
        setupBlock = {
            pressHome()
            startActivityAndWait(launchIntent())
            device.wait(Until.hasObject(By.textContains("Ahmedabad")), 10_000)
        },
    ) {
        val root = device.findObjects(By.scrollable(true)).maxByOrNull { it.visibleBounds.height() }
            ?: error("no scrollable container found on screen")
        repeat(3) {
            root.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}
