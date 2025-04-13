@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ShelfExit : ComponentActivity() {

    private var totalScannedProduct by mutableIntStateOf(0)
    private var finalConfirm by mutableStateOf(false)
    private var totalProducts by mutableIntStateOf(0)
    private var currentWarehouse by Delegates.notNull<Int>()
    private var cageNumber by mutableStateOf("")
    var scannedBarcodes = mutableStateListOf<String>()
    var scanningMode by mutableStateOf(false)
    var scannedProducts = mutableMapOf<String, Int>()
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    var products = mutableListOf<Product>()
    var barcodes = mutableListOf<String>()
    lateinit var barcode: Barcode
    private var searchCodesToKBarCodes = mutableMapOf<String, String>()
    private var openShelfExitDialog by mutableStateOf(false)
    private var openShelfExitFocus by mutableStateOf(false)
    private var enterShelfCode by mutableStateOf("")

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
            if (openShelfExitFocus) {
                enterShelfCode = it
            } else if (scanningMode) {
                syncScannedItemWithUi(it)
            } else {
                cageNumber = it
                getShelfProducts()
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

    private fun syncScannedItemWithUi(scannedBarcode: String) {
        loading = true

        val barcode = if (scannedBarcode in searchCodesToKBarCodes.keys) {
            searchCodesToKBarCodes[scannedBarcode]
        } else {
            scannedBarcode
        } ?: ""

        if (barcodes.contains(barcode)) {
            loading = true
            if (scannedProducts.keys.contains(barcode)) {
                scannedProducts[barcode] = (scannedProducts[barcode] ?: 0) + 1
                val temp = mutableListOf<Product>()
                temp.addAll(uiList)
                temp.sortByDescending {
                    it.scannedNumber
                }
                uiList.clear()
                uiList.addAll(temp)
            } else {
                scannedProducts[barcode] = 1
                val temp = mutableListOf<Product>()
                temp.addAll(uiList)
                temp.sortByDescending {
                    it.scannedNumber
                }
                uiList.clear()
                uiList.addAll(temp)
            }
            calculateTotalScanned()
            uiList.forEach {
                if (it.KBarCode == barcode) {
                    it.scannedBarcodeNumber++
                }
            }
            successBeep(state)
            loading = false
        } else {
            errorBeep(state)
            loading = false
            showLog("کالا اسکن شده در موجودی قفسه نمیباشد.", state)
        }
    }

    private fun getShelfProducts() {
        loading = true
        api.shelfContent(currentWarehouse, cageNumber, {
            uiList.clear()
            scannedProducts.clear()
            uiList.addAll(it)
            it.forEach { it1 ->
                barcodes.add(it1.KBarCode)
                it1.searchCodes.forEach { searchCode ->
                    searchCodesToKBarCodes[searchCode] = it1.KBarCode
                }
            }
            calculateTotalProducts()
            calculateTotalScanned()
            loading = false
            scanningMode = true
        }, {
            loading = false
            scanningMode = false
        })
    }

    private fun shelfExit() {
        openShelfExitDialog = true
    }

    private fun exitProductFromshelf() {
        loading = true
        api.shelfExit(
            currentWarehouse,
            cageNumber,
            null,
            scannedProducts,
            {
                loading = false
                showLog(
                    "کالاهای موردنظر با موفقیت از قفسه خارج شد",
                    state,
                    action = SnackBarActions.SUCCESS
                )
                getShelfProducts()
                openShelfExitDialog = false
                openShelfExitFocus = false
            },
            {
                loading = false
            }
        )
        openShelfExitDialog = false
        openShelfExitFocus = false
    }

    private fun addProductToOtherShelf() {
        if (enterShelfCode != "") {
            loading = true
            api.shelfExit(
                currentWarehouse,
                cageNumber,
                enterShelfCode,
                scannedProducts,
                {
                    loading = false
                    openShelfExitDialog = false
                    openShelfExitFocus = false
                    enterShelfCode = ""
                    showLog(
                        "کالاهای موردنظر با موفقیت از قفسه خارج شد و به قفسه جدید اضافه شدند",
                        state,
                        action = SnackBarActions.SUCCESS
                    )
                    getShelfProducts()
                },
                {
                    enterShelfCode = ""
                    loading = false
                })
        } else {
            showLog("شماره قفسه نباید خالی باشد", state)
        }
    }

    private fun calculateTotalScanned() {
        totalScannedProduct = 0
        scannedProducts.values.forEach {
            totalScannedProduct += it
        }
    }

    private fun calculateTotalProducts() {
        totalProducts = 0
        uiList.forEach {
            totalProducts += it.shelfCount
        }
    }

    private fun back() {
        if (scanningMode) {
            products.clear()
            scannedBarcodes.clear()
            barcodes.clear()
            uiList.clear()
            cageNumber = ""
            scanningMode = false
            finalConfirm = false
            totalScannedProduct = 0
            totalProducts = 0
            scannedProducts.clear()
            scannedBarcodes.clear()
            if (!barcode.isEnabled) {
                barcode.enable()
            }
        } else {
            finish()
        }
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
                    text = stringResource(id = R.string.shelf_exit),
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
        if (totalScannedProduct > 0) {
            BottomBarButton(
                text = "خروج"
            ) {
                shelfExit()
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
            } else if (openShelfExitDialog) {
                BasicAlertDialog(
                    onDismissRequest = {
                        exitProductFromshelf()
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = if (!openShelfExitFocus) "آیا میخواهید کالاهای خارج شده وارد قفسه دیگری شود؟" else "شماره قفسه جدید را وارد کنید",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 18.sp
                            )

                            if (openShelfExitFocus) {
                                SimpleTextField(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 16.dp)
                                        .fillMaxWidth()
                                        .testTag("CustomTextField"),
                                    hint = "ورود به قفسه",
                                    onValueChange = {
                                        enterShelfCode = it
                                    },
                                    value = enterShelfCode,
                                    onDone = {
                                        addProductToOtherShelf()
                                    }
                                )
                            }

                            Row(horizontalArrangement = Arrangement.SpaceAround) {

                                Button(
                                    onClick = {
                                        exitProductFromshelf()
                                    }, modifier = Modifier
                                        .padding(top = 10.dp, end = 16.dp)
                                        .testTag("notConfirm")
                                )
                                {
                                    if (!openShelfExitFocus) Text(text = "خیر") else Text(text = "انصراف")
                                }
                                Button(
                                    onClick = {
                                        if (!openShelfExitFocus) {
                                            openShelfExitFocus = true
                                        } else {
                                            addProductToOtherShelf()
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .testTag("confirm")
                                ) {
                                    if (!openShelfExitFocus) Text("بله") else Text("ورود")
                                }
                            }
                        }
                    }
                )
            } else {
                Text(
                    text = "شماره قفسه: $cageNumber",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 16.dp, top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "مجموع: $totalProducts",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = "اسکن: $totalScannedProduct",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                    )
                }

                if (uiList.isEmpty()) {
                    EmptyBox(text = "کالایی در این قفسه وجود ندارد")
                } else {
                    LazyColumn(
                        modifier = Modifier.testTag("RefillActivityLazyColumn")
                    ) {
                        items(uiList.size) { i ->
                            LazyColumnItem(i)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun LazyColumnItem(i: Int) {

        val text: String =
            if ((scannedProducts[uiList[i].KBarCode].toString() == "null") || (scannedProducts[uiList[i].KBarCode] == 0)) {
                ""
            } else {
                scannedProducts[uiList[i].KBarCode].toString()
            }
        Box {
            ItemWithInputText(
                i, uiList, true,
                text = text,
                onTextChange = {
                    if (it.isNotBlank()) {
                        if (it.isDigitsOnly()) {
                            scannedProducts[uiList[i].KBarCode] = Integer.valueOf(it)
                            uiList[i].scannedBarcodeNumber = Integer.valueOf(it)
                        }
                    } else {
                        scannedProducts[uiList[i].KBarCode] = 0
                        uiList[i].scannedBarcodeNumber = 0
                    }
                    calculateTotalScanned()
                },
                onConfirm = {
                },
                signNumber = uiList[i].shelfCount
            )

//            if (uiList[i].scannedNumber > 0 && !finalConfirm) {
//                Box(
//                    modifier = Modifier
//                        .padding(top = topPaddingClearButton, end = 8.dp)
//                        .background(
//                            shape = RoundedCornerShape(36.dp),
//                            color = errorContainerLight
//                        )
//                        .size(30.dp)
//                        .align(Alignment.TopEnd)
//                        .testTag("clear")
//                        .clickable {
//                            clear(uiList[i])
//                        }
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_baseline_clear_24),
//                        contentDescription = "",
//                        tint = errorLight,
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .size(20.dp)
//                    )
//                }
//            }
        }
    }

    @Composable
    fun Content2() {
        ScanOrTypeNumberPage(
            loading = loading,
            onClick = {
                getShelfProducts()
            },
            value = cageNumber,
            onValueChange = { cageNumber = it },
            item = "شماره قفسه"
        )
    }
}