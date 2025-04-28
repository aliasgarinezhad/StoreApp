package com.jeanwest.reader.features.stockDraftRequest.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.models.StockDraftRequest
import com.jeanwest.reader.models.StockDraftRequestItem
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.jalaliDate.JalaliDateConverter
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for managing the stock draft request finding process.
 *
 * This ViewModel handles the logic for finding stock draft requests, updating their status,
 * and interacting with barcode and RFID scanners. It manages the UI state for different steps
 * in the process, including searching for requests, viewing details, selecting shelves,
 * and updating the number of found items.
 *
 * @property state The [SnackbarHostState] for displaying snackbar messages.
 * @property memory The [SharedPreference] instance for accessing user preferences and data.
 * @property api The [API] instance for making API calls.
 * @property context The application context.
 */
@HiltViewModel
class StockDraftRequestFindViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var popupState = NotificationPopupHost()
        private set
    private val rf: RFID
    private val barcode: Barcode
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var selectedStockDraftRequestDetails = StockDraftRequest()
    private var selectedProductFromUiList2 = StockDraftRequestItem()
    private var selectedShelfFromUiList3 = ShelfBarcodeAddress()
    var stockDraftRequestNumber by mutableStateOf("")
    private var stockDraftRequestsList = mutableStateMapOf<Long, StockDraftRequest>()

    var openUpdateUserDialog by mutableStateOf(false)
    var updateUserDialogOnConfirm: () -> Unit = {}
    var updateUserDialogOnNotConfirmOrDismiss: () -> Unit = {}

    var step by mutableIntStateOf(1)
        private set
    var loading by mutableStateOf(false)
        private set
    var uiList3 = mutableStateListOf<ShelfBarcodeAddress>()
        private set
    var stockDraftRequestLocation by mutableStateOf("انتخاب مقصد")
        private set
    var stockDraftRequestLocationsList: MutableList<String> = mutableListOf()
        private set
    var uiList1 = mutableStateListOf<StockDraftRequest>()
        private set
    var scanning by mutableStateOf(false)
        private set
    var foundNumber by mutableIntStateOf(0)
        private set
    var notFoundNumber by mutableIntStateOf(0)
        private set
    var requestedNumber by mutableIntStateOf(0)
        private set
    var uiList2 = mutableStateListOf<StockDraftRequestItem>()
        private set
    var stockDraftRequestSearchKey by mutableStateOf("")
        private set

    var uiItem4 = mutableStateOf(StockDraftRequestItem())
        private set
    var uiItem4Shelf = mutableStateOf(ShelfBarcodeAddress())
        private set
    var isRefresh by mutableStateOf(false)


    init {

        barcode = Barcode(context) {
            if (step == 3) {
                val scannedShelf = uiList3.find { shelf ->
                    shelf.shelfID == it
                }
                if (scannedShelf == null) {
                    showLog("قفسه در لیست موجود نیست.", state)
                } else {
                    selectedShelfFromUiList3 = scannedShelf.copy()
                }
                uiItem4Shelf.value = selectedShelfFromUiList3.copy()
                uiItem4.value = selectedProductFromUiList2.copy()
                step = 4
            } else {
                syncScannedItemToServer(it)
            }
        }

        rf = RFID(context, state) {
            scanTrigger()
        }

        getStockDraftsRequestsList()

        updateUserDialogOnConfirm = {
            updateStockDraftRequestUser()
            openUpdateUserDialog = false
        }
        updateUserDialogOnNotConfirmOrDismiss = {
            openUpdateUserDialog = false
        }

        stockDraftRequestLocationsList.clear()
        stockDraftRequestLocationsList.add("انتخاب مقصد")
        stockDraftRequestLocationsList.addAll(memory.user.destinationTitles)

        requestedNumber = selectedStockDraftRequestDetails.sumOfRequestedItems
        foundNumber = selectedStockDraftRequestDetails.sumOfFoundItems
        notFoundNumber = selectedStockDraftRequestDetails.sumOfNotFoundItems
    }

    fun uiList2OnClick(item: StockDraftRequestItem) {
        searchInShelfs(item.product.KBarCode)
        selectedProductFromUiList2 = item
        selectedProductFromUiList2 = item
        barcode.enable()
    }

    fun uiList3OnClick(shelf: ShelfBarcodeAddress) {

        Log.e("JamAvari", shelf.toString())
        selectedShelfFromUiList3 = shelf.copy()
        Log.e("JamAvari", selectedShelfFromUiList3.toString())

        uiItem4Shelf.value = selectedShelfFromUiList3.copy()
        Log.e("JamAvari", uiItem4Shelf.toString())

        uiItem4.value = selectedProductFromUiList2.copy()
        step = 4
        barcode.enable()
    }

    fun uiList1OnClick(stockDraftRequest: StockDraftRequest) {
        if (stockDraftRequest.user != memory.user.username) {
            stockDraftRequestNumber =
                stockDraftRequest.number.toString()
            openUpdateUserDialog = true
        } else {
            stockDraftRequestNumber =
                stockDraftRequest.number.toString()
            getStockDraftRequestDetails(stockDraftRequestNumber)
        }
    }

    fun stockDraftRequestDropDownFilterOnClick(filter: String) {
        stockDraftRequestLocation = filter
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.enable()
        barcode.disconnectFromContext()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()
        if (step in listOf(3, 4)) {
            barcode.enable()
        } else if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    private fun searchInShelfs(barcode: String) {
        loading = true
        api.shelfBarcodeAddress(memory.user.warehouseCode, barcode, {
            it.sortedBy {
                it.qty > 0
            }
            uiList3.clear()
            uiList3.addAll(it.filter { shelfBarcodeAddress ->
                shelfBarcodeAddress.wareHouseTitle == memory.erpData.warehousesIDsToTitles[memory.user.warehouseCode.toString()]
            })
            step = 3
            this.barcode.enable()
            loading = false
        }, {
            loading = false
        })
    }

    fun bottomBar4OnClick() {

        loading = true

        api.updateStockDraftRequestNumberOfFound(
            stockDraftRequestID = stockDraftRequestNumber.toLong(),
            product = selectedProductFromUiList2.product,
            username = memory.user.username.toString(),
            source = memory.user.warehouseCode,
            reasonID = 1,
            shelfCode = selectedShelfFromUiList3.shelfID,
            shelfNumber = selectedShelfFromUiList3.shelfStockID,
            numberOfFound = this.uiItem4.value.product.scannedBarcodeNumber,
            onSuccess = {
                step = 2
                getStockDraftRequestDetails(stockDraftRequestNumber)
                barcode.disable()
                this.uiItem4.value.product.scannedBarcodeNumber = 0
                loading = false

            }, onError = {
                loading = false
            }
        )
    }

    fun bottomBar2OnClick() {

        popupState.showPopupWith2Button(
            message = "جمع آوری پایان یابد؟",
            onOkClick = {
                loading = true
                api.finalizeStockDraftRequestFindingProcess(
                    stockDraftRequestID = stockDraftRequestNumber.toLong(),
                    username = memory.user.username,
                    onSuccess = {
                        loading = false

                        popupState.showPopupWithAButton(
                            message = "پایان جمع آوری ثبت شد.",
                            onDoneButtonClick = {
                                step = 1
                                getStockDraftsRequestsList()
                                barcode.disable()
                            },
                            onDismiss = {
                                step = 1
                                getStockDraftsRequestsList()
                                barcode.disable()
                            }
                        )
                    }, onError = {
                        loading = false
                    }
                )
            },
        )
    }

    private fun updateStockDraftRequestUser() {

        loading = true

        api.updateStockDraftRequestUser(
            memory.user.username,
            stockDraftRequestNumber.toLong(),
            {
                getStockDraftRequestDetails(stockDraftRequestNumber)
            },
            {
                loading = false
            })
    }

    private fun getStockDraftsRequestsList() {
        loading = true
        api.getAllStockDraftRequestsList(
            sourceWarehouse = memory.user.warehouseCode,
            createUserId = null,
            isJoorComplete = null,
            requestTpeId = null,
            onSuccess = {
                stockDraftRequestsList.clear()
                it.forEach { it1 ->
                    if (it1.user == null || it1.user == memory.user.username) {
                        stockDraftRequestsList[it1.number] = it1
                    }
                }
                uiList1.clear()
                uiList1.addAll(stockDraftRequestsList.values)
                sortStockDraftRequestsListsByCreateDate()
                loading = false
            },
            onError = {
                loading = false
            },
        )
    }

    private fun sortStockDraftRequestsListsByCreateDate() {

        for (elements in uiList1) {
            elements.createDateJalali = elements.date
            val jalaliToGregorian = JalaliDateConverter.jalali_to_gregorian(
                elements.date.substring(0, 4).toInt(),
                elements.date.substring(5, 7).toInt(),
                elements.date.substring(8, 10).toInt()
            )
            val date = LocalDate.of(
                jalaliToGregorian[0],
                jalaliToGregorian[1],
                jalaliToGregorian[2]
            )
            elements.date = date.toString()
        }
        val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        uiList1 = uiList1.sortedByDescending {
            LocalDate.parse(it.date, dateTimeFormatter)
        }.toMutableStateList()
    }

    fun scanTrigger() {

        if (step in listOf(3, 4)) {
            barcode.startBarcodeScan()
        }
    }


    private fun syncInputItemsToServer() {

        loading = true

        val barcodeTableForV4 = mutableListOf<String>()
        var inputProductsBiggerThan100 = false

        run breakForEach@{
            selectedStockDraftRequestDetails.itemsDistinctByKBarcode.keys.forEach {
                if (it !in inputBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 100) {
                        barcodeTableForV4.add(it)
                    } else {
                        inputProductsBiggerThan100 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (barcodeTableForV4.size == 0) {
            loading = false
            makeInputProductMap()
            return
        }

        api.getItemDetails(
            mutableListOf(),
            barcodeTableForV4,
            { _, barcodes, _, invalidBarcodes ->

                barcodes.forEach { product ->
                    inputBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                if (inputProductsBiggerThan100) {
                    syncInputItemsToServer()
                } else if (invalidBarcodes.length() != 0) {
                    loading = false
                    return@getItemDetails
                } else {
                    makeInputProductMap()
                    loading = false
                }
            },
            {
                loading = false
            })
    }

    private fun makeInputProductMap() {

        selectedStockDraftRequestDetails.items.forEach {
            it.product = inputBarcodeMapWithProperties[it.KBarcode]?.copy() ?: Product()
        }

        uiList2.clear()
        uiList2.addAll(selectedStockDraftRequestDetails.items.filter { it.notFoundNumber > 0 })
        uiList2.sortBy {
            it.priority
        }
    }


    fun onAddButtonClick() {

        if (uiItem4.value.product.scannedBarcodeNumber + 1 > uiItem4.value.notFoundNumber) {
            showLog(
                "تعداد اسکن شده نباید از تعداد درخواستی بیشتر باشد.",
                state
            )
        } else if (uiItem4.value.product.scannedBarcodeNumber + 1 > uiItem4Shelf.value.qty) {
            showLog("تعداد اسکن شده نباید از موجودی قفسه بیشتر باشد.", state)
        } else {
            uiItem4.value =
                uiItem4.value.copy(product = uiItem4.value.product.copy(scannedBarcodeNumber = uiItem4.value.product.scannedBarcodeNumber + 1))
        }
    }

    fun onRemoveButtonClick() {
        if (uiItem4.value.product.scannedBarcodeNumber > 1) {
            uiItem4.value =
                uiItem4.value.copy(product = uiItem4.value.product.copy(scannedBarcodeNumber = uiItem4.value.product.scannedBarcodeNumber - 1))
        }
    }


    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size == 1 && barcodes[0].primaryKey == uiItem4.value.primaryKey) {
                    if (uiItem4.value.product.scannedBarcodeNumber + 1 > uiItem4.value.notFoundNumber) {
                        showLog(
                            "تعداد اسکن شده نباید از تعداد درخواستی بیشتر باشد.",
                            state
                        )
                        errorBeep(state)
                    } else if (uiItem4.value.product.scannedBarcodeNumber + 1 > uiItem4Shelf.value.qty) {
                        showLog("تعداد اسکن شده نباید از موجودی قفسه بیشتر باشد.", state)
                        errorBeep(state)
                    } else {
                        uiItem4.value.product.scannedBarcodeNumber += 1
                        successBeep(state)
                    }
                } else {
                    showLog("کالای اسکن شده اشتباه است.", state)
                    errorBeep(state)
                }
                loading = false
            },
            {
                loading = false
            })
    }

    private fun clear() {

        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        requestedNumber = 0
        inputBarcodeMapWithProperties.clear()
    }

    fun back() {

        when (step) {
            4 -> {
                getStockDraftRequestDetails(stockDraftRequestNumber)
            }

            3 -> {
                getStockDraftRequestDetails(stockDraftRequestNumber)
            }

            2 -> {
                step = 1
                getStockDraftsRequestsList()
                barcode.disable()

            }
        }
    }

    fun getStockDraftRequestDetails(code: String) {

        loading = true

        if (code.toLongOrNull() == null) {

            showLog("شماره درخواست حواله وارد شده نامعتبر است.", state)
            loading = false
            return
        }

        api.getStockDraftRequestDetails(code, { stockDraftRequestDetails ->

            selectedStockDraftRequestDetails = stockDraftRequestDetails
            clear()
            step = 2
            requestedNumber = selectedStockDraftRequestDetails.sumOfRequestedItems
            foundNumber = selectedStockDraftRequestDetails.sumOfFoundItems
            notFoundNumber = selectedStockDraftRequestDetails.sumOfNotFoundItems
            barcode.disable()
            syncInputItemsToServer()
        }, {
            showLog(
                "مشکلی در دریافت اطلاعات به وجود آمده است.برای بروزرسانی صفحه را از بالا به پایین بکشید",
                state,
                SnackBarActions.WARNING
            )
            loading = false
        })
    }

    fun onSearchTextFieldChange(fieldValue: String) {
        stockDraftRequestSearchKey = fieldValue
    }
}