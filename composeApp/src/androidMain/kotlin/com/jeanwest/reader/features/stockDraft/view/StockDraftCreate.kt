package com.jeanwest.reader.features.stockDraft.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButtonDropDownList
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftCreate : ComponentActivity() {

    private var stockDraftNumber = ""
    lateinit var barcode: Barcode
    lateinit var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    private var scanningMode by mutableStateOf(true)
    private var destination by mutableStateOf("انتخاب مقصد")
    var products = mutableListOf<Product>()
    private var stockDraftSpec by mutableStateOf("")
    private var scanTypeValue by mutableStateOf("بارکد")
    private var printers = mutableStateMapOf<String, Int>()
    private var printer by mutableStateOf("")
    private var openPrintDialog by mutableStateOf(false)
    private var popupState = NotificationPopupHost()
    private var scannedNumber by mutableIntStateOf(0)
    private var sortedDestinationsTitlesList = mutableStateListOf<String>()
    private var openClearDialog by mutableStateOf(false)
    private var savedBarcodesForTest = mutableListOf<String>()
    private var savedEpcsForTest = mutableListOf<String>()
    private val alreadySyncedBarcodes = mutableListOf<String>()
    private val alreadySyncedEpcs = mutableListOf<String>()

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    @Inject
    lateinit var repository: RepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
        setContent {
            Page()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) {
            syncScannedItemToServer(it)
        }

        sortedDestinationsTitlesList.addAll(memory.user.destinationTitles)
        sortedDestinationsTitlesList.remove(
            memory.erpData.warehousesIDsToTitles[memory.user.warehouseCode.toString()] ?: ""
        )
        printers.putAll(memory.erpData.printers)
        printers["بدون لیبل"] = 0
        printer = "بدون لیبل"
        loadMemory()
        syncScannedItemsToServer()
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
        if (scanTypeValue == "RFID" || !scanningMode) {
            barcode.disable()
        }
    }

    private fun printLabel() {

        if (printer == "بدون لیبل") {
            syncScannedItemsToServer()
            saveToMemory()
            popupState.showPopupWithAButton(
                "حواله با شماره $stockDraftNumber ایجاد شد.",
                onDismiss = { clear() },
                onDoneButtonClick = { clear() })
            loading = false
            return
        }

        api.printStockDraftLabel(printers[printer] ?: 0, stockDraftNumber, {
            syncScannedItemsToServer()
            saveToMemory()
            popupState.showPopupWithAButton(
                "حواله با شماره $stockDraftNumber ایجاد و دستور پرینت لیبل آن ارسال شد.",
                onDismiss = { clear() },
                onDoneButtonClick = { clear() })
            loading = false
        }, {
            loading = false
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            when (keyCode) {
                280, 293 -> {
                    scanTrigger()
                }

                4 -> {
                    back()
                }
            }
        }
        return true
    }

    fun scanTrigger() {
        if (scanTypeValue == "بارکد") {
            rf.stopScanning()
            barcode.startBarcodeScan()
        } else {
            if (!rf.scanning && !loading) {
                rf.startBulkScan()
            } else {

                rf.stopScanning()
                if ((rf.epcs.size + barcode.scannedBarcodes.size != 0) && !loading) {
                    syncScannedItemsToServer()
                }
            }
        }
    }

    private fun syncScannedItemsToServer() {

        loading = true

        CoroutineScope(Dispatchers.Default).launch {

            if (barcode.scannedBarcodes.size + rf.epcs.size == 0) {
                loading = false
                return@launch
            }

            val barcodeArray = mutableListOf<String>()
            val epcArray = mutableListOf<String>()
            var scannedProductsBiggerThan500 = false

            run breakForEach@{
                barcode.scannedBarcodes.distinct().forEach {
                    if (it !in alreadySyncedBarcodes) {
                        if (barcodeArray.size < 500) {
                            barcodeArray.add(it)
                        } else {
                            scannedProductsBiggerThan500 = true
                            return@breakForEach
                        }
                    } else {
                        val productIndex = products.indexOf(products.last { it1 ->
                            it1.scannedBarcode == it
                        })
                        products[productIndex].scannedBarcodeNumber =
                            barcode.scannedBarcodes.count { it1 ->
                                it1 == it
                            }
                    }
                }
            }

            run breakForEach@{

                rf.epcs.forEach {
                    if (it !in alreadySyncedEpcs) {
                        if (epcArray.size < 500) {
                            epcArray.add(it)
                        } else {
                            scannedProductsBiggerThan500 = true
                            return@breakForEach
                        }
                    }
                }
            }

            if (barcodeArray.size + epcArray.size == 0) {
                loading = false
                return@launch
            }

            repository.getItemDetailsAndInventory(
                epcArray,
                barcodeArray,
                { epcs, barcodes, invalidEpcs, invalidBarcodes ->
                    handleApiResponse(
                        barcodes = barcodes,
                        epcs = epcs,
                        invalidBarcodes,
                        invalidEpcs,
                        scannedProductsBiggerThan500
                    )
                },
                {
                    loading = false
                }
            )
        }
    }

    private fun handleApiResponse(
        barcodes: List<Product>,
        epcs: List<Product>,
        invalidBarcodes: JSONArray,
        invalidEpcs: JSONArray,
        scannedProductsBiggerThan500: Boolean,
    ) {

        CoroutineScope(Dispatchers.Default).launch {
            barcodes.forEach { barcode ->

                products.find {
                    barcode.primaryKey == it.primaryKey
                }?.let {
                    it.scannedBarcode = barcode.scannedBarcode
                    barcode.scannedBarcodeNumber =
                        this@StockDraftCreate.barcode.scannedBarcodes.count { it2 ->
                            it2 == barcode.scannedBarcode
                        }
                } ?: run {
                    barcode.scannedBarcodeNumber =
                        this@StockDraftCreate.barcode.scannedBarcodes.count { it1 ->
                            it1 == barcode.scannedBarcode
                        }
                    if (memory.user.isStoreUser && !memory.user.currentWarehouseCodeIsDepo) {
                        barcode.wareHouseNumber = barcode.storeNumber
                    }
                    products.add(barcode)
                }
                alreadySyncedBarcodes.add(barcode.scannedBarcode)
            }

            epcs.forEach { epc ->

                products.find {
                    epc.primaryKey == it.primaryKey
                }?.scannedEPCs?.addAll(epc.scannedEPCs) ?: run {
                    if (memory.user.isStoreUser && !memory.user.currentWarehouseCodeIsDepo) {
                        epc.wareHouseNumber = epc.storeNumber
                    }
                    products.add(epc)
                }
                alreadySyncedEpcs.addAll(epc.scannedEPCs)
            }

            for (i in 0 until invalidBarcodes.length()) {
                barcode.scannedBarcodes.remove(invalidBarcodes[i])
            }
            for (i in 0 until invalidEpcs.length()) {
                rf.epcs.remove(invalidEpcs[i])
            }

            if (scannedProductsBiggerThan500) {
                syncScannedItemsToServer()
            } else {
                uiList.addAllAndSort(products)
                scannedNumber = 0
                uiList.forEach {
                    scannedNumber += it.scannedNumber
                }
                loading = false
            }
        }
    }


    private fun createStockDraft() {

        if (memory.user.isStoreUser) {
            loading = true
            if (memory.user.currentWarehouseCodeIsDepo) {
                for (elements in uiList) {
                    if (elements.scannedNumber > elements.wareHouseNumber) {
                        popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                        loading = false
                        return
                    }
                }
            } else {
                for (elements in uiList) {
                    if (elements.scannedNumber > elements.storeNumber) {
                        popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                        loading = false
                        return
                    }
                }
            }
            localStoreDatabase.createStockDraft(
                memory.user.username,
                memory.user.warehouseCode,
                memory.erpData.warehousesIDsToTitles.entries.firstOrNull { it.value == destination }?.key?.toInt()
                    ?: 0,
                uiList,
                stockDraftSpec,
                { stockDraftID ->
                    savedBarcodesForTest.addAll(barcode.scannedBarcodes)
                    savedEpcsForTest.addAll(rf.epcs)
                    barcode.scannedBarcodes.clear()
                    products.removeAll { it1 ->
                        it1.scannedBarcodeNumber > 0
                    }
                    stockDraftNumber = stockDraftID
                    syncScannedItemsToServer()
                    saveToMemory()
                    printLabel()
                },
                {
                    loading = false
                }
            )
        } else {

            loading = true
            for (elements in uiList) {
                if (elements.scannedNumber > elements.wareHouseNumber) {
                    popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                    loading = false
                    return
                }
            }
            api.createStockDraft(
                products = uiList,
                desc = stockDraftSpec,
                source = memory.user.warehouseCode,
                destination = memory.erpData.warehousesIDsToTitles.entries.firstOrNull { it.value == destination }?.key?.toInt()
                    ?: 0,
                onSuccess = {
                    savedBarcodesForTest.addAll(barcode.scannedBarcodes)
                    savedEpcsForTest.addAll(rf.epcs)
                    barcode.scannedBarcodes.clear()
                    products.removeAll { it1 ->
                        it1.scannedBarcodeNumber > 0
                    }
                    stockDraftNumber = it.toString()
                    syncScannedItemsToServer()
                    saveToMemory()
                    printLabel()
                }, onError = {
                    loading = false
                }
            )
        }
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        val alreadySyncedBarcodes = mutableListOf<String>()
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        if (barcode in alreadySyncedBarcodes) {

            processScannedProduct(products.find { it.scannedBarcode == barcode }!!, barcode)
            loading = false
            return
        }

        repository.getBarcodeDetails(
            barcode = barcode, {
                successBeep(state)
                saveToMemory()
                var isInRefillProductList = false

                run forEacheadlineMedium@{
                    products.forEach { it1 ->
                        if (it1.KBarCode == it.KBarCode) {
                            it1.scannedBarcode = it.scannedBarcode
                            it1.scannedBarcodeNumber += 1
                            isInRefillProductList = true
                            return@forEacheadlineMedium
                        }
                    }
                }
                if (!isInRefillProductList) {
                    if (!memory.user.currentWarehouseCodeIsDepo && memory.user.isStoreUser) {
                        it.wareHouseNumber = it.storeNumber
                    }
                    it.scannedBarcodeNumber = 1
                    products.add(it)

                    uiList.addAllAndSort(products)
                    scannedNumber = 0
                    uiList.forEach { product ->
                        scannedNumber += product.scannedNumber
                    }
                    loading = false
                }
            }, {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                loading = false
            }
        )
    }

    private fun processScannedProduct(product: Product, barcode: String) {

        var isInRefillProductList = false

        run forEacheadlineMedium@{
            products.forEach { it1 ->
                if (it1.KBarCode == product.KBarCode) {

                    if (it1.scannedBarcodeNumber < it1.wareHouseNumber) {
                        it1.scannedBarcode = product.scannedBarcode
                        it1.scannedBarcodeNumber += 1
                        isInRefillProductList = true
                        successBeep(state)
                        return@forEacheadlineMedium
                    } else {
                        isInRefillProductList = true
                        errorBeep(state)
                        showLog("تعداد اسکن شده از موجودی انبار نمی تواند بیشتر باشد.", state)
                        this.barcode.scannedBarcodes.removeAll(listOf(barcode))
                        repeat(product.scannedBarcodeNumber) {
                            this.barcode.scannedBarcodes.add(product.scannedBarcode)
                        }
                    }
                }
            }
        }
        if (!isInRefillProductList) {

            if (product.wareHouseNumber > 0) {
                successBeep(state)
                product.scannedBarcodeNumber = 1
                products.add(product)

            } else {
                errorBeep(state)
                showLog("این کالا در انبار موجودی ندارد.", state)
                this.barcode.scannedBarcodes.remove(barcode)
            }
        }

        uiList.addAllAndSort(products)
        scannedNumber = 0
        uiList.forEach {
            scannedNumber += it.scannedNumber
        }
        saveToMemory()
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "CentralCheckOutBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )

        edit.putString(
            "StockDraftEpcsForTest",
            JSONArray(savedEpcsForTest).toString()
        )
        edit.putString(
            "StockDraftBarcodeForTest",
            JSONArray(savedBarcodesForTest).toString()
        )

        edit.putString(
            "CentralCheckOutEpcTable",
            JSONArray(rf.epcs).toString()
        )

        edit.putString("StockDraftCreateSelectedDestination", destination)
        edit.putString("stockDraftNumber", stockDraftNumber)
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        rf.epcs = Gson().fromJson(
            memory.getString("CentralCheckOutEpcTable", ""),
            rf.epcs.javaClass
        ) ?: mutableStateListOf()

        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("CentralCheckOutBarcodeTable", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()
        destination =
            memory.getString("StockDraftCreateSelectedDestination", "انتخاب مقصد") ?: "انتخاب مقصد"
        if (destination !in sortedDestinationsTitlesList) {
            destination = "انتخاب مقصد"
        }

    }

    fun clear(product: Product) {

        val removedRefillProducts = mutableListOf<Product>()

        products.forEach {
            if (it.KBarCode == product.KBarCode) {

                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode
                }
                rf.epcs.removeAll { it1 ->
                    it1 in it.scannedEPCs
                }
                removedRefillProducts.add(it)
            }
        }
        products.removeAll(removedRefillProducts.toSet())
        removedRefillProducts.clear()

        uiList.addAllAndSort(products)
        scannedNumber = 0
        uiList.forEach {
            scannedNumber += it.scannedNumber
        }
        saveToMemory()
    }

    private fun clear() {
        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        uiList.clear()
        products.clear()
        scannedNumber = 0
        alreadySyncedBarcodes.clear()
        alreadySyncedEpcs.clear()
        saveToMemory()
        scanningMode = true
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    private fun back() {

        if (scanningMode) {

            saveToMemory()
            finish()
        } else {
            saveToMemory()
            scanningMode = true
            if (!barcode.isEnabled && scanTypeValue == "بارکد") {
                barcode.enable()
            } else if (barcode.isEnabled && (scanTypeValue == "RFID")) {
                barcode.disable()
            }
        }
    }

    private fun openSearchActivity(product: Product) {
        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
    }

    private fun SnapshotStateList<Product>.addAllAndSort(products: MutableList<Product>): Boolean {

        this.clear()
        val returnVar = this.addAll(products)
        this.sortBy {
            it.name
        }
        this.sortByDescending {
            barcode.scannedBarcodes.lastIndexOf(it.scannedBarcode)
        }
        return returnVar
    }

    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
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
                    bottomBar = { BottomBar() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun BottomBar() {

        if (uiList.isNotEmpty() && !loading) {
            if (scanningMode) {

                BottomBarButton(text = "ارسال حواله") {
                    scanningMode = false
                    if (barcode.isEnabled) {
                        barcode.disable()
                    }
                }
            } else {
                BottomBarButton(text = "ارسال حواله") {

                    if (destination == "انتخاب مقصد" || stockDraftSpec == "") {
                        showLog("لطفا مقصد و شرح حواله را تعیین کنید", state)
                    } else if (!loading) {
                        openPrintDialog = true
                    }
                }
            }
        }
    }

    @Composable
    fun AppBar() {
        if (scanningMode && uiList.isNotEmpty()) {
            AppBarWithDeleteButton(
                title = stringResource(id = R.string.stockDraftCreate),
                onBackPressed = { back() },
                onDeletePressed = {
                    if (!loading && uiList.isNotEmpty()) {
                        openClearDialog = true
                    }
                })
        } else {
            AppBarWithBack(
                title = stringResource(id = R.string.stockDraftCreate),
                onBackPressed = { back() })
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {

            if (openClearDialog) {
                AlertDialogWith2Button(
                    "کالاهای اسکن شده پاک شوند؟",
                    "بله",
                    "خیر",
                    btnConfirmOnClick = {
                        clear()
                        openClearDialog = false
                    },
                    btnNotConfirmOnClick = { openClearDialog = false },
                    onDismiss = { openClearDialog = false })
            }

            if (loading || rf.scanning || localStoreDatabase.loading) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .background(onPrimaryLight, Shapes.small)
                        .fillMaxWidth()
                ) {
                    LoadingCircularProgressIndicator(rf.scanning, loading)
                }
            } else {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
                ) {

                    Text(
                        text = "مجموع: $scannedNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )

                    FilterDropDownList(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = scanTypeValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            scanTypeValue = it
                            if (!barcode.isEnabled && scanTypeValue == "بارکد") {
                                barcode.enable()
                            } else if (barcode.isEnabled && (scanTypeValue == "RFID" || !scanningMode)) {
                                barcode.disable()
                            }
                        },
                        values = mutableListOf("RFID", "بارکد")
                    )
                }

                if (uiList.isEmpty()) {
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
                                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            )
                        }
                    }
                } else {

                    LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                        items(uiList.size) { i ->
                            LazyColumnItem(i)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LazyColumnItem(i: Int) {

        val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

        Box {

            Item(
                i, uiList, true,
                text3 = "اسکن: " + uiList[i].scannedNumber,
                text4 = "موجودی: " + uiList[i].wareHouseNumber,
                enableWarehouseNumberCheck = true
            ) {
                openSearchActivity(uiList[i])
            }

            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp),
                        color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .testTag("clear")
                    .clickable {
                        clear(uiList[i])
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

    @Composable
    fun Content2() {

        if (loading || rf.scanning || localStoreDatabase.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(rf.scanning, loading || localStoreDatabase.loading)
            }
        } else {

            Column {
                NotificationPopUp(popupState)
                if (openPrintDialog) {
                    AlertDialogWithHeadlineMediumButtonDropDownList(
                        title = "لطفا پرینتر مورد نظر خود را مشخص کنید ",
                        btnTxt = "تایید",
                        btnOnClick = {
                            openPrintDialog = false
                            createStockDraft()
                        },
                        dropDownText = printer,
                        onDismiss = { openPrintDialog = false },
                        dropDownRes = printers.keys.toMutableList(),
                        onSelectItem = { printer = it }
                    )
                }

                Column(
                    modifier = Modifier
                        .shadow(6.dp, Shapes.medium)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        )
                        .fillMaxWidth(),
                ) {

                    Row {

                        SimpleTextField(
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp,
                                    top = 12.dp
                                )
                                .fillMaxWidth(),
                            hint = "شرح حواله را وارد کنید",
                            onValueChange = {
                                stockDraftSpec = it
                            },
                            value = stockDraftSpec,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    ) {
                        FilterDropDownListWithSearch(
                            modifier = Modifier
                                .padding(start = 12.dp),
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_location_on_24),
                                    contentDescription = "",
                                    tint = primaryLight,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 4.dp)
                                )
                            },
                            text = {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = destination,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 4.dp)
                                )
                            },
                            onClick = {
                                destination = it
                            },
                            values = sortedDestinationsTitlesList
                        )
                    }
                }

                if (uiList.isEmpty()) {
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
                                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            )
                        }
                    }
                } else {

                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {

                        items(uiList.size) { i ->
                            Item(
                                i,
                                uiList,
                                text3 = "اسکن: " + uiList[i].scannedNumber,
                                text4 = "موجودی: " + uiList[i].wareHouseNumber,
                                enableWarehouseNumberCheck = true
                            )
                        }
                    }
                }
            }
        }
    }
}
