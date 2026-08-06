package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chiraggoswami.sduidemo.core.render.PropsDecodeFailurePlaceholder
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class IconCardProps(val title: String = "", val imageUrl: String = "")

/** Small icon + label leaf, sized to content — the `grid` template for service tiles. */
@Composable
fun IconCard(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<IconCardProps>() ?: return PropsDecodeFailurePlaceholder(node)
    Column(
        Modifier.width(72.dp).clickable { node.actions?.get("onClick")?.let(ctx::dispatch) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = props.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(
            props.title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
