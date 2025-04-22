package com.jeanwest.reader.features.shelf.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.models.StockDraftRequest
import com.jeanwest.reader.models.StockDraftRequestItem
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the process of entering products into shelves.
 *
 * This ViewModel handles the logic for creating and managing stock draft requests,
 * scanning products and shelves, and updating the stock information.
 *
 * @param context The application context.
 * @param memory Shared preferences for storing user data.
 * @param api The remote API for interacting with the backend.
 * @param */
@HiltViewModel
class NewShelfInViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val memory: SharedPreference,
    val api: API,
    val repository: RepositoryImpl,
    val state: SnackbarHostState,
    val popupHost: NotificationPopupHost,
) : ViewModel() {

    private var barcode: Barcode
    var rfid: RFID
    private var scannedBarcodesWithDetails = mutableMapOf<String, Product>()
    private val scanAndCompareUiItemsWithPrimaryKeyMap = mutableMapOf<Long, Product>()
    val isStockDraftCreateByRfid = mutableStateOf(false)
    private val asyncScope = CoroutineScope(Dispatchers.Default)
    private val processedEpcs = mutableSetOf<String>()

    var loading by mutableStateOf(false)
        private set
    var screen by mutableStateOf("StockDraftRequestScreen")
        private set
    var currentScreen = "StockDraftRequestScreen"
        private set
    var request = StockDraftRequest()
        private set
    var recommendedShelfsToEnter = mutableStateListOf<ShelfBarcodeAddress>()
        private set
    var selectedProductToEnterShelf by mutableStateOf(StockDraftRequestItem())
        private set
    var selectedShelfToEnterProduct by mutableStateOf("")
        private set
    var requestsList = mutableStateListOf<StockDraftRequest>()
        private set
    var selectedProductToEnterShelfScannedNumber by mutableIntStateOf(0)
        private set
    var scanAndCompareUiList = mutableStateListOf<Product>()
        private set

    val requestItems = mutableStateListOf<StockDraftRequestItem>()

    init {
        exceptionHandler()

        barcode = Barcode(context) {
            if (screen == "ShelfEnterScreen") {
                if (selectedShelfToEnterProduct == "") {
                    onShelfScanned(it)
                } else {
                    onScanSelectedProductToEnterShelf(it)
                }
            } else if (screen == "StockDraftRequestItemsScreen") {
                onScanItemsOfSelectedRequest(it)
            } else if (screen == "ScanAndCompareProduct") {
                getProductDetails(it)
            }
        }

        rfid = RFID(context, state) {
            scanTrigger()
        }

        getUserRequestsList()
    }

    private fun onScanItemsOfSelectedRequest(barcode: String) {

        loading = true
        asyncScope.launch {
            val requestItem = requestItems.find {
                it.KBarcode == barcode || it.product.KBarCode == barcode || it.product.scannedBarcode == barcode
            }
            if (requestItem == null) {
                repository.getBarcodeDetails(
                    barcode = barcode,
                    onSuccess = { scannedItem ->
                        val requestItem2 = requestItems.find {
                            scannedItem.primaryKey == it.primaryKey
                        }
                        if (requestItem2 == null) {
                            showLog(state = state, data = "کالای اسکن شده در لیست وجود ندارد.")
                            errorBeep(state = state)
                        } else {
                            onRequestItemClick(requestItem2)
                        }
                        loading = false
                    },
                    onError = {
                        loading = false
                    })
            } else {
                onRequestItemClick(requestItem)
                loading = false
            }
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    fun onRequestClick(request: StockDraftRequest) {
        this.request = request
        getSelectedRequestDetails(request)
    }

    fun onRequestItemClick(stockDraftRequestItem: StockDraftRequestItem) {
        selectedProductToEnterShelf = stockDraftRequestItem
        selectedShelfToEnterProduct = ""
        selectedProductToEnterShelfScannedNumber = 0
        processedEpcs.clear()
        getRecommendedShelfsToEnter(stockDraftRequestItem.product.KBarCode)
    }

    private fun onShelfScanned(shelfNumber: String) {
        if (shelfNumber.startsWith("SH")) {
            selectedShelfToEnterProduct = shelfNumber
        } else {
            showLog("شماره قفسه نامعتبر است.", state)
        }
        setBarcodeScanner()
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.disconnectFromContext()
        barcode.enable()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()
        setBarcodeScanner()
    }

    fun onFinalizeRequestButtonClick() {
        if (requestItems.isNotEmpty()) {
            loading = true
            asyncScope.launch {
                scanAndCompareUiList.clear()
                scannedBarcodesWithDetails.clear()
                scanAndCompareUiItemsWithPrimaryKeyMap.clear()
                requestItems.forEach {
                    scanAndCompareUiList.add(
                        it.product.copy(
                            requestedNumber = it.notFoundNumber,
                            scannedBarcodeNumber = 0
                        )
                    )
                    scannedBarcodesWithDetails[it.product.KBarCode] = it.product.copy()
                    if (it.product.scannedBarcode.isNotEmpty()) {
                        scannedBarcodesWithDetails[it.product.scannedBarcode] = it.product.copy()
                    }
                    scanAndCompareUiItemsWithPrimaryKeyMap[it.primaryKey] =
                        it.product.copy(
                            requestedNumber = it.notFoundNumber,
                            scannedBarcodeNumber = 0
                        )
                }
                loading = false
                changeScreen("ScanAndCompareProduct")
            }
        } else {
            finishRequest()
        }
    }

    fun onEnterToShelfButtonClick() {
        if (isStockDraftCreateByRfid.value) {
            if (selectedProductToEnterShelfScannedNumber == selectedProductToEnterShelf.requestNumber) {
                callEnterShelfApi()
            } else {
                showLog("برای ورود به قفسه تمام کالاهای درخواستی را اسکن کنید.", state)
                return
            }
        } else {
            callEnterShelfApi()
        }
    }

    private fun callEnterShelfApi() {
        if (!isStockDraftCreateByRfid.value) {
            selectedProductToEnterShelf.product.scannedBarcodeNumber =
                selectedProductToEnterShelfScannedNumber
        }
        loading = true
        api.shelfEnterEpcAndUpdateStockDraft(
            stockDraftRequestID = request.number,
            shelfCode = selectedShelfToEnterProduct,
            products = selectedProductToEnterShelf,
            reasonID = 1,
            onSuccess = {
                loading = false
                getSelectedRequestDetails(request)
                showLog(
                    data = "کالاها با موفقیت به قفسه انتقال داده شدند.",
                    state = state,
                    action = SnackBarActions.SUCCESS
                )
            },
            onError = {
                loading = false
            }
        )
    }

    fun createReturnStockDrafts() {

        loading = true

        val returnProducts = mutableListOf<Product>()
        var hasNotAvailableProducts = false
        var hasAvailableProducts = false
        asyncScope.launch {
            scanAndCompareUiList.forEach { product ->
                returnProducts.add(product.copy(scannedBarcodeNumber = product.requestedNumber))
                if (product.scannedBarcodeNumber > 0) {
                    hasAvailableProducts = true
                }
                if (product.requestedNumber != product.scannedBarcodeNumber) {
                    hasNotAvailableProducts = true
                }
            }

            if (!hasAvailableProducts && !hasNotAvailableProducts) {
                showLog(data = "کالایی برای برگشت وجود ندارد.", state = state)
            } else {

                repository.api.createStockDraft(
                    products = returnProducts,
                    desc = if (hasAvailableProducts && hasNotAvailableProducts) {
                        "کالاهای دارای و بدون موجودی فیزیکی (لطفا موجودی کالا ها چک شود) برگشتی از درخواست ورود به قفسه به شماره ${request.number}"
                    } else if (hasAvailableProducts) {
                        "کالاهای دارای موجودی فیزیکی برگشتی از درخواست ورود به قفسه به شماره ${request.number}"
                    } else {
                        "کالاهای بدون موجودی فیزیکی برگشتی از درخواست ورود به قفسه به شماره ${request.number}"
                    },
                    source = 44,
                    destination = request.source.toIntOrNull() ?: 0,
                    onSuccess = {
                        popupHost.showPopupWithAButton(
                            message = if (hasAvailableProducts && hasNotAvailableProducts) {
                                "حواله برگشتی کالاهای دارای و بدون موجودی فیزیکی با شماره حواله $it با موفقیت ثبت شد. "
                            } else if (hasAvailableProducts) {
                                "حواله برگشتی کالاهای دارای موجودی فیزیکی با شماره حواله $it با موفقیت ثبت شد. "
                            } else {
                                "حواله برگشتی کالاهای بدون موجودی فیزیکی با شماره حواله $it با موفقیت ثبت شد. "
                            },
                            onDismiss = {
                                finishRequest()
                            }, onDoneButtonClick = {
                                finishRequest()
                            }
                        )
                        loading = false
                    },
                    onError = {
                        showLog(
                            data = "مشکلی در ثبت حواله پیش آمده است.",
                            state = state
                        )
                        loading = false
                    }
                )
            }
        }
    }

    private fun getProductDetails(productCode: String) {

        loading = true
        viewModelScope.launch {
            scannedBarcodesWithDetails[productCode].let { productDetails ->
                if (productDetails == null) {
                    repository.getBarcodeDetails(productCode, onSuccess = {
                        scannedBarcodesWithDetails[it.scannedBarcode] = it.copy()
                        addToScanAndCompareProductsUiList(it)
                        loading = false
                    }, onError = {
                        showLog(data = "مشخصات بارکد یافت نشد.", state = state)
                        errorBeep(state = state)
                        loading = false
                    })
                } else {
                    addToScanAndCompareProductsUiList(productDetails)
                    loading = false
                }
            }
        }
    }

    private fun addToScanAndCompareProductsUiList(product: Product) {

        scanAndCompareUiItemsWithPrimaryKeyMap[product.primaryKey].let { productDetails ->
            if (productDetails == null) {
                showLog(data = "کالا در لیست نیست.", state = state)
                errorBeep(state = state)
            } else {
                if (productDetails.requestedNumber > productDetails.scannedBarcodeNumber) {
                    successBeep(state = state)
                    productDetails.scannedBarcodeNumber++
                } else {
                    showLog(
                        data = "تعداد اسکن شده کالا از تعداد در لیست بیشتر نمی تواند باشد.",
                        state = state
                    )
                    errorBeep(state = state)
                }
            }
            scanAndCompareUiList.clear()
            scanAndCompareUiList.addAll(scanAndCompareUiItemsWithPrimaryKeyMap.values)
        }
    }

    fun getUserRequestsList() {

        loading = true

        api.getAllStockDraftRequestsListV2(
            sourceWarehouse = null,
            createUserId = memory.user.username,
            isJoorComplete = false,
            requestTypeId = null,
            onSuccess = { requests ->
                requestsList.clear()
                requestsList.addAll(requests.filter { it.destination == "IT" })
                loading = false
                if (screen != "StockDraftRequestScreen") changeScreen("StockDraftRequestScreen")
            },
            onError = {
                loading = false
            }
        )
    }

    private fun getSelectedRequestDetails(request: StockDraftRequest) {

        loading = true
        api.getStockDraftRequestDetailsV2(
            request.number.toString(),
            onSuccess = { requestDetails ->
                this.request = requestDetails
                requestItems.clear()
                isStockDraftCreateByRfid.value = requestDetails.isCreateByRfid
                val insertedPrimaryKeys = mutableListOf<String>()
                requestDetails.items.filter { it.notFoundNumber > 0 }.forEach {
                    if (it.KBarcode !in insertedPrimaryKeys) {
                        requestItems.add(it)
                        insertedPrimaryKeys.add(it.KBarcode)
                    }
                }

                if (requestItems.isEmpty()) {
                    loading = false
                    changeScreen("StockDraftRequestItemsScreen")
                } else {
                    if (isStockDraftCreateByRfid.value) {
                        barcode.disable()
                    }
                    getSelectedRequestItemsDetails()
                }
            },
            onError = {
                loading = false
            }
        )
    }

    private fun getSelectedRequestItemsDetails() {

        loading = true

        repository.getItemDetailsAndInventory(
            epcs = listOf(),
            barcodes = requestItems.map { it.KBarcode }.distinct(),
            onSuccess = { _, barcodes, _, _ ->

                requestItems.forEach { requestItem ->
                    barcodes.find { requestItem.primaryKey == it.primaryKey }?.let {
                        requestItem.product = it.copy()
                    }
                }
                loading = false
                changeScreen("StockDraftRequestItemsScreen")
            },
            onError = {
                loading = false
            }
        )
    }

    private fun onScanSelectedProductToEnterShelf(barcode: String) {
        if (selectedProductToEnterShelf.product.scannedBarcode == barcode ||
            selectedProductToEnterShelf.product.KBarCode == barcode
        ) {
            increaseSelectedProductScannedNumber()
        } else {
            loading = true
            repository.getBarcodeDetails(
                barcode = barcode,
                onSuccess = {
                    if (it.primaryKey == selectedProductToEnterShelf.primaryKey) {
                        increaseSelectedProductScannedNumber()
                    } else {
                        showLog(
                            data = "بارکد اسکن شده با کالای انتخاب شده مطابقت ندارد.",
                            state = state
                        )
                        errorBeep(state = state)
                    }
                    loading = false
                },
                onError = {
                    loading = false
                    errorBeep(state = state)
                })
        }
    }

    private fun onScanSelectedProductToEnterShelfByRfid() {
        if (rfid.scanning) {
            rfid.stopScanning()
            compareInputEpcsWithSelectedProduct()
        } else {
            rfid.startBulkScan(
                justFindInputEPCs = true,
                inputEPCs = selectedProductToEnterShelf.epcs
            )
        }
    }

    private fun compareInputEpcsWithSelectedProduct() {
        Log.e("rfidSelectedProduct", selectedProductToEnterShelf.epcs.toList().toString())
        Log.e("rfidSearchedEpcs", rfid.epcs.toList().toString())
        val commonEpcs = selectedProductToEnterShelf.epcs.intersect(rfid.epcs)
        Log.e("rfidCommon", commonEpcs.toString())
        commonEpcs.forEach { epc ->
            if (epc !in processedEpcs) {
                processedEpcs.add(epc)
                increaseSelectedProductScannedNumber()
            }
        }
    }

    private fun increaseSelectedProductScannedNumber() {
        if (selectedProductToEnterShelf.product.wareHouseNumber >= selectedProductToEnterShelfScannedNumber) {
            if (selectedProductToEnterShelf.requestNumber >= selectedProductToEnterShelfScannedNumber) {
                selectedProductToEnterShelfScannedNumber++
                successBeep(state = state)
            } else {
                showLog("تعداد اسکن شده نباید بیشتر از تعداد درخواست باشد.", state)
                errorBeep(state = state)
            }
        } else {
            showLog("تعداد اسکن شده نباید بیشتر از موجودی باشد.", state)
            errorBeep(state = state)
        }
    }

    fun increaseSelectedProductScannedNumberByInputValue(input: String) {
        val value = input.toIntOrNull() ?: 0
        if (selectedProductToEnterShelf.product.wareHouseNumber >= value) {
            if (selectedProductToEnterShelf.requestNumber >= value) {
                selectedProductToEnterShelfScannedNumber = value
                successBeep(state = state)
            } else {
                showLog("تعداد اسکن شده نباید بیشتر از تعداد درخواست باشد.", state)
                errorBeep(state = state)
            }
        } else {
            showLog("تعداد اسکن شده نباید بیشتر از موجودی باشد.", state)
            errorBeep(state = state)
        }
    }

    private fun finishRequest() {

        loading = true
        repository.api.finalizeStockDraftRequestFindingProcess(
            stockDraftRequestID = request.number,
            username = memory.user.username,
            onSuccess = {
                loading = false
                popupHost.showPopupWithAButton(
                    message = "پایان ورود به قفسه ثبت شد.",
                    onDoneButtonClick = {
                        getUserRequestsList()
                    },
                    onDismiss = {
                        getUserRequestsList()
                    }
                )
            }, onError = {
                loading = false
            }
        )
    }

    private fun getRecommendedShelfsToEnter(barcode: String) {
        loading = true
        repository.api.shelfBarcodeAddress(
            wareHouseId = 44,
            barcode = barcode,
            onSuccess = {
                recommendedShelfsToEnter.clear()
                recommendedShelfsToEnter.addAll(it)
                recommendedShelfsToEnter.sortByDescending { shelfInfo ->
                    shelfInfo.qty
                }
                loading = false
                changeScreen("ShelfEnterScreen")
            }, onError = {
                loading = false
            }
        )
    }

    private fun setBarcodeScanner() {
        if (screen == "StockDraftRequestScreen" || (isStockDraftCreateByRfid.value && screen == "ShelfEnterScreen" && selectedShelfToEnterProduct != "")) {
            barcode.disable()
        } else {
            barcode.enable()
        }
    }

    fun scanTrigger() {
        if (isStockDraftCreateByRfid.value && (currentScreen == "ShelfEnterScreen" && selectedShelfToEnterProduct != "")) {
            onScanSelectedProductToEnterShelfByRfid()
        }
    }

    fun changeScreen(screen: String) {
        this.screen = screen
        setBarcodeScanner()
    }

    fun onScreenChanged() {
        currentScreen = screen
    }

}