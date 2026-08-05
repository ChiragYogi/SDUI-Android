package com.chiraggoswami.sduidemo.screen

import com.chiraggoswami.sduidemo.core.schema.ScreenSchema

sealed interface ScreenUiState {
    data object Loading : ScreenUiState
    data class Ready(val schema: ScreenSchema) : ScreenUiState
    data class Error(val message: String) : ScreenUiState
}
