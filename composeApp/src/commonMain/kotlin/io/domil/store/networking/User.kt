package io.domil.store.networking

import kotlinx.serialization.Serializable

/**
 * Represents a user in the application.
 *
 * @property accessToken The user's access token, used for authentication.  Default value is an empty string.
 * @property username The user's unique username. Default value is an empty string.
 * @property fullName The user's full name. Default value is an empty string.
 * @property locationCode An integer representing the user's geographical location. Default value is 0.
 * @property warehouses A list of [Warehouse] objects associated with the user. Default value is an empty list.
 */
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