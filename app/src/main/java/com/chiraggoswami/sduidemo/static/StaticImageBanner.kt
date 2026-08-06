package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Plain-param twin of catalog/ImageBanner.kt. */
@Composable
fun StaticImageBanner(imageUrl: String, bottomPadding: Int = 0, onClick: () -> Unit = {}) {
    Box(Modifier.fillMaxWidth().padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = bottomPadding.dp)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop,
        )
    }
}
