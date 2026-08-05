package com.chiraggoswami.sduidemo.core.schema

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val SduiJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

fun parseScreen(raw: String): Result<ScreenSchema> =
    runCatching { SduiJson.decodeFromString<ScreenSchema>(raw) }
