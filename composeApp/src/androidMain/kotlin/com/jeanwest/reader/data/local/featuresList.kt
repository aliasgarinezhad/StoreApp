@file:OptIn(ExperimentalFoundationApi::class)

package com.jeanwest.reader.data.local

import androidx.compose.foundation.ExperimentalFoundationApi
import com.jeanwest.reader.R
import com.jeanwest.reader.features.banimode.view.BanimodeReceiveReturn
import com.jeanwest.reader.features.banimode.view.BanimodeReturn
import com.jeanwest.reader.features.cardex.viewmodel.Cardex
import com.jeanwest.reader.features.cargo.view.CargoReceive
import com.jeanwest.reader.features.cargo.view.CargoRegistration
import com.jeanwest.reader.features.carton.view.AddOrRemoveCarton
import com.jeanwest.reader.features.carton.view.CartonCreate
import com.jeanwest.reader.features.carton.view.CartonDetails
import com.jeanwest.reader.features.carton.view.CartonModification
import com.jeanwest.reader.features.carton.view.CartonTransfer
import com.jeanwest.reader.features.carton.view.CartonTransferConfirmation
import com.jeanwest.reader.features.carton.view.CartonsConfirmItems
import com.jeanwest.reader.features.carton.view.SearchAnItemInCartons
import com.jeanwest.reader.features.inventory.view.Inventory
import com.jeanwest.reader.features.inventory.view.InventoryReportDepartment
import com.jeanwest.reader.features.kiosk.view.BarcodeChecker
import com.jeanwest.reader.features.kiosk.view.BarcodeSpecial
import com.jeanwest.reader.features.kiosk.view.Kiosk
import com.jeanwest.reader.features.kiosk.view.KioskCentralWarehouse
import com.jeanwest.reader.features.logistic.view.ConfirmStockDraftLogisticInStore
import com.jeanwest.reader.features.logistic.view.SackCreate
import com.jeanwest.reader.features.logistic.view.StockDraftTransfer
import com.jeanwest.reader.features.logistic.view.StockDraftTransferConfirmation
import com.jeanwest.reader.features.logistic.view.StockDraftTransferToStoreByDriver
import com.jeanwest.reader.features.logistic.view.StockDraftTransferToStoreByDriverCancellation
import com.jeanwest.reader.features.print.view.PrintPriceLabel
import com.jeanwest.reader.features.refillStore.view.Refill
import com.jeanwest.reader.features.refillStore.view.Refill2
import com.jeanwest.reader.features.refillStore.view.RefillManual
import com.jeanwest.reader.features.shelf.inventory.view.ShelfInventory
import com.jeanwest.reader.features.shelf.view.AddressProductShelf
import com.jeanwest.reader.features.shelf.view.CreateShelfInRequest
import com.jeanwest.reader.features.shelf.view.CreateShelfStore
import com.jeanwest.reader.features.shelf.view.NewShelfIn
import com.jeanwest.reader.features.shelf.view.ShelfAddress
import com.jeanwest.reader.features.shelf.view.ShelfContent
import com.jeanwest.reader.features.shelf.view.ShelfContentStore
import com.jeanwest.reader.features.shelf.view.ShelfEnter
import com.jeanwest.reader.features.shelf.view.ShelfEnterStore
import com.jeanwest.reader.features.shelf.view.ShelfExit
import com.jeanwest.reader.features.shelf.view.TransferShelf
import com.jeanwest.reader.features.stockDraft.view.CreateStockDraftFromCarton
import com.jeanwest.reader.features.stockDraft.view.ReverseRefill
import com.jeanwest.reader.features.stockDraft.view.StockDraftAttachEPCs
import com.jeanwest.reader.features.stockDraft.view.StockDraftConfirm
import com.jeanwest.reader.features.stockDraft.view.StockDraftCreate
import com.jeanwest.reader.features.stockDraft.view.StockDraftDetails
import com.jeanwest.reader.features.stockDraft.view.StockDraftsHistory
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestConfirm
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestCreateStore
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestFindItems
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestFindStore
import com.jeanwest.reader.features.write.view.WriteTag
import com.jeanwest.reader.models.Feature

const val mainTitle = "مدیریت انبار"

/**
 * A list of available features in the application.
 *
 * Each [Feature] represents a specific functionality, defined by its unique identifier,
 * supported locations, display name, icon, and the corresponding activity class.
 *
 * The features are organized based on their relevance to different user roles and store locations,
 * allowing for dynamic UI adaptation and feature availability.
 */
