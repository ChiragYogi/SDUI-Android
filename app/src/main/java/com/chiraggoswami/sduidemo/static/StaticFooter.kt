package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val BrandPurple = Color(0xFF5B4FE9)

/** Plain-param twin of catalog/Footer.kt. */
@Composable
fun StaticFooter() {
    Column(
        Modifier.fillMaxWidth().background(BrandPurple).padding(start = 20.dp, top = 50.dp, end = 20.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "better drives,\nbetter lives",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            "Made with ❤️ in Gurugram",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}
