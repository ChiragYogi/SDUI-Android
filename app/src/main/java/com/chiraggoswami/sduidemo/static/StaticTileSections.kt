package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Buy/Sell/Loan rails: same `section > lazy_row > image_tile` shape as home_design.json, different data. */
@Composable
fun StaticBuySection() {
    StaticSection(title = "Buy car", badgeText = "Up to ₹80,000 off") {
        LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(buyTiles, key = { it.id }) { StaticImageTile(it.title, it.imageUrl) }
        }
    }
}

@Composable
fun StaticSellSection() {
    StaticSection(title = "Sell your car") {
        LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sellTiles, key = { it.id }) { StaticImageTile(it.title, it.imageUrl) }
        }
    }
}

@Composable
fun StaticLoanSection() {
    StaticSection(title = "Get loans") {
        LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(loanTiles, key = { it.id }) {
                StaticImageTile(it.title, it.imageUrl, width = 120, height = 96, titleBelow = true, titleColor = Color(0xFF1A1A1A))
            }
        }
    }
}

@Composable
fun StaticChecksSection() {
    StaticSection(title = "Car check services") {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            checkServices.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { StaticIconCard(it.title, it.imageUrl) }
                }
            }
        }
    }
}
