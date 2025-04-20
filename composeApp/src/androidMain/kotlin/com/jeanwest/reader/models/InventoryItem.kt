package com.jeanwest.reader.models

/**
 * Represents an item in an inventory.
 *
 * @property mojodiReviewID  A unique identifier for the inventory review associated with this item.  This likely represents a specific instance of an inventory count or audit.
 * @property mojodiReviewInfoID A further identifier within the inventory review, potentially representing a sub-section or grouping of items within the review.
 * @property barcodeMainID A primary barcode ID for the product associated with this item.  This should be a globally unique identifier for the product itself.
 * @property productCount The quantity of this specific item type recorded in the inventory.  Note that this could be different from the actual count (currentMojodi).
 * @property itemBarcode A specific barcode associated with this individual inventory item. Could be used for tracking specific units. May be the same as barcodeMainID for items without individual unit tracking.
 * @property currentMojodi The current actual count of this item in the inventory as determined during the review. "Mojodi" likely translates to "inventory" or "stock" in Persian.
 * @property diffMojodi The difference between the recorded productCount and the actual currentMojodi.  A positive value indicates an overstock, a negative value an understock.
 * @property epcs A list of Electronic Product Code (EPC) strings associated with individual units of this item.  Useful for RFID tracking of individual items. May be empty if items are not tracked individually with RFID.
 */
data class InventoryItem(
    var mojodiReviewID: String,
    var mojodiReviewInfoID: String,
    var barcodeMainID: Long,
    var productCount: Int,
    var itemBarcode: String,
    var currentMojodi: Int,
    var diffMojodi: Int,
    var epcs: List<String>
)
