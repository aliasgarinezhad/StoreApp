@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.refillStore.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.jeanwest.reader.features.print.view.PrintPricePerProduct
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
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
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class Refill : ComponentActivity() {

    lateinit var barcode: Barcode
    val inputBarcodes = ArrayList<String>()

    //ui parameters
    private var foundProductsNumber by mutableIntStateOf(0)
    var uiList = mutableStateListOf<Product>()
    private var popupState = NotificationPopupHost()
    var loading by mutableStateOf(false)
    var scanningMode by mutableStateOf(true)
    var refillProducts = mutableListOf<Product>()
    lateinit var rf: RFID
    private val listState = LazyListState(0)


    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var api: API

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var repository: RepositoryImpl

    var departmentFilterList = mutableListOf<String>()
    var selectedDepartmentFilter by mutableStateOf("همه انواع کالا")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) {
            syncScannedItemToServer(it)
        }

        setContent {
            Page()
        }

        loadMemory()

        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
        getRefillBarcodes()
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

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    private fun getRefillBarcodes() {
        loading = true
        api.getRefill(memory.user.calculatedLocationCode, {
            Log.e("refill1", it.toList().toString())
            inputBarcodes.clear()
            inputBarcodes.addAll(it)
            getRefill2Barcodes()
        }, {
            loading = false
        })
    }

    private fun getRefill2Barcodes() {
        loading = true
        api.getRefill2(memory.user.calculatedLocationCode, {
            Log.e("refill2: ", it.toList().toString())
            inputBarcodes.addAll(it)
            if (inputBarcodes.isEmpty()) {
                showLog("خطی صفر است.", state)
                loading = false
            } else {
                getRefillItems()
            }
        }, {
            getRefillItems()
            loading = false
        })
    }

    private fun getRefillItems() {

        loading = true

        api.getItemDetailsAndInventory(mutableListOf(), inputBarcodes, { _, barcodes, _, _ ->

            refillProducts.clear()
            barcodes.forEach {
                it.scannedBarcodeNumber = 0
                it.scannedBarcode = ""
                it.scannedEPCs.clear()
                refillProducts.add(it)
            }
            syncScannedItemsToServer()
        }, {}, true)
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (barcode.scannedBarcodes.size == 0) {
            uiList.addAllAndSort(refillProducts)
            loading = false
            return
        }

        val barcodeTableForV4 = mutableListOf<String>()

        val alreadySyncedBarcodes = mutableListOf<String>()
        refillProducts.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        barcode.scannedBarcodes.forEach {
            if (it !in alreadySyncedBarcodes) {
                barcodeTableForV4.add(it)
            } else {
                val productIndex = refillProducts.indexOf(refillProducts.last { refillProduct ->
                    refillProduct.scannedBarcode == it
                })
                refillProducts[productIndex].scannedBarcodeNumber =
                    barcode.scannedBarcodes.count { it1 ->
                        it1 == it
                    }
            }
        }

        if (barcodeTableForV4.size == 0) {
            uiList.addAllAndSort(refillProducts)
            loading = false
            return
        }

        repository.getItemDetailsAndInventory(
            mutableListOf(),
            barcodeTableForV4,
            { _, barcodes, _, invalidBarcodes ->

                val junkBarcodes = mutableListOf<String>()
                for (i in barcodes.indices) {

                    val isInRefillList = refillProducts.any { refillProduct ->
                        refillProduct.primaryKey == barcodes[i].primaryKey
                    }

                    if (isInRefillList) {

                        val productIndex =
                            refillProducts.indexOf(refillProducts.last { refillProduct ->
                                refillProduct.primaryKey == barcodes[i].primaryKey
                            })

                        refillProducts[productIndex].scannedBarcodeNumber =
                            barcode.scannedBarcodes.count { it1 ->
                                it1 == barcodes[i].scannedBarcode
                            }

                        refillProducts[productIndex].scannedBarcode = barcodes[i].scannedBarcode
                    } else {
                        junkBarcodes.add(barcodes[i].scannedBarcode)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    junkBarcodes.add(invalidBarcodes[i].toString())
                }

                barcode.scannedBarcodes.removeAll(junkBarcodes.toSet())
                uiList.addAllAndSort(refillProducts)
                foundProductsNumber = uiList.filter { refillProduct ->
                    refillProduct.scannedBarcodeNumber > 0
                }.size
                loading = false

            },
            {
                uiList.addAllAndSort(refillProducts)
                loading = false
            }, true
        )
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        val alreadySyncedBarcodes = mutableListOf<String>()
        refillProducts.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        if (barcode in alreadySyncedBarcodes) {

            successBeep(state)
            saveToMemory()

            val productIndex = refillProducts.indexOf(refillProducts.last { refillProduct ->
                refillProduct.scannedBarcode == barcode
            })

            // Validation to ensure scanned number does not exceed warehouse number
            if (refillProducts[productIndex].scannedBarcodeNumber >= refillProducts[productIndex].wareHouseNumber) {
                errorBeep(state)
                showLog("موجودی انبار کافی نیست.", state)
                this.barcode.scannedBarcodes.remove(barcode) // Remove the invalid scan
                uiList.addAllAndSort(refillProducts)
                loading = false
                return
            }

            refillProducts[productIndex].scannedBarcodeNumber =
                this.barcode.scannedBarcodes.count { it1 ->
                    it1 == barcode
                }
            uiList.addAllAndSort(refillProducts)
            loading = false
            return
        }

        repository.getBarcodeDetails(
            barcode = barcode, {

                Log.e("repository", it.toString())

                val isInRefillList = refillProducts.any { refillProduct ->
                    refillProduct.primaryKey == it.primaryKey
                }

                if (isInRefillList) {
                    successBeep(state)
                    saveToMemory()

                    val productIndex =
                        refillProducts.indexOf(refillProducts.last { refillProduct ->
                            refillProduct.primaryKey == it.primaryKey
                        })

                    refillProducts[productIndex].scannedBarcodeNumber =
                        this.barcode.scannedBarcodes.count { it1 ->
                            it1 == it.scannedBarcode
                        }

                    refillProducts[productIndex].scannedBarcode = it.scannedBarcode
                } else {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                }

                uiList.addAllAndSort(refillProducts)
                foundProductsNumber = uiList.filter { refillProduct ->
                    refillProduct.scannedBarcodeNumber > 0
                }.size
                loading = false
            }, {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                uiList.addAllAndSort(refillProducts)
                loading = false
            }
        )
    }

    @SuppressLint("ApplySharedPref")
    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "refillProductsForTest",
            Gson().toJson(refillProducts).toString()
        )

        edit.putString("RefillBarcodeTable", JSONArray(barcode.scannedBarcodes).toString())
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("RefillBarcodeTable", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()
    }

    fun clear(product: Product) {

        refillProducts.forEach {
            if (it.KBarCode == product.KBarCode) {
                it.scannedBarcodeNumber = 0
                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode
                }
                it.scannedBarcode = ""
            }
        }
        uiList.clear()
        uiList.addAllAndSort(refillProducts)
        foundProductsNumber = uiList.filter { refillProduct ->
            refillProduct.scannedBarcodeNumber > 0
        }.size
        saveToMemory()
    }

    private fun createStockDraft() {

        loading = true

        val source = memory.user.warehouseCode

        val sourceString: String
        if (memory.erpData.warehousesIDsToTitles[source.toString()].isNullOrBlank()) {
            loading = false
            popupState.showPopupWithAButton("لطفا انبار خود را درست انتخاب کنید. نوع انبار باید دپو باشد.")
            return
        } else {
            sourceString = memory.erpData.warehousesIDsToTitles[source.toString()]!!
        }

        var destination = 0
        memory.erpData.departmentWarehouses[memory.user.calculatedLocationCode.toString()]?.forEach { warehouseCode ->

            val warehouseString: String
            if (memory.erpData.warehousesIDsToTitles[warehouseCode].isNullOrBlank()) {
                loading = false
                popupState.showPopupWithAButton("کاربری شما به این فروشگاه دسترسی ندارد.")
                return
            } else {
                warehouseString = memory.erpData.warehousesIDsToTitles[warehouseCode]!!
            }

            if (warehouseCode == source.toString() || warehouseString.length >= sourceString.length) {
                return@forEach
            }

            if (warehouseString == sourceString.substring(0, warehouseString.length)
            ) {
                destination = warehouseCode.toInt()
            }
        }

        if (destination == 0) {
            popupState.showPopupWithAButton("شما به انبار فروشگاه دسترسی ندارید.")
            loading = false
            return
        }

        for (elements in uiList.filter {
            it.scannedNumber > 0
        }) {
            if (elements.scannedNumber > elements.wareHouseNumber) {
                popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمی باشد.")
                loading = false
                return
            }
        }
        localStoreDatabase.createStockDraft(
            memory.user.username,
            source,
            destination,
            refillProducts.filter {
                it.scannedNumber > 0
            }.toMutableStateList(),
            "خطی با RFID",
            { stockDraftID ->
                loading = false

                popupState.showPopupWith2Button(
                    message = "اجناس با شماره حواله $stockDraftID به فروشگاه ارسال شدند، قیمت کالاها پرینت شود؟",
                    onOkClick = {
                        val intent = Intent(this, PrintPricePerProduct::class.java)
                        intent.putExtra(
                            "StockDraftId",
                            Gson().toJson(
                                StockDraft(
                                    number = stockDraftID.toLong(),
                                    numberOfItems = barcode.scannedBarcodes.size,
                                    barcodeTable = barcode.scannedBarcodes.toMutableList(),
                                    specification = "خطی با RFID",
                                )
                            )
                        )
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        startActivity(intent)
                        finish()
                    },
                    onCancelClick = {
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        scanningMode = true
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    },
                    onDismiss = {
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        scanningMode = true
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    }
                )
            },
            {
                loading = false
            }
        )
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
            getRefillBarcodes()
        }
    }

    private fun openSearchActivity(product: Product) {

        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
    }

    private fun SnapshotStateList<Product>.addAllAndSort(products: MutableList<Product>): Boolean {


        departmentFilterList.clear()
        departmentFilterList.add("همه انواع کالا")
        products.forEach {
            departmentFilterList.add(it.departmentName)
        }
        departmentFilterList = departmentFilterList.distinct().toMutableList()

        this.clear()
        val returnVar =
            this.addAll(if (selectedDepartmentFilter == "همه انواع کالا") products else products.filter {
                it.departmentName == selectedDepartmentFilter
            })

        this.sortBy {
            it.productCode
        }
        this.sortBy {
            it.name
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
    fun AppBar() {

        TopAppBar(

            navigationIcon = {
                IconButton(
                    modifier = Modifier.testTag("back"),
                    onClick = { back() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = stringResource(id = R.string.refill),
                    modifier = Modifier
                        .padding(end = 50.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        )
    }

    @Composable
    fun BottomBar() {

        if (!loading) {

            if (uiList.filter {
                    it.scannedNumber > 0
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
    fun Content() {

        Column {

            if (loading || localStoreDatabase.loading) {

                LoadingCircularProgressIndicator(false, loading || localStoreDatabase.loading)
            } else {

                NotificationPopUp(popupState)

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                ) {

                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1.3F)
                    ) {
                        FilterDropDownList(
                            icon = { }, text = {
                            Text(
                                text = selectedDepartmentFilter,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        }, values = departmentFilterList
                        ) {
                            selectedDepartmentFilter = it
                            uiList.addAllAndSort(refillProducts)
                        }
                    }

                    Text(
                        text = "خطی: ${
                            uiList.size - uiList.filter {
                                it.scannedNumber > 0
                            }.size
                        }",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = "کل اسکن: ${barcode.scannedBarcodes.size}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    /*Text(
                        text = "پیدا شده: $foundProductsNumber",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                    )*/
                }

                /*Row(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .fillMaxWidth(),
                ) {

                }*/
                if (uiList.isEmpty()) {
                    EmptyBox(text = "کالایی در خطی وجود ندارد.")
                }
                LazyColumn(
                    modifier = Modifier.testTag("RefillActivityLazyColumn"),
                    state = listState
                ) {
                    items(uiList.size) { i ->
                        LazyColumnItem(i)
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
                text3 = "فروشگاه: " + uiList[i].storeNumber.toString(),
                text4 = "انبار: " + uiList[i].wareHouseNumber.toString(),
                enableSign = true,
                signNumber = uiList[i].scannedNumber,
                enableWarehouseNumberCheck = true,
            ) {
                openSearchActivity(uiList[i])
            }

            if (uiList[i].scannedNumber > 0) {
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
    }

    @Composable
    fun Content2() {
        Column {

            if (loading || localStoreDatabase.loading) {

                LoadingCircularProgressIndicator(false, loading || localStoreDatabase.loading)
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

                        items(refillProducts.filter {
                            it.scannedBarcodeNumber > 0
                        }.size) { i ->
                            Item(
                                i,
                                refillProducts.filter {
                                    it.scannedBarcodeNumber > 0
                                }.toMutableList(),
                                text3 = "اسکن: " + refillProducts.filter {
                                    it.scannedBarcodeNumber > 0
                                }.toMutableList()[i].scannedNumber,
                                text4 = "انبار: " + refillProducts.filter {
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