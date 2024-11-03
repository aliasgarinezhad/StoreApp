package networking

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    var RFID: Long = 0L,
    var BarcodeMain_ID: Long = 0L,
    var K_Bar_Code: String = "",
    var KBarCode: String = "",
    var productName: String = "",
    var Title2: String = "",
    var K_Name: String = "",
    var Color: String = "",
    var Size: String = "",
    var ImgUrl: String = "",
    var OrigPrice: Long = 0L,
    var SalePrice: Long = 0L,
    var dbCountDepo: Int = 0,
    var dbCountStore: Int = 0,
    var SalePercent: Int = 0
)