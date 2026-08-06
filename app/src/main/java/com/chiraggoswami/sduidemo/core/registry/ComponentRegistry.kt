package com.chiraggoswami.sduidemo.core.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode

typealias NodeRenderer = @Composable (node: SduiNode, ctx: RenderContext) -> Unit

/** Type dispatch is a map lookup, never a `when (node.type)` branch. */
// The backing Map is built once (AppRegistry's mapOf(...)) and never mutated after — @Immutable
// so composables taking a ComponentRegistry can skip, rather than being forced unstable by the
// bare Map type.
@Immutable
class ComponentRegistry(private val renderers: Map<String, NodeRenderer>) {
    fun rendererFor(type: String): NodeRenderer? = renderers[type]
}
