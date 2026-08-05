package com.chiraggoswami.sduidemo.catalog

import com.chiraggoswami.sduidemo.core.registry.ComponentRegistry

/** Component type strings. Only file that names them. */
object NodeType {
    const val COLUMN = "column"
    const val HEADER = "header"
    const val CHIP_ROW = "chip_row"
    const val HERO_CARD = "hero_card"
    const val SECTION = "section"
    const val LAZY_ROW = "lazy_row"
    const val IMAGE_TILE = "image_tile"
}

val AppRegistry = ComponentRegistry(
    mapOf(
        NodeType.COLUMN to ::ColumnNode,
        NodeType.HEADER to ::Header,
        NodeType.CHIP_ROW to ::ChipRow,
        NodeType.HERO_CARD to ::HeroCard,
        NodeType.SECTION to ::Section,
        NodeType.LAZY_ROW to ::LazyRowNode,
        NodeType.IMAGE_TILE to ::ImageTile,
    ),
)
