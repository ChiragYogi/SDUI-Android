package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
private data class ChipItem(val id: String, val label: String, val imageUrl: String? = null)

@Serializable
private data class ChipRowProps(
    val stateKey: String,
    val variant: String = "pill",
    val chips: List<ChipItem> = emptyList(),
    val textColor: String? = null,
)

/** Interactive element: tapping a chip writes [ChipRowProps.stateKey] via the node's onSelect action. */
@Composable
fun ChipRow(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ChipRowProps>() ?: return
    val style = node.style.styleObj()
    val selected = ctx.state.get(props.stateKey)
    // Default assumes the brand/dark backdrop most call sites use; a chip_row on a light
    // section (e.g. surface.default) must set textColor explicitly or it's invisible.
    val baseColor = resolveColor(props.textColor) ?: Color.White

    Row(
        Modifier
            .fillMaxWidth()
            .background(style.bg() ?: Color.Transparent)
            .padding(style.paddingValues())
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        props.chips.forEach { chip ->
            val isSelected = chip.id == selected
            Text(
                chip.label,
                color = if (isSelected) baseColor else baseColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) baseColor.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable {
                        node.actions?.get("onSelect")?.let {
                            ctx.dispatch(it, mapOf("chip.id" to JsonPrimitive(chip.id)))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
