package com.chiraggoswami.sduidemo.core.render

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Single state holder for a screen: named state keys (topTab, carsFilter, ...) plus which sheet is open. */
class StateHolder(initial: Map<String, String>) {
    private val values = mutableStateMapOf<String, String>().apply { putAll(initial) }

    var activeSheet by mutableStateOf<String?>(null)
        private set

    fun get(key: String): String? = values[key]
    fun set(key: String, value: String) { values[key] = value }
    fun openSheet(sheetId: String) { activeSheet = sheetId }
    fun dismissSheet() { activeSheet = null }
}
