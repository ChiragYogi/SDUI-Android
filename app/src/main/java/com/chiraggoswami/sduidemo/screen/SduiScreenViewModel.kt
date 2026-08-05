package com.chiraggoswami.sduidemo.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chiraggoswami.sduidemo.data.AssetScreenRepository
import com.chiraggoswami.sduidemo.data.ScreenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One ViewModel for the SDUI screen. Holds no Context; the repository does. */
class SduiScreenViewModel(
    private val repository: ScreenRepository,
    private val screenId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Loading)
    val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.loadScreen(screenId).fold(
                onSuccess = { _uiState.value = ScreenUiState.Ready(it) },
                onFailure = { _uiState.value = ScreenUiState.Error(it.message ?: "load failed") },
            )
        }
    }

    companion object {
        fun factory(context: Context, screenId: String) = viewModelFactory {
            initializer { SduiScreenViewModel(AssetScreenRepository(context.applicationContext), screenId) }
        }
    }
}
