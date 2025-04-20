package com.jeanwest.reader.models


/**
 * This data class holds user-related data. It is populated by the login API during sign-in.
 * It determines whether the user should interact with the local database or the remote API,
 * and provides the database address used by the local database.
 *
 *  Key properties and their roles:
 *  - `username`: User's unique identifier (likely numeric).
 *  - `name`: User's display name. Defaults to "null" if not provided.
 *  - `token`: Authentication token obtained during login. An empty token indicates an unauthenticated user.
 *  - `warehouseToDepartmentMap`:  Maps warehouse codes to department codes. Used to determine user's context within a warehouse.
 *  - `warehouses`:  Maps warehouse codes (as strings) to warehouse names.
 *  - `destinationTitles`: List of destination names relevant to the user.
 *  - `warehousesTitlesSorted`:  Sorted list of warehouse names.
 *  - `access`: List of access permissions granted to the user.
 *  - `locationCode`:  Code representing the user's primary location.
 *  - `warehouseCode`:  Code representing the user's currently selected warehouse.
 *  - `isLocalMode`:  Boolean flag indicating if the application should operate in offline/local mode.
 *  - `destinationMapWithId`: Maps destination names to their corresponding IDs.
 *  - `storeWarehouseCode`: Code specific to a store's warehouse.
 *  - `storeShopCode`: Code specific to a store's shop.
 *  - `assignedDepartment`: The department the user is primarily assigned to.
 *  - `assignedDepartmentType`: The type of department the user is assigned to.
 *
 *  Computed Properties:
 *  - `accountPermissionType`:  Determines the user's overall permission level based on location, department, and local mode.
 *     Possible values are defined in the `AccountPermissionType` enum (not shown in this code context but assumed to exist).
 *     This plays a crucial role in determining access and behavior within the application.
 *  - `calculatedLocationCode`: Calculates the effective location code for the user, considering their account permission type and department assignment relative to the current warehouse.
 *  - `currentWarehouseCodeIsDepo`:  Boolean indicating if the currently selected warehouse */
/*
* this data class holds user
* related data. it is filled by login-api
* during sign in. the class specifies
* that user should use localStoreDatabase or
* api class. it also provide database address
* which used by localStoreDatabase class.
 */
data class User(
    var username: Int = 0,
    var name: String = "null",
    var token: String = "",
    var warehouseToDepartmentMap: Map<Int, Int> = mutableMapOf(),
    var warehouses: MutableMap<String, String> = mutableMapOf(),
    var destinationTitles: MutableList<String> = mutableListOf(),
    var warehousesTitlesSorted: MutableList<String> = mutableListOf(),
    var access: MutableList<String> = mutableListOf(),
    var locationCode: Int = 0,
    var warehouseCode: Int = 0,
    var isLocalMode: Boolean = false,
    var destinationMapWithId: MutableMap<String, Int> = mutableMapOf(),
    var storeWarehouseCode: Int = 0,
    var storeShopCode: Int = 0,
    val assignedDepartment: Int = 0,
    val assignedDepartmentType: Int = 0,
) {

    private var accountPermissionType: AccountPermissionType = AccountPermissionType.STORE
        get() {
            field = if (isLocalMode) {
                AccountPermissionType.STORE
            } else {
                if (locationCode == 0 || locationCode == 44) {
                    if (assignedDepartmentType != 1 && assignedDepartment != 85) {
                        AccountPermissionType.CENTRAL_WAREHOUSE
                    } else {
                        AccountPermissionType.ALL
                    }
                } else {
                    AccountPermissionType.STORE
                }
            }
            return field
        }

    var calculatedLocationCode: Int = 0
        get() {
            field =

                if (accountPermissionType != AccountPermissionType.ALL) {
                    locationCode
                } else {
                    if (warehouseToDepartmentMap.getOrDefault(
                            warehouseCode,
                            0
                        ) == assignedDepartment
                    ) {
                        assignedDepartment
                    } else {
                        locationCode
                    }
                }
            return field
        }

    val currentWarehouseCodeIsDepo: Boolean
        get() = warehouses[warehouseCode.toString()]?.contains("دپو") ?: false

    val isExist: Boolean
        get() = token != ""

    var isStoreUser: Boolean = false
        get() {
            field = if (accountPermissionType == AccountPermissionType.STORE) {
                true
            } else if (accountPermissionType == AccountPermissionType.CENTRAL_WAREHOUSE) {
                false
            } else {
                warehouseToDepartmentMap.getOrDefault(
                    warehouseCode,
                    0
                ) == assignedDepartment
            }
            return field
        }
}

enum class AccountPermissionType {
    STORE,
    CENTRAL_WAREHOUSE,
    ALL
}