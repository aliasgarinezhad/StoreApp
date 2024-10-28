package io.domil.store

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.NoConnectionError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jeanwest.mobile.theme.MyApplicationTheme
import com.jeanwest.mobile.theme.Shapes
import com.jeanwest.mobile.theme.Typography
import com.jeanwest.mobile.theme.iconColor
import io.domil.store.theme.BarcodeScannerWithCamera
import io.domil.store.theme.ErrorSnackBar
import io.domil.store.theme.FilterDropDownList
import io.domil.store.theme.Item
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import networking.GetProductData
import networking.Product
import networking.createHttpClient
import org.json.JSONArray
import org.json.JSONObject
import util.onError
import util.onSuccess
import java.io.File


class ManualRefillActivity : ComponentActivity() {

    private var fullName = ""
    private var username = ""
    private var token = ""

    //charge ui parameters
    private var loading by mutableStateOf(false)
    private var state = SnackbarHostState()

    // search ui parameters
    private var productCode by mutableStateOf("")
    private var searchUiList = mutableStateListOf<Product>()
    private var filteredUiList = mutableStateListOf<Product>()
    private var colorFilterValues = mutableStateListOf("همه رنگ ها")
    private var sizeFilterValues = mutableStateListOf("همه سایز ها")
    private var colorFilterValue by mutableStateOf("همه رنگ ها")
    private var sizeFilterValue by mutableStateOf("همه سایز ها")
    private var storeFilterValue = 0
    private val beep: ToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private var isCameraOn by mutableStateOf(false)
    private lateinit var client : GetProductData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMemory()
        if (username == "") {
            val intent =
                Intent(this, UserLoginActivity::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        checkPermission()
        clearCash()
        client = GetProductData(token, createHttpClient())
        setContent {
            Page()
        }
    }

    private fun clearCash() {
        deleteRecursive(cacheDir)
        deleteRecursive(codeCacheDir)
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.let {
                for (child in it) {
                    deleteRecursive(child)
                }
            }
        }
        fileOrDirectory.delete()
    }

    private fun checkPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        username = memory.getString("username", "") ?: ""
        token = memory.getString("accessToken", "") ?: ""
        fullName = memory.getString("userFullName", "") ?: ""
        storeFilterValue = memory.getInt("userLocationCode", 0)
    }

    private fun filterUiList() {

        val sizeFilterOutput = if (sizeFilterValue == "همه سایز ها") {
            searchUiList
        } else {
            searchUiList.filter {
                it.Size == sizeFilterValue
            }
        }

        val colorFilterOutput = if (colorFilterValue == "همه رنگ ها") {
            sizeFilterOutput
        } else {
            sizeFilterOutput.filter {
                it.Color == colorFilterValue
            }
        }
        filteredUiList.clear()
        filteredUiList.addAll(colorFilterOutput)

    }
//
//    private fun getSimilarProducts() {
//
//        if (productCode == "کد محصول") {
//            CoroutineScope(Dispatchers.Default).launch {
//                state.showSnackbar(
//                    "لطفا کد محصول را وارد کنید.",
//                    null,
//                    SnackbarDuration.Long
//                )
//            }
//        }
//
//        loading = true
//
//        searchUiList.clear()
//        filteredUiList.clear()
//        colorFilterValues = mutableStateListOf("همه رنگ ها")
//        sizeFilterValues = mutableStateListOf("همه سایز ها")
//
//        val url1 =
//            "https://rfid-api.avakatan.ir/products/similars?DepartmentInfo_ID=$storeFilterValue&K_Bar_Code=$productCode"
//
//        val request1 = JsonObjectRequest(url1, { response1 ->
//
//            val products = response1.getJSONArray("products")
//
//            if (products.length() > 0) {
//                jsonArrayProcess(products)
//                loading = false
//            } else {
//
//                val url2 =
//                    "https://rfid-api.avakatan.ir/products/similars?DepartmentInfo_ID=$storeFilterValue&kbarcode=$productCode"
//
//                val request2 = JsonObjectRequest(url2, { response2 ->
//
//                    val products2 = response2.getJSONArray("products")
//
//                    if (products2.length() > 0) {
//                        jsonArrayProcess(products2)
//                    }
//                    loading = false
//                }, { it2 ->
//                    when (it2) {
//                        is NoConnectionError -> {
//                            CoroutineScope(Dispatchers.Default).launch {
//                                state.showSnackbar(
//                                    "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
//                                    null,
//                                    SnackbarDuration.Long
//                                )
//                            }
//                        }
//
//                        else -> {
//                            CoroutineScope(Dispatchers.Default).launch {
//                                state.showSnackbar(
//                                    it2.toString(),
//                                    null,
//                                    SnackbarDuration.Long
//                                )
//                            }
//                        }
//                    }
//                    loading = false
//                })
//                val queue2 = Volley.newRequestQueue(this)
//                queue2.add(request2)
//            }
//        }, {
//            when (it) {
//                is NoConnectionError -> {
//                    CoroutineScope(Dispatchers.Default).launch {
//                        state.showSnackbar(
//                            "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
//                            null,
//                            SnackbarDuration.Long
//                        )
//                    }
//                }
//
//                else -> {
//                    CoroutineScope(Dispatchers.Default).launch {
//                        state.showSnackbar(
//                            it.toString(),
//                            null,
//                            SnackbarDuration.Long
//                        )
//                    }
//                }
//            }
//            loading = false
//        })
//
//        val queue = Volley.newRequestQueue(this)
//        queue.add(request1)
//    }

