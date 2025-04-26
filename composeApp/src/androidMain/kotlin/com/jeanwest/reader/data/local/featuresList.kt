@file:OptIn(ExperimentalFoundationApi::class)

package com.jeanwest.reader.data.local

import androidx.compose.foundation.ExperimentalFoundationApi
import com.jeanwest.reader.FeatureLocation
import com.jeanwest.reader.FeaturePlatforms
import com.jeanwest.reader.factory.main.model.Feature
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
import com.jeanwest.reader.features.shelf.newShelfIn.view.CreateShelfInRequest
import com.jeanwest.reader.features.shelf.view.CreateShelfStore
import com.jeanwest.reader.features.shelf.newShelfIn.view.NewShelfIn
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
import storeapp.composeapp.generated.resources.AddressProductStore
import storeapp.composeapp.generated.resources.Cardex
import storeapp.composeapp.generated.resources.CargoReceive
import storeapp.composeapp.generated.resources.CargoRegistration
import storeapp.composeapp.generated.resources.CartonModification
import storeapp.composeapp.generated.resources.CreateShelfStore
import storeapp.composeapp.generated.resources.EnterShelfStore
import storeapp.composeapp.generated.resources.NewShelfInRequest
import storeapp.composeapp.generated.resources.PrintPriceLabel
import storeapp.composeapp.generated.resources.ReceiveLogistics
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.ReturnLogistics
import storeapp.composeapp.generated.resources.ShelfInNew
import storeapp.composeapp.generated.resources.ShelfInRequest
import storeapp.composeapp.generated.resources.ShelfInventory
import storeapp.composeapp.generated.resources.StockDraftRequestCreateStore
import storeapp.composeapp.generated.resources.StockDraftRequestFindStore
import storeapp.composeapp.generated.resources.addOrRemoveCarton
import storeapp.composeapp.generated.resources.add_list
import storeapp.composeapp.generated.resources.bani
import storeapp.composeapp.generated.resources.banimodeReceiveReturn
import storeapp.composeapp.generated.resources.banimodeReturn
import storeapp.composeapp.generated.resources.barcodeChecker
import storeapp.composeapp.generated.resources.barcodeSpecial
import storeapp.composeapp.generated.resources.barcode_scan_icon
import storeapp.composeapp.generated.resources.carton
import storeapp.composeapp.generated.resources.cartonTransferConfirmation
import storeapp.composeapp.generated.resources.cartonsDetailConfirm
import storeapp.composeapp.generated.resources.confirm_stock_draft
import storeapp.composeapp.generated.resources.createCarton
import storeapp.composeapp.generated.resources.createStockDraftFromCarton
import storeapp.composeapp.generated.resources.enter_shelf_ic
import storeapp.composeapp.generated.resources.exit_shelf_ic
import storeapp.composeapp.generated.resources.ic_cardex
import storeapp.composeapp.generated.resources.ic_history
import storeapp.composeapp.generated.resources.ic_sack
import storeapp.composeapp.generated.resources.ic_shelf
import storeapp.composeapp.generated.resources.inventory
import storeapp.composeapp.generated.resources.inventoryReportDepartment
import storeapp.composeapp.generated.resources.inventoryText
import storeapp.composeapp.generated.resources.kiosk
import storeapp.composeapp.generated.resources.kioskCurrentWarehouse
import storeapp.composeapp.generated.resources.manualRefill
import storeapp.composeapp.generated.resources.receive_logistics_ic
import storeapp.composeapp.generated.resources.refill
import storeapp.composeapp.generated.resources.refill2
import storeapp.composeapp.generated.resources.reverseRefill
import storeapp.composeapp.generated.resources.sackCreate
import storeapp.composeapp.generated.resources.search
import storeapp.composeapp.generated.resources.searchItemsInCartons
import storeapp.composeapp.generated.resources.shelf_address
import storeapp.composeapp.generated.resources.shelf_address_ic
import storeapp.composeapp.generated.resources.shelf_content
import storeapp.composeapp.generated.resources.shelf_content_ic
import storeapp.composeapp.generated.resources.shelf_enter
import storeapp.composeapp.generated.resources.shelf_exit
import storeapp.composeapp.generated.resources.shelf_transfer
import storeapp.composeapp.generated.resources.showCarton
import storeapp.composeapp.generated.resources.show_carton
import storeapp.composeapp.generated.resources.stockDraftAttachEpcs
import storeapp.composeapp.generated.resources.stockDraftCreate
import storeapp.composeapp.generated.resources.stockDraftDetails
import storeapp.composeapp.generated.resources.stockDraftRequestConfirm
import storeapp.composeapp.generated.resources.stockDraftRequestFind
import storeapp.composeapp.generated.resources.stockDraftTransfer
import storeapp.composeapp.generated.resources.stockDraftTransferConfirmation
import storeapp.composeapp.generated.resources.stockDraftTransferToStoreByDriverConfirmation
import storeapp.composeapp.generated.resources.stockDraftsHistory
import storeapp.composeapp.generated.resources.stock_draft_confirmation
import storeapp.composeapp.generated.resources.tagProgramming
import storeapp.composeapp.generated.resources.transferCarton
import storeapp.composeapp.generated.resources.transfer_cartons
import storeapp.composeapp.generated.resources.true_flase
import storeapp.composeapp.generated.resources.write

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
val features = listOf(

    Feature(
        activityClass = RefillManual::class,
        isNavAble = false,
        platformArray = listOf(FeaturePlatforms.ANDROID),
        accessKey = "CreateStockDraftFromStoreWarehouseToStore",
        title = Res.string.manualRefill,
        routeScreen = null,
        iconRes = Res.drawable.refill,
        locationsArray = listOf(FeatureLocation.STORE_WAREHOUSE),
    ),
    Feature(
        accessKey = "CreateStockDraftFromRefill",
        locationsArray = listOf(FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.refill,
        iconRes = Res.drawable.refill,
        activityClass = Refill::class,
        isNavAble = false,
        platformArray = listOf(FeaturePlatforms.ANDROID),
        routeScreen = null
    ),
    Feature(
        accessKey = "CreateStockDraftFromRefill",
        locationsArray = listOf(FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.refill2,
        iconRes = Res.drawable.barcode_scan_icon,
        activityClass = Refill2::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CreateStockDraftFromStoreToStoreWarehouse",
        locationsArray = listOf(FeatureLocation.STORE),
        title = Res.string.reverseRefill,
        iconRes = Res.drawable.add_list,
        activityClass = ReverseRefill::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ConfirmStockDraft",
        locationsArray = listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        title = Res.string.stock_draft_confirmation,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftConfirm::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "PrintPriceLabel",
        locationsArray = listOf(FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.PrintPriceLabel,
        iconRes = Res.drawable.write,
        activityClass = PrintPriceLabel::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Kiosk",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.kiosk,
        iconRes = Res.drawable.search,
        activityClass = Kiosk::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CreateStockDraft",
        locationsArray = listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        title = Res.string.stockDraftCreate,
        iconRes = Res.drawable.add_list,
        activityClass = StockDraftCreate::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestStoreCreate",
        locationsArray = listOf(
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.STORE,
            FeatureLocation.CENTRAL_WAREHOUSE,
        ),
        title = Res.string.StockDraftRequestCreateStore,
        iconRes = Res.drawable.add_list,
        activityClass = StockDraftRequestCreateStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestStoreFindAndFinish",
        locationsArray = listOf(
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.STORE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        title = Res.string.StockDraftRequestFindStore,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftRequestFindStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftDetails",
        locationsArray = listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        title = Res.string.stockDraftDetails,
        iconRes = Res.drawable.ic_history,
        activityClass = StockDraftDetails::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftDetails",
        locationsArray = listOf(
            FeatureLocation.STORE,
            FeatureLocation.STORE_WAREHOUSE,
            FeatureLocation.CENTRAL_WAREHOUSE
        ),
        title = Res.string.stockDraftsHistory,
        iconRes = Res.drawable.ic_history,
        activityClass = StockDraftsHistory::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Cardex",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.Cardex,
        iconRes = Res.drawable.ic_cardex,
        activityClass = Cardex::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "RFIDTagWrite",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.tagProgramming,
        iconRes = Res.drawable.write,
        activityClass = WriteTag::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.inventoryText,
        iconRes = Res.drawable.inventory,
        activityClass = Inventory::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.inventoryReportDepartment,
        iconRes = Res.drawable.inventory,
        activityClass = InventoryReportDepartment::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ShelfFindAnItem",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.shelf_address,
        iconRes = Res.drawable.shelf_address_ic,
        activityClass = ShelfAddress::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ShelfDetails",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.shelf_content,
        iconRes = Res.drawable.shelf_content_ic,
        activityClass = ShelfContent::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ShelfAddItem",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.shelf_enter,
        iconRes = Res.drawable.enter_shelf_ic,
        activityClass = ShelfEnter::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ShelfRemoveItem",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.shelf_exit,
        iconRes = Res.drawable.exit_shelf_ic,
        activityClass = ShelfExit::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.shelf_transfer,
        iconRes = Res.drawable.ic_shelf,
        activityClass = TransferShelf::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ProductNumberInAllDepartments",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.ShelfInventory,
        iconRes = Res.drawable.ic_shelf,
        activityClass = ShelfInventory::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftRequestFind,
        iconRes = Res.drawable.search,
        activityClass = StockDraftRequestFindItems::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),

    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.ShelfInRequest,
        iconRes = Res.drawable.ic_shelf,
        activityClass = CreateShelfInRequest::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.NewShelfInRequest,
        iconRes = Res.drawable.ic_shelf,
        activityClass = CreateShelfInRequest::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.ShelfInNew,
        iconRes = Res.drawable.ic_shelf,
        activityClass = NewShelfIn::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseCheckFoundedAndFinish",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftRequestConfirm,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftRequestConfirm::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonAddToShelfOrRemoveFrom",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.addOrRemoveCarton,
        iconRes = Res.drawable.true_flase,
        activityClass = AddOrRemoveCarton::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonCreate",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.createCarton,
        iconRes = Res.drawable.carton,
        activityClass = CartonCreate::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonDetails",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.showCarton,
        iconRes = Res.drawable.show_carton,
        activityClass = CartonDetails::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonTransferBegin",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.transferCarton,
        iconRes = Res.drawable.transfer_cartons,
        activityClass = CartonTransfer::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonTransferFinish",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.cartonTransferConfirmation,
        iconRes = Res.drawable.transfer_cartons,
        activityClass = CartonTransferConfirmation::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonCreate",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.CartonModification,
        iconRes = Res.drawable.carton,
        activityClass = CartonModification::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ProductNumberInAllDepartments",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.barcodeChecker,
        iconRes = Res.drawable.search,
        activityClass = BarcodeChecker::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ProductNumberInSpecialWarehouse",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.kioskCurrentWarehouse,
        iconRes = Res.drawable.search,
        activityClass = KioskCentralWarehouse::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "ProductNumberInSpecialWarehouses",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.barcodeSpecial,
        iconRes = Res.drawable.search,
        activityClass = BarcodeSpecial::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CreateStockDraftAddRFIDTagsData",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftAttachEpcs,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftAttachEPCs::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftLocallyBegin",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftTransfer,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftTransfer::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftLocallyFinish",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftTransferConfirmation,
        iconRes = Res.drawable.confirm_stock_draft,
        activityClass = StockDraftTransferConfirmation::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftBegin",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.ReceiveLogistics,
        iconRes = Res.drawable.receive_logistics_ic,
        activityClass = StockDraftTransferToStoreByDriver::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftCancel",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.ReturnLogistics,
        iconRes = Res.drawable.transfer_cartons,
        activityClass = StockDraftTransferToStoreByDriverCancellation::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftFinish",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.stockDraftTransferToStoreByDriverConfirmation,
        iconRes = Res.drawable.transfer_cartons,
        activityClass = ConfirmStockDraftLogisticInStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonDetailsConfirm",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.cartonsDetailConfirm,
        iconRes = Res.drawable.carton,
        activityClass = CartonsConfirmItems::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "TransferStockDraftBegin",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.sackCreate,
        iconRes = Res.drawable.ic_sack,
        activityClass = SackCreate::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonTransferBegin",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.createStockDraftFromCarton,
        iconRes = Res.drawable.ic_sack,
        activityClass = CreateStockDraftFromCarton::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "CartonDetails",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.searchItemsInCartons,
        iconRes = Res.drawable.shelf_address_ic,
        activityClass = SearchAnItemInCartons::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.CreateShelfStore,
        iconRes = Res.drawable.ic_shelf,
        activityClass = CreateShelfStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.EnterShelfStore,
        iconRes = Res.drawable.enter_shelf_ic,
        activityClass = ShelfEnterStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),

    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.AddressProductStore,
        iconRes = Res.drawable.search,
        activityClass = AddressProductShelf::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "Inventory",
        locationsArray = listOf(FeatureLocation.STORE, FeatureLocation.STORE_WAREHOUSE),
        title = Res.string.shelf_content,
        iconRes = Res.drawable.shelf_content_ic,
        activityClass = ShelfContentStore::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.CargoRegistration,
        iconRes = Res.drawable.search,
        activityClass = CargoRegistration::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "StockDraftRequestCentralWarehouseFind",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.CargoReceive,
        iconRes = Res.drawable.search,
        activityClass = CargoReceive::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "BanimodeReturn",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.banimodeReturn,
        iconRes = Res.drawable.bani,
        activityClass = BanimodeReturn::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
    ),
    Feature(
        accessKey = "BanimodeRecieveReturn",
        locationsArray = listOf(FeatureLocation.CENTRAL_WAREHOUSE),
        title = Res.string.banimodeReceiveReturn,
        iconRes = Res.drawable.bani,
        activityClass = BanimodeReceiveReturn::class,
        isNavAble = false,
        routeScreen = null,
        platformArray = listOf(FeaturePlatforms.ANDROID),
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