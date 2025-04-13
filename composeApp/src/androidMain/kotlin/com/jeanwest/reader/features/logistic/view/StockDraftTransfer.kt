@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.logistic.view

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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
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
class StockDraftTransfer : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)
    var stockDraftUiList = mutableStateListOf<StockDraft>()

    @Inject
    lateinit var state: SnackbarHostState
    private var stockDraftNumber by mutableStateOf("")
    private val listState = LazyListState(0)
    var stockDraftIDs = mutableListOf<String>()
    private var draftsMap = mutableStateMapOf<String, StockDraft>()
    private var creatingStockDraft by mutableStateOf(false)
    private var driver by mutableStateOf("انتخاب راننده")
    private var openSendDialog by mutableStateOf(false)
    private var openClearDialog by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()
        getStockDraftsDetails()
        exceptionHandler()
        setContent {
            Page()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }

        barcode = Barcode(this) { scannedBarcode ->

            if (scannedBarcode !in stockDraftIDs) {
                stockDraftIDs.add(scannedBarcode)
            } else {
                CoroutineScope(Dispatchers.Default).launch {
                    state.showSnackbar(
                        "حواله قبلا اسکن شده است.",
                        null,
                        duration = SnackbarDuration.Long
                    )
                    errorBeep(state)
                }
            }
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
    }

    private fun getStockDraftsDetails() {

        loading = true
        var stockDraftID = "0"

        run breakForEach@{
            stockDraftIDs.forEach {
                if (it !in draftsMap.keys) {
                    stockDraftID = it
                    return@breakForEach
                }
            }
        }

        if (stockDraftID == "0") {
            stockDraftUiList.clear()
            stockDraftUiList.addAll(draftsMap.values)
            stockDraftUiList.sortByDescending { stockDraft ->
                stockDraftIDs.indexOf(stockDraft.number.toString())
            }
            loading = false
            return
        }

        api.getStockDraftDetails(stockDraftID, {

            if (it.stateID == "9") {
                stockDraftIDs.remove(stockDraftID)
                showLog("حواله ابطال شده است", state)
                stockDraftNumber = ""
                errorBeep(state)
            } else if (it.stateID == "2") {
                stockDraftIDs.remove(stockDraftID)
                showLog("حواله نهایی شده است", state)
                stockDraftNumber = ""
                errorBeep(state)
            } else if (it.logisticKey != "null") {
                stockDraftIDs.remove(stockDraftID)
                showLog("حواله قبلا به راننده تحویل داده شده است", state)
                stockDraftNumber = ""
                errorBeep(state)
            } else {
                draftsMap[it.number.toString()] = it
                successBeep(state)
            }
            getStockDraftsDetails()
            saveToMemory()
        }, {
            stockDraftIDs.remove(stockDraftID)
            saveToMemory()
            errorBeep(state)
            loading = false
        })
    }

    private fun transferStockDrafts() {
        creatingStockDraft = true

        api.stockDraftTransfer(
            memory.erpData.drivers[driver].toString(),
            stockDraftUiList[0].number.toString(),
            {
                clear(stockDraftUiList[0].number.toString())
                if (stockDraftUiList.isNotEmpty()) {
                    transferStockDrafts()
                    saveToMemory()
                    openSendDialog = false
                } else {
                    saveToMemory()
                    creatingStockDraft = false
                    openSendDialog = true
                }
            }, {
                creatingStockDraft = false
                openSendDialog = false
            }
        )
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

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString("stockDraftIDs", JSONArray(stockDraftIDs).toString())
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        stockDraftIDs = Gson().fromJson(
            memory.getString("stockDraftIDs", ""),
            stockDraftIDs.javaClass
        ) ?: mutableListOf()
    }

    private fun clear() {
        stockDraftIDs.clear()
        stockDraftUiList.clear()
        draftsMap.clear()

        saveToMemory()
    }

    fun clear(stockDraftID: String) {
        stockDraftIDs.remove(stockDraftID)
        stockDraftUiList.removeAll {
            it.number.toString() == stockDraftID
        }
        draftsMap.remove(stockDraftID)
        saveToMemory()
    }

    private fun back() {
        saveToMemory()
        finish()
    }


    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
    @ExperimentalFoundationApi
    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { AppBar() },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            Content2()
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
        AppBarWithDeleteButton(title = stringResource(id = R.string.stockDraftTransfer),
            onDeletePressed = { openClearDialog = true },
            onBackPressed = { back() }
        )
    }

    @Composable
    fun BottomBar() {

        if (!loading && stockDraftUiList.isNotEmpty()) {

            BottomBarButton(text = if (creatingStockDraft) "در حال ثبت ..." else " تعداد ${stockDraftUiList.size} حواله ثبت نهایی شوند") {

                if (driver == "انتخاب راننده") {
                    showLog("لطفا راننده را انتخاب کنید", state)
                } else {
                    transferStockDrafts()
                }

            }
        }
    }

    @Composable
    fun Content2() {

        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = loading)
        } else {

            if (openClearDialog) {
                AlertDialogWith2Button(
                    "حواله های اسکن شده پاک شوند؟",
                    "بله",
                    "خیر",
                    btnConfirmOnClick = {
                        clear()
                        openClearDialog = false
                    },
                    btnNotConfirmOnClick = { openClearDialog = false },
                    onDismiss = { openClearDialog = false })
            }

            if (openSendDialog) {

                BasicAlertDialog(
                    onDismissRequest = {
                        loading = false
                        creatingStockDraft = false
                        clear()
                        openSendDialog = false
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 120.dp)
                                .wrapContentHeight()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {

                            Text(
                                text = " تعداد ${stockDraftUiList.size} حواله با $driver و با موفقیت ارسال شد.",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Button(
                                onClick = {
                                    openSendDialog = false
                                    loading = false
                                    creatingStockDraft = false
                                    clear()
                                },
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("alertBtn")
                            ) {
                                Text(text = "باشه", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                )
            }

            Column {

                Column(modifier = Modifier.fillMaxSize()) {

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
                                        if (stockDraftNumber !in stockDraftIDs) {
                                            stockDraftIDs.add(stockDraftNumber)
                                        } else {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                state.showSnackbar(
                                                    "حواله قبلا اسکن شده است.",
                                                    null,
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                        getStockDraftsDetails()
                                    }
                                })
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1.5F),
                            ) {
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

                            Text(
                                text = "مجموع: " + stockDraftUiList.size,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .weight(1F)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }

                    if (stockDraftUiList.isEmpty()) {
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
                                    style = Typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(
                                        top = 16.dp,
                                        start = 4.dp,
                                        end = 4.dp
                                    ),
                                )
                            }
                        }
                    } else {

                        LazyColumn(modifier = Modifier.padding(top = 4.dp), state = listState) {

                            items(stockDraftUiList.size) { i ->

                                Box {

                                    Item2(
                                        clickable = false,
                                        enableBottomSpace = i == stockDraftUiList.size - 1,
                                        text1 = "حواله: " + stockDraftUiList[i].number,
                                        text2 = "تاریخ: " + stockDraftUiList[i].date,
                                        text3 = "از: " + memory.erpData.warehousesIDsToTitles[stockDraftUiList[i].source.toString()],
                                        text4 = "تعداد کالاها: " + stockDraftUiList[i].numberOfItems,
                                        text5 = "به: " + memory.erpData.warehousesIDsToTitles[stockDraftUiList[i].destination.toString()],
                                        text6 = "تگ RFID: " + if (stockDraftUiList[i].epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد",
                                        text7 = "شرح: " + stockDraftUiList[i].specification,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp, end = 8.dp)
                                            .background(
                                                shape = RoundedCornerShape(36.dp),
                                                color = errorContainerLight
                                            )
                                            .size(30.dp)
                                            .align(Alignment.TopEnd)
                                            .testTag("clear")
                                            .clickable {
                                                clear(stockDraftUiList[i].number.toString())
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
                    }
                }
            }
        }
    }
}