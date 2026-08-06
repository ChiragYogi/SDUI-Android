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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BrandPurple = Color(0xFF5B4FE9)

/** Plain-param twin of catalog/Header.kt. Click targets are no-ops — same as the SDUI side's unwired `navigate`. */
@Composable
fun StaticHeader() {
    Column(Modifier.fillMaxWidth().background(BrandPurple).padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Ahmedabad", color = Color.White, modifier = Modifier.clickable {})
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)).clickable {},
                contentAlignment = Alignment.Center,
            ) { Text("CG", color = Color.White) }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable {}
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { Text("Search Baleno", color = Color.White.copy(alpha = 0.8f)) }
    }
}
