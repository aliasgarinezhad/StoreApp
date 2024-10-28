package networking

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    var RFID : Long ?,
    var BarcodeMain_ID: Long ?,
    var K_Bar_Code: String ?,
    var KBarCode: String ?,
    var productName: String ?,
    var Title2: String ?,
    var K_Name: String ?,
    var Color: String ?,
    var Size: String ?,
    var ImgUrl: String ?,
    var OrigPrice: Long ?,
    var SalePrice: Long ?,
    var dbCountDepo: Int ?,
    var dbCountStore: Int ?,
    var SalePercent: Int ?
)