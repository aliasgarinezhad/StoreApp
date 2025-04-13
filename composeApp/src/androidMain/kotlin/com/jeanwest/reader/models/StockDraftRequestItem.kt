package com.jeanwest.reader.models

/**
 * Represents a single item within a stock draft request. This data class encapsulates information about a specific product
 * requested for stock management, including its current status and control discrepancies.
 *
 * @property product The [Product] associated with this request item.  Holds detailed product information (e.g., name, scanned number). Defaults to an empty Product instance.
 * @property shelfID The unique identifier of the shelf where the product is expected to be found.
 * @property shelfCode The human-readable code associated with the shelf.
 * @property foundNumber The actual number of units of the product found on the shelf during the stock check.
 * @property requestNumber The number of units of the product that were originally requested for stock.
 * @property controlledNumber The number of units of the product that have been successfully controlled (e.g., scanned or verified).
 * @property primaryKey The unique identifier for this specific stock draft request item. Defaults to 0L.
 * @property KBarcode  The KBarcode associated with the product.  Purpose is unclear from the limited context.
 * @property priority The priority of this request item, typically used for sorting or prioritization. Defaults to 0L.
 * @property epcs A list of Electronic Product Code (EPC) strings associated with this product.  Typically used for RFID tracking. Defaults to an empty list.
 */
data class StockDraftRequestItem (

    var product: Product = Product(),
    val shelfID: String = "",
    val shelfCode: String = "",
    val foundNumber: Int = 0,
    val requestNumber: Int = 0,
    val controlledNumber: Int = 0,
    val primaryKey: Long = 0L,
    val KBarcode: String = "",
    val priority: Long = 0L,
    val epcs : List<String> = listOf()
) {
    val notControlledNumberWithRequestNumber: Int
        get() = requestNumber - controlledNumber

    val notControlledNumberWithFoundNumber: Int
        get() = foundNumber - controlledNumber

    val notFoundNumber: Int
        get() = requestNumber - foundNumber

    val controlShortageWithFoundNumber: Int
        get() {
            return (notControlledNumberWithFoundNumber - product.scannedNumber).let {
                if (it > 0) it else 0
            }
        }

}