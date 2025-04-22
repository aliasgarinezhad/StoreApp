package com.jeanwest.reader.features.stockDraft.view

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButtonDropDownList
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftDetails : ComponentActivity() {

    lateinit var barcode: Barcode
    val inputProducts = mutableMapOf<String, Product>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var draftProperties = StockDraft(
        number = 0L,
        numberOfItems = 0,
    )

    //ui parameters
    var productConflicts = mutableStateListOf<Product>()
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    private var scanTypeValue by mutableStateOf("RFID")

    @Inject
    lateinit var state: SnackbarHostState
    private var stockDraftNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    private var printer by mutableStateOf("")
    private var openPrintDialog by mutableStateOf(false)
    private var popupState = NotificationPopupHost()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcode = Barcode(this) {
            barcode.scannedBarcodes.clear()
            getWarehouseDetails(it)
        }
        setContent {
            Page()
        }

        printer = memory.erpData.printers.keys.toList()[0]

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
            scanningMode = true
            if (barcode.isEnabled && scanTypeValue == "RFID") {
                barcode.disable()
            }
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
                    uiList.clear()
                    uiList.addAll(inputProducts.values)
                    loading = false
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

    private fun printLabel() {

        api.printStockDraftLabel(
            memory.erpData.printers[printer] ?: 0,
            draftProperties.number.toString(),
            {
                popupState.showPopupWithAButton("دستور پرینت با موفقیت صادر شد.")
                loading = false
            },
            {
                loading = false
            })
    }

    private fun back() {

        if (scanningMode) {
            scanningMode = false
        } else {
            finish()
        }
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
        AppBarWithBack({ back() }, stringResource(id = R.string.stockDraftDetails))
    }


    @Composable
    fun BottomBar() {
        BottomBarButton(text = "چاپ لیبل حواله") {
            openPrintDialog = true
        }
    }

    @ExperimentalCoilApi
    @ExperimentalFoundationApi
    @Composable
    fun Content() {
        Column {
            NotificationPopUp(popupState)
            if (openPrintDialog) {
                AlertDialogWithHeadlineMediumButtonDropDownList(
                    title = "لطفا پرینتر مورد نظر خود را مشخص کنید ",
                    btnTxt = "تایید",
                    btnOnClick = {
                        openPrintDialog = false
                        printLabel()
                    },
                    dropDownText = printer,
                    onDismiss = { openPrintDialog = false },
                    dropDownRes = memory.erpData.printers.keys.toMutableList(),
                    onSelectItem = { printer = it }
                )
            }


            if (loading) {
                LoadingCircularProgressIndicator(false, loading)
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
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "مجموع: ${draftProperties.numberOfItems}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )

                        Text(
                            text = "مبدا: " + memory.erpData.warehousesIDsToTitles[draftProperties.source.toString()],
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(2F),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "تگ RFID: " + if (draftProperties.epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )

                        Text(
                            text = "مقصد: ${memory.erpData.warehousesIDsToTitles[draftProperties.destination.toString()]}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(2F),
                        )
                    }

                }

                LazyColumn {

                    items(uiList.size) { i ->
                        Item(
                            i,
                            uiList,
                            true,
                            text3 = "موجودی: " + uiList[i].draftNumber,
                            text4 = "سایز: " + uiList[i].size
                        ) {
                            val clipboard: ClipboardManager =
                                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("stockDraftId", uiList[i].KBarCode)
                            clipboard.setPrimaryClip(clip)
                            showLog("بارکد کالا کپی شد.", state)
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
}