@OptIn(ExperimentalFoundationApi::class)
val features = mutableListOf(

    Feature(
        "CreateStockDraftFromStoreWarehouseToStore",
        listOf(FeatureLocation.STORE_WAREHOUSE),
        R.string.manualRefill,
        R.drawable.refill,
        RefillManual::class.java
    ),
    Feature(
        "CreateStockDraftFromRefill",
        listOf(FeatureLocation.STORE_WAREHOUSE),
        R.string.refill,
        R.drawable.refill,
        Refill::class.java
    ),
    Feature(
        "CreateStockDraftFromRefill",
        listOf(FeatureLocation.STORE_WAREHOUSE),
        R.string.refill2,
        R.drawable.refill,
        Refill2::class.java
    ),
    Feature(
        "CreateStockDraftFromStoreToStoreWarehouse",
        listOf(FeatureLocation.STORE),
        R.string.reverseRefill,
        R.drawable.add_list,
        ReverseRefill::class.java
    ),
    Feature(
        "ConfirmStockDraft",
        listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        R.string.stock_draft_confirmation,
        R.drawable.confirm_stock_draft,
        StockDraftConfirm::class.java
    ),
    Feature(
        "PrintPriceLabel",
        listOf(FeatureLocation.STORE_WAREHOUSE),
        R.string.PrintPriceLabel,
        R.drawable.write,
        PrintPriceLabel::class.java
    ),
    Feature(
        "Kiosk",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.kiosk,
        R.drawable.search,
        Kiosk::class.java
    ),
    Feature(
        "CreateStockDraft",
        listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        R.string.stockDraftCreate,
        R.drawable.add_list,
        StockDraftCreate::class.java
    ),
    Feature(
        "StockDraftRequestStoreCreate",
        listOf(
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.STORE,
            FeatureLocation.CENTRAL_WAREHOUSE,
        ),
        R.string.StockDraftRequestCreateStore,
        R.drawable.add_list,
        StockDraftRequestCreateStore::class.java
    ),
    Feature(
        "StockDraftRequestStoreFindAndFinish",
        listOf(
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.STORE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        R.string.StockDraftRequestFindStore,
        R.drawable.confirm_stock_draft,
        StockDraftRequestFindStore::class.java
    ),
    Feature(
        "StockDraftDetails",
        listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        R.string.stockDraftDetails,
        R.drawable.ic_history,
        StockDraftDetails::class.java
    ),
    Feature(
        "StockDraftDetails",
        listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        R.string.stockDraftsHistory,
        R.drawable.ic_history,
        StockDraftsHistory::class.java
    ),
    Feature(
        "Cardex",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.Cardex,
        R.drawable.ic_cardex,
        Cardex::class.java
    ),
    Feature(
        "RFIDTagWrite",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.tagProgramming,
        R.drawable.write,
        WriteTag::class.java
    ),
    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.inventoryText,
        R.drawable.inventory,
        Inventory::class.java
    ),
    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.inventoryReportDepartment,
        R.drawable.inventory,
        InventoryReportDepartment::class.java
    ),
    Feature(
        "ShelfFindAnItem",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.shelf_address,
        R.drawable.shelf_address_ic,
        ShelfAddress::class.java
    ),
    Feature(
        "ShelfDetails",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.shelf_content,
        R.drawable.shelf_content_ic,
        ShelfContent::class.java
    ),
    Feature(
        "ShelfAddItem",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.shelf_enter,
        R.drawable.enter_shelf_ic,
        ShelfEnter::class.java
    ),
    Feature(
        "ShelfRemoveItem",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.shelf_exit,
        R.drawable.exit_shelf_ic,
        ShelfExit::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.shelf_transfer,
        R.drawable.ic_shelf,
        TransferShelf::class.java
    ),
    Feature(
        "ProductNumberInAllDepartments",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.ShelfInventory,
        R.drawable.ic_shelf,
        ShelfInventory::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftRequestFind,
        R.drawable.search,
        StockDraftRequestFindItems::class.java
    ),

    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.ShelfInRequest,
        R.drawable.ic_shelf,
        CreateShelfInRequest::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.NewShelfInRequest,
        R.drawable.ic_shelf,
        CreateShelfInRequest::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.ShelfInNew,
        R.drawable.ic_shelf,
        NewShelfIn::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseCheckFoundedAndFinish",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftRequestConfirm,
        R.drawable.confirm_stock_draft,
        StockDraftRequestConfirm::class.java
    ),
    Feature(
        "CartonAddToShelfOrRemoveFrom",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.addOrRemoveCarton,
        R.drawable.true_flase,
        AddOrRemoveCarton::class.java
    ),
    Feature(
        "CartonCreate",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.createCarton,
        R.drawable.carton,
        CartonCreate::class.java
    ),
    Feature(
        "CartonDetails",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.showCarton,
        R.drawable.show_carton,
        CartonDetails::class.java
    ),
    Feature(
        "CartonTransferBegin",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.transferCarton,
        R.drawable.transfer_cartons,
        CartonTransfer::class.java
    ),
    Feature(
        "CartonTransferFinish",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.cartonTransferConfirmation,
        R.drawable.transfer_cartons,
        CartonTransferConfirmation::class.java
    ),
    Feature(
        "CartonCreate",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.CartonModification,
        R.drawable.ic_carton_modification,
        CartonModification::class.java
    ),
    Feature(
        "ProductNumberInAllDepartments",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.barcodeChecker,
        R.drawable.search,
        BarcodeChecker::class.java
    ),
    Feature(
        "ProductNumberInSpecialWarehouse",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.kioskCurrentWarehouse,
        R.drawable.search,
        KioskCentralWarehouse::class.java
    ),
    Feature(
        "ProductNumberInSpecialWarehouses",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.barcodeSpecial,
        R.drawable.search,
        BarcodeSpecial::class.java
    ),
    Feature(
        "CreateStockDraftAddRFIDTagsData",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftAttachEpcs,
        R.drawable.confirm_stock_draft,
        StockDraftAttachEPCs::class.java
    ),
    Feature(
        "TransferStockDraftLocallyBegin",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftTransfer,
        R.drawable.confirm_stock_draft,
        StockDraftTransfer::class.java
    ),
    Feature(
        "TransferStockDraftLocallyFinish",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftTransferConfirmation,
        R.drawable.confirm_stock_draft,
        StockDraftTransferConfirmation::class.java
    ),
    Feature(
        "TransferStockDraftBegin",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.ReceiveLogistics,
        R.drawable.receive_logistics_ic,
        StockDraftTransferToStoreByDriver::class.java
    ),
    Feature(
        "TransferStockDraftCancel",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.ReturnLogistics,
        R.drawable.ic_baseline_clear_24,
        StockDraftTransferToStoreByDriverCancellation::class.java
    ),
    Feature(
        "TransferStockDraftFinish",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.stockDraftTransferToStoreByDriverConfirmation,
        R.drawable.ic_baseline_drive_eta_24,
        ConfirmStockDraftLogisticInStore::class.java
    ),
    Feature(
        "CartonDetailsConfirm",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.cartonsDetailConfirm,
        R.drawable.carton,
        CartonsConfirmItems::class.java
    ),
    Feature(
        "TransferStockDraftBegin",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.sackCreate,
        R.drawable.ic_sack,
        SackCreate::class.java
    ),
    Feature(
        "CartonTransferBegin",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.createStockDraftFromCarton,
        R.drawable.ic_sack,
        CreateStockDraftFromCarton::class.java
    ),
    Feature(
        "CartonDetails",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.searchItemsInCartons,
        R.drawable.shelf_address_ic,
        SearchAnItemInCartons::class.java
    ),
    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.CreateShelfStore,
        R.drawable.ic_shelf,
        CreateShelfStore::class.java
    ),
    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.EnterShelfStore,
        R.drawable.enter_shelf_ic,
        ShelfEnterStore::class.java
    ),

    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.AddressProductStore,
        R.drawable.ic_search,
        AddressProductShelf::class.java
    ),
    Feature(
        "Inventory",
        listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        R.string.shelf_content,
        R.drawable.shelf_content_ic,
        ShelfContentStore::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.CargoRegistration,
        R.drawable.ic_cargo,
        CargoRegistration::class.java
    ),
    Feature(
        "StockDraftRequestCentralWarehouseFind",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.CargoReceive,
        R.drawable.ic_cargo,
        CargoReceive::class.java
    ),
    Feature(
        "BanimodeReturn",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.banimodeReturn,
        R.drawable.bani,
        BanimodeReturn::class.java
    ),
    Feature(
        "BanimodeRecieveReturn",
        listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        R.string.banimodeReceiveReturn,
        R.drawable.bani,
        BanimodeReceiveReturn::class.java
    )
)

