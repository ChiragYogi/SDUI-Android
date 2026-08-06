package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chiraggoswami.sduidemo.core.render.RenderContext
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import com.chiraggoswami.sduidemo.core.schema.decodeProps
import kotlinx.serialization.Serializable

@Immutable
@Serializable
private data class CarCardProps(
    val imageUrl: String = "",
    val assuredLabel: String = "",
    val name: String = "",
    val variant: String = "",
    val specs: List<String> = emptyList(),
    val price: String = "",
    val emi: String = "",
    val footnote: String = "",
    val badges: List<String> = emptyList(),
    val wishlisted: Boolean = false,
    val rankLabel: String = "",
    val stateKey: String = "",
    // Fixed dp (default) for a horizontal rail's intrinsic card width, or "fill" to stretch
    // to the parent's width — e.g. when the same node sits in a `grid` instead of `lazy_row`.
    val width: String = "200",
)

/** Used-car listing card. Same node shape covers the sparse "trending" rail and the full "love" rail. */
@Composable
fun CarCard(node: SduiNode, ctx: RenderContext) {
    val props = remember(node) { node.decodeProps<CarCardProps>() } ?: return
    // `wishlisted` seeds the initial value; once toggled, `stateKey` (unique per card, e.g.
    // "wishlist_creta2016") owns it — same "payload seeds, state owns" split as chip_row.
    val isWishlisted = ctx.state.get(props.stateKey)?.toBoolean() ?: props.wishlisted
    val widthModifier = if (props.width == "fill") {
        Modifier.fillMaxWidth()
    } else {
        Modifier.width((props.width.toIntOrNull() ?: 200).dp)
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = widthModifier.clickable { node.actions?.get("onClick")?.let(ctx::dispatch) },
    ) {
        Column {
            CarCardImage(props, isWishlisted, node, ctx)
            CarCardDetails(props, node, ctx)
        }
    }
}

@Composable
private fun CarCardImage(props: CarCardProps, isWishlisted: Boolean, node: SduiNode, ctx: RenderContext) {
    Box(Modifier.fillMaxWidth().height(120.dp)) {
        AsyncImage(
            model = props.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Crop,
        )
        if (props.rankLabel.isNotEmpty()) {
            Text(
                "#${props.rankLabel}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(4.dp)).background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        Text(
            if (isWishlisted) "♥" else "♡",
            color = if (isWishlisted) Color.Red else Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp)
                .clip(CircleShape).background(Color(0x66000000))
                .clickable {
                    val trigger = if (isWishlisted) "onWishlistUnselect" else "onWishlistSelect"
                    node.actions?.get(trigger)?.let(ctx::dispatch)
                },
        )
    }
}

@Composable
private fun CarCardDetails(props: CarCardProps, node: SduiNode, ctx: RenderContext) {
    Column(Modifier.padding(10.dp)) {
        if (props.assuredLabel.isNotEmpty()) {
            Text(props.assuredLabel, color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Text(props.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        if (props.variant.isNotEmpty()) {
            Text(props.variant, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        if (props.specs.isNotEmpty()) {
            Text(props.specs.joinToString("  ·  "), style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        if (props.price.isNotEmpty()) {
            Text(props.price, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        if (props.emi.isNotEmpty()) {
            Row(
                Modifier.padding(top = 4.dp).clickable { node.actions?.get("onEmiClick")?.let(ctx::dispatch) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(props.emi, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                if (props.footnote.isNotEmpty()) {
                    Text(" ${props.footnote}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        if (props.badges.isNotEmpty()) {
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                props.badges.forEach { badge ->
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0x1A000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}
