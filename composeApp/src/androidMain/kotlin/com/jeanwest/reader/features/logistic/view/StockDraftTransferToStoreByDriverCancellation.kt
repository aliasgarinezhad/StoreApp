@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.logistic.view

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AppBarWithDeleteButton
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item2
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.ic_sack
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftTransferToStoreByDriverCancellation : ComponentActivity() {

    lateinit var barcode: Barcode
    lateinit var rf: RFID
    var loading by mutableStateOf(false)
    private var stockDraftUiList = mutableStateListOf<StockDraft>()
    private var vehiclesList = mutableMapOf<String, Int>()
    private var stockDraftNumber by mutableStateOf("")
    private val listState = LazyListState(0)
    private var stockDraftIDs = mutableListOf<String>()
    private var draftsMap = mutableStateMapOf<String, StockDraft>()
    private var creatingStockDraft by mutableStateOf(false)
    private var openCancelingDialog by mutableStateOf(false)
    private var openClearDialog by mutableStateOf(false)

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
        setContent {
            Page()
        }
    }

    private fun init() {
        rf = RFID(this, state) {
            scanTrigger()
        }
        barcode = Barcode(this) {
            getStockDraftsDetails(it)
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
        loadMemory()
    }

    private fun getStockDraftsDetails(barcode: String) {

        if (barcode.uppercase(Locale.getDefault()).startsWith("GN")) {
            loading = true
            api.getSacksDetails(barcode.substring(2), {
                for (i in 0 until it.size) {
                    if (it[i].stateID == "9" && it[i].destination != 36) {
                        showLog("حواله ابطال شده است", state)
                        stockDraftIDs.remove(barcode)
                        stockDraftNumber = ""
                        errorBeep(state)
                    } else if (it[i].stateID == "2" && it[i].destination != 36) {
                        showLog("حواله نهایی شده است", state)
                        stockDraftIDs.remove(barcode)
                        stockDraftNumber = ""
                        errorBeep(state)
                    } else {
                        if (it[i].number.toString() !in stockDraftIDs) {
                            stockDraftIDs.add(it[i].number.toString())
                            stockDraftUiList.add(it[i])
                            stockDraftNumber = ""
                            saveToMemory()
                            loading = false
                            successBeep(state)
                        } else {
                            loading = false
                            showLog("حواله قبلا اسکن شده است.", state)
                            stockDraftNumber = ""
                            errorBeep(state)
                        }
                    }
                    loading = false
                }
                loading = false
            }, {
                loading = false
                showLog("شماره گونی وارد شده معتبر نیست.", state)
                stockDraftNumber = ""
                errorBeep(state)
            })
        } else {
            if (barcode.isDigitsOnly()) {
                if (barcode !in stockDraftIDs) {
                    loading = true
                    api.getStockDraftDetails(barcode, {
                        if (it.stateID == "9" && it.destination != 36) {
                            showLog("حواله ابطال شده است", state)
                            stockDraftIDs.remove(barcode)
                            stockDraftNumber = ""
                            errorBeep(state)
                        } else {
                            stockDraftIDs.add(it.number.toString())
                            stockDraftUiList.add(it)
                            stockDraftNumber = ""
                            successBeep(state)
                        }
                        saveToMemory()
                        loading = false
                    }, {
                        errorBeep(state)
                        saveToMemory()
                        loading = false
                    })
                } else {
                    loading = false
                    showLog("حواله قبلا اسکن شده است.", state)
                    stockDraftNumber = ""
                    errorBeep(state)
                }
            } else {
                showLog("فرمت حواله وارد شده نامعتبر است.", state)
                errorBeep(state)
            }
        }
    }

    private fun transferStockDrafts() {
        val stockDraftIds = ArrayList<Long>()
        for (i in 0 until stockDraftUiList.size) {
            stockDraftIds.add(stockDraftUiList[i].number)
        }
        loading = true
        creatingStockDraft = true
        api.returnLogistics(stockDraftIds, {
            loading = false
            creatingStockDraft = false
            openCancelingDialog = true
        }, {
            loading = false
            creatingStockDraft = false
            openCancelingDialog = false
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    private fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()
        edit.putString(
            "ReturnLogisticStockDraftsBarcodeTable",
            Gson().toJson(stockDraftUiList).toString()
        )
        edit.apply()
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val type = object : TypeToken<List<StockDraft>>() {}.type
        val temp = mutableStateListOf<StockDraft>()
        temp.addAll(
            (
                    Gson().fromJson(
                        memory.getString("ReturnLogisticStockDraftsBarcodeTable", ""),
                        type
                    ) ?: mutableStateListOf()
                    )
        )
        stockDraftUiList.clear()
        stockDraftUiList.addAll(temp)
        temp.forEach {
            stockDraftIDs.add(it.number.toString())
        }
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun clear(stockDraftID: String) {
        stockDraftIDs.remove(stockDraftID)
        stockDraftUiList.removeAll {
            it.number.toString() == stockDraftID
        }
        draftsMap.remove(stockDraftID)
        saveToMemory()
    }

    fun clear() {
        barcode.scannedBarcodes.clear()
        stockDraftIDs.clear()
        stockDraftUiList.clear()
        draftsMap.clear()
        stockDraftNumber = ""
        saveToMemory()
    }

    private fun back() {
        finish()
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
                            Content()
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
        AppBarWithDeleteButton(
            title = stringResource(id = R.string.ReturnLogistics),
            onDeletePressed = { openClearDialog = true },
            onBackPressed = { back() }
        )
    }

    @Composable
    fun BottomBar() {
        if (!loading && stockDraftUiList.isNotEmpty()) {
            BottomBarButton(text = if (creatingStockDraft) "در حال ثبت ..." else " تعداد ${stockDraftUiList.size} حواله برگشت داده شوند.") {
                transferStockDrafts()
            }
        }
    }

    @Preview
    @Composable
    fun Content() {
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

            if (openCancelingDialog) {
                BasicAlertDialog(
                    onDismissRequest = {
                        loading = false
                        creatingStockDraft = false
                        openCancelingDialog = false
                        clear()
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {

                            Text(
                                text = "تعداد ${stockDraftUiList.size} حواله با موفقیت برگشت زده شد.",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Button(
                                onClick = {
                                    loading = false
                                    creatingStockDraft = false
                                    openCancelingDialog = false
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
                                        getStockDraftsDetails(stockDraftNumber)
                                    }
                                })
                        }

                        if (stockDraftUiList.isEmpty()) {
                            EmptyBox(text = "هنوز حواله ای برای لغو تحویل اسکن نکرده اید")
                        } else {

                            LazyColumn(
                                modifier = Modifier.padding(top = 4.dp),
                                state = listState
                            ) {

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
                                            text6 = "راننده: " + stockDraftUiList[i].driver,
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
                                        if (stockDraftUiList[i].isTwoShel) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 34.dp, end = 8.dp)
                                                    .background(
                                                        shape = RoundedCornerShape(36.dp),
                                                        color = onPrimaryLight
                                                    )
                                                    .size(30.dp)
                                                    .align(Alignment.TopEnd)
                                                    .testTag("sackIc")
                                                    .clickable {
                                                        clear(stockDraftUiList[i].number.toString())
                                                    }
                                            ) {
                                                Icon(
                                                    painter = org.jetbrains.compose.resources.painterResource(
                                                        Res.drawable.ic_sack),
                                                    contentDescription = "",
                                                    tint = Color.Blue,
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
    }
}