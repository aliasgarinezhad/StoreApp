package com.jeanwest.reader.features.carton.view

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.FilterDropDownList
import com.jeanwest.reader.view.Item4
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.ScanOrTypeNumberPage
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class AddOrRemoveCarton : ComponentActivity() {

    lateinit var barcode: Barcode
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var cartonProperties = Carton(number = "0", numberOfItems = 0)
    private var cartons = mutableStateMapOf<String, Carton>()
    lateinit var rf: RFID
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Carton>()

    @Inject
    lateinit var state: SnackbarHostState
    private var cageNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)
    private var createTypeValue by mutableStateOf("ورود کارتن")
    private var creatingStockDraft by mutableStateOf(false)

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
        barcode = Barcode(this) {
            if (scanningMode) {
                syncScannedItemsToServer()
            } else {
                if (it.startsWith("SH")) {
                    cageNumber = it
                    scanningMode = true
                    barcode.barcode = ""
                    barcode.scannedBarcodes.clear()
                } else {
                    showLog(state = state, data = "شماره قفسه نامعتبر است.")
                    barcode.barcode = ""
                    barcode.scannedBarcodes.clear()
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

    private fun addOrRemoveCartons() {

        loading = true
        val inOut = if (createTypeValue == "ورود کارتن") "RegisterIn" else "RegisterOut"

        api.addOrRemoveCarton(cartons.keys.toList(), cageNumber, inOut, {
            back()
            showLog("$createTypeValue با موفقیت انجام شد.", state, action = SnackBarActions.SUCCESS)
            loading = false
        }, {
            loading = false
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

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun clear(carton: Carton) {
        barcode.scannedBarcodes.remove(carton.number)
        cartons.remove(carton.number)
        uiList.clear()
        uiList.addAll(cartons.values)
    }

    private fun clear() {

        inputBarcodeMapWithProperties.clear()
        cartonProperties = Carton()
        uiList.clear()
        cageNumber = ""
        barcode.scannedBarcodes.clear()
        cartons.clear()
    }

    private fun syncScannedItemsToServer() {

        loading = true

        var cartonCode = ""
        run breakForEach@{
            barcode.scannedBarcodes.forEach {
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
            return
        }

        api.getCartonsDetails(listOf(cartonCode), { carton ->

            if (carton.isNotEmpty()) {

                if (carton[0].cartonSource.toIntOrNull() == memory.user.warehouseCode) {
                    cartons[cartonCode] = carton[0]
                    uiList.clear()
                    uiList.addAll(cartons.values)
                    successBeep(state)
                } else {
                    errorBeep(state)
                    showLog("کارتن در انبار جاری نیست.", state)
                    barcode.scannedBarcodes.remove(cartonCode)
                }
                syncScannedItemsToServer()

            } else {
                errorBeep(state)
                barcode.scannedBarcodes.remove(cartonCode)
                loading = false
            }
        }, {
            errorBeep(state)
            barcode.scannedBarcodes.remove(cartonCode)
            loading = false
        })
    }

    private fun back() {
        if (scanningMode) {
            clear()
            scanningMode = false
            if (!barcode.isEnabled) {
                barcode.enable()
            }
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
        AppBarWithBack({ back() }, stringResource(id = R.string.addOrRemoveCarton))
    }

    @Composable
    fun BottomBar() {
        if (scanningMode && uiList.isNotEmpty()) {
            BottomBarButton(
                text = if (creatingStockDraft) "در حال ثبت ..." else if (createTypeValue == "ورود کارتن") {
                    "ورود کارتن"
                } else "خروج کارتن"
            ) {
                if (!creatingStockDraft) {
                    addOrRemoveCartons()
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
                        text = "قفسه: $cageNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1.5F)
                    )

                    Text(
                        text = "مجموع: " + (uiList.size).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1F)
                    )

                    Row(modifier = Modifier.weight(1.5F)) {
                        FilterDropDownList(
                            icon = {},
                            text = {
                                Text(
                                    text = createTypeValue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 16.dp)
                                )
                            },
                            onClick = {
                                createTypeValue = it
                            },
                            values = mutableListOf("ورود کارتن", "خروج کارتن")
                        )
                    }
                }

                if (uiList.isEmpty()) {
                    EmptyBox("هنوز کارتنی اسکن نکرده اید")
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
                        shape = RoundedCornerShape(36.dp), color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .testTag("clear")
                    .clickable {
                        clear(uiList[i])
                    }) {
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
        ScanOrTypeNumberPage(
            loading = loading, onClick = {
            if (!cageNumber.startsWith("SH")) {
                showLog(state = state, data = "شماره قفسه نامعتبر است.")
                barcode.barcode = ""
                barcode.scannedBarcodes.clear()
            } else {
                scanningMode = true
                barcode.barcode = ""
                barcode.scannedBarcodes.clear()
            }
        }, value = cageNumber, onValueChange = { cageNumber = it }, item = "شماره قفسه"
        )
    }
}