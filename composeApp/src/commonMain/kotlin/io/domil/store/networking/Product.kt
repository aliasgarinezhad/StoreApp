package io.domil.store.networking

import kotlinx.serialization.Serializable

/**
 * Represents a product with its associated details.
 *
 * @property ProductMain_ID The unique identifier for the product. Defaults to 0L.
 * @property RFID The RFID tag associated with the product. Defaults to 0L.
 * @property BarcodeMain_ID The identifier for the main barcode associated with the product. Defaults to 0L.
 * @property K_Bar_Code An alternative barcode for the product. Defaults to "".
 * @property KBarCode Another alternative barcode for the product. Defaults to "".
 * @property Title2 A secondary title or description for the product. Defaults to "".
 * @property K_Name The Persian name of the product. Defaults to "".
 * @property Color The color of the product. Defaults to "".
 * @property Size The size of the product. Defaults to "".
 * @property Brand The brand of the product. Defaults to "".
 * @property ImgUrl The URL of the product's image. Defaults to "".
 * @property DepoMojodi The quantity of the product in the warehouse (depot). Defaults to 0.
 * @property StoreMojodi The quantity of the product in the store. Defaults to 0.
 * @property EndUserPrice The price of the product for the end user. Defaults to 0L.
 * @property SalePrice The sale price of the product. Defaults to 0L.
 * @property SalePercent The percentage discount applied to the product. Defaults to 0.
 * @property Reduction The amount of reduction in price. Defaults to 0L.
 */
@Serializable
data class Product(
    var ProductMain_ID : Long = 0L,
    var RFID: Long = 0L,
    var BarcodeMain_ID: Long = 0L,
    var K_Bar_Code: String = "",
    var KBarCode: String = "",
    var Title2: String = "",
    var K_Name: String = "",
    var Color: String = "",
    var Size: String = "",
    var Brand : String = "",
    var ImgUrl: String = "",
    var DepoMojodi: Int = 0,
    var StoreMojodi: Int = 0,
    var EndUserPrice: Long = 0L,
    var SalePrice: Long = 0L,
    var SalePercent: Int = 0,
    var Reduction: Long = 0L,
)