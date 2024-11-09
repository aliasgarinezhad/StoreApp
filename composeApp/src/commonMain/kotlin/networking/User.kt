package networking

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var accessToken: String = "",
    var username: String = "",
    var fullName: String = "",
    var locationCode: Int = 0,
    var warehouses: List<Warehouse> = emptyList(),
)

@Serializable
data class Warehouse(
    var WareHouse_ID: String = "",
    var WareHouseTitle: String = "",
    var WareHouseTypes_ID: String = "",
    var DepartmentInfo_ID: String = ""
)