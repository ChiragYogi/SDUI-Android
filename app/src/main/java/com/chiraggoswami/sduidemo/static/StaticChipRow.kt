package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Plain-param twin of catalog/ChipRow.kt — no node/state indirection, just a selected id + callback. */
@Composable
fun StaticChipRow(
    chips: List<Pair<String, String>>,
    selected: String,
    baseColor: Color = Color.White,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { (id, label) ->
            val isSelected = id == selected
            Text(
                label,
                color = if (isSelected) baseColor else baseColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) baseColor.copy(alpha = 0.12f) else Color.Transparent)
                    .clickable { onSelect(id) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
