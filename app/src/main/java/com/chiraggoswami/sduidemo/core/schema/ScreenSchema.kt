package com.chiraggoswami.sduidemo.core.schema

import kotlinx.serialization.Serializable

@Serializable
data class ScreenSchema(
    val schemaVersion: Int,
    val screenId: String,
    val theme: String = "system",
    val initialState: Map<String, String> = emptyMap(),
    val sheets: Map<String, SduiNode> = emptyMap(),
    val root: SduiNode,
)
