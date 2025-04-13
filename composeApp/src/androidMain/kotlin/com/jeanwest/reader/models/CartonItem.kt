package com.jeanwest.reader.models

/**
 * Represents an item within a carton.
 *
 * @property product The [Product] contained in the carton.  Provides details about the specific product, such as SKU, name, etc.
 * @property cartonNumber A unique identifier for the carton containing this item.  This allows for tracking and locating items within a warehouse or inventory system.
 * @property epcs A list of Electronic Product Code (EPC) strings associated with the items within this carton.  EPCs are often used for RFID tracking and provide unique identifiers for individual items within a carton. Defaults to an empty list if no EPCs are provided.
 * @property qtyInCarton The quantity of the specified [product] contained within the carton.
 */
data class CartonItem (
    val product: Product,
    val cartonNumber: String,
    val epcs: List<String> = listOf(),
    val qtyInCarton: Int
)