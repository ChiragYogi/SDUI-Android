package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Plain-param twin of catalog/ImageTile.kt. `titleBelow`/`titleColor` mirror the `titleStyle.position` cases in use. */
@Composable
fun StaticImageTile(
    title: String,
    imageUrl: String,
    width: Int = 160,
    height: Int = 120,
    titleBelow: Boolean = false,
    titleColor: Color = Color.White,
    onClick: () -> Unit = {},
) {
    Column(Modifier.width(width.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (!titleBelow) {
                Text(
                    title,
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                )
            }
        }
        if (titleBelow) {
            Text(title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
