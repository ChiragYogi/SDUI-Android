package com.chiraggoswami.sduidemo.core.render

import android.util.Log
import com.chiraggoswami.sduidemo.core.schema.ActionSpec
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private const val TAG = "ActionDispatcher"

/** Executes the closed set of 8 action types. Unknown type or malformed payload: log, never crash. */
class ActionDispatcher(
    private val state: StateHolder,
    private val onNavigate: (route: String, params: Map<String, String>) -> Unit = { _, _ -> },
    private val onOpenUrl: (url: String) -> Unit = {},
    private val onTrack: (event: String, payload: Map<String, String>) -> Unit = { _, _ -> },
    private val onShowSnackbar: (message: String) -> Unit = {},
) {
    fun dispatch(action: ActionSpec, scope: Map<String, JsonElement> = emptyMap()) {
        val payload = interpolate(action.payload, scope) as? JsonObject
        when (action.type) {
            "navigate" -> payload?.str("route")?.let { onNavigate(it, payload.strMap("params")) }
            "set_state" -> {
                val key = payload?.str("key")
                val value = payload?.str("value")
                if (key != null && value != null) state.set(key, value)
            }
            "open_sheet" -> payload?.str("sheetId")?.let { state.openSheet(it) }
            "dismiss" -> state.dismissSheet()
            "open_url" -> payload?.str("url")?.let { onOpenUrl(it) }
            "track" -> onTrack(payload?.str("event") ?: "unknown", payload?.strMap() ?: emptyMap())
            "show_snackbar" -> payload?.str("message")?.let(onShowSnackbar)
            "sequence" -> action.actions?.forEach { dispatch(it, scope) }
            else -> Log.w(TAG, "unknown or missing action type '${action.type}' — ignored")
        }
    }
}

private fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
private fun JsonObject.strMap(key: String? = null): Map<String, String> {
    val obj = if (key == null) this else (this[key] as? JsonObject) ?: return emptyMap()
    return obj.mapValues { (_, v) -> (v as? JsonPrimitive)?.contentOrNull ?: "" }
}
