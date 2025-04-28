@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.ItemWithInputText
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ShelfEnter : ComponentActivity() {

    private var totalProducts by mutableIntStateOf(0)
    private var currentWarehouse by Delegates.notNull<Int>()
    private var cageNumber by mutableStateOf("")
    var scannedProducts = mutableMapOf<String, Int>()
    var scanningMode by mutableStateOf(false)
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    var products = mutableListOf<Product>()
    lateinit var barcode: Barcode
    private var typedProductBarcode by mutableStateOf("")

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var state: SnackbarHostState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
        setContent {
            Page()
        }
    }

    private fun init() {
        currentWarehouse = memory.user.warehouseCode
        barcode = Barcode(this) {
            if (scanningMode) {
                syncScannedItemToServer(it)
            } else {
                if (it.startsWith("SH")) {
                    scanningMode = true
                    barcode.scannedBarcodes.clear()
                    cageNumber = it
                } else {
                    barcode.scannedBarcodes.clear()
                    showLog("شماره قفسه نامعتبر است", state)
                }
            }
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
        if (scanningMode) {
            barcode.disable()
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
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

    fun clear(product: Product) {
        scannedProducts.remove(product.scannedBarcode)
        uiList.remove(product)
        val temp = mutableListOf<Product>()
        temp.addAll(uiList)
        uiList.clear()
        uiList.addAll(temp)
        calculateTotalScanned()
    }

    private fun shelfEnter() {
        loading = true

        CoroutineScope(Dispatchers.Default).launch {

            val apiProducts = mutableMapOf<String, Int>()

            scannedProducts.forEach { scannedProduct ->

                val apiProduct = uiList.find { uiListProduct ->
                    uiListProduct.scannedBarcode == scannedProduct.key
                }

                if (apiProduct == null) {
                    showLog("مشکلی در پردازش اطلاعات ارسالی به وجود آمده است.", state)
                } else {
                    apiProducts[apiProduct.KBarCode] = apiProduct.scannedBarcodeNumber
                }
            }

            api.shelfEntry(currentWarehouse, cageNumber, apiProducts.toMap(), {
                loading = false
                clearAll()
                showLog(
                    "اجناس اسکن شده با موفقیت در قفسه ثبت شدند.",
                    state,
                    action = SnackBarActions.SUCCESS
                )
            }, {
                loading = false
            })
        }
    }

    private fun clearAll() {
        barcode.scannedBarcodes.clear()
        scannedProducts.clear()
        scanningMode = false
        products.clear()
        uiList.clear()
        calculateTotalScanned()
    }

    private fun calculateTotalScanned() {
        totalProducts = 0
        scannedProducts.values.forEach {
            totalProducts += it
        }
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        if (scannedProducts.keys.contains(barcode)) {
            successBeep(state)
            scannedProducts[barcode] = scannedProducts.getValue(barcode).plus(1)
            calculateTotalScanned()
            uiList.forEach {
                if (it.scannedBarcode == barcode) {
                    it.scannedBarcodeNumber++
                    val temp = mutableListOf<Product>()
                    temp.addAll(uiList)
                    uiList.clear()
                    uiList.addAll(temp)
                }
            }
            loading = false
        } else {
            api.getItemDetails(
                mutableListOf(),
                mutableListOf(barcode),
                { _, barcodes, _, _ ->
                    if (barcodes.size == 1) {
                        successBeep(state)
                        barcodes[0].scannedBarcodeNumber++
                        uiList.add(barcodes[0])
                        scannedProducts[barcodes[0].scannedBarcode] = 1
                        calculateTotalScanned()
                    } else {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(barcode)
                    }
                    loading = false
                },
                {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                    showLog("مشخصات کالا یافت نشد", state)
                    loading = false
                })
        }
    }

    private fun back() {
        if (scanningMode) {
            if (!barcode.isEnabled) {
                barcode.enable()
            }
            clearAll()
        } else {
            finish()
        }
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
                    text = stringResource(id = R.string.shelf_enter),
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
        if (uiList.isNotEmpty()) {
            BottomBarButton(
                text = "بررسی و ارسال"
            ) {
                shelfEnter()
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
                SimpleTextField(
                    value = typedProductBarcode,
                    hint = "بارکد کالا",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 6.dp,
                            top = 12.dp
                        )
                        .fillMaxWidth(),
                    onValueChange = {
                        typedProductBarcode = it
                    },
                    onDone = {
                        if (typedProductBarcode != "") {
                            syncScannedItemToServer(typedProductBarcode)
                            typedProductBarcode = ""
                        } else {
                            showLog("حواله وارد شده نامعتبر است.", state = state)
                        }
                    },
                    keyboardType = KeyboardType.Text
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
                ) {
                    Text(
                        text = "شماره قفسه: $cageNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        text = "مجموع: $totalProducts",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 30.dp)
                    )
                }

                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کالایی برای ورود به قفسه اسکن نکرده اید")
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
        val text: String =
            if ((scannedProducts[uiList[i].scannedBarcode].toString() == "null") || (scannedProducts[uiList[i].scannedBarcode] == 0)) {
                ""
            } else {
                scannedProducts[uiList[i].scannedBarcode].toString()
            }

        Box {
            ItemWithInputText(
                i, uiList, true,
                text = text,
                onTextChange = {
                    if (it.isNotBlank()) {
                        if (it.isDigitsOnly()) {
                            scannedProducts[uiList[i].scannedBarcode] = Integer.valueOf(it)
                            uiList[i].scannedBarcodeNumber = Integer.valueOf(it)
                        }
                    } else {
                        scannedProducts[uiList[i].scannedBarcode] = 0
                        uiList[i].scannedBarcodeNumber = 0
                    }
                    calculateTotalScanned()
                },
                onConfirm = {
                }
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
        clearAll()
        ScanOrTypeNumberPage(
            loading = loading,
            onClick = {
                if (cageNumber.startsWith("SH")) {
                    scanningMode = true
                    barcode.scannedBarcodes.clear()
                } else {
                    barcode.scannedBarcodes.clear()
                    showLog("شماره قفسه نامعتبر است", state)
                }
            },
            value = cageNumber,
            onValueChange = { cageNumber = it },
            item = "شماره قفسه"
        )
    }
}