package com.jeanwest.reader.models

/**
 * Represents a cardex entry, detailing information about a delivery or stock movement of an item.
 *
 * @property barcodeMainID The main barcode associated with the item. Defaults to "null".
 * @property deliveryID The unique identifier for the delivery. Defaults to "null".
 * @property deliveryWareHouseTitle The title or name of the warehouse involved in the delivery. Defaults to "null".
 * @property deliveryDateShamsi The date of the delivery in the Shamsi (Solar Hijri) calendar. Defaults to "null".
 * @property itemBarcode The barcode of the specific item being delivered or moved. Defaults to "null".
 * @property itemName The name or description of the item. Defaults to "null".
 * @property deliveryType A string indicating the type of delivery (e.g., "Sale", "Return", "Transfer"). Defaults to "null".
 * @property deliveryDetails Additional details or notes about the delivery. Defaults to "null".
 * @property deliveryQty The quantity of items delivered or moved. A positive value indicates an inward movement, while a negative value indicates an outward movement. Defaults to 0.
 * @property runningMojodi The running balance or stock level after the delivery. Defaults to 0.
 * @property srcHeaderID An identifier potentially linking to a source document or header. Defaults to "null".
 * @property createDate The date when the cardex entry was created. Defaults to "null".
 * @property deliveryDetailsID A unique identifier for the specific delivery details. Defaults to "null".
 * @property qtyAfterDelivery The quantity of the item in stock after the delivery. Defaults to 0.
 * @property inCogValue The value related to in-transit or in-consignment goods. Defaults to 0.
 * @property manualOutPrice The manually adjusted price for outgoing items. Defaults to 0.
 * @property salesValue The sales value associated with the delivery. Defaults to 0.
 * @property deliveryTypesID An identifier for a group or category of delivery types. Defaults to "null".
 */
data class Cardex(
    var barcodeMainID: String = "null",
    var deliveryID: String = "null",
    var deliveryWareHouseTitle: String = "null",
    var deliveryDateShamsi: String = "null",
    var itemBarcode: String = "null",
    var itemName: String = "null",
    var deliveryType: String = "null",
    var deliveryDetails: String = "null",
    var deliveryQty: Int = 0,
    var runningMojodi: Int = 0,
    var srcHeaderID: String = "null",
    var createDate: String = "null",
    var deliveryDetailsID: String = "null",
    var qtyAfterDelivery: Int = 0,
    var inCogValue: Int = 0,
    var manualOutPrice: Int = 0,
    var salesValue: Int = 0,
    var deliveryTypesID: String = "null",
) {
    val qtyBeforeDelivery: Int
        get() = qtyAfterDelivery - deliveryQty

    val deliveryTypeIsStockDraft: Boolean
        get() = this.deliveryType.contains("حواله")

    val deliveryTypeDetails: String
        get() {
            return if (deliveryTypeIsStockDraft) {
                if (deliveryQty < 0) {
                    "حواله ارسالی"
                } else {
                    "حواله دریافتی"
                }
            } else {
                deliveryType
            }
        }
    val deliveryHasColor: Boolean
        get() = deliveryQty >= 0
}