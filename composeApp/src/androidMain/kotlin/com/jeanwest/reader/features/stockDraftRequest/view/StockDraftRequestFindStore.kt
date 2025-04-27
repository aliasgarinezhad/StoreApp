package com.jeanwest.reader.features.stockDraftRequest.view

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.AppBarWithDeleteButton
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.ScanOrTypeNumberPage
import com.jeanwest.reader.view.primaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraftRequest
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject


@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftRequestFindStore : ComponentActivity() {

    lateinit var barcode: Barcode
    var inputProducts = mutableMapOf<String, Product>()
    private var scannedProducts = mutableMapOf<String, Product>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var scannedBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var stockDraftRequestProperties = StockDraftRequest()

    //ui parameters
    var productConflicts = mutableStateListOf<Product>()
    var loading by mutableStateOf(false)
    var shortagesNumber by mutableIntStateOf(0)
    var additionalNumber by mutableIntStateOf(0)
    private var shortageCodesNumber by mutableIntStateOf(0)
    private var additionalCodesNumber by mutableIntStateOf(0)
    var uiList = mutableStateListOf<Product>()
    private val scanValues = mutableListOf("اضافی", "کسری")
    private var scanFilter by mutableIntStateOf(1)
    private var openClearDialog by mutableStateOf(false)

    @Inject
    lateinit var state: SnackbarHostState
    private var stockDraftRequestNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)
    private var openFinishDialog by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase
    private var popupState = NotificationPopupHost()
    private var isInDepo = true
    var finalStockdraftId by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()

        isInDepo = if (memory.user.isStoreUser) {
            memory.user.warehouses[memory.user.warehouseCode.toString()]?.contains("دپو")
                ?: false
        } else {
            true
        }

        if (scanningMode) {
            syncInputItemsToServer()
        }
        exceptionHandler()
        setContent {
            Page()
        }
    }

    private fun init() {


        barcode = Barcode(this) {
            if (scanningMode) {
                syncScannedItemToServer(it)
            } else {
                barcode.scannedBarcodes.clear()
                getStockDraftRequestDetails(it)
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
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
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

        if (scanningMode) {
            barcode.startBarcodeScan()
        }
    }

    private fun calculateConflicts() {

        val conflicts = mutableListOf<Product>()

        conflicts.addAll(inputProducts.filter {
            it.key !in scannedProducts.keys
        }.values)

        conflicts.addAll(scannedProducts.filter {
            it.key !in inputProducts.keys
        }.values)

        inputProducts.filter {
            it.key in scannedProducts.keys
        }.forEach {

            val product: Product = it.value.copy()
            product.scannedEPCs = scannedProducts[it.key]!!.scannedEPCs
            product.scannedBarcodeNumber = scannedProducts[it.key]!!.scannedBarcodeNumber
            product.scannedBarcode = scannedProducts[it.key]!!.scannedBarcode
            val sourceNumber = if (isInDepo) product.wareHouseNumber else product.storeNumber
            product.draftNumber = sourceNumber
            conflicts.add(product)
        }

        productConflicts.clear()
        productConflicts.addAll(conflicts)
    }

    private fun filterResult(conflictResult: MutableList<Product>) {

        shortagesNumber = 0
        conflictResult.filter {
            it.conflictType == "کسری"
        }.forEach {
            shortagesNumber += it.conflictNumber
        }

        additionalNumber = 0
        conflictResult.filter {
            it.conflictType == "اضافی"
        }.forEach {
            additionalNumber += it.conflictNumber
        }

        shortageCodesNumber = conflictResult.filter { it.conflictType == "کسری" }.size
        additionalCodesNumber =
            conflictResult.filter { it.conflictType == "اضافی" }.size

        val uiListParameters =
            when {
                scanValues[scanFilter] == "اضافی" -> {
                    conflictResult.filter {
                        it.conflictType == "اضافی"
                    } as ArrayList<Product>
                }

                scanValues[scanFilter] == "کسری" -> {
                    conflictResult.filter {
                        it.conflictType == "کسری"
                    } as ArrayList<Product>
                }

                else -> {
                    conflictResult
                }
            }
        uiList.clear()
        uiList.addAll(uiListParameters)

        uiList.sortBy {
            it.name
        }
        uiList.sortByDescending {
            it.scannedNumber
        }
    }

    private fun getStockDraftRequestDetails(code: String) {

        loading = true

        if (code.toLongOrNull() == null) {
            showLog("شماره درخواست حواله وارد شده نامعتبر است.", state)
            loading = false
            return
        }

        api.getStockDraftRequestDetails(code, { stockDraftRequestDetails ->
            if (stockDraftRequestDetails.stateId == 13) {
                showLog("شماره درخواست حواله وارد شده نامعتبر است.", state)
                loading = false
            } else if (stockDraftRequestDetails.source != memory.user.warehouseCode.toString()) {
                showLog(
                    "مبدا درخواست حواله انبار جاری انتخابی شما نیست. لطفا انبار جاری را به درستی انتخاب کنید.",
                    state
                )
                loading = false
            } else {
                clear()
                stockDraftRequestProperties = stockDraftRequestDetails
                scanningMode = true
                saveToMemory()
                syncInputItemsToServer()
            }
        }, {
            loading = false
        })
    }

    private fun syncInputItemsToServer() {

        loading = true

        val barcodeTableForV4 = mutableListOf<String>()
        var inputProductsBiggerThan1000 = false

        run breakForEach@{
            stockDraftRequestProperties.itemsDistinctByKBarcode.keys.forEach {
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
            makeInputProductMap()
            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false
            return
        }

        if (memory.user.isStoreUser) {

            api.getItemDetailsAndInventory(
                mutableListOf(),
                barcodeTableForV4,
                { _, barcodes, _, _ ->

                    barcodes.forEach { product ->
                        inputBarcodeMapWithProperties[product.scannedBarcode] = product
                    }

                    if (inputProductsBiggerThan1000) {
                        syncInputItemsToServer()
                    } else {
                        makeInputProductMap()
                        makeScannedProductMap()
                        calculateConflicts()
                        filterResult(productConflicts)
                        loading = false
                    }

                },
                {
                    loading = false
                }, true
            )
        } else {
            api.getProductsV5(
                warehouses = listOf(memory.user.warehouseCode.toString()),
                epcs = mutableListOf(),
                barcodes = barcodeTableForV4,
                { _, barcodes, _, _ ->

                    barcodes.forEach { product ->
                        inputBarcodeMapWithProperties[product.scannedBarcode] = product
                    }

                    if (inputProductsBiggerThan1000) {
                        syncInputItemsToServer()
                    } else {
                        makeInputProductMap()
                        makeScannedProductMap()
                        calculateConflicts()
                        filterResult(productConflicts)
                        loading = false
                    }
                },
                {
                    loading = false
                },
            )
        }
    }

    private fun makeInputProductMap() {

        inputProducts.clear()
        stockDraftRequestProperties.itemsDistinctByKBarcode.keys.forEach {

            val product = inputBarcodeMapWithProperties[it]!!.copy()

            val sourceNumber = if (isInDepo) product.wareHouseNumber else product.storeNumber
            if (sourceNumber > 0) {
                product.draftNumber = sourceNumber

                if (product.KBarCode !in inputProducts.keys) {
                    inputProducts[product.KBarCode] = product.copy()
                } else {
                    inputProducts[product.KBarCode]!!.draftNumber += product.draftNumber
                }
            }
        }
    }

    private fun makeScannedProductMap() {

        scannedProducts.clear()

        val shouldBeCleanedFromScannedBarcodes = mutableListOf<String>()

        barcode.scannedBarcodes.distinct().forEach {

            if (scannedBarcodeMapWithProperties[it] == null) {
                showLog("مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید", state)
                return
            }

            scannedBarcodeMapWithProperties[it]?.copy()?.let { product ->
                product.scannedBarcodeNumber = barcode.scannedBarcodes.count { it1 ->
                    it == it1
                }

                if (product.KBarCode !in scannedProducts.keys) {
                    scannedProducts[product.KBarCode] = product.copy()
                } else {
                    scannedProducts[product.KBarCode]!!.scannedBarcodeNumber += product.scannedBarcodeNumber
                }
            }
        }

        shouldBeCleanedFromScannedBarcodes.forEach {
            scannedBarcodeMapWithProperties.remove(it)
            barcode.scannedBarcodes.remove(it)
        }
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        if (barcode in scannedBarcodeMapWithProperties.keys) {

            val product = scannedProducts[scannedBarcodeMapWithProperties[barcode]!!.KBarCode]!!
            val sourceNumber = if (isInDepo) product.wareHouseNumber else product.storeNumber

            if (product.scannedNumber < sourceNumber) {

                successBeep(state)
                saveToMemory()
                makeScannedProductMap()
                calculateConflicts()
                filterResult(productConflicts)

            } else {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                showLog("تعداد اسکن شده از موجودی انبار بیشتر است.", state)
            }
            loading = false
            return
        }

        if (memory.user.isStoreUser) {

            api.getItemDetailsAndInventory(
                mutableListOf(),
                mutableListOf(barcode),
                { _, barcodes, _, invalidBarcodes ->

                    if (barcodes.size == 1 && invalidBarcodes.length() == 0) {

                        if (barcodes[0].KBarCode in inputProducts) {
                            successBeep(state)
                            saveToMemory()
                            scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] =
                                barcodes[0]
                            makeScannedProductMap()
                            calculateConflicts()
                            filterResult(productConflicts)

                        } else {
                            errorBeep(state)
                            this.barcode.scannedBarcodes.remove(barcodes[0].scannedBarcode)
                            showLog("کالا در لیست نشان داده شده موجود نیست.", state)
                        }
                    } else if (invalidBarcodes.length() == 1) {
                        this.barcode.scannedBarcodes.remove(barcode)
                        errorBeep(state)
                    }
                    loading = false
                },
                {
                    this.barcode.scannedBarcodes.remove(barcode)
                    errorBeep(state)
                    calculateConflicts()
                    filterResult(productConflicts)
                    loading = false
                }, true
            )
        } else {
            api.getProductsV5(
                warehouses = listOf(memory.user.warehouseCode.toString()),
                epcs = mutableListOf(),
                barcodes = mutableListOf(barcode),
                { _, barcodes, _, invalidBarcodes ->

                    if (barcodes.size == 1 && invalidBarcodes.length() == 0) {

                        if (barcodes[0].KBarCode in inputProducts) {
                            successBeep(state)
                            saveToMemory()
                            scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] =
                                barcodes[0]
                            makeScannedProductMap()
                            calculateConflicts()
                            filterResult(productConflicts)

                        } else {
                            errorBeep(state)
                            this.barcode.scannedBarcodes.remove(barcodes[0].scannedBarcode)
                            showLog("کالا در لیست نشان داده شده موجود نیست.", state)
                        }
                    } else if (invalidBarcodes.length() == 1) {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(invalidBarcodes[0])
                    }
                    loading = false
                },
                {
                    errorBeep(state)
                    calculateConflicts()
                    filterResult(productConflicts)
                    loading = false
                }
            )
        }
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "StockDraftRequestFindStoreBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putBoolean("StockDraftRequestFindStoreScanningMode", scanningMode)
        edit.putString(
            "StockDraftRequestFindStoreDraftProperties",
            Gson().toJson(stockDraftRequestProperties)
        )

        edit.putString(
            "StockDraftRequestFindStoreInputProducts",
            Gson().toJson(inputProducts)
        )

        edit.putString(
            "StockDraftRequestFindStoreScannedProducts",
            Gson().toJson(scannedProducts)
        )

        edit.putString(
            "StockDraftRequestFindStoreInputBarcodeDetailsMap",
            Gson().toJson(inputBarcodeMapWithProperties)
        )

        edit.putString(
            "StockDraftRequestFindStoreScannedBarcodeDetailsMap",
            Gson().toJson(scannedBarcodeMapWithProperties)
        )

        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        val type = object : TypeToken<MutableMap<String, Product>>() {}.type

        scanningMode = memory.getBoolean("StockDraftRequestFindStoreScanningMode", false)

        if (scanningMode) {
            stockDraftRequestProperties = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreDraftProperties", ""),
                stockDraftRequestProperties.javaClass
            )
            barcode.scannedBarcodes = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreBarcodeTable", ""),
                barcode.scannedBarcodes.javaClass
            ) ?: mutableStateListOf()

            inputProducts = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreInputProducts", ""),
                type
            ) ?: mutableMapOf()

            scannedProducts = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreScannedProducts", ""),
                type
            ) ?: mutableMapOf()

            inputBarcodeMapWithProperties = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreInputBarcodeDetailsMap", ""),
                type
            ) ?: mutableMapOf()

            scannedBarcodeMapWithProperties = Gson().fromJson(
                memory.getString("StockDraftRequestFindStoreScannedBarcodeDetailsMap", ""),
                type
            ) ?: mutableMapOf()

            calculateConflicts()
            filterResult(productConflicts)
        }
    }

    private fun clear() {
        barcode.scannedBarcodes.clear()
        scannedProducts.clear()
        scannedBarcodeMapWithProperties.clear()
        calculateConflicts()
        filterResult(productConflicts)
        scanFilter = 1
        saveToMemory()
    }

    private fun back() {
        saveToMemory()
        finish()
    }

    private fun openSearchActivity(product: Product) {
        val searchResultProduct = Product(
            name = product.name,
            KBarCode = product.KBarCode,
            imageUrl = product.imageUrl,
            color = product.color,
            size = product.size,
            productCode = product.productCode,
            rfidKey = product.rfidKey,
            primaryKey = product.primaryKey,
            originalPrice = product.originalPrice,
            salePrice = product.salePrice,
            storeNumber = product.storeNumber,
            wareHouseNumber = product.wareHouseNumber,
        )

        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(searchResultProduct).toString())
        startActivity(intent)
    }

    private fun createStockDraft() {

        loading = true

        uiList.forEach { product ->

            val sourceNumber = if (isInDepo) product.wareHouseNumber else product.storeNumber

            if (product.scannedNumber > sourceNumber) {
                popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                loading = false
                return
            }
        }

        if (memory.user.isStoreUser) {

            localStoreDatabase.createStockDraft(
                memory.user.username,
                stockDraftRequestProperties.source.toIntOrNull() ?: 0,
                stockDraftRequestProperties.destination.toIntOrNull() ?: 0,
                scannedProducts.values.toMutableList(),
                "درخواست شماره $stockDraftRequestNumber جمع آوری با RFID ",
                { stockDraftID ->
                    finalStockdraftId = stockDraftID
                    clear()
                    scanningMode = false
                    makeScannedProductMap()
                    calculateConflicts()
                    filterResult(productConflicts)
                    saveToMemory()
                    popupState.showPopupWithAButton("حواله با شماره $stockDraftID ایجاد شد.")
                    loading = false
                },
                {
                    loading = false
                    showLog("مشکلی در ارتباط با سرور به وجود آمده است.", state = state)
                }
            )
        } else {
            api.createStockDraft(
                source = stockDraftRequestProperties.source.toIntOrNull() ?: 0,
                destination = stockDraftRequestProperties.destination.toIntOrNull() ?: 0,
                products = scannedProducts.values.toMutableList(),
                desc = "درخواست شماره $stockDraftRequestNumber جمع آوری با RFID ",
                onSuccess = { stockDraftID ->
                    clear()
                    scanningMode = false
                    makeScannedProductMap()
                    calculateConflicts()
                    filterResult(productConflicts)
                    saveToMemory()
                    popupState.showPopupWithAButton("حواله با شماره $stockDraftID ایجاد شد.")
                    loading = false
                },
                onError = {
                    loading = false
                    showLog("مشکلی در ارتباط با سرور به وجود آمده است.", state = state)
                }
            )
        }
    }

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
                            if (scanningMode) Content() else Content2()
                        }
                    },
                    snackbarHost = { ErrorSnackBar(state) },
                    bottomBar = { if (scanningMode) BottomBar() },
                )
            }
        }
    }

    @Composable
    fun AppBar() {
        if (scanningMode && barcode.scannedBarcodes.isNotEmpty()) {
            AppBarWithDeleteButton(
                title = stringResource(id = R.string.StockDraftRequestFindStore),
                onBackPressed = { back() },
                onDeletePressed = {
                    if (!this@StockDraftRequestFindStore.loading && !this@StockDraftRequestFindStore.loading) {
                        openClearDialog = true
                    }
                })
        } else {
            AppBarWithBack(
                title = stringResource(id = R.string.StockDraftRequestFindStore),
                onBackPressed = { back() })
        }
    }

    @Composable
    fun BottomBar() {


        if (!loading) {
            if (scanningMode) {
                BottomBarButton(text = "صدور حواله") {
                    openFinishDialog = true
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
                    btnConfirmOnClick = {
                        openClearDialog = false
                        clear()
                    },
                    btnNotConfirmOnClick = { openClearDialog = false },
                    onDismiss = { openClearDialog = false })
            }

            if (openFinishDialog) {
                AlertDialogWith2Button(
                    title = "کالاهای اسکن شده حواله شوند؟",
                    btnConfirm = "بله",
                    btnNotConfirm = "خیر، کالاها پاک شوند",
                    btnConfirmOnClick = {
                        openFinishDialog = false
                        createStockDraft()
                    },
                    btnNotConfirmOnClick = {
                        openFinishDialog = false
                        clear()
                        scanningMode = false
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                        saveToMemory()
                    },
                    onDismiss = { openFinishDialog = false }
                )
            }

            if (loading || localStoreDatabase.loading) {
                LoadingCircularProgressIndicator(isDataLoading = loading || localStoreDatabase.loading)
            } else {
                NotificationPopUp(popupState)
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
                            text = "اسکن: ${barcode.scannedBarcodes.size}",
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
                    }
                }

                LazyColumn {

                    items(uiList.size) { i ->
                        Item(
                            i,
                            uiList,
                            true,
                            text3 = "موجودی: " + uiList[i].draftNumber,
                            text4 = uiList[i].conflictType + ":" + " " + uiList[i].conflictNumber
                        ) {
                            openSearchActivity(uiList[i])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Content2() {
        ScanOrTypeNumberPage(
            loading = loading || localStoreDatabase.loading,
            onClick = { getStockDraftRequestDetails(stockDraftRequestNumber) },
            value = stockDraftRequestNumber,
            onValueChange = { stockDraftRequestNumber = it },
            item = "شماره درخواست حواله",
            popupHost = popupState,
        )
    }
}