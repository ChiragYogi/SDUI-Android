package com.chiraggoswami.sduidemo.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.chiraggoswami.sduidemo.core.render.PropsDecodeFailurePlaceholder
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class TextProps(val text: String = "", val variant: String = "body", val color: String? = null)

/** Generic text primitive. `variant` selects a typography scale; `color` resolves like any other semantic token. */
@Composable
fun TextNode(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<TextProps>() ?: return PropsDecodeFailurePlaceholder(node)
    val style = when (props.variant) {
        "title" -> MaterialTheme.typography.titleLarge
        "heading" -> MaterialTheme.typography.headlineSmall
        "caption" -> MaterialTheme.typography.bodySmall
        else -> MaterialTheme.typography.bodyMedium
    }
    Text(props.text, style = style, color = resolveColor(props.color) ?: Color.Unspecified)
}
