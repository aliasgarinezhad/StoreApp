package com.jeanwest.reader.models

import kotlin.math.abs

/**
 * Represents a product with various attributes related to inventory, scanning, and sales.
 *
 * @property KBarCode The product's K-Bar code (unique identifier).
 * @property name The product's name.
 * @property imageUrl The URL of the product's image.
 * @property kName An alternative name for the product.
 * @property primaryKey The primary key of the product in the database.
 * @property productCode A code associated with the product.
 * @property size The product's size (e.g., S, M, L).
 * @property color The product's color.
 * @property originalPrice The original price of the product.
 * @property salePrice The current sale price of the product.
 * @property rfidKey The RFID key associated with the product.
 * @property wareHouseNumber The number of units of the product in the warehouse.
 * @property countedWarehouseNumber The number of units of the product counted in the warehouse during an inventory process.
 * @property storeNumber The number of units of the product in the store.
 * @property shelfCount The number of units of the product on the shelf.
 * @property departmentName The name of the department the product belongs to.
 * @property shelfAddress The address of the product on the shelf.
 * @property countedStoreNumber The number of units of the product counted in the store during an inventory process.
 * @property brandName The brand name of the product.
 * @property warehouseCode The code of the warehouse where the product is stored.
 * @property scannedEPCs A mutable list of scanned EPC (Electronic Product Code) values for the product.
 * @property scannedBarcode The last scanned barcode for the product.
 * @property scannedBarcodeNumber The total number of times the product's barcode has been scanned.
 * @property requestedNumber The number of units of the product requested (e.g., for restocking).
 * @property draftNumber The draft number associated with the product (used for comparison with scanned number).
 * @property inventoryOnDepo A boolean indicating whether the product is in the depot (warehouse).
 * @property searchCodes A mutable list of codes used for searching for the product.
 * @property manualScannedNumber The number of units of the product manually scanned (e.g., during */
data class Product(
    var KBarCode: String = "",
    var name: String = "",
    var imageUrl: String = "",
    var kName: String = "",
    var primaryKey: Long = 0,
    var productCode: String = "",
    var size: String = "",
    var color: String = "",
    var originalPrice: String = "",
    var salePrice: String = "",
    var rfidKey: Long = 0,
    var wareHouseNumber: Int = 0,
    var countedWarehouseNumber: Int = 0,
    var storeNumber: Int = 0,
    var shelfCount: Int = 0,
    var departmentName: String = "",
    var shelfAddress: String = "",
    var countedStoreNumber: Int = 0,
    var brandName: String = "",
    var warehouseCode: String = "",
    var scannedEPCs: MutableList<String> = mutableListOf(),
    var scannedBarcode: String = "",
    var scannedBarcodeNumber: Int = 0,
    var requestedNumber: Int = 0,
    var draftNumber: Int = 0,
    var inventoryOnDepo: Boolean = true,
    var searchCodes: MutableList<String> = mutableListOf(),
    var manualScannedNumber: Int = 0,
    var inventoryIsInProgress: Boolean = false,
    var isPrinted: Boolean = false,
    var spec: String = "",
    var spec2: String = "",
    var isConfirmed : Boolean = false,
    var shelfCode: String = "-",
    var sexTile: String = ""
) {
    val scannedNumber: Int
        get() = scannedEPCNumber + scannedBarcodeNumber + manualScannedNumber

    val scannedEPCNumber: Int
        get() = scannedEPCs.size

    val conflictNumber: Int
        get() = abs(scannedNumber - draftNumber)

    val conflictType: String
        get() {
            return when {
                scannedNumber > draftNumber -> {
                    "اضافی"
                }

                scannedNumber < draftNumber -> {
                    "کسری"
                }

                else -> {
                    "تایید شده"
                }
            }
        }

    val inventoryNumber: Int
        get() = wareHouseNumber

    private val inventoryCountedNumber: Int
        get() = countedWarehouseNumber

    val inventoryConflictAbs: Int
        get() = abs(inventoryConflictNumber)

    val inventoryConflictNumber: Int
        get() {

            val currentConflictNumber = manualScannedNumber - inventoryNumber

            return if (inventoryCountedNumber == 0) {
                if (!inventoryIsInProgress) {
                    currentConflictNumber
                } else {
                    inventoryCountedNumber
                }
            } else if (inventoryCountedNumber > 0) {
                if (currentConflictNumber == 0 || (currentConflictNumber > inventoryCountedNumber)) {
                    currentConflictNumber
                } else {
                    inventoryCountedNumber
                }
            } else {
                if (currentConflictNumber > inventoryCountedNumber) {
                    currentConflictNumber
                } else {
                    inventoryCountedNumber
                }
            }
        }

    val inventoryConflictType: String
        get() {
            return when {
                inventoryConflictNumber > 0 -> {
                    "اضافی"
                }

                inventoryConflictNumber < 0 -> {
                    "کسری"
                }

                else -> {
                    "تایید شده"
                }
            }
        }
}
