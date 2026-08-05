package com.chiraggoswami.sduidemo.core.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ActionSpec(
    val type: String? = null,
    val payload: JsonElement? = null,
    val actions: List<ActionSpec>? = null, // "sequence" only
)
