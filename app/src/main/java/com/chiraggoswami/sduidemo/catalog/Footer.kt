package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.core.render.PropsDecodeFailurePlaceholder
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class FooterProps(val headline: String = "", val subtitle: String = "", val textColor: String? = null)

/** Static screen-end sign-off. No actions — it's a terminal node, not a CTA. */
@Composable
fun Footer(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<FooterProps>() ?: return PropsDecodeFailurePlaceholder(node)
    val style = node.style.styleObj()
    val color = resolveColor(props.textColor) ?: Color.White
    Column(
        Modifier.fillMaxWidth().background(style.bg() ?: Color.Transparent).padding(style.paddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(props.headline, color = color, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            props.subtitle,
            color = color.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}
