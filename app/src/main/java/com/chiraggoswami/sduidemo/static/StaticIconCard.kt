package com.chiraggoswami.sduidemo.static

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

/** Plain-param twin of catalog/IconCard.kt. */
@Composable
fun StaticIconCard(title: String, imageUrl: String, onClick: () -> Unit = {}) {
    Column(
        Modifier.width(72.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
