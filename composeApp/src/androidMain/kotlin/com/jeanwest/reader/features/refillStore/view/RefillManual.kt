package com.jeanwest.reader.features.refillStore.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.print.view.PrintPricePerProduct
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class RefillManual : ComponentActivity() {

    lateinit var barcode: Barcode

    //ui parameters
    var loading by mutableStateOf(false)
    private var popupState = NotificationPopupHost()
    var uiList = mutableStateListOf<Product>()
    private var scanningMode by mutableStateOf(true)
    private val listState = LazyListState(0)
    private val scannedBarcodes = mutableListOf<String>()
    private var stockDraftId by mutableStateOf("")

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var repository: RepositoryImpl

    @Inject
    lateinit var localDatabase: LocalStoreDatabase

    var products = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
        setContent {
            Page()
        }
    }

    private fun init() {

        barcode = Barcode(this) {
            syncScannedItemToServer(it)
        }
        loadMemory()
        if (memory.user.isLocalMode && !memory.user.currentWarehouseCodeIsDepo) {
            popupState.showPopupWithAButton(
                message = "مبدا انتخابی باید دپو باشد",
                onDismiss = { back() },
                onDoneButtonClick = { back() },
            )
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
        if (!scanningMode) {
            barcode.disable()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (barcode.scannedBarcodes.size == 0) {
            uiList.addAllAndSort(products)
            loading = false
            return
        }

        val barcodeArray = mutableListOf<String>()
        val alreadySyncedBarcodes = mutableListOf<String>()

        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        barcode.scannedBarcodes.forEach {
            if (it !in alreadySyncedBarcodes) {
                barcodeArray.add(it)
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

        if (barcodeArray.size == 0) {
            uiList.addAllAndSort(products)
            loading = false
            return
        }

        repository.getItemDetailsAndInventory(
            mutableListOf(),
            barcodeArray,
            { _, barcodes, _, invalidBarcodes ->

                barcodes.forEach {
                    var isInRefillProductList = false

                    run forEacheadlineMedium@{
                        products.forEach { it1 ->
                            if (it1.primaryKey == it.primaryKey) {
                                it1.scannedBarcode = it.scannedBarcode
                                it1.scannedBarcodeNumber += 1
                                isInRefillProductList = true
                                return@forEacheadlineMedium
                            }
                        }
                    }
                    if (!isInRefillProductList) {
                        it.scannedBarcodeNumber = 1
                        products.add(it)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    barcode.scannedBarcodes.remove(invalidBarcodes[i])
                }

                uiList.addAllAndSort(products)
                loading = false
            },
            {
                loading = false
            }, true
        )
    }

    private fun createStockDraft() {

        loading = true

        if (!memory.user.currentWarehouseCodeIsDepo) {
            loading = false
            popupState.showPopupWithAButton("لطفا انبار خود را درست انتخاب کنید. نوع انبار باید دپو باشد.")
            return
        }

        val sourceID = memory.user.warehouseCode.toString()
        val sourceTitle = memory.user.warehouses[sourceID]!!
        var destinationID = 0

        memory.user.warehouses.forEach { (warehouseID, warehouseTitle) ->

            if (warehouseID == sourceID || warehouseTitle.length >= sourceTitle.length) {
                return@forEach
            }

            if (warehouseTitle == sourceTitle.substring(0, warehouseTitle.length)
            ) {
                destinationID = warehouseID.toInt()
            }
        }

        if (destinationID == 0) {
            popupState.showPopupWithAButton("شما به انبار فروشگاه دسترسی ندارید.")
            loading = false
            return
        }

        for (elements in uiList) {
            if (elements.scannedNumber > elements.wareHouseNumber) {
                popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                loading = false
                return

            }
        }

        localDatabase.createStockDraft(
            memory.user.username,
            sourceID.toInt(),
            destinationID,
            uiList,
            "شارژ با RFID",
            { stockDraftID ->
                loading = false
                this.stockDraftId = stockDraftID

                popupState.showPopupWith2Button(
                    message = "اجناس با شماره حواله $stockDraftId به فروشگاه ارسال شدند، قیمت کالاها پرینت شود؟",
                    onCancelClick = {
                        barcode.scannedBarcodes.clear()
                        products.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        scanningMode = true
                        barcode.enable()
                    },
                    onOkClick = {
                        val intent = Intent(baseContext, PrintPricePerProduct::class.java)
                        intent.putExtra(
                            "StockDraftId",
                            Gson().toJson(
                                StockDraft(
                                    number = stockDraftId.toLong(),
                                    numberOfItems = barcode.scannedBarcodes.size,
                                    barcodeTable = barcode.scannedBarcodes.toMutableList(),
                                    specification = "شارژ با RFID",
                                )
                            )
                        )
                        barcode.scannedBarcodes.clear()
                        products.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        startActivity(intent)
                        finish()
                    },
                    onDismiss = {
                        barcode.scannedBarcodes.clear()
                        products.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        scanningMode = true
                        barcode.enable()
                    }
                )
            },
            {
                loading = false
            }
        )
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
            saveToMemory()
            val productIndex = products.indexOf(products.last { it1 ->
                it1.scannedBarcode == barcode
            })

            // Validation to ensure scanned number does not exceed warehouse number
            if (products[productIndex].scannedBarcodeNumber >= products[productIndex].wareHouseNumber) {
                errorBeep(state)
                showLog("موجودی انبار کافی نیست.", state)
                this.barcode.scannedBarcodes.remove(barcode) // Remove the invalid scan
                uiList.addAllAndSort(products)
                loading = false
                return
            }
            products[productIndex].scannedBarcodeNumber =
                this.barcode.scannedBarcodes.count { it1 ->
                    it1 == barcode
                }

            uiList.addAllAndSort(products)
            loading = false
            return
        }

        repository.getBarcodeDetails(
            barcode, {
                successBeep(state)
                var isInRefillProductList = false

                run forEacheadlineMedium@{
                    products.forEach { it1 ->
                        if (it1.primaryKey == it.primaryKey) {
                            it1.scannedBarcode = it.scannedBarcode
                            it1.scannedBarcodeNumber += 1
                            isInRefillProductList = true
                            return@forEacheadlineMedium
                        }
                    }
                }
                if (!isInRefillProductList) {
                    it.scannedBarcodeNumber = 1
                    products.add(it)
                }

                saveToMemory()
                uiList.addAllAndSort(products)
                loading = false
            }, {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                uiList.addAllAndSort(products)
                loading = false
            }
        )
    }

    @SuppressLint("ApplySharedPref")
    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "ManualRefillWarehouseManagerBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )

        edit.putString(
            "ManualRefillProducts",
            Gson().toJson(products).toString()
        )

        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        val type = object : TypeToken<List<Product>>() {}.type

        if (this.memory.user.isLocalMode) {
            saveToMemory()
        } else {

            barcode.scannedBarcodes = Gson().fromJson(
                memory.getString("ManualRefillWarehouseManagerBarcodeTable", ""),
                barcode.scannedBarcodes.javaClass
            ) ?: mutableStateListOf()

            products = Gson().fromJson(
                memory.getString("ManualRefillProducts", ""),
                type
            ) ?: mutableListOf()

            uiList.addAllAndSort(products)
        }
    }

    fun clear(product: Product) {

        val removedRefillProducts = mutableListOf<Product>()

        products.forEach {
            if (it.primaryKey == product.primaryKey) {

                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode || it1 == it.KBarCode || (it.searchCodes.isNotEmpty() && it1 == it.searchCodes[0])
                }
                removedRefillProducts.add(it)
            }
        }
        products.removeAll(removedRefillProducts.toSet())
        removedRefillProducts.clear()

        uiList.addAllAndSort(products)
        barcode.scannedBarcodes.remove(product.KBarCode)
        saveToMemory()
    }

    private fun clear() {
        barcode.scannedBarcodes.clear()
        uiList.clear()
        products.clear()
        scannedBarcodes.clear()
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
            } else {
                barcode.disable()
            }
        }
    }

    private fun findKeyOfValueContainingWords(
        map: Map<String, String>,
        word1: String,
        word2: String,
    ): String? {
        return map.entries.find {
            it.value.contains(word1, ignoreCase = true) && !it.value.contains(
                word2,
                ignoreCase = true
            )
        }?.key
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
        this.sortBy {
            it.scannedNumber > 0
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
                    bottomBar = { if (memory.user.calculatedLocationCode in listOf(68, 42, 53, 29)) BottomBar() else BottomBarNotMegamall() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun BottomBar() {
        if (!loading) {
            if (scanningMode) {

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

                            Button(onClick = {
                                Intent(
                                    this@RefillManual,
                                    RefillManualAddItems::class.java
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(this)
                                }
                            }) {
                                Text(text = "تعریف شارژ")
                            }

                            Button(onClick = {
                                if (products.filter { it1 ->
                                        it1.scannedNumber > 0
                                    }.toMutableStateList().isEmpty()) {
                                    showLog("هنوز کالایی برای ارسال اسکن نکرده اید", state)
                                } else {
                                    scanningMode = false
                                    if (barcode.isEnabled) {
                                        barcode.disable()
                                    }
                                }

                            }) {
                                Text(text = "ارسال به فروشگاه")
                            }
                        }
                    }
                }
            } else {
                if (uiList.filter { it1 ->
                        it1.scannedNumber > 0
                    }.toList().isNotEmpty() && !loading) {
                    BottomBarButton("ارسال به فروشگاه") {
                        createStockDraft()
                    }
                }
            }
        }
    }

    @Composable
    fun BottomBarNotMegamall() {

        if (!loading) {
            if (uiList.filter { it1 ->
                    it1.scannedNumber > 0
                }.toList().isNotEmpty() && !loading) {
                if (scanningMode) {
                    BottomBarButton(text = "ارسال به فروشگاه") {
                        scanningMode = false
                        if (barcode.isEnabled) {
                            barcode.disable()
                        }
                    }
                } else {
                    BottomBarButton("ارسال به فروشگاه") {
                        createStockDraft()
                    }
                }
            }
        }
    }

    @Composable
    fun AppBar() {
        if (scanningMode && uiList.isNotEmpty()) {
            AppBarWithDeleteButton(
                title = stringResource(id = R.string.manualRefill),
                onBackPressed = { back() },
                onDeletePressed = {
                    if (!loading && uiList.isNotEmpty()) {

                        popupState.showPopupWith2Button(
                            message = "کالاهای اسکن شده پاک شوند؟",
                            onOkClick = {
                                clear()
                            }
                        )
                    }
                })
        } else {
            AppBarWithBack(
                title = stringResource(id = R.string.manualRefill),
                onBackPressed = { back() })
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {

            if (loading || localDatabase.loading) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .background(onPrimaryLight, Shapes.small)
                        .fillMaxWidth()
                ) {
                    LoadingCircularProgressIndicator(false, loading || localDatabase.loading)
                }
            } else {

                NotificationPopUp(popupState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
                ) {
                    Text(
                        text = "مجموع: " + (barcode.scannedBarcodes.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                    )
                }

                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کالایی برای ارسال اسکن نکرده اید")
                } else {

                    LazyColumn(modifier = Modifier.padding(bottom = 56.dp), state = listState) {

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
                text4 = "انبار: " + uiList[i].wareHouseNumber,
                colorFull = uiList[i].scannedNumber >= uiList[i].requestedNumber,
                enableWarehouseNumberCheck = true,
            ) {
                if (!memory.user.isLocalMode) {
                    openSearchActivity(uiList[i])
                }
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

        Column {

            if (loading || localDatabase.loading) {

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .background(onPrimaryLight, Shapes.small)
                        .fillMaxWidth()
                ) {
                    LoadingCircularProgressIndicator(false, loading || localDatabase.loading)
                }
            } else {

                NotificationPopUp(popupState)

                if (uiList.filter {
                        it.scannedBarcodeNumber > 0
                    }.toMutableList().isEmpty()) {
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
                                "هنوز کالایی برای ارسال به فروشگاه اسکن نکرده اید",
                                style = Typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            )
                        }
                    }
                } else {

                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {

                        items(uiList.filter {
                            it.scannedBarcodeNumber > 0
                        }.size) { i ->
                            Item(
                                i,
                                uiList.filter {
                                    it.scannedBarcodeNumber > 0
                                }.toMutableList(),
                                text3 = "اسکن: " + uiList.filter {
                                    it.scannedBarcodeNumber > 0
                                }.toMutableList()[i].scannedNumber,
                                text4 = "انبار: " + uiList.filter {
                                    it.scannedBarcodeNumber > 0
                                }.toMutableList()[i].wareHouseNumber.toString(),
                                enableWarehouseNumberCheck = true,
                            )
                        }
                    }
                }
            }
        }
    }
}