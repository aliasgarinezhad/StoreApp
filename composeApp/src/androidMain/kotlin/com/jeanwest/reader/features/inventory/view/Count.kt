package com.jeanwest.reader.features.inventory.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.PowerSlider
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
@ExperimentalFoundationApi
class Count : ComponentActivity() {

    lateinit var rf: RFID
    lateinit var barcode: Barcode
    val scannedProducts = mutableMapOf<String, Product>()
    private var scannedEpcMapWithProperties = mutableMapOf<String, Product>()
    private var scannedBarcodeMapWithProperties = mutableMapOf<String, Product>()

    //ui parameters
    var uiList = mutableStateMapOf<String, Product>()
    var loading by mutableStateOf(false)
    private var filteredUiList = mutableStateListOf<Product>()
    private var scanFilter by mutableStateOf("اضافی")
    private var signedFilter by mutableStateOf("همه")
    private var signedProductCodes = mutableListOf<String>()
    private var openClearDialog by mutableStateOf(false)
    private var scanTypeValue by mutableStateOf("RFID")

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var api: API

    @Inject
    lateinit var memory: SharedPreference

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
            syncScannedItemToServer()
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
        if (scanTypeValue == "RFID") {
            barcode.disable()
        }
    }

    private fun calculateConflicts() {

        val conflicts = mutableMapOf<String, Product>()

        conflicts.putAll(scannedProducts)

        uiList.clear()
        uiList.putAll(conflicts)
        filterUiList()
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


    private fun filterUiList() {

        val signedFilterOutput =
            when (signedFilter) {
                "همه" -> {
                    uiList.values
                }

                "نشانه دار" -> {
                    uiList.values.filter {
                        it.KBarCode in signedProductCodes
                    } as MutableList<Product>
                }

                "بی نشانه" -> {
                    uiList.values.filter {
                        it.KBarCode !in signedProductCodes
                    } as MutableList<Product>
                }

                else -> {
                    uiList.values
                }
            }

        val uiListParameters = signedFilterOutput.filter {
            it.conflictType == scanFilter
        } as MutableList<Product>

        uiListParameters.sortBy {
            it.productCode
        }
        uiListParameters.sortBy {
            it.name
        }

        filteredUiList.clear()
        filteredUiList.addAll(uiListParameters)
    }

    private fun syncScannedItemsToServer() {

        loading = true
        if (rf.epcs.isEmpty() && barcode.scannedBarcodes.isEmpty()) {
            calculateConflicts()
            loading = false
            return
        }

        val epcTableForV4 = mutableListOf<String>()
        val barcodeTableForV4 = mutableListOf<String>()
        var scannedProductsBiggerThan1000 = false

        run breakForEach@{
            rf.epcs.forEach {
                if (it !in scannedEpcMapWithProperties.keys) {
                    if (epcTableForV4.size < 1000) {
                        epcTableForV4.add(it)
                    } else {
                        scannedProductsBiggerThan1000 = true

                        return@breakForEach
                    }
                }
            }
        }
        run breakForEach@{
            barcode.scannedBarcodes.distinct().forEach {
                if (it !in scannedBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 1000) {
                        barcodeTableForV4.add(it)
                    } else {
                        scannedProductsBiggerThan1000 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (epcTableForV4.size == 0 && barcodeTableForV4.size == 0) {

            makeScannedProductMap()
            calculateConflicts()
            loading = false
            return
        }

        api.getItemDetailsAndInventory(
            epcTableForV4,
            barcodeTableForV4,
            { epcs, barcodes, invalidEpcs, invalidBarcodes ->

                epcs.forEach { product ->
                    scannedEpcMapWithProperties[product.scannedEPCs[0]] = product
                }

                barcodes.forEach { product ->
                    scannedBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                for (i in 0 until invalidBarcodes.length()) {
                    barcode.scannedBarcodes.remove(invalidBarcodes[i])
                }
                for (i in 0 until invalidEpcs.length()) {
                    rf.epcs.remove(invalidEpcs[i])
                }

                if (scannedProductsBiggerThan1000) {
                    syncScannedItemsToServer()
                } else {
                    makeScannedProductMap()
                    calculateConflicts()
                    loading = false
                }

            },
            {
                calculateConflicts()
                loading = false
            })
    }

    private fun makeScannedProductMap() {

        scannedProducts.clear()

        rf.epcs.forEach {

            val product = scannedEpcMapWithProperties[it]!!

            if (product.KBarCode in scannedProducts.keys) {
                scannedProducts[product.KBarCode]!!.scannedEPCs.add(it)
            } else {
                scannedProducts[product.KBarCode] = product.copy(scannedEPCs = mutableListOf(it))
                scannedProducts[product.KBarCode]!!.scannedEPCs = mutableListOf(it)
            }
        }

        barcode.scannedBarcodes.distinct().forEach {
            val product = scannedBarcodeMapWithProperties[it]!!.copy()
            product.scannedBarcodeNumber = barcode.scannedBarcodes.count { it1 ->
                it == it1
            }

            if (product.KBarCode !in scannedProducts.keys) {
                scannedProducts[product.KBarCode] = product.copy()
            } else {
                scannedProducts[product.KBarCode]!!.scannedBarcodeNumber += product.scannedBarcodeNumber
            }
        }
    }

    private fun syncScannedItemToServer() {

        loading = true

        if (barcode.barcode in scannedBarcodeMapWithProperties.keys) {
            successBeep(state)
            makeScannedProductMap()
            calculateConflicts()
            loading = false
            return
        }

        api.getItemDetailsAndInventory(
            mutableListOf(),
            mutableListOf(barcode.barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size == 1) {
                    successBeep(state)
                    scannedBarcodeMapWithProperties[barcodes[0].scannedBarcode] = barcodes[0]
                    makeScannedProductMap()
                    calculateConflicts()
                } else {
                    barcode.scannedBarcodes.remove(barcode.barcode)
                }
                loading = false
            },
            {
                calculateConflicts()
                loading = false
            })
    }

    private fun clear() {

        barcode.scannedBarcodes.clear()
        rf.epcs.clear()
        scannedBarcodeMapWithProperties.clear()
        scannedEpcMapWithProperties.clear()
        scannedProducts.clear()
        calculateConflicts()
    }

    private fun back() {
        rf.stopScanning()
        finish()
    }

    private fun openSearchActivity(product: Product) {
        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
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

            actions = {
                IconButton(
                    modifier = Modifier.testTag("CountActivityClearButton"),
                    onClick = {
                        if (!rf.scanning && !loading) {
                            openClearDialog = true
                        }
                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                        contentDescription = ""
                    )
                }
            },

            title = {
                Text(
                    text = "شمارش",
                    modifier = Modifier
                        .padding(start = 10.dp)
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

            if (openClearDialog) {
                AlertDialogWith2Button(
                    title = "کالاهای اسکن شده پاک شوند؟",
                    btnConfirm = "بله",
                    btnNotConfirm = "خیر",
                    btnConfirmOnClick = {
                        openClearDialog = false
                        clear()
                    },
                    btnNotConfirmOnClick = {
                        openClearDialog = false
                    },
                    onDismiss = { openClearDialog = false }
                )
            }

            if (rf.scanning || loading) {
                LoadingCircularProgressIndicator(rf.scanning, loading)
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
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {

                        Text(
                            text = "اسکن: ${barcode.scannedBarcodes.size + rf.epcs.size}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                                .weight(1F),
                        )
                    }

                    PowerSlider(scanTypeValue == "RFID", rf.scanningPower) {
                        rf.scanningPower = it
                    }
                }

                LazyColumn {

                    items(filteredUiList.size) { i ->
                        Item(
                            i,
                            filteredUiList,
                            true,
                            text3 = "رنگ: " + filteredUiList[i].color,
                            text4 = "اسکن: " + filteredUiList[i].scannedNumber,
                            filteredUiList[i].KBarCode in signedProductCodes,
                            onLongClick = {
                                if (filteredUiList[i].KBarCode !in signedProductCodes) {
                                    signedProductCodes.add(filteredUiList[i].KBarCode)
                                } else {
                                    signedProductCodes.remove(filteredUiList[i].KBarCode)
                                }
                                filterUiList()
                            }, onClick = {
                                openSearchActivity(filteredUiList[i])
                            }
                        )
                    }
                }
            }
        }
    }
}