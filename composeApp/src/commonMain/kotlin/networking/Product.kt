package networking

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    var BarcodeMain_ID: Long,
    var KBarCode: String,
    var kbarcode: String,
    var SearchCode: String,
    var K_Bar_Code: String,
    var productName: String,
    var ItemName: String,
    var K_Name: String,
    var OrgPrice: Long,
    var SalePrice: Long,
    var CodingDepartmentLevel1: String,
    var CodingDepartmentLevel2: String,
    var CodingDepartmentLevel3: String,
    var Title: String,
    var ImgUrl: String,
    var RFID: Long,
    var K_Desc: String,
    var Color: String,
    var Size: String,
    var BrandGroupName: String,
    var SeasonCode2: String,
    var WareHouse_ID: Long,
    var Mojodi: Int,
    var RfidCount: Int,
    var depoCount: Int,
    var storeCount: Int,
    var diffRfidDepoCount: Int,
    var diffRidStoreCount: Int
)