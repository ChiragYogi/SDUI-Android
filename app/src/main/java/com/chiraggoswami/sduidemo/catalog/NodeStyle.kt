package com.chiraggoswami.sduidemo.catalog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

// Resolution: starts with '#' -> literal; else -> token lookup; unknown -> null -> component default.
private val colorTokens = mapOf(
    "surface.brand" to Color(0xFF5B4FE9),
    "surface.default" to Color.White,
    "surface.buyTile" to Color(0xFFFFB74D),
    "surface.sellTile" to Color(0xFF66BB6A),
    "text.onBrand" to Color.White,
    "text.primary" to Color(0xFF1A1A1A),
)

fun resolveColor(token: String?): Color? = when {
    token.isNullOrBlank() -> null
    token.startsWith("#") -> runCatching { Color(android.graphics.Color.parseColor(token)) }.getOrNull()
    else -> colorTokens[token]
}

fun JsonElement?.styleObj(): JsonObject? = this as? JsonObject

fun JsonObject?.bg(): Color? = resolveColor((this?.get("bg") as? JsonPrimitive)?.contentOrNull)

fun JsonObject?.paddingValues(): PaddingValues {
    val p = this?.get("padding") as? JsonObject ?: return PaddingValues(0.dp)
    fun side(key: String) = (p[key] as? JsonPrimitive)?.intOrNull?.dp ?: 0.dp
    return PaddingValues(start = side("start"), top = side("top"), end = side("end"), bottom = side("bottom"))
}

fun JsonObject?.itemSpacingDp(default: Int = 8): Int =
    (this?.get("itemSpacing") as? JsonPrimitive)?.intOrNull ?: default
