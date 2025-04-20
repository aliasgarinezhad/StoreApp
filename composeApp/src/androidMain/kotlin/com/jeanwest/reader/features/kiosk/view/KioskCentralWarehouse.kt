@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.kiosk.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class KioskCentralWarehouse : ComponentActivity() {

    var uiList = mutableStateListOf<Product>()
    private var storeCode = 0
    private var productColor by mutableStateOf("")
    private var productSize by mutableStateOf("")
    var loading by mutableStateOf(false)
    private var showDetailsMode by mutableStateOf(false)
    lateinit var barcode: Barcode
    lateinit var rf: RFID

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var api: API

    @OptIn(ExperimentalCoilApi::class)
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
            getProductDetails(it)
        }
        run breakForEach@{
            memory.erpData.departmentWarehouses.forEach {
                it.value.forEach { innerIt ->
                    if (innerIt == memory.user.warehouseCode.toString()) {
                        storeCode = it.key.toInt()
                        return@breakForEach
                    }
                }
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
    }

    private fun getProductDetails(barcode: String) {
        loading = true
        showDetailsMode = false
        api.getProductsV5(listOf(memory.user.warehouseCode.toString()), mutableListOf(),
            mutableListOf(barcode), { _, products, _, _ ->
                if (products.isEmpty()) {
                    showLog("بارکد وارد شده نامعتبر است.", state)
                    loading = false
                } else {
                    productColor = products[0].color
                    productSize = products[0].size
                    uiList.add(products[0])
                    getSimilarProductsDetails(barcode)
                }
            }, {
                loading = false
            }
        )
    }

    private fun getSimilarProductsDetails(barcode: String) {
        api.getProductsSimilarCentralWarehouse(storeCode, barcode, { it ->
            val sortedUi = it.sortedWith(compareByDescending {
                it.color == productColor
            })
            uiList.addAll(sortedUi)
            showDetailsMode = true
            loading = false
        }, {
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

    private fun openSearchActivity(product: Product) {

        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
    }

    private fun back() {
        if (showDetailsMode) {
            showDetailsMode = false
        } else {
            finish()
        }
    }

    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
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
                        stringResource(R.string.kioskCurrentWarehouse),
                        textAlign = TextAlign.Center,
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
                Row(
                    modifier = Modifier
                        .shadow(6.dp, Shapes.medium)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        )
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .height(90.dp)
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
                                "قیمت: " + uiList[0].originalPrice,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                            )
                            Text(
                                "قیمت فروش: " + uiList[0].salePrice,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                            )
                        }
                    }
                }
                LazyColumn {

                    items(uiList.size) { i ->
                        Item(
                            i,
                            uiList,
                            text1 = "رنگ: " + uiList[i].color,
                            text2 = "سایز: " + uiList[i].size,
                            text3 = "موجودی: " + uiList[i].wareHouseNumber,
                            text4 = uiList[i].brandName,
                            clickable = true
                        ) {
                            openSearchActivity(uiList[i])
                        }
                    }
                }
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
}