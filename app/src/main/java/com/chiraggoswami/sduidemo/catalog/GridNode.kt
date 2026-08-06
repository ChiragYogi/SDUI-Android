package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.core.render.PropsDecodeFailurePlaceholder
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.resolveChildren
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class GridProps(val columns: Int = 2)

/** Fixed-column wrap grid. Children resolved via [resolveChildren] — literal or template+items. */
@Composable
fun GridNode(node: SduiNode, ctx: RenderContext) {
    val props = remember(node) { node.decodeProps<GridProps>() } ?: return PropsDecodeFailurePlaceholder(node)
    val style = node.style.styleObj()
    val spacing = style.itemSpacingDp().dp
    val children = remember(node) { resolveChildren(node) }
    Column(
        Modifier.fillMaxWidth().padding(style.paddingValues()),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        children.chunked(props.columns).forEach { rowChildren ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                rowChildren.forEach { child -> RenderNode(child, ctx) }
            }
        }
    }
}
