package com.jeanwest.reader.features.kiosk.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.ScanOrTypeNumberPage
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
class BarcodeChecker : ComponentActivity() {

    private var productCode by mutableStateOf("")
    var uiList = mutableStateListOf<Product>()

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API
    var loading by mutableStateOf(false)
    private var showDetailsMode by mutableStateOf(false)
    lateinit var barcode: Barcode
    private var warehousesMustBeChecked = mutableListOf<String>()
    var productDepartmentNumbers = mutableStateMapOf<String, Int>()
    lateinit var rf: RFID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            Page()
        }
        exceptionHandler()
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
            getProductDetails(it)
        }

        warehousesMustBeChecked.addAll(memory.erpData.warehousesIDsToTitles.keys)

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

    private fun getProductDetails(barcode: String) {
        loading = true
        uiList.clear()
        uiList.clear()
        productCode = ""
        productDepartmentNumbers.clear()

        api.getProductsV5(
            warehousesMustBeChecked,
            mutableListOf(),
            mutableListOf(barcode),
            { _, it, _, _ ->
                if (it.size >= 1) {
                    uiList.addAll(it)
                    productCode = uiList[0].productCode

                    memory.erpData.departmentWarehouses.forEach { department ->
                        var number = 0
                        uiList.forEach { product ->
                            if (product.warehouseCode in department.value) {
                                number += product.wareHouseNumber
                            }
                        }
                        productDepartmentNumbers[department.key] = number
                    }

                    showDetailsMode = true
                }
                loading = false
            },
            {
                loading = false
            })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == 4) {
            back()
        }
        return true
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    private fun back() {
        if (showDetailsMode) {
            showDetailsMode = false
        } else {
            finish()
        }
    }
    
    @ExperimentalCoilApi
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
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 0.dp, end = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.barcodeChecker), textAlign = TextAlign.Center,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { back() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = ""
                    )
                }
            }
        )
    }

    @Composable
    fun Content() {

        var barcode by rememberSaveable {
            mutableStateOf("")
        }

        if (loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(false, loading)
            }
        } else if (showDetailsMode) {

            Column(modifier = Modifier.fillMaxSize()) {

                LazyColumnItem()
            }

        } else {

            ScanOrTypeNumberPage(
                loading = loading,
                onClick = {
                    getProductDetails(barcode)
                },
                value = barcode,
                onValueChange = {
                    barcode = it
                },
                item = "بارکد محصول"
            )
        }
    }

    @Composable
    fun LazyColumnItem() {
        Column {
            Column(
                modifier = Modifier
                    .height(290.dp)
                    .fillMaxWidth()
                    .shadow(6.dp, Shapes.medium)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(5.dp)
            ) {

                Row(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uiList[0].imageUrl),
                        contentDescription = "",
                        modifier = Modifier
                            .height(200.dp)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            uiList[0].name,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )

                        Text(
                            "کد فرعی: " + uiList[0].productCode,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "سایز: " + uiList[0].size,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                        Text(
                            "رنگ: " + uiList[0].color,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                    }
                }

            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {

                    productDepartmentNumbers.forEach {

                        Text(
                            text = memory.erpData.departments[it.key] + ": " + it.value,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                .shadow(4.dp, Shapes.medium)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
                                .fillMaxWidth()
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .height(128.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
