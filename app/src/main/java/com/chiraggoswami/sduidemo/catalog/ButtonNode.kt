package com.chiraggoswami.sduidemo.catalog

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class ButtonProps(val label: String = "", val variant: String = "primary")

/** Standalone CTA primitive. `variant` picks the style; layout math stays with the parent. */
@Composable
fun ButtonNode(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ButtonProps>() ?: return
    val onClick: () -> Unit = { node.actions?.get("onClick")?.let(ctx::dispatch) }
    when (props.variant) {
        "secondary" -> OutlinedButton(onClick = onClick) { Text(props.label) }
        else -> Button(onClick = onClick) { Text(props.label) }
    }
}
