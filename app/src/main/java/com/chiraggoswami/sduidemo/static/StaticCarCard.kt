package com.chiraggoswami.sduidemo.static

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Plain-param twin of catalog/CarCard.kt. `isWishlisted`/`onWishlistToggle` replace node.stateKey lookup. */
@Composable
fun StaticCarCard(
    item: StaticCarItem,
    isWishlisted: Boolean,
    onWishlistToggle: () -> Unit,
    onClick: () -> Unit = {},
    onEmiClick: () -> Unit = {},
) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(200.dp).clickable(onClick = onClick)) {
        Column {
            StaticCarCardImage(item, isWishlisted, onWishlistToggle)
            StaticCarCardDetails(item, onEmiClick)
        }
    }
}

@Composable
private fun StaticCarCardImage(item: StaticCarItem, isWishlisted: Boolean, onWishlistToggle: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(120.dp)) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentScale = ContentScale.Crop,
        )
        if (item.rankLabel.isNotEmpty()) {
            Text(
                "#${item.rankLabel}",
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
                .clickable(onClick = onWishlistToggle),
        )
    }
}

@Composable
private fun StaticCarCardDetails(item: StaticCarItem, onEmiClick: () -> Unit) {
    Column(Modifier.padding(10.dp)) {
        if (item.assuredLabel.isNotEmpty()) {
            Text(item.assuredLabel, color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        if (item.variant.isNotEmpty()) {
            Text(item.variant, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        if (item.specs.isNotEmpty()) {
            Text(item.specs.joinToString("  ·  "), style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        if (item.price.isNotEmpty()) {
            Text(item.price, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        if (item.emi.isNotEmpty()) {
            Row(Modifier.padding(top = 4.dp).clickable(onClick = onEmiClick), verticalAlignment = Alignment.CenterVertically) {
                Text(item.emi, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                if (item.footnote.isNotEmpty()) {
                    Text(" ${item.footnote}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        if (item.badges.isNotEmpty()) {
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item.badges.forEach { badge ->
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
