package com.chiraggoswami.sduidemo.core.render

import androidx.compose.runtime.Stable
import com.chiraggoswami.sduidemo.core.registry.ComponentRegistry
import com.chiraggoswami.sduidemo.core.schema.ActionSpec
import kotlinx.serialization.json.JsonElement

/**
 * Everything a component composable gets besides its own node: state, registry, dispatch.
 *
 * `@Stable`, not `@Immutable`: [StateHolder]'s exposed state can change over time (that's the
 * whole point of it), but this object's own identity and equals()-based skip behavior are
 * trustworthy — it's built once via `remember` in ScreenContent and never replaced.
 */
@Stable
class RenderContext(
    val state: StateHolder,
    val registry: ComponentRegistry,
    private val dispatcher: ActionDispatcher,
) {
    fun dispatch(action: ActionSpec, scope: Map<String, JsonElement> = emptyMap()) {
        dispatcher.dispatch(action, scope)
    }
}
