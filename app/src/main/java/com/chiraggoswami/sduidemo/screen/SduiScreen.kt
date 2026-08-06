package com.chiraggoswami.sduidemo.screen

import android.os.Trace
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
 * initial-composition trace section for :macrobenchmark's SduiBreakdownBenchmark.
 *
 * CAVEAT (see PERF.md): [SECTION_VIEW_BUILD] only times the synchronous composition of the
 * call below — not layout, not draw, not the off-screen items every `lazy_row` defers
 * composing until scrolled into view, not `AsyncImage`'s async decode. Composition/layout/draw
 * are separate passes Choreographer schedules independently, so a trace section around a
 * composable call can only ever bound the first of the three. Treat this as a lower-bound
 * diagnostic for "how long did building the initially-visible tree take," not "time to fully on
 * screen" — `reportFullyDrawn()` + Macrobenchmark's `timeToFullDisplayMs` is the credible number
 * for that, and isn't wired up here (yet).
 */
@Composable
private fun ScrollableRoot(ready: ScreenUiState.Ready, ctx: RenderContext) {
    Column(
        Modifier
            .testTag(HOME_SCROLL_ROOT_TAG)
            .semantics { testTagsAsResourceId = true }
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Trace.beginSection(SECTION_VIEW_BUILD)
        RenderNode(ready.schema.root, ctx)
        Trace.endSection()
    }
}
