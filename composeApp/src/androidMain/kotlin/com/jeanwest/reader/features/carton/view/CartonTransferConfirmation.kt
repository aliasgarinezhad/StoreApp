package com.jeanwest.reader.features.carton.view

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AlertDialogWithHeadlineMediumButton1InputText
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item2
import com.jeanwest.reader.view.Item4
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Carton
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
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
class CartonTransferConfirmation @Inject constructor(
) : ComponentActivity() {
    lateinit var barcode: Barcode
    var loading by mutableStateOf(false)

    @Inject
    lateinit var state: SnackbarHostState
    private var openDialog by mutableStateOf(false)
    private val listState = LazyListState(0)
    var cartonTransfers = mutableStateMapOf<String, MutableList<String>>()
    private var selectedCartonTransferDriverName = ""
    private var deliveryCode by mutableStateOf("")
    var cartons = mutableStateMapOf<String, Carton>()
    var uiList = mutableStateListOf<Carton>()
    private var scanningMode by mutableStateOf(false)
    lateinit var rf: RFID
    private var openResumeScanDialog by mutableStateOf(false)
    private var previousScannedBarcodes = mutableListOf<String>()
    private var cartonNumber by mutableStateOf("")
    private var isRefreshStep0 by mutableStateOf(false)


    @Inject
    lateinit var api: API

    @Inject
    lateinit var memory: SharedPreference

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

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) { scannedBarcode ->

            val it = if (scannedBarcode.uppercase()
                    .startsWith("GN")
            ) scannedBarcode.substring(2) else scannedBarcode

            barcode.scannedBarcodes = barcode.scannedBarcodes.distinct().toMutableStateList()
            processScannedCartons(it)
            saveToMemory()
        }
    }

    private fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()
        edit.putString(
            "CartonTransferConfirmationBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putString(
            "CartonTransferConfirmationDriverName",
            selectedCartonTransferDriverName
        )
        edit.apply()
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        previousScannedBarcodes = Gson().fromJson(
            memory.getString("CartonTransferConfirmationBarcodeTable", ""),
            previousScannedBarcodes.javaClass
        ) ?: mutableListOf()

        if (previousScannedBarcodes.isNotEmpty()) {
            selectedCartonTransferDriverName =
                memory.getString("CartonTransferConfirmationDriverName", "") ?: ""
            openResumeScanDialog = true
        }
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
        api.getDriversCartonsListsV2(
            iSDeliverToDest = false,
            iSConfirmedByRFID = false, {

                cartonTransfers.clear()
                cartonTransfers.putAll(it)

                api.getDriversCartonsListsV2(
                    iSDeliverToDest = false,
                    iSConfirmedByRFID = true, { logistics ->
                        cartonTransfers.putAll(logistics)
                        loading = false
                    }, {
                        loading = false
                    })
            }, {
                loading = false
            })
    }

    private fun confirmCheckIns() {

        loading = true

        api.confirmCartonTransfer(
            memory.erpData.drivers[selectedCartonTransferDriverName].toString(),
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

        api.getCartonsDetails(
            codes = cartonTransfers[selectedCartonTransferDriverName]?.toList() ?: mutableListOf(),
            { cartons ->

                cartons.forEach { carton ->
                    this.cartons[carton.number] = carton
                }

                uiList.clear()
                uiList.addAll(this.cartons.values)

                if (!barcode.isEnabled) {
                    barcode.enable()
                }

                barcode.scannedBarcodes.forEach {
                    processScannedCartons(it, false)
                }
                loading = false
            },
            {
                loading = false
            }
        )
    }

    private fun processScannedCartons(barcode: String, enableBeep: Boolean = true) {

        if (barcode in cartons.keys) {
            if (cartons[barcode]!!.isConfirmed) {
                if (enableBeep) errorBeep(state)
                showLog("کارتن قبلا اسکن شده!", state)
            } else {
                cartons[barcode]!!.isConfirmed = true
                uiList.clear()
                uiList.addAll(cartons.values)
                if (enableBeep) successBeep(state)
                saveToMemory()
            }
        } else {
            if (enableBeep) errorBeep(state)
            showLog("کارتن در لیست ارسالی وجود ندارد!", state)
            this.barcode.scannedBarcodes.remove(barcode)
        }
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
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
        cartons.clear()
        barcode.scannedBarcodes.clear()
        barcode.barcode = ""
        saveToMemory()
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
                    text = stringResource(id = R.string.cartonTransferConfirmation),
                    modifier = Modifier
                        .padding(end = 50.dp)
                        .fillMaxSize()
                        .wrapContentSize(),
                    textAlign = TextAlign.Right,
                )
            }
        )
    }

    @Composable
    fun BottomBar() {

        if (!loading && scanningMode) {

            BottomBarButton(text = "دریافت کارتن ها") {

                var hasShortage = false
                run breakForEach@{
                    cartons.values.forEach {
                        if (!it.isConfirmed) {
                            CoroutineScope(Dispatchers.Default).launch {
                                state.showSnackbar(
                                    "لطفا کسری لیست را برطرف کنید.",
                                    null,
                                    duration = SnackbarDuration.Long
                                )
                            }
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
                        text = "تعداد کارتن ها: " + (uiList.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                    )

                    Text(
                        text = "اسکن شده: " + (uiList.filter { it.isConfirmed }.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1F)
                            .align(Alignment.CenterVertically)
                    )
                }

                Row {
                    SimpleTextField(
                        value = cartonNumber,
                        hint = "شماره کارتن",
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp,
                                top = 12.dp
                            )
                            .fillMaxWidth(),
                        onValueChange = {
                            cartonNumber = it
                        },
                        onDone = {
                            if (cartonNumber.isNotBlank()) {
                                processScannedCartons(cartonNumber, true)
                            }
                        })
                }

                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کارتنی برای ارسال اسکن نکرده اید")
                } else {

                    LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                        items(uiList.size) { i ->

                            Item2(
                                clickable = false,
                                enableBottomSpace = i == uiList.size - 1,
                                text1 = "شناسه عملیاتی : " + uiList[i].driverOperationId,
                                text2 = "تنوع جنس: " + uiList[i].barcodeTable.distinct().size,
                                text3 = "مبدا: " + memory.erpData.warehousesIDsToTitles[uiList[i].operationSource],
                                text4 = "جمع اجناس: " + uiList[i].numberOfItems,
                                text5 = "مقصد: " + if (memory.erpData.warehousesIDsToTitles[uiList[i].operationDes] == null) "-" else memory.erpData.warehousesIDsToTitles[uiList[i].operationDes],
                                text6 = "تاریخ: " + uiList[i].date,
                                text7 = "شرح: " + if (uiList[i].specification == "null") "بدون شرح" else uiList[i].specification,
                                colorFull = uiList[i].isConfirmed,
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
                        CoroutineScope(Dispatchers.Default).launch {
                            if (deliveryCode.toIntOrNull() != null) {
                                if (deliveryCode.toInt() == memory.erpData.drivers[selectedCartonTransferDriverName]) {
                                    syncInputCartonsToServer()
                                    scanningMode = true
                                    if (!barcode.isEnabled) {
                                        barcode.enable()
                                    }
                                } else {
                                    showLog("کد تحویل اشتباه است.", state)
                                    selectedCartonTransferDriverName = ""
                                }
                            } else {
                                showLog("کد تحویل اشتباه است.", state)
                                selectedCartonTransferDriverName = ""
                            }
                            deliveryCode = ""
                        }
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
                    title = "شما کارتن هایی دارید که اسکن کردید ولی نهایی نشده است، ادامه اسکن قبل را ادامه میدهید؟",
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
                        scanningMode = true
                        syncInputCartonsToServer()
                        barcode.scannedBarcodes.clear()
                        barcode.scannedBarcodes.addAll(previousScannedBarcodes)
                        barcode.scannedBarcodes =
                            barcode.scannedBarcodes.distinct().toMutableStateList()
                        previousScannedBarcodes.clear()
                    }) {
                }
            }

            LaunchedEffect(isRefreshStep0) {
                if (isRefreshStep0) {
                    getDraftIDs()
                    isRefreshStep0 = false
                }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshStep0),
                onRefresh = {
                    isRefreshStep0 = true
                }
            ) {

                Column(modifier = Modifier.fillMaxSize()) {

                    LazyColumn(modifier = Modifier.padding(top = 4.dp), state = listState) {

                        items(cartonTransfers.size) { i ->
                            Item4(
                                clickable = true,
                                enableBottomSpace = i == cartonTransfers.size - 1,
                                text1 = "نام راننده: " + cartonTransfers.keys.toList()[i],
                                text2 = ("تعداد کارتن ها: " + cartonTransfers[cartonTransfers.keys.toList()[i]]?.size),
                                text4 = "",
                                text3 = "",
                            ) {

                                if (cartonTransfers.keys.toList()[i] != selectedCartonTransferDriverName) {
                                    selectedCartonTransferDriverName =
                                        cartonTransfers.keys.toList()[i]
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
}