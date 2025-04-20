@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.stockDraftRequest.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftRequestCreateStore : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)

    @Inject
    lateinit var state: SnackbarHostState
    var uiList = mutableStateListOf<Product>()
    private var scanningMode by mutableStateOf(true)
    var products = mutableListOf<Product>()
    private var popupState = NotificationPopupHost()
    private var scannedNumber by mutableIntStateOf(0)

    @Inject
    lateinit var memory: SharedPreference
    var filteredStockDraftType = mutableStateListOf<String>()

    @Inject
    lateinit var api: API

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    private var selectedStockDraftRequestType by mutableStateOf("انتخاب نوع درخواست")
    private var allowedType = mutableListOf(3, 4, 8, 9, 10)
    var stockDraftId by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
        loadMemory()
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
        filteredStockDraftType = memory.erpData.stockDraftRequestTypes.filter {
            it.value in allowedType
        }.keys.toMutableStateList()
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
        barcode.startBarcodeScan()
    }

    private fun createStockDraftRequest() {

        loading = true

        if (memory.user.isStoreUser) {

            localStoreDatabase.createStockDraftRequest(
                memory.user.username,
                memory.user.warehouseCode,
                1919,
                stockDraftRequestType = memory.erpData.stockDraftRequestTypes[selectedStockDraftRequestType]
                    ?: 0,
                uiList,
                {

                    stockDraftId = it
                    barcode.scannedBarcodes.clear()
                    products.clear()
                    uiList.clear()
                    scannedNumber = 0
                    saveToMemory()
                    popupState.showPopupWithAButton(
                        message = it,
                        onDoneButtonClick = {
                            scanningMode = true
                            if (!barcode.isEnabled) {
                                barcode.enable()
                            }
                        },
                        onDismiss = {
                            scanningMode = true
                            if (!barcode.isEnabled) {
                                barcode.enable()
                            }
                        })
                    loading = false
                },
                {
                    loading = false
                }
            )
        } else {
            api.createStockDraftRequest(
                memory.user.username,
                memory.user.warehouseCode,
                1919,
                stockDraftRequestType = memory.erpData.stockDraftRequestTypes[selectedStockDraftRequestType]
                    ?: 0,
                uiList,
                {
                    Log.e("generated stock draft request", it)

                    stockDraftId = it
                    barcode.scannedBarcodes.clear()
                    products.clear()
                    uiList.clear()
                    scannedNumber = 0
                    saveToMemory()
                    popupState.showPopupWithAButton(it)
                    popupState.onDoneButtonClick = {
                        scanningMode = true
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    }
                    popupState.onDismiss = {
                        scanningMode = true
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    }
                    loading = false
                },
                {
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
            successBeep(state)
            val productIndex = products.indexOf(products.last { it1 ->
                it1.scannedBarcode == barcode
            })
            products[productIndex].scannedBarcodeNumber =
                this.barcode.scannedBarcodes.count { it1 ->
                    it1 == barcode
                }

            uiList.addAllAndSort(products)
            loading = false
            return
        }

        if (memory.user.isStoreUser) {

            api.getItemDetailsAndInventory(
                mutableListOf(),
                mutableListOf(barcode),
                { _, barcodes, _, _ ->

                    if (barcodes.size == 1) {
                        successBeep(state)
                        var isInRefillProductList = false

                        run forEacheadlineMedium@{
                            products.forEach { it1 ->
                                if (it1.KBarCode == barcodes[0].KBarCode) {
                                    it1.scannedBarcode = barcodes[0].scannedBarcode
                                    it1.scannedBarcodeNumber += 1
                                    isInRefillProductList = true
                                    return@forEacheadlineMedium
                                }
                            }
                        }
                        if (!isInRefillProductList) {
                            if (!memory.user.currentWarehouseCodeIsDepo) {
                                barcodes[0].wareHouseNumber = barcodes[0].storeNumber
                            }
                            barcodes[0].scannedBarcodeNumber = 1
                            products.add(barcodes[0])
                        }
                    } else {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(barcode)
                    }

                    uiList.addAllAndSort(products)
                    loading = false
                },
                {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                    uiList.addAllAndSort(products)
                    loading = false
                }, true
            )
        } else {

            api.getProductsV5(
                warehouses = listOf(memory.user.warehouseCode.toString()),
                epcs = mutableListOf(),
                barcodes = mutableListOf(barcode),
                { _, barcodes, _, _ ->

                    if (barcodes.size == 1) {
                        successBeep(state)
                        var isInScannedProductsList = false

                        run forEacheadlineMedium@{
                            products.forEach { it1 ->
                                if (it1.KBarCode == barcodes[0].KBarCode) {
                                    it1.scannedBarcode = barcodes[0].scannedBarcode
                                    it1.scannedBarcodeNumber += 1
                                    isInScannedProductsList = true
                                    return@forEacheadlineMedium
                                }
                            }
                        }
                        if (!isInScannedProductsList) {
                            barcodes[0].scannedBarcodeNumber = 1
                            products.add(barcodes[0])
                        }
                    } else {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(barcode)
                    }

                    uiList.addAllAndSort(products)
                    loading = false
                },
                {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                    uiList.addAllAndSort(products)
                    loading = false
                }
            )
        }
    }

    fun extractNumberFromString(input: String): Long? {
        val regex = Regex("\\d+")
        val matchResult = regex.find(input)
        return matchResult?.value?.toLong()
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()
        Log.e("beforExtract", stockDraftId)
        val stockDraftSaved = extractNumberFromString(stockDraftId)
        Log.e("stockDraftSaved", stockDraftSaved.toString())
        edit.putString(
            "StockDraftRequestCreateStoreBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putString(
            "StockDraftRequestCreateStoreScannedItems",
            Gson().toJson(products).toString()
        )

        edit.putString(
            "StockDraftRequestCreateStoreId",
            stockDraftSaved.toString()
        )

        edit.apply()
    }

    private fun loadMemory() {

        val memory =
            PreferenceManager.getDefaultSharedPreferences(this@StockDraftRequestCreateStore)

        val type = object : TypeToken<MutableList<Product>>() {}.type

        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("StockDraftRequestCreateStoreBarcodeTable", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()

        products = Gson().fromJson(
            memory.getString("StockDraftRequestCreateStoreScannedItems", ""),
            type
        ) ?: mutableListOf()

        uiList.addAllAndSort(products)


    }

    fun clear(product: Product) {

        val removedRefillProducts = mutableListOf<Product>()

        products.forEach {
            if (it.KBarCode == product.KBarCode) {

                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode
                }
                removedRefillProducts.add(it)
            }
        }
        products.removeAll(removedRefillProducts.toSet())
        removedRefillProducts.clear()

        uiList.addAllAndSort(products)
        saveToMemory()
    }

    private fun back() {

        if (scanningMode) {

            saveToMemory()
            finish()
        } else {
            saveToMemory()
            scanningMode = true
            if (!barcode.isEnabled) {
                barcode.enable()
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
            it.productCode
        }
        this.sortBy {
            it.name
        }
        scannedNumber = 0
        uiList.forEach {
            scannedNumber += it.scannedNumber
        }
        saveToMemory()

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
                BottomBarButton(text = "ثبت درخواست حواله") {
                    scanningMode = false
                    if (barcode.isEnabled) {
                        barcode.disable()
                    }
                }
            } else {
                BottomBarButton(text = "ثبت درخواست حواله") {
                    if (selectedStockDraftRequestType == "انتخاب نوع درخواست") {
                        showLog("لطفا نوع درخواست را انتخاب کنید", state)
                    } else if (!loading) {
                        createStockDraftRequest()
                    }
                }
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

            title = {
                Text(
                    text = stringResource(id = R.string.StockDraftRequestCreateStore),
                    modifier = Modifier
                        .padding(end = 50.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {

            if (loading || localStoreDatabase.loading) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .background(onPrimaryLight, Shapes.small)
                        .fillMaxWidth()
                ) {
                    LoadingCircularProgressIndicator(isDataLoading = loading || localStoreDatabase.loading)
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
                }

                if (uiList.isEmpty()) {
                    EmptyBox(text = "هنوز کالایی برای ثبت درخواست حواله اسکن نکرده اید.")
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

        if (loading || localStoreDatabase.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(isDataLoading = loading || localStoreDatabase.loading)
            }
        } else {

            Column {
                NotificationPopUp(popupState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .weight(1F),
                    ) {

                        FilterDropDownList(
                            modifier = Modifier
                                .padding(start = 12.dp),
                            icon = {},
                            text = {
                                Text(
                                    style = MaterialTheme.typography.bodyMedium,
                                    text = selectedStockDraftRequestType,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 12.dp)
                                )
                            },
                            onClick = {
                                selectedStockDraftRequestType = it
                            },
                            values = filteredStockDraftType
                        )
                    }
                }

                if (uiList.isEmpty()) {
                    EmptyBox(text = "هنوز کالایی برای ثبت درخواست حواله اسکن نکرده اید.")
                } else {

                    LazyColumn {

                        items(uiList.size) { i ->
                            Item(
                                i,
                                uiList,
                                text3 = "اسکن: " + uiList[i].scannedNumber,
                                text4 = "موجودی: " + uiList[i].wareHouseNumber,
                            )
                        }
                    }
                }
            }
        }
    }
}
