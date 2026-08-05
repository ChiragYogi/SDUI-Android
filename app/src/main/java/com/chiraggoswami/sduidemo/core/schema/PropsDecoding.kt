package com.chiraggoswami.sduidemo.core.schema

import android.util.Log
import kotlinx.serialization.json.decodeFromJsonElement

@PublishedApi
internal const val PROPS_DECODING_TAG = "PropsDecoding"

/**
 * The one place per-component props get decoded. Returns null on any
 * malformed/missing field instead of throwing — callers treat null as
 * "skip this node," never crash. Logged here so a silent skip is never truly
 * silent, even though the debug placeholder for this path isn't wired yet
 * (each component just early-returns on null).
 */
inline fun <reified T> SduiNode.decodeProps(): T? {
    val rawProps = props
    if (rawProps == null) {
        Log.w(PROPS_DECODING_TAG, "no props for id=$id type=$type — skipped")
        return null
    }
    return runCatching { SduiJson.decodeFromJsonElement<T>(rawProps) }
        .onFailure { Log.w(PROPS_DECODING_TAG, "malformed props for id=$id type=$type: ${it.message} — skipped") }
        .getOrNull()
}
