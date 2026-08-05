package com.chiraggoswami.sduidemo.core.render

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private val TOKEN = Regex("""@\{([\w.]+)\}""")

/**
 * Substitutes @{key} tokens against [scope]. If a string value is *exactly* one
 * token, the raw scoped JsonElement is substituted (array/number/bool preserved).
 * A token inside a longer string is plain string interpolation — always a string.
 */
fun interpolate(element: JsonElement?, scope: Map<String, JsonElement>): JsonElement? = when (element) {
    null -> null
    is JsonArray -> JsonArray(element.map { interpolate(it, scope) ?: JsonNull })
    is JsonObject -> JsonObject(element.mapValues { (_, v) -> interpolate(v, scope) ?: JsonNull })
    is JsonPrimitive -> interpolateString(element, scope)
}

private fun interpolateString(element: JsonPrimitive, scope: Map<String, JsonElement>): JsonElement {
    val content = element.contentOrNull ?: return element
    val whole = TOKEN.matchEntire(content)
    if (whole != null) return scope[whole.groupValues[1]] ?: JsonNull
    return JsonPrimitive(
        TOKEN.replace(content) { m ->
            (scope[m.groupValues[1]] as? JsonPrimitive)?.contentOrNull ?: m.value
        },
    )
}
