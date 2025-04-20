package com.jeanwest.reader.models

data class ShelfItem(
    val product: Product,
    val shelfNumber: String,
    val epcs: List<String> = listOf(),
    val qtyInShelf: Int
)
