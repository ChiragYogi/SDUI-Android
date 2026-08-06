package com.chiraggoswami.sduidemo.core.schema

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// Never mutated after parseScreen/TemplateExpander builds it — selection/pressed/etc. state
// lives in StateHolder, not here — so @Immutable is an honest promise, not just a perf hack.
// Without it every composable keyed on SduiNode is compiler-inferred unstable (JsonElement,
// List, Map aren't provably immutable) and can never skip recomposition.
@Immutable
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
