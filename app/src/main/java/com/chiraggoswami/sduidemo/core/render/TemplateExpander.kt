package com.chiraggoswami.sduidemo.core.render

import com.chiraggoswami.sduidemo.core.schema.ActionSpec
import com.chiraggoswami.sduidemo.core.schema.SduiNode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/** A node's children: literal `children`, or `template` expanded once per `items` entry. */
fun resolveChildren(node: SduiNode): List<SduiNode> {
    val template = node.template
    val items = node.items
    return if (template != null && items != null) expandItems(template, items) else node.children.orEmpty()
}

private fun expandItems(template: SduiNode, items: List<JsonObject>): List<SduiNode> =
    items.map { item ->
        val scope: Map<String, JsonElement> = item.mapKeys { (key, _) -> "item.$key" }
        expandNode(template, scope).copy(id = item["id"]?.jsonPrimitive?.content)
    }

private fun expandNode(node: SduiNode, scope: Map<String, JsonElement>): SduiNode = node.copy(
    props = interpolate(node.props, scope),
    style = interpolate(node.style, scope),
    actions = node.actions?.mapValues { (_, action) -> expandAction(action, scope) },
    children = node.children?.map { expandNode(it, scope) },
)

private fun expandAction(action: ActionSpec, scope: Map<String, JsonElement>): ActionSpec = action.copy(
    payload = interpolate(action.payload, scope),
    actions = action.actions?.map { expandAction(it, scope) },
)
