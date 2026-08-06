package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
private data class CtaProps(val label: String = "", val style: String = "")

@Immutable
@Serializable
private data class HeroCardProps(
    val variant: String = "stacked",
    val eyebrow: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val priceBadge: String = "",
    val priceAlign: String = "start",
    val tags: List<String> = emptyList(),
    val cta: CtaProps? = null,
)

/** `variant` picks a pre-built arrangement; the component still owns the layout math. */
@Composable
fun HeroCard(node: SduiNode, ctx: RenderContext) {
    val props = remember(node) { node.decodeProps<HeroCardProps>() } ?: return PropsDecodeFailurePlaceholder(node)
    val style = node.style.styleObj()
    Column(Modifier.fillMaxWidth().background(style.bg() ?: Color.Transparent).padding(style.paddingValues())) {
        Card(shape = RoundedCornerShape(12.dp)) {
            when (props.variant) {
                "split" -> SplitHero(props, node, ctx)
                else -> StackedHero(props, node, ctx)
            }
        }
    }
}

@Composable
private fun StackedHero(props: HeroCardProps, node: SduiNode, ctx: RenderContext) {
    Column(Modifier.padding(12.dp)) {
        Text(props.eyebrow, style = MaterialTheme.typography.labelSmall)
        Text(props.title, style = MaterialTheme.typography.titleMedium)
        Text(props.subtitle, style = MaterialTheme.typography.bodySmall)
        AsyncImage(
            model = props.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(140.dp).padding(vertical = 8.dp),
            contentScale = ContentScale.Crop,
        )
        Row {
            Text(props.priceBadge, style = MaterialTheme.typography.titleSmall)
            props.tags.forEach { Text("  ·  $it", style = MaterialTheme.typography.bodySmall) }
        }
        HeroCta(props, node, ctx)
    }
}

@Composable
private fun SplitHero(props: HeroCardProps, node: SduiNode, ctx: RenderContext) {
    Row(Modifier.padding(12.dp)) {
        Column(Modifier.weight(0.4f)) {
            AsyncImage(
                model = props.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                props.priceBadge,
                style = MaterialTheme.typography.titleSmall,
                textAlign = if (props.priceAlign == "center") TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(0.6f)) {
            Text(props.eyebrow, style = MaterialTheme.typography.labelSmall)
            Text(props.title, style = MaterialTheme.typography.titleMedium)
            Text(props.subtitle, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.padding(top = 6.dp)) {
                props.tags.forEach { tag ->
                    Text(
                        tag,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x1A000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            HeroCta(props, node, ctx)
        }
    }
}

@Composable
private fun HeroCta(props: HeroCardProps, node: SduiNode, ctx: RenderContext) {
    props.cta?.let { cta ->
        Button(
            onClick = { node.actions?.get("onCtaClick")?.let(ctx::dispatch) },
            modifier = Modifier.padding(top = 8.dp),
        ) { Text(cta.label) }
    }
}
