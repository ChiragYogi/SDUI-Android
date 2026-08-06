package com.chiraggoswami.sduidemo.static

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val BrandPurple = Color(0xFF5B4FE9)

/** Plain-param twin of catalog/HeroCard.kt's "split" variant — the only variant home.json uses. */
@Composable
fun StaticHeroSection() {
    Column(Modifier.fillMaxWidth().background(BrandPurple).padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 20.dp)) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(12.dp)) {
                Column(Modifier.weight(0.4f)) {
                    AsyncImage(
                        model = "https://images.pexels.com/photos/28673504/pexels-photo-28673504.jpeg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        "₹ 5.21 Lakh",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(0.6f)) {
                    Text("BUY USED CAR", style = MaterialTheme.typography.labelSmall)
                    Text("High demand for the car you viewed · 2016 Creta", style = MaterialTheme.typography.titleMedium)
                    Text("Inspected · Certified · Best Price", style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.padding(top = 6.dp)) {
                        listOf("72,971 km", "Petrol", "Manual").forEach { tag ->
                            Text(
                                tag,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(end = 4.dp).clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x1A000000)).padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Button(onClick = {}, modifier = Modifier.padding(top = 8.dp)) { Text("Book FREE test drive") }
                }
            }
        }
    }
}
