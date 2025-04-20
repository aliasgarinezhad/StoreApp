@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.stockDraft.view

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.ScanTypeDropDownList
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
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
class StockDraftAttachEPCs : ComponentActivity() {

    lateinit var barcode: Barcode
    val inputProducts = mutableMapOf<String, Product>()
    private var scannedProducts = mutableMapOf<String, Product>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var scannedEpcMapWithProperties = mutableMapOf<String, Product>()
    private var scannedBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var draftProperties = StockDraft(
        number = 0L,
        numberOfItems = 0,
    )
    lateinit var rf: RFID

    //ui parameters
    var productConflicts = mutableStateListOf<Product>()
    var loading by mutableStateOf(false)
    private var shortagesNumber by mutableIntStateOf(0)
    private var additionalNumber by mutableIntStateOf(0)
    private var shortageCodesNumber by mutableIntStateOf(0)
    private var additionalCodesNumber by mutableIntStateOf(0)
    var uiList = mutableStateListOf<Product>()
    private val scanValues = mutableListOf("اضافی", "کسری")
    private var scanFilter by mutableStateOf(1)
    private var openClearDialog by mutableStateOf(false)
    private var scanTypeValue by mutableStateOf("RFID")

    @Inject
    lateinit var state: SnackbarHostState
    private var stockDraftNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)
    private var openFinishDialog by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()
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
                getWarehouseDetails(it)
            }
        }
        rf = RFID(this, state) {
            scanTrigger()
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
        if (scanTypeValue == "RFID" && scanningMode) {
            barcode.disable()
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
            if (scanTypeValue == "بارکد") {
                rf.stopScanning()
                barcode.startBarcodeScan()
            } else {
                if (!rf.scanning) {
                    rf.startBulkScan()
                } else {

                    rf.stopScanning()
                    if (rf.epcs.isNotEmpty() || barcode.scannedBarcodes.isNotEmpty()) {
                        syncScannedItemsToServer()
                    }
                }
            }
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
            it.productCode
        }
        uiList.sortBy {
            it.name
        }
    }

    private fun getWarehouseDetails(code: String) {

        loading = true

        if (code.toLongOrNull() == null) {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "شماره حواله وارد شده نامعتبر است.",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
            loading = false
            return
        }

        api.getStockDraftDetails(code, {
            draftProperties = it
            saveToMemory()
            clear()
            scanningMode = true
            if (barcode.isEnabled && scanTypeValue == "RFID") {
                barcode.disable()
            }
            saveToMemory()
            syncInputItemsToServer()
        }, {
            loading = false
        })
    }

    private fun syncInputItemsToServer() {

        loading = true

        val barcodeTableForV4 = mutableListOf<String>()
        var inputProductsBiggerThan1000 = false

        run breakForEach@{
            draftProperties.barcodeTable.distinct().forEach {
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
            { _, barcodes, _, _ ->

                barcodes.forEach { product ->
                    inputBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                if (inputProductsBiggerThan1000) {
                    syncInputItemsToServer()
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

        inputProducts.clear()
        draftProperties.barcodeTable.distinct().forEach {
            val product = inputBarcodeMapWithProperties[it]!!.copy()
            product.draftNumber = draftProperties.barcodeTable.count { it1 ->
                it == it1
            }

            if (product.KBarCode !in inputProducts.keys) {
                inputProducts[product.KBarCode] = product.copy()
            } else {
                inputProducts[product.KBarCode]!!.draftNumber += product.draftNumber
            }
        }
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (rf.epcs.isEmpty() && barcode.scannedBarcodes.isEmpty()) {
            calculateConflicts()
            filterResult(productConflicts)
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

            calculateConflicts()
            filterResult(productConflicts)
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
                    scannedBarcodeMapWithProperties[it.scannedBarcode] = it
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
                    makeScannedProductMap()
                    calculateConflicts()
                    filterResult(productConflicts)
                    loading = false
                }

            },
            {
                calculateConflicts()
                filterResult(productConflicts)
                loading = false
            })
    }

    private fun makeScannedProductMap() {

        scannedProducts.clear()

        rf.epcs.forEach {

            if (scannedEpcMapWithProperties[it] == null) {
                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید",
                        null,
                        duration = SnackbarDuration.Long
                    )
                }
                return
            }

            scannedEpcMapWithProperties[it]?.let { product ->

                if (product.KBarCode in scannedProducts.keys) {
                    scannedProducts[product.KBarCode]!!.scannedEPCs.add(it)
                } else {
                    scannedProducts[product.KBarCode] =
                        product.copy(scannedEPCs = mutableListOf(it))
                    scannedProducts[product.KBarCode]!!.scannedEPCs = mutableListOf(it)
                }
            }
        }

        val shouldBeCleanedFromScannedBarcodes = mutableListOf<String>()

        barcode.scannedBarcodes.distinct().forEach {

            if (scannedBarcodeMapWithProperties[it] == null) {

                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید",
                        null,
                        duration = SnackbarDuration.Long
                    )
                }
                return
            }

            scannedBarcodeMapWithProperties[it]?.copy()?.let { product ->
                product.scannedBarcodeNumber = barcode.scannedBarcodes.count { it1 ->
                    it == it1
                }

                if ((product.brandName == "primaryLight" || product.brandName == "JootiJeans" || product.brandName == "Baleno")
                    && !product.name.contains("جوراب")
                    && !product.name.contains("عينك")
                    && !product.name.contains("شاپينگ")
                ) {
                    CoroutineScope(Dispatchers.Default).launch {
                        state.showSnackbar(
                            "این کالا باید با RFID اسکن شود",
                            null,
                            duration = SnackbarDuration.Long
                        )
                    }
                    shouldBeCleanedFromScannedBarcodes.add(it)
                } else {

                    if (product.KBarCode !in scannedProducts.keys) {
                        scannedProducts[product.KBarCode] = product.copy()
                    } else {
                        scannedProducts[product.KBarCode]!!.scannedBarcodeNumber += product.scannedBarcodeNumber
                    }
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
            successBeep(state)
            saveToMemory()
            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false
            return
        }

        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, invalidBarcodes ->

                if (barcodes.size == 1 && invalidBarcodes.length() == 0) {
                    successBeep(state)
                    saveToMemory()
                    scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] = barcodes[0]
                    makeScannedProductMap()
                    calculateConflicts()
                    filterResult(productConflicts)
                } else if (invalidBarcodes.length() == 1) {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                }
                loading = false
            },
            {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                calculateConflicts()
                filterResult(productConflicts)
                loading = false
            })
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString("CentralWarehouseCheckInEPCTable", JSONArray(rf.epcs).toString())
        edit.putString(
            "CentralWarehouseCheckInBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putBoolean("scanningMode", scanningMode)
        edit.putString("draftProperties", Gson().toJson(draftProperties))
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        scanningMode = memory.getBoolean("scanningMode", false)

        if (scanningMode) {
            draftProperties = Gson().fromJson(
                memory.getString("draftProperties", ""),
                draftProperties.javaClass
            )
            rf.epcs = Gson().fromJson(
                memory.getString("CentralWarehouseCheckInEPCTable", ""),
                rf.epcs.javaClass
            ) ?: mutableStateListOf()

            barcode.scannedBarcodes = Gson().fromJson(
                memory.getString("CentralWarehouseCheckInBarcodeTable", ""),
                barcode.scannedBarcodes.javaClass
            ) ?: mutableStateListOf()
        }

    }

    private fun clear() {

        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        scannedProducts.clear()
        scannedBarcodeMapWithProperties.clear()
        scannedEpcMapWithProperties.clear()
        calculateConflicts()
        filterResult(productConflicts)
        saveToMemory()
    }

    private fun back() {
        saveToMemory()
        rf.stopScanning()
        finish()
    }

    private fun openSearchActivity(product: Product) {
        val searchResultProduct = Product(
            name = product.name + " از حواله شماره " + draftProperties.number,
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

    private fun editCheckIns() {

        loading = true

        if (additionalNumber != 0 || shortagesNumber != 0) {

            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "لطفا ابتدا مغایرت ها را برطرف نمایید.",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
            loading = false
            return
        }

        api.editStockDraft(draftProperties.number, productConflicts, {
            clear()
            scanningMode = false
            if (!barcode.isEnabled) {
                barcode.enable()
            }
            saveToMemory()
            loading = false
        }, {
            loading = false
        })
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

                IconButton(
                    modifier = Modifier.testTag("CheckInTestTag"),
                    onClick = {
                        if (!this@StockDraftAttachEPCs.loading && !this@StockDraftAttachEPCs.loading && !rf.scanning) {
                            openClearDialog = true
                        }
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = stringResource(id = R.string.stockDraftAttachEpcs),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Right,
                )
            }
        )
    }

    @Composable
    fun BottomBar() {
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
                        if (!barcode.isEnabled && scanTypeValue == "بارکد") {
                            barcode.enable()
                        } else if (barcode.isEnabled && scanTypeValue == "RFID" && scanningMode) {
                            barcode.disable()
                        }
                    }
                    ScanFilterDropDownList(modifier = Modifier.align(Alignment.CenterVertically))
                    Button(onClick = { openFinishDialog = true }) {
                        Text(text = "پایان تروفالس")
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
                    btnConfirmOnClick = {
                        openClearDialog = false
                        clear()
                    },
                    btnNotConfirmOnClick = { openClearDialog = false },
                    onDismiss = { openClearDialog = false })
            }

            if (openFinishDialog) {
                AlertDialogWith2Button(
                    title = "کالاهای اسکن شده ثبت شوند؟",
                    btnConfirm = "بله",
                    btnNotConfirm = "خیر، نتایج پاک شوند",
                    btnConfirmOnClick = {
                        openFinishDialog = false
                        editCheckIns()
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
                            text = "اسکن: ${rf.epcs.size + barcode.scannedBarcodes.size}",
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
            loading = loading,
            onClick = { getWarehouseDetails(stockDraftNumber) },
            value = stockDraftNumber,
            onValueChange = { stockDraftNumber = it },
            item = "شماره حواله"
        )
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
                        filterResult(productConflicts)
                    }, text = { Text(text = it) })
                }
            }
        }
    }
}