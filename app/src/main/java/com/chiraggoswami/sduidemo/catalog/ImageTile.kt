package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.chiraggoswami.sduidemo.core.render.PropsDecodeFailurePlaceholder
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class TitleStyle(
    val color: String? = null,
    val size: Int = 14,
    val bold: Boolean = false,
    // "bottom_start" (default, overlaid on image) | "top_start" (overlaid) | "below" (outside the image box)
    val position: String = "bottom_start",
)

@Serializable
private data class ImageTileProps(
    val title: String = "",
    val imageUrl: String = "",
    val bg: String? = null,
    val titleStyle: TitleStyle = TitleStyle(),
    val imageFill: Boolean = false,
    val width: Int = 160,
    val height: Int = 120,
)

@Composable
fun ImageTile(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ImageTileProps>() ?: return PropsDecodeFailurePlaceholder(node)
    val titleStyle = props.titleStyle
    Column(Modifier.width(props.width.dp).clickable { node.actions?.get("onClick")?.let(ctx::dispatch) }) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(props.height.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(resolveColor(props.bg) ?: Color.LightGray),
        ) {
            // imageFill: image covers the whole tile (card-sized). Otherwise it's icon-sized,
            // so a solid `bg` stays the dominant look per row instead of being hidden under it.
            AsyncImage(
                model = props.imageUrl,
                contentDescription = null,
                modifier = if (props.imageFill) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.size(48.dp).align(Alignment.Center).clip(RoundedCornerShape(6.dp))
                },
                contentScale = ContentScale.Crop,
            )
            if (titleStyle.position != "below") {
                val alignment = if (titleStyle.position == "top_start") Alignment.TopStart else Alignment.BottomStart
                TileTitle(props.title, titleStyle, Color.White, Modifier.align(alignment).padding(8.dp))
            }
        }
        if (titleStyle.position == "below") {
            TileTitle(props.title, titleStyle, Color.Black, Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun TileTitle(text: String, style: TitleStyle, defaultColor: Color, modifier: Modifier) {
    Text(
        text,
        color = resolveColor(style.color) ?: defaultColor,
        fontSize = style.size.sp,
        fontWeight = if (style.bold) FontWeight.Bold else FontWeight.Medium,
        modifier = modifier,
    )
}
