package com.jeanwest.reader.models

/**
 * Represents the address of a product on a shelf, identified by its barcode.
 *
 * This data class encapsulates information about a product's location within a warehouse,
 * including the shelf identifier, warehouse title, stock ID, quantity, color, and size.
 *
 * @property shelfStockID  Unique identifier for the product stock on the shelf. Defaults to 0.
 * @property shelfID  Identifier of the shelf where the product is located. Defaults to an empty string.
 * @property wareHouseTitle  Title of the warehouse containing the shelf. Defaults to an empty string.
 * @property qty  Quantity of the product available on the shelf. Defaults to 0.
 * @property color  Color of the product. Defaults to an empty string.
 * @property size  Size of the product. Defaults to an empty string.
 */
data class ShelfBarcodeAddress(
    var shelfStockID: Int = 0,
    var shelfID: String = "",
    var wareHouseTitle: String = "",
    var qty: Int = 0,
    var color: String = "",
    var size: String = ""
)