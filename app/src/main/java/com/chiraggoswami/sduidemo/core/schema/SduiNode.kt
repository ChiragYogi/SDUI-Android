package com.chiraggoswami.sduidemo.core.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class SduiNode(
    val id: String? = null,
    val type: String? = null,
    val props: JsonElement? = null,
    val style: JsonElement? = null,
    val actions: Map<String, ActionSpec>? = null,
    val visibleWhen: VisibleWhen? = null,
    val children: List<SduiNode>? = null,
    val template: SduiNode? = null,
    val items: List<JsonObject>? = null,
    val fallback: SduiNode? = null,
    val minSchemaVersion: Int? = null,
)
