package com.jeanwest.reader.models

/**
 * Represents a shelf within a store.
 *
 * @property depShelfId The unique identifier for the shelf within the department. Defaults to 0.
 * @property shelfTitle The title or name of the shelf. Defaults to an empty string.
 * @property shelfTypeId The identifier for the type of shelf. Defaults to 0.
 * @property shelfTitleType A string describing the type of shelf. Defaults to an empty string.
 * @property wareId The identifier of the warehouse or area the shelf belongs to. Defaults to 0.
 * @property shelfDes A description of the shelf. Defaults to an empty string.
 * @property shelfSkus A mutable list of SKU strings (stock keeping units) associated with the products on the shelf. Defaults to an empty mutable list.
 */
data class StoreShelf(
    var depShelfId: Int = 0,
    var shelfTitle: String = "",
    var shelfTypeId: Int = 0,
    var shelfTitleType: String = "",
    var wareId: Int = 0,
    var shelfDes: String = "",
    val shelfSkus: MutableList<String> = mutableListOf(),
)
