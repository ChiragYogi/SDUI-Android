package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Plain-param twin of catalog/Section.kt: title row + slot content. `badge`/`onTrailingClick` optional. */
@Composable
fun StaticSection(
    title: String,
    badgeText: String? = null,
    trailingLabel: String? = null,
    onTrailingClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(Color.White).padding(top = 20.dp, bottom = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                badgeText?.let {
                    Text(
                        it,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE5271B)).padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            trailingLabel?.let { Text(it, modifier = Modifier.clickable(onClick = onTrailingClick)) }
        }
        content()
    }
}
