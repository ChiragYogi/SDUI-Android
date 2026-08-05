package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.render.RenderNode
import com.chiraggoswami.sduidemo.core.render.resolveChildren
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class BadgeProps(val text: String = "", val bg: String? = null, val textColor: String? = null)

@Serializable
private data class TrailingProps(val label: String = "")

@Serializable
private data class SectionTitleStyle(
    val color: String? = null,
    val size: Int = 20,
    val bold: Boolean = true,
)

@Serializable
private data class SectionProps(
    val title: String = "",
    val titleStyle: SectionTitleStyle = SectionTitleStyle(),
    val badge: BadgeProps? = null,
    val trailing: TrailingProps? = null,
)

@Composable
fun Section(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<SectionProps>() ?: return
    val style = node.style.styleObj()
    Column(Modifier.fillMaxWidth().background(style.bg() ?: Color.Transparent).padding(style.paddingValues())) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    props.title,
                    color = resolveColor(props.titleStyle.color) ?: Color.Unspecified,
                    fontSize = props.titleStyle.size.sp,
                    fontWeight = if (props.titleStyle.bold) FontWeight.Bold else FontWeight.Normal,
                )
                props.badge?.let { badge ->
                    Text(
                        badge.text,
                        color = resolveColor(badge.textColor) ?: Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(resolveColor(badge.bg) ?: Color.Red)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            props.trailing?.let { trailing ->
                Text(
                    trailing.label,
                    modifier = Modifier.clickable { node.actions?.get("onTrailingClick")?.let(ctx::dispatch) },
                )
            }
        }
        resolveChildren(node).forEach { RenderNode(it, ctx) }
    }
}
