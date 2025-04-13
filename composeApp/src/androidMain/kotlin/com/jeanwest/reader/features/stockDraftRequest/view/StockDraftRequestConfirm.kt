@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.stockDraftRequest.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AlertDialogWith2ButtonDropDownList
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.ScanTypeDropDownList
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.primaryContainerLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.secondaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.features.shared.warningColor
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraftRequest
import com.jeanwest.reader.models.StockDraftRequestItem
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.jalaliDate.JalaliDateConverter
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftRequestConfirm : ComponentActivity() {

    lateinit var rf: RFID
    lateinit var barcode: Barcode
    val inputProducts = mutableMapOf<String, StockDraftRequestItem>()
    var scannedProducts = mutableStateMapOf<String, StockDraftRequestItem>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var scannedEpcMapWithProperties = mutableMapOf<String, Product>()
    private var scannedBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var selectedStockDraftRequestDetails = StockDraftRequest()

    //ui parameters
    var productConflicts = mutableStateListOf<StockDraftRequestItem>()
    var loading by mutableStateOf(false)
    private var shortagesNumber by mutableIntStateOf(0)
    private var additionalNumber by mutableIntStateOf(0)
    var numberOfScanned by mutableIntStateOf(0)
    private var itemsUiList = mutableStateListOf<StockDraftRequestItem>()
    private var stockDraftRequestsUiList = mutableStateListOf<StockDraftRequest>()
    private val scanValues =
        mutableListOf("اضافی", "کسری درخواستی", "همه", "بدون مغایرت", "کسری جور شده")
    private var scanFilter by mutableIntStateOf(2)
    private var openClearDialog by mutableStateOf(false)
    private var scanTypeValue by mutableStateOf("بارکد")
    private var createTypeValue by mutableStateOf("ایجاد حواله")
    private var stockDraftRequestNumber by mutableStateOf("")
    private var stockDraftRequestLocation by mutableStateOf("انتخاب مقصد")
    private var stockDraftRequestLocationsList = mutableListOf<String>()
    var step by mutableIntStateOf(0)
    private var openFinishDialog by mutableStateOf(false)
    private val listState = LazyListState(0)
    private var stockDraftRequestsList = mutableStateMapOf<Long, StockDraftRequest>()
    private var popupState = NotificationPopupHost()
    private var printer by mutableStateOf("")
    private var requestDescription by mutableStateOf("تایید جورکنی با RFID")
    private var openPrintDialog by mutableStateOf(false)
    private var stockDraftRequestSearchKey by mutableStateOf("")
    private var packageType by mutableStateOf("")

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exceptionHandler()
        init()
        setContent {
            Page()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) {
            if (step == 1) {
                syncScannedItemToServer(it)
            }
        }

        printer = memory.erpData.printers.keys.toList()[0]
        packageType = memory.erpData.packageTypes.keys.toList()[0]
        loadMemory()

        stockDraftRequestLocationsList.clear()
        stockDraftRequestLocationsList.add("انتخاب مقصد")
        stockDraftRequestLocationsList.addAll(memory.user.destinationTitles)
        when (step) {
            1 -> {
                syncInputItemsToServer()
            }

            else -> {
                step = 0
                getStockDraftsRequestsList()
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

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        barcode.enable()
        barcode.disconnectFromContext()
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
        if (scanTypeValue == "بارکد" && step == 1) {
            barcode.enable()
        } else {
            barcode.disable()
        }
    }

    private fun finalizeRequest(stockDraftRequestNumber: Long) {

        popupState.showPopupWith2Button(
            message = "درخواست بسته شود؟",
            onOkClick = {
                loading = true
                api.closeStockDraftRequest(
                    stockDraftRequestID = stockDraftRequestNumber,
                    username = memory.user.username,
                    onSuccess = {
                        loading = false
                        popupState.showPopupWithAButton(
                            message = "درخواست بسته شد.",
                            onDoneButtonClick = {
                                getStockDraftsRequestsList()
                                barcode.disable()
                            },
                            onDismiss = {
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

    private fun createNewStockDraft() {

        loading = true

        api.createStockDraftByStockDraftRequest(
            requestID = selectedStockDraftRequestDetails.number.toString(),
            products = scannedProducts.values.toMutableList(),
            desc = requestDescription,
            source = memory.user.warehouseCode,
            destination = selectedStockDraftRequestDetails.destination.toIntOrNull() ?: 0,
            packageType = memory.erpData.packageTypes[packageType] ?: 0,
            {
                printStockDraftLabel(it)
            }, {
                loading = false
            }
        )
    }

    private fun createNewCarton() {
        loading = true
        api.createCartonByRequest(
            stockDraftRequestNumber = selectedStockDraftRequestDetails.number.toString(),
            printer = memory.erpData.printers[printer] ?: 0,
            packageType = memory.erpData.packageTypes[packageType] ?: 0,
            desc = requestDescription,
            products = scannedProducts.values.toMutableList(),
            onSuccess = {
                printCartonLabel(it)
            }
        ) {
            loading = false
        }
    }

    private fun printStockDraftLabel(stockDraftNumber: String) {

        if (printer == "بدون لیبل") {
            popupState.showPopupWithAButton(
                "حواله با شماره $stockDraftNumber با تعداد ${numberOfScanned} ایجاد شد.",
                onDoneButtonClick = {
                    clear()
                    getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                },
                onDismiss = {
                    clear()
                    getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                })
            loading = false
            return
        }

        api.printStockDraftLabel(
            memory.erpData.printers[printer] ?: 0,
            stockDraftNumber,
            {
                popupState.showPopupWithAButton(
                    "حواله با شماره $stockDraftNumber با تعداد ${numberOfScanned} ایجاد و دستور پرینت لیبل آن ارسال شد.",
                    onDoneButtonClick = {
                        clear()
                        getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                    }, onDismiss = {
                        clear()
                        getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                    })
                loading = false
            },
            {
                loading = false
            })
    }

    private fun printCartonLabel(cartonNumber: String) {

        if (printer == "بدون لیبل") {
            popupState.showPopupWithAButton("کارتن با شماره $cartonNumber با تعداد $numberOfScanned ایجاد شد.",
                onDoneButtonClick = {
                    clear()
                    getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                },
                onDismiss = {
                    clear()
                    getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                })
            loading = false
            return
        }

        api.printCartonLabel(
            memory.erpData.printers[printer] ?: 0,
            cartonNumber,
            {
                popupState.showPopupWithAButton(
                    "کارتن با شماره $cartonNumber با تعداد $numberOfScanned ایجاد و دستور پرینت لیبل آن ارسال شد.",
                    onDoneButtonClick = {
                        clear()
                        getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                    }, onDismiss = {
                        clear()
                        getStockDraftRequestDetails(selectedStockDraftRequestDetails.number.toString())
                    })
                loading = false
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
                it.forEach { request ->
                    if (request.sumOfNotControlledItems > 0) {
                        stockDraftRequestsList[request.number] = request
                    }
                }
                stockDraftRequestsUiList.clear()
                stockDraftRequestsUiList.addAll(stockDraftRequestsList.values)
                sortStockDraftRequestsListsByCreateDate()
                loading = false
            },
            onError = {
                loading = false
            })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 280 || keyCode == 293) {
                scanTrigger()
            } else if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    fun scanTrigger() {

        if (step == 1) {
            if (scanTypeValue == "بارکد") {
                rf.stopScanning()
                barcode.startBarcodeScan()
            } else {
                if (!rf.scanning) {

                    rf.startBulkScan()
                } else {

                    rf.stopScanning()
                    numberOfScanned = rf.epcs.size + barcode.scannedBarcodes.size
                    if (numberOfScanned != 0) {
                        syncScannedItemsToServer()
                    }
                }
            }
        }
    }

    private fun calculateConflicts() {

        val conflicts = mutableListOf<StockDraftRequestItem>()

        inputProducts.filter {
            it.key !in scannedProducts.keys
        }.forEach {
            conflicts.add(it.value)
        }

        conflicts.addAll(scannedProducts.filter {
            it.key !in inputProducts.keys
        }.values)

        inputProducts.filter {
            it.key in scannedProducts.keys
        }.forEach {

            val product = it.value
            product.product.scannedEPCs = scannedProducts[it.key]!!.product.scannedEPCs
            product.product.scannedBarcodeNumber =
                scannedProducts[it.key]!!.product.scannedBarcodeNumber
            product.product.scannedBarcode = scannedProducts[it.key]!!.product.scannedBarcode
            conflicts.add(product)
        }

        productConflicts.clear()
        productConflicts.addAll(conflicts)
    }

    private fun filterResult() {

        shortagesNumber = 0
        productConflicts.filter {
            it.product.conflictType == "کسری"
        }.forEach {
            shortagesNumber += it.product.conflictNumber
        }

        additionalNumber = 0
        productConflicts.filter {
            it.product.conflictType == "اضافی"
        }.forEach {
            additionalNumber += it.product.conflictNumber
        }

        val uiListParameters =
            when {
                scanValues[scanFilter] == "اضافی" -> {
                    productConflicts.filter {
                        it.product.conflictType == "اضافی"
                    }.toList()
                }

                scanValues[scanFilter] == "کسری درخواستی" -> {
                    productConflicts.filter {
                        it.product.conflictType == "کسری"
                    }.toList()
                }

                scanValues[scanFilter] == "کسری جور شده" -> {
                    productConflicts.filter {
                        it.product.conflictType == "کسری" && it.controlShortageWithFoundNumber > 0
                    }.toList()
                }

                scanValues[scanFilter] == "بدون مغایرت" -> {
                    productConflicts.filter {
                        it.product.conflictType == "تایید شده"
                    }.toList()
                }

                scanValues[scanFilter] == "همه اجناس" -> {
                    productConflicts
                }

                else -> {
                    productConflicts
                }
            }
        itemsUiList.clear()
        itemsUiList.addAll(uiListParameters)

        itemsUiList.sortBy {
            it.product.name
        }
        itemsUiList.sortByDescending {
            barcode.scannedBarcodes.lastIndexOf(it.product.scannedBarcode)
        }
    }

    private fun syncInputItemsToServer() {

        loading = true

        val barcodeTableForV4 = mutableListOf<String>()
        var inputProductsBiggerThan1000 = false

        run breakForEach@{
            selectedStockDraftRequestDetails.itemsDistinctByKBarcode.keys.forEach {
                if (it !in inputBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 1000) {
                        barcodeTableForV4.add(it)
                    } else {
                        inputProductsBiggerThan1000 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (barcodeTableForV4.size == 0) {
            syncScannedItemsToServer()
            loading = false
            return
        }

        api.getItemDetails(
            mutableListOf(),
            barcodeTableForV4,
            { _, barcodes, _, invalidBarcodes ->

                barcodes.forEach { product ->
                    inputBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                if (inputProductsBiggerThan1000) {
                    syncInputItemsToServer()
                } else if (invalidBarcodes.length() != 0) {
                    loading = false
                    return@getItemDetails
                } else {
                    makeInputProductMap()
                    syncScannedItemsToServer()
                }
            },
            {
                loading = false
            })
    }

    private fun makeInputProductMap() {

        selectedStockDraftRequestDetails.itemsDistinctByKBarcode.forEach {
            it.value.product =
                inputBarcodeMapWithProperties[it.key]?.copy(draftNumber = it.value.notControlledNumberWithRequestNumber)
                    ?: Product()
        }

        inputProducts.clear()
        inputProducts.putAll(selectedStockDraftRequestDetails.itemsDistinctByKBarcode.filter { it.value.notControlledNumberWithRequestNumber > 0 })
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (numberOfScanned == 0) {
            calculateConflicts()
            filterResult()
            loading = false
            return
        }

        val epcArray = mutableListOf<String>()
        val barcodeArray = mutableListOf<String>()
        var scannedProductsBiggerThan1000 = false

        run breakForEach@{
            rf.epcs.forEach {
                if (it !in scannedEpcMapWithProperties.keys) {
                    if (epcArray.size < 1000) {
                        epcArray.add(it)
                    } else {
                        scannedProductsBiggerThan1000 = true
                        return@breakForEach
                    }
                }
            }
        }
        run breakForEach@{
            barcode.scannedBarcodes.forEach {

                if (it !in scannedBarcodeMapWithProperties.keys && it !in barcodeArray) {
                    if (barcodeArray.size < 1000) {
                        barcodeArray.add(it)
                    } else {
                        scannedProductsBiggerThan1000 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (epcArray.size == 0 && barcodeArray.size == 0) {

            processScannedProducts()
            calculateConflicts()
            filterResult()
            loading = false
            return
        }

        api.getItemDetails(
            epcArray,
            barcodeArray,
            { epcs, barcodes, invalidEpcs, invalidBarcodes ->

                epcs.forEach {
                    scannedEpcMapWithProperties[it.scannedEPCs[0]] = it
                }

                barcodes.forEach {
                    if (it.KBarCode in selectedStockDraftRequestDetails.itemsDistinctByKBarcode.keys) {
                        scannedBarcodeMapWithProperties[it.scannedBarcode] = it
                    } else {
                        barcode.scannedBarcodes.remove(it.scannedBarcode)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    barcode.scannedBarcodes.remove(invalidBarcodes[i])
                }
                for (i in 0 until invalidEpcs.length()) {
                    rf.epcs.remove(invalidEpcs[i])
                }

                if (scannedProductsBiggerThan1000) {
                    syncScannedItemsToServer()
                } else {
                    processScannedProducts()
                    calculateConflicts()
                    filterResult()
                    loading = false
                }
            },
            {
                calculateConflicts()
                filterResult()
                loading = false
            })
    }

    private fun processScannedProducts() {

        scannedProducts.clear()

        rf.epcs.forEach {

            if (scannedEpcMapWithProperties[it] == null) {
                showLog("مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید.", state)
                errorBeep(state)
                return
            } else {

                scannedEpcMapWithProperties[it]?.copy()?.let { product ->
                    if (product.KBarCode in scannedProducts.keys) {
                        scannedProducts[product.KBarCode]!!.product.scannedEPCs.add(it)
                    } else {
                        scannedProducts[product.KBarCode] = StockDraftRequestItem(
                            product = product.copy(
                                scannedEPCs = mutableListOf(it)
                            )
                        )

                        scannedProducts[product.KBarCode]!!.product.scannedEPCs = mutableListOf(it)
                    }
                }
            }
        }

        barcode.scannedBarcodes.distinct().forEach {

            if (scannedBarcodeMapWithProperties[it] == null) {
                showLog("مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید.", state)
                errorBeep(state)
                return
            } else {
                scannedBarcodeMapWithProperties[it]?.copy()?.let { product ->
                    product.scannedBarcodeNumber = barcode.scannedBarcodes.count { it1 ->
                        it == it1
                    }

                    if (product.KBarCode !in scannedProducts.keys) {
                        scannedProducts[product.KBarCode] =
                            StockDraftRequestItem(product = product.copy())
                    } else {
                        scannedProducts[product.KBarCode]!!.product.scannedBarcodeNumber += product.scannedBarcodeNumber
                    }
                }
            }
        }

        numberOfScanned = barcode.scannedBarcodes.size + rf.epcs.size
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        if (barcode in scannedBarcodeMapWithProperties.keys) {

            val kBarCode = scannedBarcodeMapWithProperties[barcode]?.KBarCode
            val input = if (kBarCode != null) inputProducts[kBarCode]?.product else null
            val scanned = if (kBarCode != null) scannedProducts[kBarCode]?.product else null

            if (input == null) {
                showLog("کالا در لیست موجود نیست.", state)
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                loading = false
                return
            } else if (scanned != null && input.draftNumber <= scanned.scannedNumber) {

                showLog("تعداد اسکن شده بیشتر از درخواستی است.", state)
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                loading = false
                return
            } else {
                successBeep(state)
                numberOfScanned = rf.epcs.size + this.barcode.scannedBarcodes.size
                saveToMemory()
                processScannedProducts()
                calculateConflicts()
                filterResult()
                loading = false
                return
            }
        }

        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size == 1) {

                    val input =
                        inputProducts[barcodes[0].KBarCode]?.product
                    val scanned = scannedProducts[barcodes[0].KBarCode]?.product

                    if (input == null || (scanned != null && input.draftNumber <= scanned.scannedNumber)) {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(barcode)
                        showLog("کالا در لیست موجود نیست.", state)
                    } else {
                        successBeep(state)
                        numberOfScanned = rf.epcs.size + this.barcode.scannedBarcodes.size
                        saveToMemory()
                        scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] =
                            barcodes[0]
                        processScannedProducts()
                        calculateConflicts()
                        filterResult()
                    }
                } else {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                }
                loading = false
            },
            {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                calculateConflicts()
                filterResult()
                loading = false
            })
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString("StockDraftRequestsEPCTable", JSONArray(rf.epcs).toString())
        edit.putString(
            "StockDraftRequestsBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putString(
            "StockDraftRequestsDraftProperties",
            Gson().toJson(selectedStockDraftRequestDetails)
        )
        edit.putString("StockDraftRequestsEPCTable", JSONArray(rf.epcs).toString())
        edit.putString("inputProductsForTest", Gson().toJson(inputProducts.values).toString())
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)


        rf.epcs = Gson().fromJson(
            memory.getString("StockDraftRequestsEPCTable", ""),
            rf.epcs.javaClass
        ) ?: mutableStateListOf()

        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("StockDraftRequestsBarcodeTable", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()

        if (barcode.scannedBarcodes.isNotEmpty() || rf.epcs.isNotEmpty()) {
            step = 1
            if (scanTypeValue == "بارکد") {
                barcode.enable()
            }
            selectedStockDraftRequestDetails = Gson().fromJson(
                memory.getString("StockDraftRequestsDraftProperties", ""),
                selectedStockDraftRequestDetails.javaClass
            )
        }

        numberOfScanned = rf.epcs.size + barcode.scannedBarcodes.size
    }

    private fun clearScannedItems() {
        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        numberOfScanned = 0
        scannedProducts.clear()
        scannedBarcodeMapWithProperties.clear()
        scannedEpcMapWithProperties.clear()
        calculateConflicts()
        filterResult()
        saveToMemory()
    }

    private fun clearOneScan(product: Product) {
        if (product.scannedBarcodeNumber > 0) {
            product.scannedBarcodeNumber -= 1
            // remove just one from scannedBarcodes
            barcode.scannedBarcodes.remove(product.scannedBarcode)
        }
        if (product.scannedEPCs.isNotEmpty()) {
            val epcToRemove = product.scannedEPCs.removeAt(product.scannedEPCs.lastIndex)
            rf.epcs.remove(epcToRemove)
            scannedEpcMapWithProperties.remove(epcToRemove)
        }

        // If now fully zero, remove from the maps
        if (product.scannedNumber == 0) {
            scannedBarcodeMapWithProperties.remove(product.scannedBarcode)
            scannedProducts.remove(product.KBarCode)
        }

        numberOfScanned = rf.epcs.size + barcode.scannedBarcodes.size
        processScannedProducts()
        calculateConflicts()
        filterResult()
        saveToMemory()
    }


    private fun clear() {

        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        numberOfScanned = 0
        scannedProducts.clear()
        scannedBarcodeMapWithProperties.clear()
        scannedEpcMapWithProperties.clear()
        inputProducts.clear()
        inputBarcodeMapWithProperties.clear()
        calculateConflicts()
        filterResult()
        saveToMemory()
    }

    fun clear(product: Product) {

        rf.epcs = rf.epcs.filter {
            it !in product.scannedEPCs
        }.toMutableStateList()

        product.scannedEPCs.forEach {
            scannedEpcMapWithProperties.remove(it)
        }

        barcode.scannedBarcodes.removeAll(listOf(product.scannedBarcode))

        repeat(product.scannedBarcodeNumber - 1) {
            barcode.scannedBarcodes.add(product.scannedBarcode)
        }

        if (product.scannedBarcodeNumber == 1) {
            scannedBarcodeMapWithProperties.remove(product.scannedBarcode)
            scannedProducts.remove(product.KBarCode)
        }

        numberOfScanned = rf.epcs.size + barcode.scannedBarcodes.size
        processScannedProducts()
        calculateConflicts()
        filterResult()
        saveToMemory()
    }

    private fun back() {

        when (step) {
            2 -> {
                step = 1
                if (scanTypeValue == "بارکد") {
                    barcode.enable()
                }
            }

            1 -> {
                if (rf.epcs.isNotEmpty() || barcode.scannedBarcodes.isNotEmpty()) {
                    saveToMemory()
                    rf.stopScanning()
                    finish()
                } else {
                    clear()
                    step = 0
                }
            }

            else -> {
                finish()
            }
        }
    }

    private fun openSearchActivity(product: Product) {

        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
    }

    private fun getStockDraftRequestDetails(code: String) {

        loading = true

        if (code.toLongOrNull() == null) {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "شماره درخواست حواله وارد شده نامعتبر است.",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
            loading = false
            return
        }

        api.getStockDraftRequestDetails(
            code,
            { stockDraftRequestDetails ->
                this.selectedStockDraftRequestDetails = stockDraftRequestDetails
                step = 1
                if (scanTypeValue == "بارکد") {
                    barcode.enable()
                }
                saveToMemory()
                syncInputItemsToServer()
            },
            {
                loading = false
            })
    }

    private fun sortStockDraftRequestsListsByCreateDate() {

        for (elements in stockDraftRequestsUiList) {
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
        stockDraftRequestsUiList = stockDraftRequestsUiList.sortedByDescending {
            LocalDate.parse(it.date, dateTimeFormatter)
        }.toMutableStateList()
    }

    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalCoilApi::class)
    @ExperimentalFoundationApi
    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            if (step == 1) Content() else if (step == 0) Content2() else Content3()
                        }
                    },
                    snackbarHost = { ErrorSnackBar(state) },
                    bottomBar = { BottomBar() },
                )
            }
        }
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            navigationIcon = {
                IconButton(onClick = { back() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = ""
                    )
                }
            },

            actions = {
                if (step == 1) {
                    IconButton(
                        modifier = Modifier.testTag("CheckInTestTag"),
                        onClick = {
                            if (!loading && !rf.scanning) {
                                openClearDialog = true
                            }
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                            contentDescription = ""
                        )
                    }
                }
            },

            title = {
                Text(
                    text = stringResource(id = R.string.stockDraftRequestConfirm),
                    modifier = if (step == 1) {
                        Modifier
                            .padding(end = 10.dp)
                            .fillMaxSize()
                            .wrapContentSize()
                    } else {
                        Modifier
                            .padding(end = 50.dp)
                            .fillMaxSize()
                            .wrapContentSize()
                    },
                    textAlign = TextAlign.Right,
                )
            }
        )
    }

    @Composable
    fun BottomBar() {

        if (step == 1) {

            BottomAppBar(
                modifier = Modifier.wrapContentHeight()
            ) {

                Column {

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        ScanTypeDropDownList(
                            Modifier.align(Alignment.CenterVertically), scanTypeValue
                        ) {
                            scanTypeValue = it
                            if (scanTypeValue == "بارکد" && step == 1) {
                                barcode.enable()
                            } else if (scanTypeValue == "RFID" && step == 1) {
                                barcode.disable()
                            }
                        }
                        ScanFilterDropDownList(modifier = Modifier.align(Alignment.CenterVertically))
                        Button(onClick = {
                            if (numberOfScanned > 0) {
                                openFinishDialog = true
                            } else {
                                clear()
                                step = 0
                                barcode.disable()
                                saveToMemory()
                            }
                        }) {
                            Text(text = "پایان اسکن")
                        }
                    }
                }
            }
        } else if (step == 2) {

            if (scannedProducts.values.toMutableList().isNotEmpty() && !loading) {
                BottomBarButton(
                    text = if (createTypeValue == "ایجاد حواله") "ثبت حواله" else "ثبت کارتن"
                ) {
                    if (!loading) {
                        openPrintDialog = true
                    }
                }
            }
        }
    }

    @ExperimentalCoilApi
    @ExperimentalFoundationApi
    @Composable
    fun Content() {
        Column {
            if (openClearDialog) {
                AlertDialogWith2Button(
                    title = "کالاهای اسکن شده پاک شوند؟",
                    btnConfirm = "بله",
                    btnNotConfirm = "خیر",
                    btnNotConfirmOnClick = { openClearDialog = false },
                    btnConfirmOnClick = {
                        openClearDialog = false
                        barcode.scannedBarcodes.clear()
                        rf.epcs.clear()
                        numberOfScanned = 0
                        scannedProducts.clear()
                        scannedBarcodeMapWithProperties.clear()
                        scannedEpcMapWithProperties.clear()
                        calculateConflicts()
                        filterResult()
                        saveToMemory()
                    },
                    onDismiss = { openClearDialog = false }
                )
            }

            if (openFinishDialog) {
                AlertDialogWith2Button(
                    title = "کالاهای اسکن شده ثبت شوند؟",
                    btnConfirm = "بله",
                    btnNotConfirm = "خیر، نتایج پاک شوند",
                    btnNotConfirmOnClick = {
                        openFinishDialog = false
                        clear()
                        step = 0
                        if (barcode.isEnabled) {
                            barcode.disable()
                        }
                        saveToMemory()
                        when (step) {
                            1 -> {
                                syncInputItemsToServer()
                            }

                            else -> {
                                step = 0
                                getStockDraftsRequestsList()
                            }
                        }
                    },
                    btnConfirmOnClick = {
                        openFinishDialog = false
                        if (additionalNumber == 0) {
                            step = 2
                            if (barcode.isEnabled) {
                                barcode.disable()
                            }
                        } else {
                            showLog("ابتدا اضافی ها را برطرف کنید.", state)
                        }
                    }, onDismiss = { openFinishDialog = false })
            }

            if (rf.scanning || loading) {
                LoadingCircularProgressIndicator(rf.scanning, loading)
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .border(
                            BorderStroke(1.dp, primaryLight),
                            shape = MaterialTheme.shapes.small
                        )
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.small
                        )
                        .fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        Text(
                            text = "اسکن: $numberOfScanned",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                                .weight(1F),
                        )
                        Text(
                            text = "کسری: $shortagesNumber",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )
                        Text(
                            text = "اضافی: $additionalNumber",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )
                    }
                }

                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                    val uiItems = itemsUiList.toList()

                    items(uiItems.size) { i ->

                        ItemWithClearButton(
                            product = uiItems[i],
                            onClearButtonClick = {  clearOneScan(uiItems[i].product) },
                            showFoundNumber = scanValues[scanFilter] == "کسری جور شده",
                            onItemClick = { openSearchActivity(uiItems[i].product) },
                            isFirstItem = i == 0,
                            isLastItem = i == uiItems.size - 1
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Content2() {

        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = loading)
        } else {

            NotificationPopUp(state = popupState)
            Column(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .padding(bottom = 0.dp)
                        .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        )
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SimpleTextField(
                            modifier = Modifier
                                .padding(
                                    top = 12.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 12.dp
                                )
                                .weight(1F)
                                .fillMaxWidth(),
                            hint = "جستجو مقصد یا کد درخواست",
                            onValueChange = { stockDraftRequestSearchKey = it },
                            value = stockDraftRequestSearchKey
                        )
                    }

                    Row {
                        FilterDropDownListWithSearch(
                            modifier = Modifier
                                .padding(bottom = 12.dp, start = 16.dp),
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_location_city_24),
                                    contentDescription = "",
                                    tint = primaryLight,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 6.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = stockDraftRequestLocation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 6.dp)
                                )
                            },
                            onClick = {
                                stockDraftRequestLocation = it
                            },
                            values = stockDraftRequestLocationsList
                        )
                    }
                }
                val filteredUiList = if (stockDraftRequestLocation == "انتخاب مقصد") {
                    stockDraftRequestsUiList
                } else {
                    stockDraftRequestsUiList.filter {
                        it.destination.startsWith(stockDraftRequestLocation)
                    }
                }

                val filteredUiList2 = if (stockDraftRequestSearchKey != "") {
                    if (stockDraftRequestSearchKey.toLongOrNull() == null) {
                        filteredUiList.filter {
                            it.destination.contains(stockDraftRequestSearchKey)
                        }
                    } else {
                        filteredUiList.filter {
                            it.number.toString().contains(stockDraftRequestSearchKey)
                        }
                    }
                } else {
                    filteredUiList
                }

                if (filteredUiList2.isEmpty()) {
                    EmptyBox(text = "هیچ درخواستی برای انبار مبدا و فیلتر انتخاب شده وجود ندارد.")
                } else {
                    LazyColumn(state = listState) {


                        items(filteredUiList2.size) { i ->
                            SurfaceWith2Columns5RowsTwoButton(
                                enableBottomSpace = i == filteredUiList2.size - 1,
                                text1 = "درخواست: " + filteredUiList2[i].number,
                                text2 = "درخواستی: " + filteredUiList2[i].sumOfRequestedItems,
                                text3 = "مقصد: " + filteredUiList2[i].destination,
                                text4 = "جور شده: " + filteredUiList2[i].sumOfFoundItems,
                                text5 = "شرح: " + filteredUiList2[i].specification,
                                text6 = "کنترل شده: " + filteredUiList2[i].sumOfControlledItems,
                                text7 = "جمع کننده: " + filteredUiList2[i].collectorName,
                                onButton1Click = {
                                    stockDraftRequestNumber =
                                        filteredUiList2[i].number.toString()
                                    getStockDraftRequestDetails(stockDraftRequestNumber)
                                },
                                onButton2Click = {
                                    finalizeRequest(filteredUiList2[i].number)
                                },
                                button1Title = "کنترل",
                                button2Title = "بستن درخواست",
                                button1Icon = Icons.Filled.Build,
                                button2Icon = Icons.Filled.Close
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Content3() {
        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = loading)
        } else {


            Column {

                NotificationPopUp(popupState)
                if (openPrintDialog) {
                    AlertDialogWith2ButtonDropDownList(
                        title = "لطفا پرینتر و نوع بسته بندی را مشخص کنید",
                        btnTxt = "تایید",
                        btnOnClick = {
                            openPrintDialog = false
                            if (createTypeValue == "ایجاد حواله") {
                                createNewStockDraft()
                            } else {
                                createNewCarton()
                            }
                        },
                        dropDownText = printer,
                        onDismiss = {
                            openPrintDialog = false
                        },
                        dropDownRes = memory.erpData.printers.keys.toList(),
                        onSelectItem = { printer = it },
                        dropDown2Text = packageType,
                        dropDown2Res = memory.erpData.packageTypes.keys.toList(),
                        onSelectItem2 = { packageType = it },

                        )
                }

                Row {

                    FilterDropDownList(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 24.dp),
                        icon = {},
                        text = {
                            Text(
                                text = createTypeValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            createTypeValue = it
                        },
                        values = mutableListOf("ایجاد کارتن", "ایجاد حواله")
                    )

                    OutlinedTextField(
                        value = requestDescription,
                        singleLine = true,
                        label = {
                            Text(
                                text = if (createTypeValue == "ایجاد کارتن") "شرح کارتن" else "شرح حواله"
                            )
                        },
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp
                            )
                            .fillMaxWidth(),
                        onValueChange = {
                            requestDescription = it
                        })
                }

                Column {

                    if (scannedProducts.values.toMutableList().isEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 56.dp)
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .width(256.dp)
                            ) {
                                Box(

                                    modifier = Modifier
                                        .background(color = Color.White, shape = Shapes.medium)
                                        .size(256.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_empty_box),
                                        contentDescription = "",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                Text(
                                    "هنوز کالایی برای ثبت حواله اسکن نکرده اید",
                                    style = Typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(
                                        top = 16.dp,
                                        start = 4.dp,
                                        end = 4.dp
                                    ),
                                )
                            }
                        }
                    } else {

                        Text(
                            text = "مجموع: $numberOfScanned",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp, top = 16.dp),
                        )

                        LazyColumn(modifier = Modifier.padding(top = 8.dp, bottom = 64.dp)) {

                            val uiItems = scannedProducts.values.toList()

                            items(uiItems.size) { i ->
                                Item(
                                    uiItems[i].product,
                                    text3 = "اسکن: " + uiItems[i].product.scannedNumber,
                                    text4 = "سایز: " + uiItems[i].product.size,
                                    isFirstItem = i == 0,
                                    isLastItem = i == uiItems.size - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ScanFilterDropDownList(modifier: Modifier) {

        var expanded by rememberSaveable {
            mutableStateOf(false)
        }

        Box(modifier = modifier) {
            Row(
                modifier = Modifier
                    .testTag("checkInFilterDropDownList")
                    .clickable { expanded = true }) {
                Text(text = scanValues[scanFilter])
                Icon(imageVector = Icons.Filled.ArrowDropDown, "")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentWidth()
            ) {

                scanValues.forEach {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        scanFilter = scanValues.indexOf(it)
                        filterResult()
                    }, text = { Text(text = it) })
                }
            }
        }
    }
}


@Composable
fun ItemWithClearButton(
    product: StockDraftRequestItem,
    onClearButtonClick: (product: Product) -> Unit,
    onItemClick: (product: Product) -> Unit,
    isLastItem: Boolean,
    isFirstItem: Boolean,
    showFoundNumber: Boolean,
) {

    val topPaddingClearButton = if (isFirstItem) 8.dp else 4.dp

    Box {

        Item(
            product.product, true,
            text3 = "موجودی: " + if (showFoundNumber) product.notControlledNumberWithFoundNumber else product.product.draftNumber,
            text4 = "اسکن: " + product.product.scannedNumber,
            onClick = { onItemClick(product.product) },
            isLastItem = isLastItem,
            isFirstItem = isFirstItem
        )

        if (product.product.scannedNumber > 0) {

            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp),
                        color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(TopEnd)
                    .testTag("clear")
                    .clickable {
                        onClearButtonClick(product.product)
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "",
                    tint = errorLight,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(
    product: Product,
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    isLastItem: Boolean,
    isFirstItem: Boolean,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (isFirstItem) 16.dp else 12.dp
    val bottomPadding = if (isLastItem) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = if (product.scannedNumber > product.wareHouseNumber && enableWarehouseNumberCheck) {
                    errorLight
                } else if (colorFull) {
                    secondaryLight
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .shadow(0.dp, shape = Shapes.large)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = Shapes.large
                    )
                    .border(
                        BorderStroke(2.dp, color = primaryContainerLight),
                        shape = Shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (product.requestedNumber > 0 || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else product.requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") product.KBarCode else text1,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") product.name else text2,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = primaryContainerLight,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SurfaceWith2Columns5RowsTwoButton(
    enableBottomSpace: Boolean = false,
    text1: String,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    text7: String = "",
    button1Title: String,
    button2Title: String,
    onButton1Click: () -> Unit,
    onButton2Click: () -> Unit,
    button1Icon: ImageVector,
    button2Icon: ImageVector,
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                1.dp,
                color = secondaryLight,
                shape = MaterialTheme.shapes.small
            )
            .background(
                MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(150.dp)
            .testTag("items")
    ) {

        Row(
            modifier = Modifier
                .height(76.dp)
                .fillMaxWidth(),
        ) {

            Row(
                modifier = Modifier
                    .wrapContentHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.5F)
                        .fillMaxHeight()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text1,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text3,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text5,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text2,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                    )

                    Text(
                        text = text4,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .background(
                                color = Color.White,
                                shape = RectangleShape
                            ),
                    )

                    Text(
                        text = text6,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )
                }
            }
        }
        Row {
            Text(
                text = text7,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }

        Row {
            OutlinedButton(
                onClick = onButton1Click,
                modifier = Modifier
                    //.align(Alignment.CenterHorizontally)
                    .padding(start = 16.dp, bottom = 12.dp)
            ) {
                Icon(
                    imageVector = button1Icon,
                    contentDescription = ""
                )
                Text(
                    text = button1Title,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            OutlinedButton(
                onClick = onButton2Click,
                modifier = Modifier
                    //.align(Alignment.CenterHorizontally)
                    .padding(start = 16.dp, bottom = 12.dp)
            ) {
                Icon(
                    imageVector = button2Icon,
                    contentDescription = ""
                )
                Text(
                    text = button2Title,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
/*
@Preview
@Composable
fun Preview2() {

    val stockDraftNumber = "114300067587"
    val cartonNumber = "114300067587"
    val numberOfScanned = 36

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = { paddingValues ->
                    AlertDialogWitheadlineMediumButton(
                        onDismiss = { },
                        title = "حواله با شماره $stockDraftNumber با تعداد ${numberOfScanned} ایجاد شد.",
                        btnTxt = "باشه",
                        btnOnClick = { }
                    )
                })
        }
    }
}*/