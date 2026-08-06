package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.resolveChildren
import com.chiraggoswami.sduidemo.core.schema.SduiNode

/** Horizontal carousel/rail. Children resolved via [resolveChildren] — literal or template+items. */
@Composable
fun LazyRowNode(node: SduiNode, ctx: RenderContext) {
    val style = node.style.styleObj()
    val children = remember(node) { resolveChildren(node) }
    LazyRow(
        Modifier.fillMaxWidth().padding(style.paddingValues()),
        horizontalArrangement = Arrangement.spacedBy(style.itemSpacingDp().dp),
    ) {
        items(children, key = { it.id ?: it.hashCode() }) { child -> RenderNode(child, ctx) }
    }
}
