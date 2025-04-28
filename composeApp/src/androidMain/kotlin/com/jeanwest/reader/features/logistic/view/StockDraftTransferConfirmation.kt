@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.logistic.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AlertDialogWithHeadlineMediumButton1InputText
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item2
import com.jeanwest.reader.view.Item4
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.StockDraft
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
class StockDraftTransferConfirmation : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)

    @Inject
    lateinit var state: SnackbarHostState
    private var openDialog by mutableStateOf(false)
    private val listState = LazyListState(0)
    private var deliveryCode by mutableStateOf("")
    var stockDrafts = mutableStateMapOf<String, StockDraft>()
    var stockDraftTransfers = mutableStateMapOf<String, MutableList<String>>()
    var uiList = mutableStateListOf<StockDraft>()
    private var scanningMode by mutableStateOf(false)
    private var selectedStockDraftTransferDriverName = ""
    private var openResumeScanDialog by mutableStateOf(false)
    private var previousScannedBarcodes = mutableListOf<String>()
    private var stockDraftNumber by mutableStateOf("")

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()
        getDraftIDs()
        exceptionHandler()
        setContent {
            Page()
        }

        this.onBackPressedDispatcher.addCallback(this) {
            back()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) { scannedBarcode ->
            if (scanningMode) {
                val it = if (scannedBarcode.uppercase()
                        .startsWith("GN")
                ) scannedBarcode.substring(2) else scannedBarcode
                barcode.scannedBarcodes.add(it)
                this.barcode.scannedBarcodes =
                    this.barcode.scannedBarcodes.distinct().toMutableStateList()
                processScannedStockDrafts(it)
                saveToMemory()
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
        if (!scanningMode) {
            barcode.disable()
        }
    }

    private fun getDraftIDs() {
        loading = true
        api.getDriversStockDraftsLists({
            stockDraftTransfers.clear()
            stockDraftTransfers.putAll(it)
            loading = false
        }, {
            loading = false
        })
    }

    private fun confirmCheckIns() {

        loading = true

        api.confirmStockDraftTransfer(
            memory.erpData.drivers[selectedStockDraftTransferDriverName].toString(),
            {
                loading = false
                clear()
                scanningMode = false
                if (barcode.isEnabled) {
                    barcode.disable()
                }
                getDraftIDs()
            },
            {
                loading = false
            })
    }

    private fun syncInputCartonsToServer() {

        loading = true

        var cartonCode = ""
        run breakForEach@{
            stockDraftTransfers[selectedStockDraftTransferDriverName]?.forEach {
                if (it !in stockDrafts.keys) {
                    cartonCode = it
                    return@breakForEach
                }
            }
        }

        if (cartonCode == "") {
            scanningMode = true
            if (!barcode.isEnabled) {
                barcode.enable()
            }
            uiList.clear()
            uiList.addAll(stockDrafts.values)
            if (barcode.scannedBarcodes.isNotEmpty()) {
                barcode.scannedBarcodes.forEach {
                    processScannedStockDrafts(it, false)
                }
            }
            loading = false
            return
        }

        api.getStockDraftDetails(
            cartonCode,
            {
                stockDrafts[cartonCode] = it
                uiList.clear()
                uiList.addAll(stockDrafts.values)
                syncInputCartonsToServer()
            },
            {
                loading = false
            })
    }

    private fun processScannedStockDrafts(barcode: String, enableBeep: Boolean = true) {
        if (barcode in stockDrafts.keys) {
            stockDrafts[barcode]!!.isScanned = true
            uiList.clear()
            uiList.addAll(stockDrafts.values)
            if (enableBeep) successBeep(state)
            saveToMemory()
        } else {
            if (enableBeep) errorBeep(state)
            showLog("حواله در لیست ارسالی وجود ندارد!", state)
            this.barcode.scannedBarcodes.remove(barcode)
        }
    }

    private fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()
        edit.putString(
            "StockDraftTransferConfirmationBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putString(
            "StockDraftTransferConfirmationDriverName",
            selectedStockDraftTransferDriverName
        )
        edit.apply()
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        previousScannedBarcodes = Gson().fromJson(
            memory.getString("StockDraftTransferConfirmationBarcodeTable", ""),
            previousScannedBarcodes.javaClass
        ) ?: mutableListOf()

        if (previousScannedBarcodes.isNotEmpty()) {
            selectedStockDraftTransferDriverName =
                memory.getString("StockDraftTransferConfirmationDriverName", "") ?: ""
            openResumeScanDialog = true
        }
    }

    private fun back() {
        if (scanningMode) {
            scanningMode = false
            if (barcode.isEnabled) {
                barcode.disable()
            }
            getDraftIDs()
        } else {
            finish()
        }
    }

    fun clear() {
        stockDrafts.clear()
        barcode.scannedBarcodes.clear()
        barcode.barcode = ""
        saveToMemory()
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }
    
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
                    bottomBar = { BottomBar() },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun BottomBar() {

        if (!loading && scanningMode) {

            BottomBarButton(text = "دریافت حواله ها") {

                var hasShortage = false
                run breakForEach@{
                    stockDrafts.values.forEach {
                        if (!it.isScanned) {
                            showLog("لطفا کسری لیست را برطرف کنید.", state)
                            hasShortage = true
                            return@breakForEach
                        }
                    }
                }

                if (!hasShortage) {
                    confirmCheckIns()
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
                    text = stringResource(id = R.string.stockDraftTransferConfirmation),
                    modifier = Modifier
                        .padding(end = 50.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Right,
                )
            }
        )
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {

            if (loading) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .background(onPrimaryLight, Shapes.small)
                        .fillMaxWidth()
                ) {
                    LoadingCircularProgressIndicator(false, loading)
                }
            } else {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
                ) {
                    Text(
                        text = "تعداد حواله ها: " + (uiList.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        text = "اسکن شده: " + (uiList.filter { it.isScanned }.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                    )
                }

                Row {
                    SimpleTextField(
                        value = stockDraftNumber,
                        hint = "شماره حواله",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp,
                                top = 12.dp
                            )
                            .fillMaxWidth(),
                        onValueChange = {
                            stockDraftNumber = it
                        },
                        onDone = {
                            if (stockDraftNumber.isNotBlank()) {
                                processScannedStockDrafts(stockDraftNumber, true)
                            }
                        })
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
                                "هنوز حواله ای برای ارسال اسکن نکرده اید",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            )
                        }
                    }
                } else {

                    LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                        items(uiList.size) { i ->
                            Item2(
                                clickable = false,
                                enableBottomSpace = i == uiList.size - 1,
                                text1 = "شناسه عملیاتی : " + uiList[i].driverOperationId,
                                text2 = "تاریخ: " + uiList[i].date,
                                text3 = "از: " + memory.erpData.warehousesIDsToTitles[uiList[i].source.toString()],
                                text4 = "تعداد کالاها: " + uiList[i].numberOfItems,
                                text5 = "به: " + memory.erpData.warehousesIDsToTitles[uiList[i].destination.toString()],
                                text6 = "تگ RFID: " + if (uiList[i].epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد",
                                text7 = "شرح: " + if (uiList[i].specification == "null") "بدون شرح" else uiList[i].specification,
                                colorFull = uiList[i].isScanned,
                            )
                        }
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

            if (openDialog) {
                AlertDialogWithHeadlineMediumButton1InputText(
                    title = "کد تحویل را وارد کنید",
                    btnTxt = "تایید",
                    btnOnClick = {
                        openDialog = false
                        if (deliveryCode.toIntOrNull() != null) {
                            if (deliveryCode.toInt() == memory.erpData.drivers[selectedStockDraftTransferDriverName]) {
                                syncInputCartonsToServer()
                            } else {
                                showLog("کد تحویل اشتباه است.", state)
                                selectedStockDraftTransferDriverName = ""
                            }
                        } else {
                            showLog("کد تحویل اشتباه است.", state)
                            selectedStockDraftTransferDriverName = ""
                        }
                        deliveryCode = ""
                    },
                    defaultText = deliveryCode,
                    onValueChange = {
                        deliveryCode = it
                    },
                    onDismiss = { openDialog = false }
                )
            }

            if (openResumeScanDialog) {
                AlertDialogWith2Button(
                    title = "شما حواله هایی دارید که اسکن کردید ولی نهایی نشده است، ادامه اسکن قبل را ادامه میدهید؟",
                    btnConfirm = "ادامه",
                    btnNotConfirm = "لغو",
                    btnNotConfirmOnClick = {
                        loading = true
                        barcode.scannedBarcodes.clear()
                        uiList.clear()
                        saveToMemory()
                        openResumeScanDialog = false
                        scanningMode = false
                        loading = false
                    },
                    btnConfirmOnClick = {
                        openResumeScanDialog = false
                        syncInputCartonsToServer()
                        scanningMode = true
                        barcode.scannedBarcodes.clear()
                        barcode.scannedBarcodes.addAll(previousScannedBarcodes)
                        barcode.scannedBarcodes =
                            barcode.scannedBarcodes.distinct().toMutableStateList()
                        previousScannedBarcodes.clear()
                    }) {
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {

                LazyColumn(modifier = Modifier.padding(top = 4.dp), state = listState) {

                    items(stockDraftTransfers.size) { i ->
                        Item4(
                            clickable = true,
                            enableBottomSpace = i == stockDraftTransfers.keys.size - 1,
                            text1 = "نام راننده: " + stockDraftTransfers.keys.toList()[i],
                            text2 = "تعداد حواله ها: " + stockDraftTransfers[stockDraftTransfers.keys.toList()[i]]?.size,
                            text4 = "",
                            text3 = "",
                        ) {

                            if (stockDraftTransfers.keys.toList()[i] != selectedStockDraftTransferDriverName) {
                                selectedStockDraftTransferDriverName =
                                    stockDraftTransfers.keys.toList()[i]
                                clear()
                            }
                            openDialog = true
                        }
                    }
                }
            }
        }
    }
}