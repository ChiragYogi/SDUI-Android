package com.chiraggoswami.sduidemo.screen

import android.os.Trace
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chiraggoswami.sduidemo.catalog.AppRegistry
import com.chiraggoswami.sduidemo.core.render.ActionDispatcher
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.StateHolder
import com.chiraggoswami.sduidemo.ui.theme.SDUIDemoTheme
import kotlinx.coroutines.launch

// Intended for :macrobenchmark's ScrollBenchmark via By.res(packageName, HOME_SCROLL_ROOT_TAG) —
// testTagsAsResourceId is what makes a Compose testTag visible to UiAutomator at all. Verified
// working via a manual `adb shell am start` + `uiautomator dump` on the benchmark build, but
// unreliable from inside MacrobenchmarkRule's own UiDevice session on the physical test device
// (see ScrollBenchmark.kt's doc comment) — ScrollBenchmark currently falls back to
// By.scrollable(true) instead. Left wired up here since the manual path proves it's correct and
// it may still resolve on other devices/CI; not deleted over one device's flakiness.
private const val HOME_SCROLL_ROOT_TAG = "home_scroll_root"
private const val SECTION_VIEW_BUILD = "sdui_view_build"

@Composable
fun SduiScreen(screenId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: SduiScreenViewModel = viewModel(factory = SduiScreenViewModel.factory(context, screenId))
    when (val state = viewModel.uiState.collectAsState().value) {
        is ScreenUiState.Loading -> Box(modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is ScreenUiState.Error -> Box(modifier.fillMaxSize(), Alignment.Center) { Text("Failed to load: ${state.message}") }
        is ScreenUiState.Ready -> ScreenContent(state, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(ready: ScreenUiState.Ready, modifier: Modifier) {
    val stateHolder = remember { StateHolder(ready.schema.initialState) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val dispatcher = remember {
        ActionDispatcher(
            stateHolder,
            onShowSnackbar = { message -> coroutineScope.launch { snackbarHostState.showSnackbar(message) } },
        )
    }
    val ctx = remember { RenderContext(stateHolder, AppRegistry, dispatcher) }

    // schema.theme == "light" pins the screen to the light scheme regardless of system/dynamic
    // color, so a server-authored screen can't be handed unreviewed dark-mode contrast.
    val forceLight = ready.schema.theme == "light"
    SDUIDemoTheme(darkTheme = !forceLight && isSystemInDarkTheme(), dynamicColor = !forceLight) {
        Box(modifier.fillMaxSize()) {
            ScrollableRoot(ready, ctx)
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }

        stateHolder.activeSheet?.let { sheetId ->
            ready.schema.sheets[sheetId]?.let { sheetNode ->
                ModalBottomSheet(onDismissRequest = { stateHolder.dismissSheet() }) {
                    RenderNode(sheetNode, ctx)
                }
            }
        }
    }
}

/**
 * The scrollable root, `testTag`ged for :macrobenchmark's ScrollBenchmark, wrapping the
 * initial-composition trace section for :macrobenchmark's SduiBreakdownBenchmark, and reporting
 * "fully drawn" via [ReportDrawnWhen] once this composition commits.
 *
 * [SECTION_VIEW_BUILD] alone is a lower bound (see PERF.md) — it only times synchronous
 * composition, not layout/draw/lazy-loaded items/async image decode. `reportFullyDrawn()` is
 * the actual fix for that gap: Android's TTID fires on *any* first frame (satisfied here by
 * SduiScreen's own Loading spinner, before this composable ever runs — see PERF.md's headline
 * finding), but TTFD (`timeToFullDisplayMs`, read by the same `StartupTimingMetric` already in
 * use) only fires once this happens, giving both variants a genuinely comparable "real content
 * is visible" signal.
 *
 * First cut called `Activity.reportFullyDrawn()` directly from `Modifier.onGloballyPositioned` —
 * wrong: that callback fires mid-layout, before the frame reaches the RenderThread, and
 * Macrobenchmark's trace query failed outright ("No RT frame slice associated with UI thread
 * frame slice ends after reportFullyDrawn"). `ReportDrawnWhen` (built on `FullyDrawnReporter`)
 * is the actual AndroidX-recommended API for this — it owns the correct timing internally
 * instead of a hand-rolled `ViewTreeObserver`/`view.post` guess at it.
 */
@Composable
private fun ScrollableRoot(ready: ScreenUiState.Ready, ctx: RenderContext) {
    var contentReady by remember { mutableStateOf(false) }
    ReportDrawnWhen { contentReady }
    Column(
        Modifier
            .testTag(HOME_SCROLL_ROOT_TAG)
            .semantics { testTagsAsResourceId = true }
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Compose doesn't allow try/catch around a composable call (compiler-enforced), so this
        // can't be try/finally-wrapped the way AssetScreenRepository's trace pairs are. Safe in
        // practice: RenderNode is designed never to throw for data problems (malformed props,
        // unknown types) — those skip-and-log — so only a genuine programming bug reaches here,
        // and that should crash loudly rather than be silently trace-safe.
        Trace.beginSection(SECTION_VIEW_BUILD)
        RenderNode(ready.schema.root, ctx)
        Trace.endSection()
    }
    // Runs after this composition commits — flips once, SideEffect is idempotent-safe for that.
    SideEffect { contentReady = true }
}
