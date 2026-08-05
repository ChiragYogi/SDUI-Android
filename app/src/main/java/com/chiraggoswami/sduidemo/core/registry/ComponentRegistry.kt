package com.chiraggoswami.sduidemo.core.registry

import androidx.compose.runtime.Composable
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode

typealias NodeRenderer = @Composable (node: SduiNode, ctx: RenderContext) -> Unit

/** Type dispatch is a map lookup, never a `when (node.type)` branch. */
class ComponentRegistry(private val renderers: Map<String, NodeRenderer>) {
    fun rendererFor(type: String): NodeRenderer? = renderers[type]
}