//    private fun jsonArrayProcess(similarProductsJsonArray: JSONArray) {
//
//        for (i in 0 until similarProductsJsonArray.length()) {
//
//            val json = similarProductsJsonArray.getJSONObject(i)
//
//            colorFilterValues.add(json.getString("Color"))
//            sizeFilterValues.add(json.getString("Size"))
//
//            val product = Product(
//                name = json.getString("productName"),
//                kBarCode = json.getString("KBarCode"),
//                imageUrl = json.getString("ImgUrl"),
//                wareHouseNumber = json.getInt("dbCountDepo"),
//                productCode = json.getString("K_Bar_Code"),
//                size = json.getString("Size"),
//                color = json.getString("Color"),
//                originalPrice = json.getString("OrigPrice"),
//                salePrice = json.getString("SalePrice"),
//                primaryKey = json.getLong("BarcodeMain_ID"),
//                rfidKey = json.getLong("RFID"),
//                kName = json.getString("K_Name"),
//                requestedNum = 0,
//                storeNumber = json.getInt("dbCountStore"),
//            )
//            searchUiList.add(product)
//        }
//
//        colorFilterValues = colorFilterValues.distinct().toMutableStateList()
//        sizeFilterValues = sizeFilterValues.distinct().toMutableStateList()
//
//        getScannedProductProperties()
//    }

    private fun getScannedProductProperties() {

//        loading = true
//
//        val url = "https://rfid-api.avakatan.ir/products/v4"
//
//        val request = object : JsonObjectRequest(Method.POST, url, null, {
//
//            val barcodes = it.getJSONArray("KBarCodes")
//            if (barcodes.length() != 0) {
//                colorFilterValue = barcodes.getJSONObject(0).getString("Color")
//            }
//            productCode = searchUiList[0].productCode
//            filterUiList()
//            loading = false
//
//        }, {
//            when (it) {
//                is NoConnectionError -> {
//
//                    CoroutineScope(Dispatchers.Default).launch {
//                        state.showSnackbar(
//                            "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
//                            null,
//                            SnackbarDuration.Long
//                        )
//                    }
//                }
//
//                else -> {
//                    CoroutineScope(Dispatchers.Default).launch {
//                        state.showSnackbar(
//                            it.toString(),
//                            null,
//                            SnackbarDuration.Long
//                        )
//                    }
//                }
//            }
//            loading = false
//
//        }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params["Content-Type"] = "application/json;charset=UTF-8"
//                params["Authorization"] = "Bearer $token"
//                return params
//            }
//
//            override fun getBody(): ByteArray {
//                val json = JSONObject()
//                val epcArray = JSONArray()
//                val barcodeArray = JSONArray()
//
//                barcodeArray.put(productCode)
//
//                json.put("epcs", epcArray)
//                json.put("KBarCodes", barcodeArray)
//                return json.toString().toByteArray()
//            }
//        }
//
//        val queue = Volley.newRequestQueue(this)
//        queue.add(request)

        CoroutineScope(Dispatchers.IO).launch {
            client.getScannedProductProperties(productCode)
                .onSuccess {
                    searchUiList.clear()
                    searchUiList.addAll(it)
                    filterUiList()
                    Log.e("api response", "finally worked")
                }
                .onError {
                    Log.e("api response", it.name)
                }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    content = { SearchContent() },
                    snackbarHost = { ErrorSnackBar(state) },
                    floatingActionButton = { BarcodeScanButton() },
                    floatingActionButtonPosition = FabPosition.End,
                )
            }
        }
    }

    @Composable
    fun BarcodeScanButton() {
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier
                .align(BottomStart)
                .padding(start = 16.dp), onClick = {
                isCameraOn = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_barcode_scan),
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "اسکن کالای جدید",
                    style = Typography.h1
                )
            }
        }
    }

    @Composable
    fun SearchContent() {

        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                    .background(
                        color = MaterialTheme.colors.onPrimary,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxWidth(),
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductCodeTextField(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 14.dp)
                            .weight(1F)
                            .fillMaxWidth(),
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
                                tint = iconColor,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 4.dp)
                            )
                        },
                        text = {
                            Text(
                                style = MaterialTheme.typography.body2,
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
                                tint = iconColor,
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        text = {
                            Text(
                                text = sizeFilterValue,
                                style = MaterialTheme.typography.body2,
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

            if (loading) {
                Row(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primary)

                    if (loading) {
                        Text(
                            text = "در حال بارگذاری",
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            BarcodeScannerWithCamera(isCameraOn, this@ManualRefillActivity) { barcodes ->
                isCameraOn = false
                productCode = barcodes[0].displayValue.toString()
                beep.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                //getSimilarProducts()
                getScannedProductProperties()
            }

            if (filteredUiList.isEmpty()) {
                EmptyList()
            } else {
                LazyColumn {
                    items(filteredUiList.size) { i ->
                        Item(i, filteredUiList, true) {
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductCodeTextField(
        modifier: Modifier
    ) {

        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            textStyle = MaterialTheme.typography.body2,

            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = ""
                )
            },
            value = productCode,
            onValueChange = {
                productCode = it
            },
            modifier = modifier
                .testTag("SearchProductCodeTextField")
                .background(
                    color = MaterialTheme.colors.secondary,
                    shape = MaterialTheme.shapes.small
                ),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                isCameraOn = false
                //getSimilarProducts()
            }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colors.secondary
            ),
            placeholder = { Text(text = "کد محصول") }
        )
    }

    @Composable
    fun EmptyList() {
        Box(
            modifier = Modifier
                .padding(bottom = 56.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Center)
                    .width(256.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(color = Color.White, shape = Shapes.medium)
                        .size(256.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_big_barcode_scan),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Center)
                            .clickable { isCameraOn = true }
                    )
                }

                Text(
                    "بارکد را اسکن یا کد محصول را در کادر جستجو وارد کنید",
                    style = Typography.h1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}