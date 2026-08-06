package com.chiraggoswami.sduidemo.static

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Plain-param twin of the `emi_breakup` sheet node tree in home_design.json. */
@Composable
fun StaticEmiSheet(tenure: String, onTenureSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp)) {
        Text("EMI breakup", style = MaterialTheme.typography.titleLarge)
        StaticChipRow(
            chips = emiTenures,
            selected = tenure,
            baseColor = Color(0xFF1A1A1A),
            onSelect = onTenureSelect,
        )
        val (amount, breakdown) = emiByTenure[tenure] ?: emiByTenure.getValue("60")
        Text(amount, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
        Text(breakdown, style = MaterialTheme.typography.bodySmall)
        Text(
            "*Indicative. Final EMI depends on lender approval.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp),
        )
        Button(onClick = onDismiss, modifier = Modifier.padding(top = 12.dp)) { Text("Got it") }
    }
}
