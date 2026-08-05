package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.resolveChildren
import com.chiraggoswami.sduidemo.core.schema.SduiNode

/** The root node type, and the sheet-container type. Vertical layout, nothing else. */
@Composable
fun ColumnNode(node: SduiNode, ctx: RenderContext) {
    val style = node.style.styleObj()
    Column(
        Modifier
            .fillMaxWidth()
            .background(style.bg() ?: Color.Transparent)
            .padding(style.paddingValues()),
    ) {
        resolveChildren(node).forEach { RenderNode(it, ctx) }
    }
}
