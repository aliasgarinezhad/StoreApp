package com.jeanwest.reader.features.carton.view

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Carton
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
class CartonTransfer : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Carton>()
    private var scanningMode by mutableStateOf(true)
    private var openDeleteDialog by mutableStateOf(false)
    var scannedBarcodes = mutableListOf<String>()
    private var destination by mutableStateOf("انتخاب مقصد")
    var driver by mutableStateOf("انتخاب راننده")
    var cartons = mutableStateMapOf<String, Carton>()
    private var stockDraftSpec by mutableStateOf("ارسال کارتن ها با RFID")
    var sortedWarehouseTitlesList = mutableListOf<String>()
    private var popupState = NotificationPopupHost()
    private var cartonNumber by mutableStateOf("")
    private var totalProduct by mutableIntStateOf(0)

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
        syncScannedItemsToServer(false)
        setContent {
            Page()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) { scannedBarcode ->
            scannedCarton((scannedBarcode))
        }

        loadMemory()
        sortedWarehouseTitlesList.addAll(memory.erpData.sortedWarehousesList)
        sortedWarehouseTitlesList.addAll(memory.user.warehousesTitlesSorted)
        sortedWarehouseTitlesList = sortedWarehouseTitlesList.distinct().toMutableStateList()
        sortedWarehouseTitlesList.remove(
            memory.erpData.warehousesIDsToTitles[memory.user.warehouseCode.toString()] ?: ""
        )
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

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    private fun calculateTotalProduct() {
        totalProduct = 0
        for (i in 0 until uiList.size) {
            totalProduct += uiList[i].numberOfItems
        }
    }

    private fun syncScannedItemsToServer(enableBeep: Boolean = true) {

        loading = true

        var cartonCode = ""
        run breakForEach@{
            scannedBarcodes.forEach {
                if (it !in cartons.keys) {
                    cartonCode = it
                    return@breakForEach
                }
            }
        }

        if (cartonCode == "") {
            uiList.clear()
            uiList.addAll(cartons.values)
            loading = false
            calculateTotalProduct()
            return
        }

        api.getCartonsDetails(
            listOf(cartonCode),
            { carton ->
                if (carton.isNotEmpty()) {
                    if (carton[0].cartonSource.toIntOrNull() == memory.user.warehouseCode) {
                        cartons[cartonCode] = carton[0]
                        uiList.clear()
                        uiList.addAll(cartons.values)
                        if (enableBeep) successBeep(state)
                    } else {
                        if (enableBeep) errorBeep(state)
                        showLog("کارتن در انبار جاری نیست.", state)
                        scannedBarcodes.remove(cartonCode)
                    }
                    syncScannedItemsToServer()
                } else {
                    if (enableBeep) errorBeep(state)
                    scannedBarcodes.remove(cartonCode)
                    saveToMemory()
                    loading = false
                }
            },
            {
                if (enableBeep) errorBeep(state)
                scannedBarcodes.remove(cartonCode)
                saveToMemory()
                loading = false
            })
        calculateTotalProduct()
    }

    private fun scannedCarton(carton: String) {
        val it = if (carton.uppercase()
                .startsWith("GN")
        ) carton.substring(2) else carton

        scannedBarcodes.add(it)
        scannedBarcodes = scannedBarcodes.distinct().toMutableList()
        cartonNumber = ""
        saveToMemory()
        syncScannedItemsToServer()
    }


    private fun createNewStockDraftByCarton() {

        loading = true

        api.createStockDraftByCarton(
            uiList,
            stockDraftSpec,
            memory.user.warehouseCode,
            memory.erpData.warehousesTitlesToIDs[destination] ?: 0,
            memory.erpData.drivers[driver] ?: 0,
            {
                val stockDraftId = extractNumberFromString(it)
                val numberOfCartons = uiList.size.toString()
                loading = false
                popupState.showPopupWithAButton(
                    message = "تعداد $numberOfCartons کارتن با مجموع کالای $totalProduct عدد با شماره حواله $stockDraftId با موفقیت ارسال شدند. ",
                    onDismiss = {
                        clear()
                    }, onDoneButtonClick = {
                        clear()
                    })
            }, {
                loading = false
            }
        )
        calculateTotalProduct()
    }

    private fun extractNumberFromString(input: String): Long? {
        val regex = Regex("\\d+")
        val matchResult = regex.find(input)
        return matchResult?.value?.toLong()
    }

    private fun checkDataForCreatingNewCartonLogistic() {

        if (destination == "انتخاب مقصد") {
            showLog("لطفا مقصد را انتخاب کنید", state)
        } else if (!loading) {
            createNewStockDraftByCarton()
        }
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "StockDraftByCartonsScannedBarcodes",
            JSONArray(scannedBarcodes).toString()
        )

        edit.putString(
            "StockDraftByCartonsProducts",
            Gson().toJson(cartons).toString()
        )

        edit.apply()
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        val type = object : TypeToken<SnapshotStateMap<String, Carton>>() {}.type

        scannedBarcodes = Gson().fromJson(
            memory.getString("StockDraftByCartonsScannedBarcodes", ""),
            scannedBarcodes.javaClass
        ) ?: mutableListOf()

        cartons = Gson().fromJson(
            memory.getString("StockDraftByCartonsProducts", ""),
            type
        ) ?: SnapshotStateMap()
        calculateTotalProduct()
    }

    fun clear(carton: Carton) {
        scannedBarcodes.remove(carton.number)
        cartons.remove(carton.number)
        uiList.clear()
        uiList.addAll(cartons.values)
        saveToMemory()
        calculateTotalProduct()
    }

    fun clear() {
        scannedBarcodes.clear()
        cartons.clear()
        uiList.clear()
        uiList.addAll(cartons.values)
        scanningMode = true
        destination = "انتخاب مقصد"
        driver = "انتخاب راننده"
        calculateTotalProduct()
        barcode.enable()
        saveToMemory()
        openDeleteDialog = false
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
                    snackbarHost = { ErrorSnackBar(state) },
                    bottomBar = { BottomBar() })
            }
        }
    }

    @Composable
    fun AppBar() {
        AppBarWithDeleteButton({ back() }, {
            if (uiList.isNotEmpty()) {
                openDeleteDialog = true
            }
        }, stringResource(id = R.string.transferCarton))
    }

    @Composable
    fun BottomBar() {
        if (uiList.isNotEmpty() && !loading) {

            if (scanningMode) {
                BottomBarButton(text = "ارسال کارتن ها") {
                    if (cartons.isEmpty()) {
                        showLog("هنوز کارتنی برای ارسال اسکن نکرده اید", state)
                    } else {
                        scanningMode = false
                        if (barcode.isEnabled) {
                            barcode.disable()
                        }
                    }
                }
            } else {
                BottomBarButton(text = "تایید ارسال") {
                    checkDataForCreatingNewCartonLogistic()
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {
            if (loading) {
                LoadingCircularProgressIndicator(false, loading)

            } else if (openDeleteDialog) {
                AlertDialogWith2Button("همه کالاهای اسکن شده پاک شوند؟", "بله", "خیر", {
                    openDeleteDialog = false
                }, {
                    clear()
                }) {
                    openDeleteDialog
                }
            } else {
                SimpleTextField(
                    value = cartonNumber,
                    hint = "شماره کارتن",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 6.dp,
                            top = 12.dp
                        )
                        .fillMaxWidth(),
                    onValueChange = {
                        cartonNumber = it
                    },
                    onDone = {
                        if (cartonNumber != "") {
                            scannedCarton(cartonNumber)
                        } else {
                            showLog("حواله وارد شده نامعتبر است.", state = state)
                        }
                    },
                    keyboardType = KeyboardType.Text
                )
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Text(
                        text = "جمع کارتن: " + (uiList.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(top = 6.dp, start = 16.dp)
                    )
                    Text(
                        text = "جمع کالاها: $totalProduct",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(top = 6.dp, start = 16.dp)
                    )
                }
                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کارتنی برای ارسال اسکن نکرده اید")
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
            Item4(
                enableBottomSpace = i == uiList.size - 1,
                text1 = uiList[i].number,
                text2 = "انبار جاری: " + memory.erpData.warehousesIDsToTitles[uiList[i].cartonSource],
                text3 = "تنوع جنس: " + uiList[i].barcodeTable.distinct().size,
                text4 = "جمع اجناس: " + uiList[i].numberOfItems,
            )

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

            if (loading) {
                LoadingCircularProgressIndicator(false, loading)
            } else {

                NotificationPopUp(popupState)

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
                        OutlinedTextField(
                            value = stockDraftSpec,
                            singleLine = true,
                            label = { Text(text = "شرح حواله") },
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 12.dp)
                                .fillMaxWidth(),
                            onValueChange = {
                                stockDraftSpec = it
                            })
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
                            values = sortedWarehouseTitlesList
                        )
                        FilterDropDownList(
                            modifier = Modifier
                                .padding(start = 16.dp),
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_drive_eta_24),
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
                                    text = driver,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 6.dp)
                                )
                            },
                            onClick = {
                                driver = it
                            },
                            values = memory.erpData.drivers.keys.toMutableList()
                        )
                    }
                }

                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کارتنی برای ارسال اسکن نکرده اید")
                } else {

                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {

                        items(uiList.size) { i ->
                            Item4(
                                enableBottomSpace = i == uiList.size - 1,
                                text1 = uiList[i].number,
                                text2 = "انبار جاری: " + memory.erpData.warehousesIDsToTitles[uiList[i].cartonSource],
                                text3 = "تنوع جنس: " + uiList[i].barcodeTable.distinct().size,
                                text4 = "جمع اجناس: " + uiList[i].numberOfItems,
                            )
                        }
                    }
                }
            }
        }
    }
}