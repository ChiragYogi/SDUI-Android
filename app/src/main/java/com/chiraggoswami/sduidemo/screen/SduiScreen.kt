package com.chiraggoswami.sduidemo.screen

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chiraggoswami.sduidemo.catalog.AppRegistry
import com.chiraggoswami.sduidemo.core.render.ActionDispatcher
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.StateHolder
import com.chiraggoswami.sduidemo.ui.theme.SDUIDemoTheme
import kotlinx.coroutines.launch

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
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                RenderNode(ready.schema.root, ctx)
            }
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
