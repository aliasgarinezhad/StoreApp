package com.jeanwest.reader.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * Data class holding ERP-related data, primarily loaded from a database or API, and used within the application.
 * Uses `SnapshotStateMap` and `SnapshotStateList` for reactive updates within Compose UI.  Provides convenient access
 * via computed properties for reverse lookups and transformations.  String keys are generally assumed to represent IDs
 * that can be parsed as Integers, while String values represent human-readable titles or names.  Integer values often
 * represent associated IDs (e.g., for drivers, printers, request types).
 *
 * @property warehousesIDsToTitles A map where keys are warehouse IDs (String) and values are their titles (String).
 * @property departments A map where keys are department IDs (String) and values are their names (String).
 * @property departmentWarehouses A map where keys are department IDs (String) and values are lists of warehouse IDs (String) associated with that department.
 * @property sortedWarehousesList A list of warehouse IDs (String), sorted according to some application-specific logic (not defined within this class).
 * @property drivers A map where keys are driver names (String) and values are their corresponding IDs (Int).
 * @property printers A map where keys are printer names (String) and values are their corresponding IDs (Int).  Note: Uses a standard `MutableMap`.
 * @property packageTypes A map where keys are package type names (String) and values are their corresponding IDs (Int). Note: Uses a standard `MutableMap`.
 * @property stockDraftRequestTypes A map where keys are stock draft request type names (String) and values are their corresponding IDs (Int).
 * @property warehousesToDepartments A computed property that returns a map where keys are warehouse IDs (Int) and values are their associated department IDs (Int).  Lazily populated based on `departmentWarehouses`.
 * @property warehousesTitlesToIDs A computed property that returns a map where keys are warehouse titles (String) and values are their corresponding IDs (Int). Lazily populated based on `warehousesIDsToTitles`.
 */
data class ERPData(
    var warehousesIDsToTitles: SnapshotStateMap<String, String> = mutableStateMapOf(),
    var departments: SnapshotStateMap<String, String> = mutableStateMapOf(),
    var departmentWarehouses: SnapshotStateMap<String, MutableList<String>> = mutableStateMapOf(),
    var sortedWarehousesList: SnapshotStateList<String> = mutableStateListOf(),
    var drivers: SnapshotStateMap<String, Int> = mutableStateMapOf(),
    var printers: MutableMap<String, Int> = mutableMapOf(),
    var packageTypes: MutableMap<String, Int> = mutableMapOf(),
    var stockDraftRequestTypes: SnapshotStateMap<String, Int> = mutableStateMapOf(),
) {
    var warehousesToDepartments: MutableMap<Int, Int> = mutableMapOf()
        get() {
            if (field.isEmpty()) {
                departmentWarehouses.forEach { (department, departmentWarehouses) ->
                    if (department.toIntOrNull() != null) {
                        departmentWarehouses.forEach { warehouse ->
                            if (warehouse.toIntOrNull() != null) {
                                field[warehouse.toInt()] = department.toInt()
                            }
                        }
                    }
                }
            }
            return field
        }

    var warehousesTitlesToIDs: MutableMap<String, Int> = mutableMapOf()
        get() {
            if (field.isEmpty()) {
                warehousesIDsToTitles.forEach { (warehouseID, warehouseTitle) ->
                    if (warehouseID.toIntOrNull() != null) {
                        field[warehouseTitle] = warehouseID.toInt()
                    }
                }
            }
            return field
        }
}