package com.jeanwest.reader.features.stockDraft.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.Item7
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.ScanTypeDropDownList
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.models.StockDraftHistory
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.jalaliDate.JalaliDateConverter
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftConfirm : ComponentActivity() {

    lateinit var rf: RFID
    lateinit var barcode: Barcode
    val inputProducts = mutableMapOf<Long, Product>()
    private var scannedProducts = mutableMapOf<Long, Product>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var scannedEpcMapWithProperties = mutableMapOf<String, Product>()
    var scannedBarcodeMapWithProperties = mutableMapOf<String, Product>()
    var draftProperties = StockDraft()

    //ui parameters
    var productConflicts = mutableStateListOf<Product>()
    var loading by mutableStateOf(false)
    private var shortagesNumber by mutableIntStateOf(0)
    private var additionalNumber by mutableIntStateOf(0)
    var itemsUiList = mutableStateListOf<Product>()
    var stockDraftUiList = mutableListOf<StockDraftHistory>()
    private val scanValues = mutableListOf("اضافی", "کسری")
    private var scanFilter by mutableIntStateOf(1)
    private var scanTypeValue by mutableStateOf("بارکد")
    private var popupState = NotificationPopupHost()
    private var stockDraftNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)
    private var openFinishDialog by mutableStateOf(false)
    private val listState = LazyListState(0)
    private var stockDraftIDs = mutableListOf<Long>()
    private var draftsMap = mutableStateMapOf<Long, StockDraft>()

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var localDatabase: LocalStoreDatabase

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
            if (scanningMode) {
                getItemDetails(it)
            } else {
                getStockDraftDetails(it)
            }
        }

        loadMemory()

        if (scanningMode && !memory.user.isLocalMode) {
            syncInputItemsToServer()
        } else {
            getStockDraftsDetails()
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

    private fun getStockDraftsDetails() {
        api.stockDraftsHistory(
            null,
            memory.user.warehouseCode,
            null,
            null,
            null,
            true,
            perPage = "80",
            { it ->

                stockDraftUiList.clear()
                stockDraftUiList.addAll(it)
                stockDraftUiList.sortedByDescending {
                    it.createDate
                }
                loading = false
            },
            {
                loading = false
            })
        loading = true

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
                    syncScannedItemsToServer()
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

        val uiListParameters = when {
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
        itemsUiList.clear()
        itemsUiList.addAll(uiListParameters)
        itemsUiList.sortBy {
            it.productCode
        }
        itemsUiList.sortBy {
            it.name
        }
    }

    private fun syncInputItemsToServer() {

        loading = true

        if (memory.user.isLocalMode) {

            inputBarcodeMapWithProperties.putAll(draftProperties.items)
            makeInputProductMap()
            rf.inputEPCs.clear()
            rf.inputEPCs.addAll(draftProperties.epcsToPrimaryKeysMap.keys)
            rf.justFindInputEPCs = true
            syncScannedItemsToServer()
        } else {

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
                        rf.inputEPCs.clear()
                        rf.inputEPCs.addAll(draftProperties.epcsToPrimaryKeysMap.keys)
                        rf.justFindInputEPCs = true
                        syncScannedItemsToServer()
                    }
                },
                {
                    loading = false
                })
        }
    }

    private fun makeInputProductMap() {

        inputProducts.clear()
        draftProperties.barcodeTable.distinct().forEach {
            val product = inputBarcodeMapWithProperties[it]!!.copy()
            product.draftNumber = draftProperties.barcodeTable.count { it1 ->
                it == it1
            }

            if (product.primaryKey !in inputProducts.keys) {
                inputProducts[product.primaryKey] = product.copy()
            } else {
                inputProducts[product.primaryKey]!!.draftNumber += product.draftNumber
            }
        }
    }

    fun syncScannedItemsToServer() {

        loading = true

        if (barcode.scannedBarcodes.size == 0) {
            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false
            return
        }

        val barcodeArray = mutableListOf<String>()
        var scannedProductsBiggerThan1000 = false

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

        if (barcodeArray.size == 0) {

            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false
            return
        }

        api.getItemDetails(mutableListOf(), barcodeArray, { _, barcodes, _, invalidBarcodes ->

            barcodes.forEach {
                scannedBarcodeMapWithProperties[it.scannedBarcode] = it
                scannedBarcodeMapWithProperties[it.scannedBarcode]!!.scannedBarcodeNumber =
                    barcode.scannedBarcodes.count { scannedBarcode ->
                        scannedBarcode == scannedBarcodeMapWithProperties[it.scannedBarcode]!!.scannedBarcode
                    }
            }

            for (i in 0 until invalidBarcodes.length()) {
                barcode.scannedBarcodes.remove(invalidBarcodes[i])
            }

            if (scannedProductsBiggerThan1000) {
                syncScannedItemsToServer()
            } else {
                makeScannedProductMap()
                calculateConflicts()
                filterResult(productConflicts)
                loading = false
            }

        }, {
            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false
        })
    }

    private fun makeScannedProductMap() {

        Log.e("scannedProductMap", draftProperties.epcsToPrimaryKeysMap.keys.toList().toString())

        rf.epcs = rf.epcs.filter {
            it in draftProperties.epcsToPrimaryKeysMap.keys
        }.toMutableStateList()

        scannedProducts.clear()

        rf.epcs.forEach {

            if (draftProperties.epcsToPrimaryKeysMap[it] == null) {
                showLog("مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید", state)
                return
            }

            draftProperties.epcsToPrimaryKeysMap[it]?.let { productPrimaryKey ->

                val scannedProduct = inputBarcodeMapWithProperties.values.first { inputProduct ->
                    inputProduct.primaryKey == productPrimaryKey
                }.copy(scannedEPCs = mutableListOf(it))

                if (scannedProduct.primaryKey in scannedProducts.keys) {
                    scannedProducts[productPrimaryKey]!!.scannedEPCs.add(it)
                } else {
                    scannedProducts[productPrimaryKey] = scannedProduct
                    scannedProducts[productPrimaryKey]!!.scannedEPCs = mutableListOf(it)
                }
            }
        }

        barcode.scannedBarcodes.distinct().forEach {

            if (scannedBarcodeMapWithProperties[it] == null) {
                showLog("مشکلی در پردازش اطلاعات به وجود آمده است. لطفا دوباره تلاش کنید", state)
                return
            }

            scannedBarcodeMapWithProperties[it]?.copy()?.let { product ->
                product.scannedBarcodeNumber = barcode.scannedBarcodes.count { it1 ->
                    it == it1
                }

                if (product.primaryKey !in scannedProducts.keys) {
                    scannedProducts[product.primaryKey] = product.copy()
                } else {
                    scannedProducts[product.primaryKey]!!.scannedBarcodeNumber += product.scannedBarcodeNumber
                }
            }
        }
        saveToMemory()
    }

    private fun confirmStockDraft() {

        if (memory.user.isStoreUser) {

            loading = true

            localDatabase.confirmStockDraft(
                memory.user.username,
                draftProperties.source,
                draftProperties.destination,
                draftProperties.number,
                productConflicts.filter { it.scannedNumber > 0 }.toMutableList(),
                {
                    clearAll()
                    scanningMode = false
                    if (!barcode.isEnabled) {
                        barcode.enable()
                    }
                    saveToMemory()
                    getStockDraftsDetails()
                    popupState.showPopupWithAButton(it)
                },
                {
                    popupState.showPopupWithAButton(it)
                    loading = false
                },
            )
        } else {
            loading = true
            api.confirmStockDraft(
                draftProperties.number,
                productConflicts.filter { it.scannedNumber > 0 }.toMutableList(),
                {
                    clearAll()
                    scanningMode = false
                    if (!barcode.isEnabled) {
                        barcode.enable()
                    }
                    saveToMemory()
                    getStockDraftsDetails()
                    popupState.showPopupWithAButton(it)
                },
                {
                    loading = false
                }
            )
        }
    }

    private fun getItemDetails(barcode: String) {

        loading = true

        if (barcode in scannedBarcodeMapWithProperties.keys) {
            successBeep(state)
            saveToMemory()
            makeScannedProductMap()
            calculateConflicts()
            filterResult(productConflicts)
            loading = false

        } else {

            if (memory.user.isLocalMode) {
                localDatabase.getItemDetails(
                    epcs = listOf(),
                    barcodes = listOf(barcode),
                    { _, barcodes, _, invalidBarcodes ->

                        if (barcodes.size == 1 && invalidBarcodes.length() == 0) {
                            successBeep(state)
                            saveToMemory()
                            scannedBarcodeMapWithProperties[barcode] =
                                barcodes[0]

                            //Log.e("")
                            makeScannedProductMap()
                            calculateConflicts()
                            filterResult(productConflicts)
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
                    })

            } else {

                api.getItemDetails(
                    mutableListOf(),
                    mutableListOf(barcode),
                    { _, barcodes, _, invalidBarcodes ->

                        if (barcodes.size == 1 && invalidBarcodes.length() == 0) {
                            successBeep(state)
                            saveToMemory()
                            scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] =
                                barcodes[0]
                            makeScannedProductMap()
                            calculateConflicts()
                            filterResult(productConflicts)
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
                    })
            }
        }
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString("CheckInBarcodeTable", JSONArray(barcode.scannedBarcodes).toString())
        edit.putBoolean("CheckInScanningMode", scanningMode)
        edit.putInt("StockDraftConfirmLastUsername", this.memory.user.username)
        edit.putInt("StockDraftConfirmLastWarehouseCode", this.memory.user.warehouseCode)
        edit.putString("CheckInDraftProperties", Gson().toJson(draftProperties))
        edit.putLong("StockDraftConfirmLastScanTime", System.currentTimeMillis())
        edit.putString("CheckInEPCTable", JSONArray(rf.epcs).toString())
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        try {


            scanningMode = memory.getBoolean("CheckInScanningMode", false)

            if (scanningMode) {
                if (!this.memory.user.isLocalMode) {
                    draftProperties = Gson().fromJson(
                        memory.getString("CheckInDraftProperties", ""), draftProperties.javaClass
                    )
                    rf.epcs = Gson().fromJson(
                        memory.getString("CheckInEPCTable", ""), rf.epcs.javaClass
                    ) ?: mutableStateListOf()

                    barcode.scannedBarcodes = Gson().fromJson(
                        memory.getString("CheckInBarcodeTable", ""),
                        barcode.scannedBarcodes.javaClass
                    ) ?: mutableStateListOf()

                    val lastUsername = memory.getInt("StockDraftConfirmLastUsername", 0)
                    val lastWarehouseCode = memory.getInt("StockDraftConfirmLastWarehouseCode", 0)
                    val lastScanTime = memory.getLong("StockDraftConfirmLastScanTime", 0L)

                    if ((rf.epcs.size == 0 && barcode.scannedBarcodes.size == 0) || lastUsername != this.memory.user.username || lastWarehouseCode != this.memory.user.warehouseCode || System.currentTimeMillis() > lastScanTime + 43200000) {
                        scanningMode = false
                        clearAll()
                    }
                } else {
                    clearAll()
                    scanningMode = false
                }
            }
        } catch (_: Exception) {

        }
    }

    private fun clearAll() {
        stockDraftNumber = ""
        inputProducts.clear()
        inputBarcodeMapWithProperties.clear()
        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        scannedProducts.clear()
        scannedBarcodeMapWithProperties.clear()
        scannedEpcMapWithProperties.clear()
        stockDraftUiList.clear()
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

    private fun getStockDraftDetails(code: String) {

        loading = true

        if (code.toLongOrNull() == null) {
            showLog("شماره حواله وارد شده نامعتبر است.", state)
            loading = false
            return
        } else {
            if (memory.user.isLocalMode) {
                localDatabase.getStockDraftDetails(code, {
                    draftProperties = it
                    clearAll()
                    scanningMode = true
                    if (scanTypeValue == "RFID") {
                        barcode.disable()
                    }
                    saveToMemory()
                    syncInputItemsToServer()
                    loading = false
                }, {
                    loading = false
                })
            } else {
                api.getStockDraftDetails(code, {
                    if (!memory.user.isStoreUser && it.destination != memory.user.warehouseCode) {
                        showLog("انبار مقصد حواله با انبار جاری انتخابی شما متفاوت است.", state)
                        loading = false
                    } else if (it.stateID in listOf("2", "9") && it.positionID == "2") {
                        showLog("حواله نهایی شده است.", state)
                        stockDraftNumber = ""
                        loading = false
                    } else {
                        draftProperties = it
                        clearAll()
                        scanningMode = true
                        if (scanTypeValue == "RFID") {
                            barcode.disable()
                        }
                        saveToMemory()
                        syncInputItemsToServer()
                    }
                }, {
                    loading = false
                })
            }
        }
    }

    fun clear(product: Product) {

        barcode.scannedBarcodes = barcode.scannedBarcodes.filter {
            it != product.scannedBarcode
        }.toMutableStateList()

        rf.epcs = rf.epcs.filter {
            it !in product.scannedEPCs
        }.toMutableStateList()

        scannedBarcodeMapWithProperties.remove(product.scannedBarcode)
        product.scannedEPCs.forEach {
            scannedEpcMapWithProperties.remove(it)
        }

        scannedProducts.remove(product.primaryKey)

        calculateConflicts()
        filterResult(productConflicts)
        saveToMemory()
    }

    private fun warehouseCodeToString(code: String): String {
        return memory.erpData.warehousesIDsToTitles[code].toString()
    }

    private fun dateConverter(date: String): String {
        val intArrayFormatJalaliCreateDate = JalaliDateConverter.gregorian_to_jalali(
            date.substring(0, 4).toInt(),
            date.substring(5, 7).toInt(),
            date.substring(8, 10).toInt()
        )
        return "${intArrayFormatJalaliCreateDate[0]}/${intArrayFormatJalaliCreateDate[1]}/${intArrayFormatJalaliCreateDate[2]}"
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
        AppBarWithBack(
            title = stringResource(id = R.string.stock_draft_confirmation),
            onBackPressed = { back() })
    }

    @Composable
    fun BottomBar() {

        BottomAppBar {

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
                        Text(text = "تایید نهایی")
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
            if (openFinishDialog) {
                AlertDialogWith2Button(
                    "کالاهای اسکن شده ثبت شوند؟",
                    "بله",
                    "خیر، نتایج پاک شوند",
                    btnNotConfirmOnClick = {
                        openFinishDialog = false
                        clearAll()
                        scanningMode = false
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                        saveToMemory()
                        getStockDraftsDetails()
                    },
                    btnConfirmOnClick = {
                        openFinishDialog = false
                        confirmStockDraft()
                    },
                    onDismiss = { openFinishDialog = false })
            }

            if (rf.scanning || loading || localDatabase.loading) {
                LoadingCircularProgressIndicator(
                    rf.scanning, loading || localDatabase.loading
                )
            } else {

                NotificationPopUp(popupState)

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
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

                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "شماره حواله: ${draftProperties.number}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                }

                LazyColumn(modifier = Modifier.padding(bottom = 56.dp), state = listState) {
                    items(itemsUiList.size) { i ->
                        AdditionalItems(i)
                    }
                }
            }
        }
    }

    @Composable
    fun AdditionalItems(i: Int) {

        val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

        Box {

            Item(
                i, itemsUiList, true,
                text3 = "موجودی: " + itemsUiList[i].draftNumber,
                text4 = itemsUiList[i].conflictType + ": " + itemsUiList[i].conflictNumber,
            ) {
                openSearchActivity(itemsUiList[i])
            }

            if (itemsUiList[i].conflictType == "اضافی") {

                Box(
                    modifier = Modifier
                        .padding(top = topPaddingClearButton, end = 8.dp)
                        .background(
                            shape = RoundedCornerShape(36.dp), color = errorContainerLight
                        )
                        .size(30.dp)
                        .align(TopEnd)
                        .testTag("clear")
                        .clickable {
                            clear(itemsUiList[i])
                        }) {
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

    @Composable
    fun Content2() {
        if (loading || localDatabase.loading) {
            LoadingCircularProgressIndicator(isDataLoading = loading || localDatabase.loading)
        } else {

            Column(modifier = Modifier.fillMaxSize()) {

                NotificationPopUp(popupState)

                SimpleTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                        .testTag("CustomTextField"),
                    hint = "شماره حواله",
                    onValueChange = {
                        stockDraftNumber = it
                    },
                    value = stockDraftNumber,
                    onDone = {
                        getStockDraftDetails(stockDraftNumber)
                    },
                    keyboardType = KeyboardType.Number
                )

                LazyColumn(modifier = Modifier.padding(top = 4.dp)) {

                    val filteredStockDraftUiList = if (stockDraftNumber == "") {
                        stockDraftUiList
                    } else {
                        stockDraftUiList.filter {
                            it.stockDraftStatusID.startsWith(stockDraftNumber)
                        }
                    }

                    items(filteredStockDraftUiList.size) { i ->
                        Item7(
                            clickable = true,
                            enableBottomSpace = i == filteredStockDraftUiList.size - 1,
                            text1 = "حواله: " + filteredStockDraftUiList[i].stockDraftID,
                            text2 = "از: " + warehouseCodeToString(filteredStockDraftUiList[i].fromWareHouseID),
                            text4 = "به: " + warehouseCodeToString(filteredStockDraftUiList[i].toWareHouseID),
                            text3 = "وضعیت: " + filteredStockDraftUiList[i].statusTitle,
                            text7 = "تاریخ ایجاد: " + dateConverter(filteredStockDraftUiList[i].createDate),
                            text5 = "تاریخ ارسال: " + dateConverter(filteredStockDraftUiList[i].sendDate),
                            text6 = "کد تحویل: " + filteredStockDraftUiList[i].deliveryCode
                        ) {
                            stockDraftNumber = filteredStockDraftUiList[i].stockDraftID
                            getStockDraftDetails(stockDraftNumber)

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
                        filterResult(productConflicts)
                    }, text = { Text(text = it) })
                }
            }
        }
    }
}