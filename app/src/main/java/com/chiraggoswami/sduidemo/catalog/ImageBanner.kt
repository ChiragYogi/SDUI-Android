package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Serializable
private data class ImageBannerProps(
    val imageUrl: String = "",
    val aspectRatio: Float = 2f,
    val cornerRadius: Int = 0,
)

/** Full-width tappable promo image. Ratio/corner radius are schema-driven, no client-side crop math. */
@Composable
fun ImageBanner(node: SduiNode, ctx: RenderContext) {
    val props = node.decodeProps<ImageBannerProps>() ?: return
    val style = node.style.styleObj()
    Box(Modifier.fillMaxWidth().background(style.bg() ?: Color.Transparent).padding(style.paddingValues())) {
        AsyncImage(
            model = props.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(props.aspectRatio)
                .clip(RoundedCornerShape(props.cornerRadius.dp))
                .clickable { node.actions?.get("onClick")?.let(ctx::dispatch) },
            contentScale = ContentScale.Crop,
        )
    }
}
