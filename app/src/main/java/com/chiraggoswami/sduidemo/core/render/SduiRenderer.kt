package com.chiraggoswami.sduidemo.core.render

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chiraggoswami.sduidemo.BuildConfig
import com.chiraggoswami.sduidemo.core.schema.SduiNode

private const val TAG = "SduiRenderer"

/**
 * Renders one node: visibility check, then registry lookup, then the two-tier
 * fallback — a `fallback` node if present, otherwise skip-and-log. Never throws.
 */
@Composable
fun RenderNode(node: SduiNode, ctx: RenderContext) {
    val visible = node.visibleWhen?.isVisible { key -> ctx.state.get(key) } ?: true
    if (!visible) return

    val renderer = node.type?.let { ctx.registry.rendererFor(it) }
    when {
        renderer != null -> renderer(node, ctx)
        node.fallback != null -> RenderNode(node.fallback, ctx)
        else -> {
            Log.w(TAG, "unknown or missing type '${node.type}' (id=${node.id}) — skipped")
            DebugSkipPlaceholder("⚠ unknown component: ${node.type}")
        }
    }
}

/**
 * A component's props failed to decode (missing/malformed JSON) — `decodeProps()` already
 * logged the specific cause. Every `catalog/` component calls this from its own
 * `?: return` so the skip is visible in debug builds, not just logcat, same as the
 * unknown-type case above.
 */
@Composable
fun PropsDecodeFailurePlaceholder(node: SduiNode) {
    DebugSkipPlaceholder("⚠ malformed props: ${node.type} (id=${node.id})")
}

@Composable
private fun DebugSkipPlaceholder(message: String) {
    if (BuildConfig.DEBUG) {
        Text(
            message,
            color = Color.Red,
            modifier = Modifier.background(Color(0x22FF0000)).padding(8.dp),
        )
    }
}
