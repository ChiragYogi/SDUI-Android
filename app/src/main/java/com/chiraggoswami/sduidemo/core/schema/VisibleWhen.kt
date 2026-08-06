package com.chiraggoswami.sduidemo.core.schema

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class VisibleWhen(
    val state: String? = null,
    val equals: String? = null,
    val notEquals: String? = null,
    @SerialName("in") val inList: List<String>? = null,
    val notIn: List<String>? = null,
) {
    /** No [state] key to check against -> nothing to condition on -> always visible. */
    fun isVisible(lookup: (String) -> String?): Boolean {
        val key = state ?: return true
        val value = lookup(key)
        return when {
            equals != null -> value == equals
            notEquals != null -> value != notEquals
            inList != null -> value in inList
            notIn != null -> value !in notIn
            else -> true
        }
    }
}
