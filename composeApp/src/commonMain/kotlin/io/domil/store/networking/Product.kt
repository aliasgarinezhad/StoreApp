package io.domil.store.networking

import kotlinx.serialization.Serializable

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