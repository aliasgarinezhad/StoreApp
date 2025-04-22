@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.refillStore.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.android.volley.NoConnectionError
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * This activity allows users to manually add items to a refill list.
 *
 * Users can scan a product barcode or manually enter the product code */
@AndroidEntryPoint
class RefillManualAddItems : ComponentActivity() {

    private var productCode by mutableStateOf("")
    private var uiList = mutableStateListOf<Product>()
    private var filteredUiList = mutableStateListOf<Product>()
    private var colorFilterValues = mutableStateListOf("همه رنگ ها")
    private var sizeFilterValues = mutableStateListOf("همه سایز ها")
    private lateinit var barcode: Barcode
    private var colorFilterValue by mutableStateOf("همه رنگ ها")
    private var sizeFilterValue by mutableStateOf("همه سایز ها")

    @Inject
    lateinit var state: SnackbarHostState
    private lateinit var queue: RequestQueue

    @Inject
    lateinit var memory: SharedPreference
    lateinit var rf: RFID
    private var products = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()
        filterUiList()
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
            productCode = it
            getSimilarProducts()
        }
        queue = Volley.newRequestQueue(this)

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

    @SuppressLint("ApplySharedPref")
    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "ManualRefillProducts",
            Gson().toJson(products).toString()
        )

        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        val type = object : TypeToken<List<Product>>() {}.type

        products = Gson().fromJson(
            memory.getString("ManualRefillProducts", ""),
            type
        ) ?: mutableListOf()
    }

    private fun filterUiList() {

        uiList.forEach {
            var isRequested = false
            products.forEach { it1 ->
                if (it.KBarCode == it1.KBarCode) {
                    it.requestedNumber = it1.requestedNumber
                    isRequested = true
                }
            }
            if (!isRequested) {
                it.requestedNumber = 0
            }
        }

        val wareHouseFilterOutput = uiList.filter {
            it.wareHouseNumber > 0
        }

        val sizeFilterOutput = if (sizeFilterValue == "همه سایز ها") {
            wareHouseFilterOutput
        } else {
            wareHouseFilterOutput.filter {
                it.size == sizeFilterValue
            }
        }

        val colorFilterOutput = if (colorFilterValue == "همه رنگ ها") {
            sizeFilterOutput
        } else {
            sizeFilterOutput.filter {
                it.color == colorFilterValue
            }
        }

        filteredUiList.clear()
        filteredUiList.addAll(colorFilterOutput)
    }

    private fun getSimilarProducts() {

        uiList.clear()
        filteredUiList.clear()
        colorFilterValues.clear()
        colorFilterValues.add("همه رنگ ها")
        sizeFilterValues.clear()
        sizeFilterValues.add("همه سایز ها")

        val url1 =
            "https://rfid-api.avakatan.ir/products/similars?DepartmentInfo_ID=${memory.user.calculatedLocationCode}&K_Bar_Code=$productCode"

        val request1 = JsonObjectRequest(url1, { response1 ->

            val products = response1.getJSONArray("products")

            if (products.length() > 0) {
                jsonArrayProcess(products)
            } else {

                val url2 =
                    "https://rfid-api.avakatan.ir/products/similars?DepartmentInfo_ID=${memory.user.calculatedLocationCode}&kbarcode=$productCode"

                val request2 = JsonObjectRequest(url2, { response2 ->

                    val products2 = response2.getJSONArray("products")

                    if (products2.length() > 0) {
                        jsonArrayProcess(products2)
                    }
                }, { it2 ->
                    when (it2) {
                        is NoConnectionError -> {
                            CoroutineScope(Dispatchers.Default).launch {
                                state.showSnackbar(
                                    "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
                                    null,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }

                        else -> {
                            val error =
                                JSONObject(it2.networkResponse.data.decodeToString()).getJSONObject(
                                    "error"
                                )
                            CoroutineScope(Dispatchers.Default).launch {
                                state.showSnackbar(
                                    error.getString("message"),
                                    null,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    }
                })
                queue.add(request2)
            }
        }, {
            when (it) {
                is NoConnectionError -> {
                    CoroutineScope(Dispatchers.Default).launch {
                        state.showSnackbar(
                            "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
                            null,
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                else -> {
                    val error =
                        JSONObject(it.networkResponse.data.decodeToString()).getJSONObject("error")
                    CoroutineScope(Dispatchers.Default).launch {
                        state.showSnackbar(
                            error.getString("message"),
                            null,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        })

        queue.add(request1)
    }

    private fun jsonArrayProcess(similarProductsJsonArray: JSONArray) {

        for (i in 0 until similarProductsJsonArray.length()) {

            val json = similarProductsJsonArray.getJSONObject(i)

            colorFilterValues.add(json.getString("Color"))
            sizeFilterValues.add(json.getString("Size"))

            uiList.add(
                Product(
                    name = json.getString("productName"),
                    KBarCode = json.getString("KBarCode"),
                    imageUrl = json.getString("ImgUrl"),
                    wareHouseNumber = json.getInt("dbCountDepo"),
                    productCode = json.getString("K_Bar_Code"),
                    size = json.getString("Size"),
                    color = json.getString("Color"),
                    originalPrice = json.getString("OrigPrice"),
                    salePrice = json.getString("SalePrice"),
                    primaryKey = json.getLong("BarcodeMain_ID"),
                    rfidKey = json.getLong("RFID"),
                    scannedEPCs = mutableListOf(),
                    scannedBarcode = "",
                    scannedBarcodeNumber = 0,
                    kName = json.getString("K_Name"),
                    requestedNumber = 0,
                    storeNumber = json.getInt("dbCountStore"),
                )
            )
        }

        productCode = uiList[0].productCode
        colorFilterValues = colorFilterValues.distinct().toMutableStateList()
        sizeFilterValues = sizeFilterValues.distinct().toMutableStateList()
        filterUiList()
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

    private fun addToRefillList(kBarCode: String) {

        val productIndexInUiList = filteredUiList.indexOfLast {
            it.KBarCode == kBarCode
        }

        products.forEach {
            if (filteredUiList[productIndexInUiList].KBarCode == it.KBarCode) {
                if (it.requestedNumber >= it.wareHouseNumber) {
                    CoroutineScope(Main).launch {
                        state.showSnackbar(
                            "تعداد درخواستی از موجودی انبار بیشتر است.",
                            null,
                            duration = SnackbarDuration.Long,
                        )
                    }
                    return
                } else {
                    it.requestedNumber++
                    filterUiList()
                    return
                }
            }
        }
        products.add(filteredUiList[productIndexInUiList])
        products[products.indexOfLast {
            it.KBarCode == kBarCode
        }].requestedNumber++

        filterUiList()
        saveToMemory()
    }

    private fun back() {
        queue.stop()
        finish()
    }
    
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
                        "اضافه کردن کالای جدید", textAlign = TextAlign.Center,
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

        Column {

            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SimpleTextField(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 14.dp)
                            .weight(1F)
                            .fillMaxWidth(),
                        hint = "کد محصول",
                        onDone = { getSimilarProducts() },
                        onValueChange = { productCode = it },
                        value = productCode
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {

                    FilterDropDownList(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_color_lens_24),
                                contentDescription = "",
                                tint = primaryLight,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 4.dp)
                            )
                        },
                        text = {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                text = colorFilterValue,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 4.dp)
                            )
                        },
                        onClick = {
                            colorFilterValue = it
                            filterUiList()
                        },
                        values = colorFilterValues
                    )

                    FilterDropDownList(
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.size),
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
                                text = sizeFilterValue,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        onClick = {
                            sizeFilterValue = it
                            filterUiList()
                        },
                        values = sizeFilterValues
                    )
                }
            }

            LazyColumn {

                items(filteredUiList.size) { i ->
                    Item(
                        i, filteredUiList, true,
                        text3 = "فروشگاه: " + filteredUiList[i].storeNumber,
                        text4 = "انبار: " + filteredUiList[i].wareHouseNumber
                    ) {
                        addToRefillList(filteredUiList[i].KBarCode)
                    }
                }
            }
        }
    }
}