val cartonsFeatures = listOf(
    "CartonAddToShelfOrRemoveFrom",
    "CartonCreate",
    "CartonDetails",
    "CartonTransferBegin",
    "CartonTransferFinish",
    "CartonBarcodeAddress",
    "CartonDetailsConfirm",
    "CartonModification"
)

val stockDraftsFeatures = listOf(
    "CreateStockDraftAddRFIDTagsData",
    "StockDraftDetails",
    "ConfirmStockDraft",
    "CreateStockDraft"
)

val shelfFeatures = listOf(
    "ShelfRemoveItem",
    "ShelfAddItem",
    "ShelfDetails",
    "ShelfAddressCarton",
    "ShelfFindAnItem"
)

val driverFeatures = listOf(
    "TransferStockDraftFinish",
    "TransferStockDraftCancel",
    "TransferStockDraftBegin"
)

val transferFeatures = listOf(
    "TransferStockDraftLocallyFinish",
    "TransferStockDraftLocallyBegin",
    "SackCreate",
    "CreateStockDraftFromCarton",
)

val mojoodiReviewFeatures = listOf(
    "ProductNumberInSpecialWarehouses",
    "ProductNumberInSpecialWarehouse",
    "ProductNumberInAllDepartments",
    "RFIDTagWrite"
)

val requestFeatures = listOf(
    "StockDraftRequestCentralWarehouseFind",
    "StockDraftRequestCentralWarehouseCheckFoundedAndFinish",
    "StockDraftRequestStoreFindAndFinish",
    "StockDraftRequestStoreCreate"
)

val banimodeFeatures = listOf(
    "BanimodeRecieveReturn",
    "BanimodeReturn"
)