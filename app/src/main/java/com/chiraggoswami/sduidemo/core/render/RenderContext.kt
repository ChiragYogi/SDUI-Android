package com.chiraggoswami.sduidemo.core.render

import com.chiraggoswami.sduidemo.core.registry.ComponentRegistry
import com.chiraggoswami.sduidemo.core.schema.ActionSpec
import kotlinx.serialization.json.JsonElement

/** Everything a component composable gets besides its own node: state, registry, dispatch. */
class RenderContext(
    val state: StateHolder,
    val registry: ComponentRegistry,
    private val dispatcher: ActionDispatcher,
) {
    fun dispatch(action: ActionSpec, scope: Map<String, JsonElement> = emptyMap()) {
        dispatcher.dispatch(action, scope)
    }
}
