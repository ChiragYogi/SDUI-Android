package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class ImageTileProps(
    val title: String = "",
    val imageUrl: String = "",
    val bg: String? = null,
    val titleColor: String? = null,
    val titleBelow: Boolean = false,
    val titleSize: Int = 14,
    val width: Int = 160,
    val height: Int = 120,
)

@Composable
fun ImageTile(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ImageTileProps>() ?: return
    Column(Modifier.width(props.width.dp).clickable { node.actions?.get("onClick")?.let(ctx::dispatch) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(props.height.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(resolveColor(props.bg) ?: Color.LightGray),
        ) {
            // Icon-sized, not full-bleed — the tile's brand color (bg) stays the dominant look
            // per row, rather than being hidden under a full-size image.
            AsyncImage(
                model = props.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).align(Alignment.Center).clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )
            if (!props.titleBelow) {
                Text(
                    props.title,
                    color = resolveColor(props.titleColor) ?: Color.White,
                    fontSize = props.titleSize.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                )
            }
        }
        if (props.titleBelow) {
            Text(
                props.title,
                color = resolveColor(props.titleColor) ?: Color.Black,
                fontSize = props.titleSize.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
