package com.jeanwest.reader.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.BaniReturn
import com.jeanwest.reader.models.Cardex
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.CartonItem
import com.jeanwest.reader.models.Inventory
import com.jeanwest.reader.models.InventoryItem
import com.jeanwest.reader.models.Logistic
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.models.StockDraftHistory
import com.jeanwest.reader.models.StockDraftRequest
import com.jeanwest.reader.models.StockDraftRequestItem
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.models.User
import com.jeanwest.reader.useCases.commonCatchHandler
import com.jeanwest.reader.useCases.fileNameToVersionIntFormat
import com.jeanwest.reader.useCases.jalaliDate.JalaliDateConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mutableListOf
import kotlin.collections.mutableListOf as mutableListOf1

/*
* this class used for api calls.
* all api calls are here. if an specific
* error occurred, this class will show
* that as SnackBar by state parameter.
* it gets tokens, user related data
* and device related automatically
* from user memory saved in
* device. it implements
* Requests interface same as
* LocalStoreDatabase class.
 */
@Singleton
class API @Inject constructor(
    @ApplicationContext context: Context,
    var memory: SharedPreference,
    var state: SnackbarHostState,
) : Requests {

    private val tag = "api"
    private var queue = Volley.newRequestQueue(context)
    private var allInventoryItems: MutableList<InventoryItem> = mutableListOf1()
    private val serverAddress = "https://rfid-api.avakatan.ir"
    private val header = mutableMapOf(
        "AppVersion" to context.packageManager.getPackageInfo(context.packageName, 0).versionName,
        "accept" to "application/json",
        "Content-Type" to "application/json;charset=UTF-8",
        "Authorization" to "Bearer ${memory.user.token}",
        "UserName" to memory.user.username.toString(),
        "SerialNumber" to memory.device.serialNumber,
        "AppType" to if (memory.user.isStoreUser) "store" else "warehouse",
    )
    private val apiTimeout = 60000
    private val requestSetting = DefaultRetryPolicy(
        apiTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )

    fun setHeader(user: User) {
        header.clear()
        header.putAll(
            mutableMapOf(
                "accept" to "application/json",
                "Content-Type" to "application/json;charset=UTF-8",
                "Authorization" to "Bearer ${user.token}",
                "UserName" to user.username.toString(),
                "SerialNumber" to memory.device.serialNumber,
                "AppType" to if (memory.user.isStoreUser) "store" else "warehouse"
            )
        )
    }

    override fun getItemDetailsAndInventory(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
        local: Boolean,
    ) {

        val responseEpcs = mutableListOf1<Product>()
        val responseBarcodes = mutableListOf1<Product>()

        if ((epcs.size + barcodes.size) == 0) {
            showLog("لیست خالی است.", state)
            onError()
            return
        }

        var url = "$serverAddress/products/v4"

        if (local) {
            url += "/localdb"
        }

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val epcsJsonArray = it.getJSONArray("epcs")
            val barcodesJsonArray = it.getJSONArray("KBarCodes")
            val invalidBarcodesJsonArray = it.getJSONArray("invalidBarCodes")
            val invalidEpcsJsonArray = it.getJSONArray("invalidEpcs")

            if (invalidEpcsJsonArray.length() > 0) {
                showLog("مشخصات برخی ای پی سی ها یافت نشد", state)
            }
            if (invalidBarcodesJsonArray.length() > 0) {
                showLog("مشخصات بارکد " + invalidBarcodesJsonArray[0] + " یافت نشد.", state)
            }

            try {

                for (i in 0 until epcsJsonArray.length()) {
                    val product = Product(
                        name = epcsJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = epcsJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = epcsJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = epcsJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = epcsJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = epcsJsonArray.getJSONObject(i).getString("Size"),
                        color = epcsJsonArray.getJSONObject(i).getString("Color"),
                        originalPrice = epcsJsonArray.getJSONObject(i).getString("OrgPrice"),
                        salePrice = epcsJsonArray.getJSONObject(i).getString("SalePrice"),
                        rfidKey = epcsJsonArray.getJSONObject(i).getLong("RFID"),
                        storeNumber = epcsJsonArray.getJSONObject(i).getInt("storeCount"),
                        wareHouseNumber = epcsJsonArray.getJSONObject(i).getInt("depoCount"),
                        scannedEPCs = mutableListOf1(
                            epcsJsonArray.getJSONObject(i).getString("epc")
                        ),
                        kName = epcsJsonArray.getJSONObject(i).getString("K_Name"),
                        countedStoreNumber = epcsJsonArray.getJSONObject(i)
                            .getInt("diffRidStoreCount"),
                        countedWarehouseNumber = epcsJsonArray.getJSONObject(i)
                            .getInt("diffRfidDepoCount"),
                        brandName = epcsJsonArray.getJSONObject(i).getString("BrandGroupName"),
                        departmentName = epcsJsonArray.getJSONObject(i)
                            .getString("CodingDepartmentLevel3")
                    )
                    responseEpcs.add(product)
                }

                for (i in 0 until barcodesJsonArray.length()) {
                    val product = Product(
                        name = barcodesJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = barcodesJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = barcodesJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = barcodesJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = barcodesJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = barcodesJsonArray.getJSONObject(i).getString("Size"),
                        color = barcodesJsonArray.getJSONObject(i).getString("Color"),
                        originalPrice = barcodesJsonArray.getJSONObject(i).getString("OrgPrice"),
                        salePrice = barcodesJsonArray.getJSONObject(i).getString("SalePrice"),
                        rfidKey = barcodesJsonArray.getJSONObject(i).getLong("RFID"),
                        wareHouseNumber = barcodesJsonArray.getJSONObject(i).getInt("depoCount"),
                        storeNumber = barcodesJsonArray.getJSONObject(i).getInt("storeCount"),
                        scannedBarcode = barcodesJsonArray.getJSONObject(i).getString("kbarcode"),
                        kName = barcodesJsonArray.getJSONObject(i).getString("K_Name"),
                        brandName = barcodesJsonArray.getJSONObject(i).getString("BrandGroupName"),
                        //sexTile = barcodesJsonArray.getJSONObject(i).getString("SexTitle"),
                        countedStoreNumber = barcodesJsonArray.getJSONObject(i)
                            .getInt("diffRidStoreCount"),
                        countedWarehouseNumber = barcodesJsonArray.getJSONObject(i)
                            .getInt("diffRfidDepoCount"),
                        departmentName = barcodesJsonArray.getJSONObject(i)
                            .getString("CodingDepartmentLevel3")
                    )
                    responseBarcodes.add(product)
                }
            } catch (e: Exception) {
                commonCatchHandler(e)
                onError()
            }

            onSuccess(
                responseEpcs, responseBarcodes, invalidEpcsJsonArray, invalidBarcodesJsonArray
            )

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val json = JSONObject()
                val epcArray = JSONArray()

                epcs.forEach {
                    epcArray.put(it)
                }

                json.put("epcs", epcArray)

                val barcodeArray = JSONArray()

                barcodes.forEach {
                    barcodeArray.put(it)
                }

                json.put("KBarCodes", barcodeArray)

                return json.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getProductsV5(
        warehouses: List<String>,
        epcs: List<String> = mutableListOf1(),
        barcodes: List<String> = mutableListOf1(),
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
    ) {

        val responseEpcs = mutableListOf1<Product>()
        val responseBarcodes = mutableListOf1<Product>()

        if ((epcs.size + barcodes.size) == 0) {
            return
        }

        val url = "$serverAddress/products/v5"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            Log.e(tag, it.toString())

            val epcsJsonArray = it.getJSONArray("epcs")
            val barcodesJsonArray = it.getJSONArray("KBarCodes")
            val invalidBarcodesJsonArray = it.getJSONArray("invalidBarCodes")
            val invalidEpcsJsonArray = it.getJSONArray("invalidEpcs")

            if (invalidEpcsJsonArray.length() > 0) {
                showLog("مشخصات برخی ای پی سی ها یافت نشد", state)
            }
            if (invalidBarcodesJsonArray.length() > 0) {
                showLog("مشخصات بارکد " + invalidBarcodesJsonArray[0] + " یافت نشد.", state)
            }

            try {

                for (i in 0 until epcsJsonArray.length()) {
                    val product = Product(
                        name = epcsJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = epcsJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = epcsJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = epcsJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = epcsJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = epcsJsonArray.getJSONObject(i).getString("Size"),
                        color = epcsJsonArray.getJSONObject(i).getString("Color"),
                        originalPrice = epcsJsonArray.getJSONObject(i).getString("OrgPrice"),
                        salePrice = epcsJsonArray.getJSONObject(i).getString("SalePrice"),
                        rfidKey = epcsJsonArray.getJSONObject(i).getLong("RFID"),
                        wareHouseNumber = epcsJsonArray.getJSONObject(i).getInt("dbCount"),
                        sexTile = epcsJsonArray.getJSONObject(i).getString("SexTitle"),
                        scannedEPCs = mutableListOf1(
                            epcsJsonArray.getJSONObject(i).getString("epc")
                        ),
                        kName = epcsJsonArray.getJSONObject(i).getString("K_Name"),
                        warehouseCode = epcsJsonArray.getJSONObject(i).getString("WareHouse_ID"),
                        brandName = epcsJsonArray.getJSONObject(i).getString("BrandGroupName"),
                        departmentName = epcsJsonArray.getJSONObject(i)
                            .getString("CodingDepartmentLevel3")
                    )
                    responseEpcs.add(product)
                }

                for (i in 0 until barcodesJsonArray.length()) {
                    val product = Product(
                        name = barcodesJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = barcodesJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = barcodesJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = barcodesJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = barcodesJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = barcodesJsonArray.getJSONObject(i).getString("Size"),
                        color = barcodesJsonArray.getJSONObject(i).getString("Color"),
                        originalPrice = barcodesJsonArray.getJSONObject(i).getString("OrgPrice"),
                        salePrice = barcodesJsonArray.getJSONObject(i).getString("SalePrice"),
                        rfidKey = barcodesJsonArray.getJSONObject(i).getLong("RFID"),
                        sexTile = barcodesJsonArray.getJSONObject(i).getString("SexTitle"),
                        wareHouseNumber = barcodesJsonArray.getJSONObject(i).getInt("dbCount"),
                        scannedBarcode = barcodesJsonArray.getJSONObject(i).getString("kbarcode"),
                        kName = barcodesJsonArray.getJSONObject(i).getString("K_Name"),
                        warehouseCode = barcodesJsonArray.getJSONObject(i)
                            .getString("WareHouse_ID"),
                        brandName = barcodesJsonArray.getJSONObject(i).getString("BrandGroupName"),
                        departmentName = barcodesJsonArray.getJSONObject(i)
                            .getString("CodingDepartmentLevel3")
                    )
                    responseBarcodes.add(product)
                }

            } catch (e: Exception) {
                commonCatchHandler(e)
                onError()
            }
            onSuccess(
                responseEpcs, responseBarcodes, invalidEpcsJsonArray, invalidBarcodesJsonArray
            )

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val json = JSONObject()
                val epcArray = JSONArray()
                val warehouseArray = JSONArray()

                warehouses.forEach {
                    warehouseArray.put(it.toInt())
                }

                json.put("WarehouseID", warehouseArray)

                epcs.forEach {
                    epcArray.put(it)
                }

                json.put("epcs", epcArray)

                val barcodeArray = JSONArray()

                barcodes.forEach {
                    barcodeArray.put(it)
                }

                json.put("KBarCodes", barcodeArray)

                Log.e(this@API.tag, json.toString())

                return json.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getProductImagesList(
        barcode: String,
        onSuccess: (List<String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/products/gallery?KBarCode=$barcode"

        val imgUrls = mutableListOf1<String>()

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            for (i in 0 until it.length()) {
                imgUrls.add(it.getString(i))
            }
            onSuccess(imgUrls)
        }, {
            onError()
            apiErrorProcess(state, it)
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    override fun getItemDetails(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
    ) {

        val responseEpcs = mutableListOf1<Product>()
        val responseBarcodes = mutableListOf1<Product>()

        if ((epcs.size + barcodes.size) == 0) {
            return
        }

        val url = "$serverAddress/products/details"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val epcsJsonArray = it.getJSONArray("epcs")
            val barcodesJsonArray = it.getJSONArray("KBarCodes")
            val invalidBarcodesJsonArray = it.getJSONArray("invalidBarCodes")
            val invalidEpcsJsonArray = it.getJSONArray("invalidEpcs")

            if (invalidEpcsJsonArray.length() > 0) {
                showLog("مشخصات برخی ای پی سی ها یافت نشد", state)
            }
            if (invalidBarcodesJsonArray.length() > 0) {
                showLog("مشخصات بارکد " + invalidBarcodesJsonArray[0] + " یافت نشد.", state)
            }

            try {

                for (i in 0 until epcsJsonArray.length()) {
                    val product = Product(
                        name = epcsJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = epcsJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = epcsJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = epcsJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = epcsJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = epcsJsonArray.getJSONObject(i).getString("Size"),
                        color = epcsJsonArray.getJSONObject(i).getString("Color"),
                        rfidKey = epcsJsonArray.getJSONObject(i).getLong("RFID"),
                        scannedEPCs = mutableListOf1(
                            epcsJsonArray.getJSONObject(i).getString("epc")
                        ),
                        kName = epcsJsonArray.getJSONObject(i).getString("K_Name"),
                    )
                    responseEpcs.add(product)
                }

                for (i in 0 until barcodesJsonArray.length()) {
                    val product = Product(
                        name = barcodesJsonArray.getJSONObject(i).getString("productName"),
                        KBarCode = barcodesJsonArray.getJSONObject(i).getString("KBarCode"),
                        imageUrl = barcodesJsonArray.getJSONObject(i).getString("ImgUrl"),
                        primaryKey = barcodesJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                        productCode = barcodesJsonArray.getJSONObject(i).getString("K_Bar_Code"),
                        size = barcodesJsonArray.getJSONObject(i).getString("Size"),
                        color = barcodesJsonArray.getJSONObject(i).getString("Color"),
                        rfidKey = barcodesJsonArray.getJSONObject(i).getLong("RFID"),
                        scannedBarcode = barcodesJsonArray.getJSONObject(i).getString("kbarcode"),
                        kName = barcodesJsonArray.getJSONObject(i).getString("K_Name"),
                    )
                    responseBarcodes.add(product)
                }

                onSuccess(
                    responseEpcs, responseBarcodes, invalidEpcsJsonArray, invalidBarcodesJsonArray
                )

            } catch (e: Exception) {
                commonCatchHandler(e)
                onError()
            }

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val json = JSONObject()
                val epcArray = JSONArray()

                epcs.forEach {
                    epcArray.put(it)
                }

                json.put("epcs", epcArray)

                val barcodeArray = JSONArray()

                barcodes.forEach {
                    barcodeArray.put(it)
                }

                json.put("KBarCodes", barcodeArray)

                return json.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun syncServerToLocalWarehouse(onSuccess: () -> Unit) {

        val url = "$serverAddress/department-infos/sync"
        val request = object : StringRequest(Method.GET, url, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 501) {
                showLog(
                    "ارسال و دریافت برای شعبه ${memory.user.calculatedLocationCode} تعریف نشده است.",
                    state
                )
            } else {
                apiErrorProcess(state, it)
            }
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun confirmStockDraftTransfer(
        deliveryCode: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/delivery-transfer/finalize/$deliveryCode"
        val request = object : StringRequest(Method.GET, url, {
            showLog(
                "دریافت حواله ها با موفقیت انجام شد.", state, action = SnackBarActions.SUCCESS
            )
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید یا موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(request)
    }

    fun confirmCartonTransfer(
        deliveryCode: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoon/delivery-transfer/finalize/$deliveryCode"
        val request = object : StringRequest(Method.GET, url, {
            showLog("کارتن ها با موفقیت دریافت شدند.", state, action = SnackBarActions.SUCCESS)
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید یا موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(request)
    }

    fun stockDraftTransfer(
        driverCode: String,
        stockDraftID: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/$stockDraftID/logistic-havale/finalize/$driverCode"
        val request = object : StringRequest(Method.GET, url, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید یا موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(request)
    }

    fun getProductsSearchCode(
        mainId: String,
        onSuccess: (List<String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/products/search-codes?BarcodeMainID=$mainId"
        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val item = mutableListOf1<String>()

            val barcodesJsonArray = it.getJSONArray("searchCodes")
            for (i in 0 until barcodesJsonArray.length()) {
                item.add(barcodesJsonArray[i].toString())
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(request)
    }

    fun getProductsSimilar(
        storeCode: Int,
        barcode: String,
        onSuccess: (products: List<Product>) -> Unit,
        onError: () -> Unit,
        local: Boolean,
    ) {
        var url = "$serverAddress/products/similars?DepartmentInfo_ID=$storeCode&kbarcode=$barcode"

        if (local) {
            url =
                "$serverAddress/products/similars/localdb?DepartmentInfo_ID=$storeCode&kbarcode=$barcode"
        }

        val request = JsonObjectRequest(url, {

            val products = mutableListOf1<Product>()
            val productsJsonArray = it.getJSONArray("products")
            if (productsJsonArray.length() > 0) {

                for (i in 0 until productsJsonArray.length()) {

                    val json = productsJsonArray.getJSONObject(i)

                    products.add(
                        Product(
                            name = json.getString("productName"),
                            KBarCode = json.getString("KBarCode"),
                            imageUrl = json.getString("ImgUrl"),
                            storeNumber = json.getInt("dbCountStore"),
                            wareHouseNumber = json.getInt("dbCountDepo"),
                            productCode = json.getString("K_Bar_Code"),
                            size = json.getString("Size"),
                            color = json.getString("Color"),
                            originalPrice = json.getString("OrigPrice"),
                            salePrice = json.getString("SalePrice"),
                            primaryKey = json.getLong("BarcodeMain_ID"),
                            rfidKey = json.getLong("RFID"),
                            kName = json.getString("K_Name"),
                        )
                    )
                }
                onSuccess(products)
            } else {
                showLog("این کد فرعی هیچ موجودی در فروشگاه شما ندارد.", state)
                onError()
            }
        }, {
            apiErrorProcess(state, it)
            onError()
        })
        queue.add(request)
    }

    fun getProductsSimilarStyleCode(
        storeCode: Int,
        barcode: String,
        onSuccess: (products: List<Product>) -> Unit,
        onError: () -> Unit,
        local: Boolean,
    ) {
        var url =
            "$serverAddress/products/similars?DepartmentInfo_ID=$storeCode&K_Bar_Code=$barcode"

        if (local) {
            url =
                "$serverAddress/products/similars/localdb?DepartmentInfo_ID=$storeCode&K_Bar_Code=$barcode"
        }

        val request = JsonObjectRequest(url, {

            val products = mutableListOf1<Product>()
            val productsJsonArray = it.getJSONArray("products")
            if (productsJsonArray.length() > 0) {

                for (i in 0 until productsJsonArray.length()) {

                    val json = productsJsonArray.getJSONObject(i)

                    products.add(
                        Product(
                            name = json.getString("productName"),
                            KBarCode = json.getString("KBarCode"),
                            imageUrl = json.getString("ImgUrl"),
                            storeNumber = json.getInt("dbCountStore"),
                            wareHouseNumber = json.getInt("dbCountDepo"),
                            productCode = json.getString("K_Bar_Code"),
                            size = json.getString("Size"),
                            color = json.getString("Color"),
                            originalPrice = json.getString("OrigPrice"),
                            salePrice = json.getString("SalePrice"),
                            primaryKey = json.getLong("BarcodeMain_ID"),
                            rfidKey = json.getLong("RFID"),
                            kName = json.getString("K_Name"),
                            //          brandName = json.getString("BrandGroupName")
                        )
                    )
                }
                onSuccess(products)
            } else {
                showLog("این کد فرعی هیچ موجودی در فروشگاه شما ندارد.", state)
                onError()
            }
        }, {
            apiErrorProcess(state, it)
            onError()
        })
        queue.add(request)
    }

    fun getProductsSimilarCentralWarehouse(
        wareCode: Int,
        barcode: String,
        onSuccess: (products: List<Product>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/products/similars?WareHouse_ID=$wareCode&kbarcode=$barcode"

        val request = JsonObjectRequest(url, {

            val products = mutableListOf1<Product>()
            val productsJsonArray = it.getJSONArray("products")
            if (productsJsonArray.length() > 0) {

                for (i in 0 until productsJsonArray.length()) {

                    val json = productsJsonArray.getJSONObject(i)

                    products.add(
                        Product(
                            name = json.getString("productName"),
                            KBarCode = json.getString("KBarCode"),
                            imageUrl = json.getString("ImgUrl"),
                            storeNumber = json.getInt("dbCountStore"),
                            wareHouseNumber = json.getInt("dbCountDepo"),
                            productCode = json.getString("K_Bar_Code"),
                            size = json.getString("Size"),
                            color = json.getString("Color"),
                            originalPrice = json.getString("OrigPrice"),
                            salePrice = json.getString("SalePrice"),
                            primaryKey = json.getLong("BarcodeMain_ID"),
                            rfidKey = json.getLong("RFID"),
                            kName = json.getString("K_Name"),
                            brandName = json.getString("BrandGroupName")
                        )
                    )
                }
                onSuccess(products)
            } else {
                showLog("این کد فرعی هیچ موجودی در فروشگاه شما ندارد.", state)
                onError()
            }
        }, {
            apiErrorProcess(state, it)
            onError()
        })
        queue.add(request)
    }

    fun getProductsSimilarWithStyleAndWareCode(
        wareCode: Int,
        styleCode: String,
        onSuccess: (products: List<Product>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/products/similars?WareHouse_ID=$wareCode&K_Bar_Code=$styleCode"
        val request = JsonObjectRequest(url, {

            val products = mutableListOf1<Product>()
            val productsJsonArray = it.getJSONArray("products")
            if (productsJsonArray.length() > 0) {

                for (i in 0 until productsJsonArray.length()) {

                    val json = productsJsonArray.getJSONObject(i)

                    products.add(
                        Product(
                            name = json.getString("productName"),
                            KBarCode = json.getString("KBarCode"),
                            imageUrl = json.getString("ImgUrl"),
                            storeNumber = json.getInt("dbCountStore"),
                            wareHouseNumber = json.getInt("dbCountDepo"),
                            productCode = json.getString("K_Bar_Code"),
                            size = json.getString("Size"),
                            color = json.getString("Color"),
                            originalPrice = json.getString("OrigPrice"),
                            salePrice = json.getString("SalePrice"),
                            primaryKey = json.getLong("BarcodeMain_ID"),
                            rfidKey = json.getLong("RFID"),
                            kName = json.getString("K_Name"),
                            brandName = json.getString("BrandGroupName")
                        )
                    )
                }
                onSuccess(products)
            } else {
                showLog("این کد فرعی هیچ موجودی در فروشگاه شما ندارد.", state)
                onError()
            }
        }, {
            apiErrorProcess(state, it)
            onError()
        })
        queue.add(request)
    }

    fun getRefillNew(
        depCode: Int,
        onSuccess: (products: List<Product>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/refill/v2?DepartmentCode=$depCode"

        val request = object : JsonObjectRequest(Method.GET, url, null, {
            val productArray = it.getJSONArray("products")
            val products = mutableListOf1<Product>()

            for (i in 0 until productArray.length()) {
                val product = Product()

                product.KBarCode = productArray.getJSONObject(i).getString("KBarCode")
                product.color = productArray.getJSONObject(i).getString("Color")
                product.size = productArray.getJSONObject(i).getString("Size")
                product.name = productArray.getJSONObject(i).getString("ProductName")
                product.storeNumber = productArray.getJSONObject(i).getInt("StoreCount")
                product.wareHouseNumber = productArray.getJSONObject(i).getInt("DepoCount")
                product.requestedNumber = productArray.getJSONObject(i).getInt("RequestedCount")
                product.primaryKey = productArray.getJSONObject(i).getLong("BarcodeMain_ID")
                product.imageUrl = productArray.getJSONObject(i).getString("ImgUrl")
                product.departmentName =
                    productArray.getJSONObject(i).getString("CodingDepartmentTitle")
                products.add(product)
            }
            onSuccess(products)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getRefill(
        depCode: Int,
        onSuccess: (barcodes: List<String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/refill/localdb?DepartmentCode=$depCode"

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val barcodes = mutableListOf1<String>()

            for (i in 0 until it.length()) {

                barcodes.add(it.getJSONObject(i).getString("KBarCode"))
            }
            onSuccess(barcodes)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun getRefill2(
        depCode: Int,
        onSuccess: (barcodes: List<String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/refill2/localdb?DepartmentCode=$depCode"

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val barcodes = mutableListOf1<String>()

            for (i in 0 until it.length()) {
                barcodes.add(it.getJSONObject(i).getString("KBarCode"))
            }

            onSuccess(barcodes)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(request)
    }

    fun getSacksDetails(
        sackNumber: String,
        onSuccess: (draftProperties: List<StockDraft>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/stock-draft/group/$sackNumber"
        val request = object : JsonObjectRequest(url, fun(it) {

            val item = it.getJSONArray("items")
            val stockDraftList = mutableListOf1<StockDraft>()
            for (i in 0 until item.length()) {
                val draftProperties = StockDraft(
                    number = item.getJSONObject(i).getLong("StockDraft_ID"),
                    numberOfItems = item.getJSONObject(i).getInt("SumQty"),
                    source = item.getJSONObject(i).getInt("FromWareHouse_ID"),
                    destination = item.getJSONObject(i).getInt("ToWareHouse_ID"),
                    barcodeTable = mutableListOf1(),
                    specification = item.getJSONObject(i).getString("StockDraftDescription"),
                    epcsToPrimaryKeysMap = mutableMapOf(),
                    isTwoShel = true
                )
                stockDraftList.add(draftProperties)
            }
            onSuccess(stockDraftList)

        }, {
            if (it?.networkResponse?.statusCode == 404) {
                showLog("شماره گونی وارد شده معتبر نیست یا کاربر به آن دسترسی ندارد.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    override fun createStockDraftRequest(
        user: Int,
        source: Int,
        destination: Int,
        stockDraftRequestType: Int,
        products: List<Product>,
        onSuccess: (stockDraftID: String) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft-requests/poducts-request/create"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val stockDraftNumber = it.getString("Message") ?: "0"
            onSuccess(stockDraftNumber)
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val barcodeArray = JSONArray()

                products.forEach { product ->

                    if (product.scannedEPCNumber > 0) {
                        product.scannedEPCs.forEach { epc ->
                            val productJson = JSONObject()
                            productJson.put("BarcodeMain_ID", product.primaryKey)
                            productJson.put("ItemName", product.name)
                            productJson.put(
                                "SearchCode",
                                product.scannedBarcode.ifEmpty { product.KBarCode }
                            )
                            productJson.put("QTY", 1)
                            productJson.put("epc", epc)
                            barcodeArray.put(productJson)
                        }
                    }

                    if (product.scannedBarcodeNumber > 0) {
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", product.primaryKey)
                        productJson.put("ItemName", product.name)
                        productJson.put(
                            "SearchCode",
                            product.scannedBarcode.ifEmpty { product.KBarCode }
                        )
                        productJson.put("QTY", product.scannedBarcodeNumber)
                        barcodeArray.put(productJson)
                    }
                }

                body.put("FromWareHouse_ID", source)
                body.put("ToWareHouse_ID", destination)
                body.put("RequestType", stockDraftRequestType)
                body.put("products", barcodeArray)

                Log.e(this@API.tag, body.toString())

                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    override fun getStockDraftDetails(
        code: String,
        onSuccess: (draftProperties: StockDraft) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/$code/details/v2"
        Log.e("url", url)
        val request = object : JsonObjectRequest(url, fun(it) {
            Log.e("detailsError", "false")
            val epcToKBarCodeMap = mutableMapOf<String, Long>()
            val draftBarcodes = mutableListOf1<String>()
            val specification = it.getString("StockDraftDescription")
            val logisticKey = it.getString("LogesticKey")
            val source = it.getInt("FromWareHouse_ID")
            val destination = it.getInt("ToWareHouse_ID")
            val stateID = it.getString("StateID")
            val positionID = it.getString("PositionID")
            val logisticDeliverDate = it.getString("LogesticDeliverDate")
            val logisticGetDate = it.getString("LogesticGetDate")
            val createDate = it.getString("CreateDate")
            val confirmDate = it.getString("ConfirmDate")
            val isConfirmed = it.getBoolean("IsConfirmed")
            val driverOperationId = it.getString("DriverOperation_ID")

            val miladiCreateDate = it.getString("CreateDate").substring(0, 10)
            val intArrayFormatJalaliCreateDate = JalaliDateConverter.gregorian_to_jalali(
                miladiCreateDate.substring(0, 4).toInt(),
                miladiCreateDate.substring(5, 7).toInt(),
                miladiCreateDate.substring(8, 10).toInt()
            )
            val jalaliCreateDate =
                "${intArrayFormatJalaliCreateDate[0]}/${intArrayFormatJalaliCreateDate[1]}/${intArrayFormatJalaliCreateDate[2]}"

            var numberOfItems = 0
            val stockDraftDetails = it.getJSONArray("details")
            for (i in 0 until stockDraftDetails.length()) {
                numberOfItems += stockDraftDetails.getJSONObject(i).getInt("Qty")

                repeat(stockDraftDetails.getJSONObject(i).getInt("Qty")) { _ ->
                    draftBarcodes.add(stockDraftDetails.getJSONObject(i).getString("kbarcode"))
                }
                if (stockDraftDetails.getJSONObject(i).getString("EPC") != "null") {
                    epcToKBarCodeMap[stockDraftDetails.getJSONObject(i).getString("EPC")
                        .uppercase()] = stockDraftDetails.getJSONObject(i).getLong("BarcodeMain_ID")
                }
            }

            val draftProperties = StockDraft(
                number = code.toLong(),
                numberOfItems = numberOfItems,
                barcodeTable = draftBarcodes,
                date = jalaliCreateDate,
                source = source,
                sourceTitle = memory.erpData.warehousesIDsToTitles[source.toString()] ?: "",
                destination = destination,
                destinationTitle = memory.erpData.warehousesIDsToTitles[destination.toString()]
                    ?: "",
                specification = specification,
                epcsToPrimaryKeysMap = epcToKBarCodeMap,
                logisticKey = logisticKey,
                isConfirmed = isConfirmed,
                stateID = stateID,
                positionID = positionID,
                logisticDeliverDate = logisticDeliverDate,
                createDate = createDate,
                logisticGetDate = logisticGetDate,
                confirmDate = confirmDate,
                driverOperationId = driverOperationId
            )

            onSuccess(draftProperties)

        }, {
            Log.e("detailsError", "true")
            if (it?.networkResponse?.statusCode == 404) {
                showLog("شماره حواله وارد شده معتبر نیست یا کاربر به آن دسترسی ندارد.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getStockDraftRequestDetails(
        code: String,
        onSuccess: (
            stockDraftRequestDetails: StockDraftRequest,
        ) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft-requests/$code/details"
        val request = object : JsonObjectRequest(url, fun(stockDraftRequestDetails) {

            val itemsDetails = mutableListOf1<StockDraftRequestItem>()

            val detailsArray = stockDraftRequestDetails.getJSONArray("StockDraftRequestDetails")
            for (i in 0 until detailsArray.length()) {

                val detailJson = detailsArray.getJSONObject(i)

                // Build list of epcs from the JSON array
                val epcsJsonArray = detailJson.getJSONArray("epcs")
                val epcsList = mutableListOf<String>()
                for (j in 0 until epcsJsonArray.length()) {
                    epcsList.add(epcsJsonArray.getString(j))
                }

                val itemDetail = StockDraftRequestItem(
                    product = Product(),
                    shelfCode = detailJson.getString("ShelfCode"),
                    shelfID = detailJson.getString("ShelfID"),
                    KBarcode = detailJson.getString("KBarCode"),
                    requestNumber = detailJson.getInt("Qty"),
                    foundNumber = detailJson.getInt("QtyJoor"),
                    controlledNumber = detailJson.getInt("QtyControl"),
                    primaryKey = detailJson.getLong("BaseBarcodeMain_ID"),
                    priority = detailJson.getLong("priority"),
                    epcs = epcsList
                )
                itemsDetails.add(itemDetail)
            }

            var sumOfRequested = 0
            var sumOfFound = 0
            var sumOfControlled = 0
            val itemsDistinctByKBarcode = mutableMapOf<String, StockDraftRequestItem>()

            itemsDetails.forEach { item ->
                itemsDistinctByKBarcode[item.KBarcode] = item
            }

            itemsDistinctByKBarcode.keys.forEach {
                val item = itemsDistinctByKBarcode[it]!!
                sumOfRequested += item.requestNumber
                sumOfFound += item.foundNumber
                sumOfControlled += item.controlledNumber
            }

            val stockDraftRequest = StockDraftRequest(
                number = code.toLong(),
                date = stockDraftRequestDetails.getString("CreateDate"),
                user = stockDraftRequestDetails.getString("CreateUserID").toIntOrNull(),
                source = stockDraftRequestDetails.getString("SourceWareHouse_ID"),
                destination = stockDraftRequestDetails.getString("DestinationWareHouse_ID"),
                specification = stockDraftRequestDetails.getString("Description"),
                stateId = stockDraftRequestDetails.getInt("StateID"),
                items = itemsDetails,
                sumOfControlledItems = sumOfControlled,
                sumOfRequestedItems = sumOfRequested,
                sumOfFoundItems = sumOfFound,
                itemsDistinctByKBarcode = itemsDistinctByKBarcode
            )
            onSuccess(stockDraftRequest)

        }, {
            if (it?.networkResponse?.statusCode == 404) {
                showLog("شماره درخواست حواله نامعتبر است.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun getStockDraftRequestDetailsV2(
        code: String,
        onSuccess: (
            stockDraftRequestDetails: StockDraftRequest,
        ) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft-requests/$code/details/v2"
        val request = object : JsonObjectRequest(url, fun(stockDraftRequestDetails) {

            val itemsDetails = mutableListOf1<StockDraftRequestItem>()

            val detailsArray = stockDraftRequestDetails.getJSONArray("StockDraftRequestDetails")
            for (i in 0 until detailsArray.length()) {

                val detailJson = detailsArray.getJSONObject(i)

                // Build list of epcs from the JSON array
                val epcsJsonArray = detailJson.getJSONArray("epcs")
                val epcsList = mutableListOf<String>()
                for (j in 0 until epcsJsonArray.length()) {
                    epcsList.add(epcsJsonArray.getString(j))
                }

                val itemDetail = StockDraftRequestItem(
                    product = Product(),
                    KBarcode = detailJson.getString("KBarCode"),
                    requestNumber = detailJson.getInt("Qty"),
                    foundNumber = detailJson.getInt("QtyJoor"),
                    controlledNumber = detailJson.getInt("QtyControl"),
                    primaryKey = detailJson.getLong("BaseBarcodeMain_ID"),
                    epcs = epcsList
                )
                itemsDetails.add(itemDetail)
            }

            var sumOfRequested = 0
            var sumOfFound = 0
            var sumOfControlled = 0
            val itemsDistinctByKBarcode = mutableMapOf<String, StockDraftRequestItem>()

            itemsDetails.forEach { item ->
                itemsDistinctByKBarcode[item.KBarcode] = item
            }

            itemsDistinctByKBarcode.keys.forEach {
                val item = itemsDistinctByKBarcode[it]!!
                sumOfRequested += item.requestNumber
                sumOfFound += item.foundNumber
                sumOfControlled += item.controlledNumber
            }

            val stockDraftRequest = StockDraftRequest(
                number = code.toLong(),
                date = stockDraftRequestDetails.getString("CreateDate"),
                user = stockDraftRequestDetails.getString("CreateUserID").toIntOrNull(),
                source = stockDraftRequestDetails.getString("SourceWareHouse_ID"),
                destination = stockDraftRequestDetails.getString("DestinationWareHouse_ID"),
                specification = stockDraftRequestDetails.getString("Description"),
                stateId = stockDraftRequestDetails.getInt("StateID"),
                items = itemsDetails,
                sumOfControlledItems = sumOfControlled,
                sumOfRequestedItems = sumOfRequested,
                sumOfFoundItems = sumOfFound,
                itemsDistinctByKBarcode = itemsDistinctByKBarcode
            )
            onSuccess(stockDraftRequest)

        }, {
            if (it?.networkResponse?.statusCode == 404) {
                showLog("شماره درخواست حواله نامعتبر است.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun updateStockDraftRequestNumberOfFound(
        stockDraftRequestID: Long,
        product: Product,
        username: String,
        source: Int,
        reasonID: Int,
        shelfNumber: Int,
        shelfCode: String,
        numberOfFound: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft-requests/$stockDraftRequestID/pick"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            showLog(it.getString("MessageText"), state, action = SnackBarActions.SUCCESS)
            onSuccess()

        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر به صفحه قبل برگردید و موجودی درخواستی این کالا را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getDefault()

                val body = JSONObject()
                body.put("PersonID", username.toInt())
                body.put("BaseBarcodeMain_ID", product.primaryKey)
                body.put("BarcodeMain_ID", product.primaryKey)
                body.put("KBarCode", product.KBarCode)
                body.put("SourceWareHouseID", source)
                body.put("QtyJoor", numberOfFound)
                body.put("JoorSelectTime", sdf.format(Date()))
                body.put("ShelfID", shelfNumber)
                body.put("ShelfCode", shelfCode)
                body.put("ReasonID", reasonID)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun shelfUpdateStockDraftRequestNumberOfFound(
        stockDraftRequestID: Long,
        product: Product,
        username: String,
        source: Int,
        reasonID: Int,
        shelfNumber: Int,
        shelfCode: String,
        numberOfFound: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft-requests/$stockDraftRequestID/pick/find"
        val request = object : JsonObjectRequest(Method.POST, url, null, {

            showLog(it.getString("MessageText"), state, action = SnackBarActions.SUCCESS)
            onSuccess()

        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر به صفحه قبل برگردید و موجودی درخواستی این کالا را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getDefault()

                val body = JSONObject()
                body.put("PersonID", username.toInt())
                body.put("BaseBarcodeMain_ID", product.primaryKey)
                body.put("BarcodeMain_ID", product.primaryKey)
                body.put("KBarCode", product.KBarCode)
                body.put("SourceWareHouseID", source)
                body.put("QtyJoor", numberOfFound)
                body.put("JoorSelectTime", sdf.format(Date()))
                body.put("ShelfID", shelfNumber)
                body.put("ShelfCode", shelfCode)
                body.put("ReasonID", reasonID)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun finalizeStockDraftRequestFindingProcess(
        username: Int,
        stockDraftRequestID: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft-requests/$stockDraftRequestID/pick/close"

        val request = object : JsonObjectRequest(Method.PATCH, url, null, {
            onSuccess()

        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("StockDraftRequest_ID", stockDraftRequestID)
                body.put("PersonID", username)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun closeStockDraftRequest(
        username: Int,
        stockDraftRequestID: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url =
            "$serverAddress/stock-draft-requests/$stockDraftRequestID/create-stock-draft/close"

        val request = object : JsonObjectRequest(Method.PATCH, url, null, {
            onSuccess()
        }, {
            showLog(
                "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                state,
                action = SnackBarActions.WARNING
            )
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("StockDraftRequest_ID", stockDraftRequestID)
                body.put("PersonID", username)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun updateStockDraftRequestUser(
        username: Int,
        stockDraftRequestID: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft-requests/receive"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            showLog(
                data = "درخواست به شما اختصاص داده شد.",
                state = state,
                action = SnackBarActions.SUCCESS
            )
            onSuccess()

        }, {
            showLog(
                "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                state,
                action = SnackBarActions.WARNING
            )
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("StockDraftRequest_ID", stockDraftRequestID)
                body.put("PersonID", username)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun deleteCarton(
        cartonNumber: String,
        warehouseCode: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        var cartonId = 0L
        if (cartonNumber.uppercase().startsWith("CN")) {
            cartonId = cartonNumber.substring(3).toLong()
        } else {
            cartonId = cartonNumber.toLong()
        }
        val url = "$serverAddress/cartoons?CartonID=$cartonId&WareHouseID=$warehouseCode"
        Log.e(tag, url)
        val request =
            object : StringRequest(/* method = */ Method.DELETE,/* url = */ url, /* listener = */ {
                onSuccess()
            }, /* errorListener = */ {
                onError()
            }) {
                override fun getHeaders(): Map<String, String> {
                    return header
                }
            }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun confirmCartonByRfid(
        cartonNumber: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoon/$cartonNumber/delivery-transfer/finalize-by-rfid"
        val request = object : JsonObjectRequest(Method.GET, url, null, {

            onSuccess()

        }, {
            showLog(
                "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                state,
                action = SnackBarActions.WARNING
            )
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun getCartonsDetails(
        codes: List<String>,
        onSuccess: (carton: List<Carton>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoons/details"
        val request = object : JsonObjectRequest(Method.POST, url, null, fun(it) {

            val cartons = mutableListOf1<Carton>()

            try {
                val cartonDetailsJsonArray = it.getJSONArray("items")

                for (index in 0 until cartonDetailsJsonArray.length()) {

                    val cartonDetailsJsonObject = cartonDetailsJsonArray.getJSONObject(index)

                    val code = cartonDetailsJsonObject.getString("CartoonNum")
                    val productList = mutableListOf1<CartonItem>()
                    val cartonBarcodes = mutableListOf1<String>()
                    val epcToPrimaryKeyMap = mutableMapOf<String, Long>()
                    val specification = cartonDetailsJsonObject.getString("CartoonDesc")
                    val source =
                        cartonDetailsJsonObject.getString("DriverOperation_FromWareHouse_ID")
                    val des = cartonDetailsJsonObject.getString("DriverOperation_ToWareHouse_ID")
                    val cartonSource = cartonDetailsJsonObject.getString("WareHouse_ID")
                    val cartonDes = cartonDetailsJsonObject.getString("DestWareHouse_ID") ?: ""
                    val id = cartonDetailsJsonObject.getString("Cartoons_ID")
                    val driverOperationId = cartonDetailsJsonObject.getString("DriverOperation_ID")
                    val shelfAddress = cartonDetailsJsonObject.getString("CartonShelfID")
                    val isConfirmedByRFID = cartonDetailsJsonObject.getString("ISConfirmedByRFID")
                        .toBooleanStrictOrNull() ?: false
                    val miladiCreateDate =
                        cartonDetailsJsonObject.getString("CreateDate").substring(0, 10)
                    var everyProductHaveRfid = true
                    val intArrayFormatJalaliCreateDate = JalaliDateConverter.gregorian_to_jalali(
                        miladiCreateDate.substring(0, 4).toInt(),
                        miladiCreateDate.substring(5, 7).toInt(),
                        miladiCreateDate.substring(8, 10).toInt()
                    )
                    val jalaliCreateDate =
                        "${intArrayFormatJalaliCreateDate[0]}/${intArrayFormatJalaliCreateDate[1]}/${intArrayFormatJalaliCreateDate[2]}"


                    val productsJsonArray = cartonDetailsJsonObject.getJSONArray("cartoonDetails")
                    var numberOfItems = 0
                    for (i in 0 until productsJsonArray.length()) {
                        val barcode = productsJsonArray.getJSONObject(i).getString("ItemBarcode")
                        val itemName = productsJsonArray.getJSONObject(i).getString("ItemName")
                        val productQty = productsJsonArray.getJSONObject(i).getInt("Qty")
                        val imageUrl = productsJsonArray.getJSONObject(i).getString("ImgUrl")
                        val epcsArray = productsJsonArray.getJSONObject(i).getJSONArray("epcs")
                        val primaryKey =
                            productsJsonArray.getJSONObject(i).getLong("BarcodeMain_ID")
                        val productEpcs = mutableMapOf<String, Long>()
                        if (epcsArray.length() == 0) {
                            everyProductHaveRfid = false
                        } else {
                            for (a in 0 until epcsArray.length()) {
                                productEpcs[epcsArray[a].toString()] = primaryKey
                            }
                            epcToPrimaryKeyMap.putAll(productEpcs)
                        }
                        val product = CartonItem(
                            product = Product(
                                KBarCode = barcode,
                                name = itemName,
                                draftNumber = productQty,
                                imageUrl = imageUrl,
                                primaryKey = primaryKey
                            ),
                            cartonNumber = code,
                            epcs = productEpcs.keys.toList(),
                            qtyInCarton = productQty
                        )
                        productList.add(product)

                        numberOfItems += productsJsonArray.getJSONObject(i).getInt("Qty")

                        repeat(productsJsonArray.getJSONObject(i).getInt("Qty")) { _ ->
                            cartonBarcodes.add(
                                productsJsonArray.getJSONObject(i).getString("ItemBarcode")
                            )
                        }
                    }

                    val carton = Carton(
                        number = code,
                        id = id.toLong(),
                        numberOfItems = numberOfItems,
                        barcodeTable = cartonBarcodes,
                        date = jalaliCreateDate,
                        operationSource = source,
                        operationDes = des,
                        cartonSource = cartonSource,
                        cartonDes = cartonDes,
                        specification = specification,
                        driverOperationId = driverOperationId,
                        isConfirmedByRFID = isConfirmedByRFID,
                        epcs = epcToPrimaryKeyMap.keys.toList(),
                        everyProductHaveEpc = everyProductHaveRfid,
                        products = productList,
                        epcsToPrimaryKeyMap = epcToPrimaryKeyMap,
                        shelfAddress = shelfAddress
                    )
                    cartons.add(carton)
                }

                if (cartons.isEmpty()) {
                    onError()
                    showLog("مشخصات کارتن (ها) یافت نشد.", state)
                } else {
                    onSuccess(cartons)
                }

            } catch (e: Exception) {
                showLog("مشکلی در پردازش مشخصات کارتن پیش آمده است.", state)
                onError()
                commonCatchHandler(e)
            }

        }, {
            apiErrorProcess(state, it)

            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val cartonIDsJSONArray = JSONArray()

                codes.forEach { code ->

                    if (code.uppercase().startsWith("CN")) {
                        cartonIDsJSONArray.put(code.substring(3).toLong())
                        Log.e(this@API.tag, code.substring(3).toLong().toString())
                    } else {
                        cartonIDsJSONArray.put(code.toLong())
                    }
                }

                body.put("CartoonIDs", cartonIDsJSONArray)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun editStockDraft(
        code: Long,
        products: List<Product>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/$code"

        val request = object : StringRequest(Method.PATCH, url, {
            showLog(
                "حواله " + code + "با موفقیت تایید شد.", state, action = SnackBarActions.SUCCESS
            )
            onSuccess()
        }, {
            showLog(
                "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                state,
                action = SnackBarActions.WARNING
            )
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONArray()

                products.forEach {

                    val item = JSONObject()
                    item.put("BarcodeMain_ID", it.primaryKey)
                    item.put("epcs", JSONArray(it.scannedEPCs))
                    body.put(item)
                }
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun confirmStockDraft(
        code: Long,
        products: List<Product>,
        onSuccess: (it: String) -> Unit,
        onError: () -> Unit,
        local: Boolean = false,
    ) {

        var url = "$serverAddress/stock-draft/$code/confirm-via-erp"
        if (local) {
            url += "/localdb"
        }
        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess(it.getString("Message"))
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val productsJsonArray = JSONArray()

                products.forEach {
                    repeat(it.scannedBarcodeNumber + it.scannedEPCNumber) { _ ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("K_Name", it.kName)
                        productsJsonArray.put(productJson)
                    }
                }
                body.put("kbarcodes", productsJsonArray)

                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun createSack(
        printerModel: Int,
        stockDraftIDs: List<Long>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/grouping"
        val request = object : JsonArrayRequest(Method.POST, url, null, {
            onSuccess()

        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("PrinterModel_ID", printerModel)
                body.put("StockDraft_IDs", JSONArray(stockDraftIDs))
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getStockDraftRequestType(
        onSuccess: (stockDraftRequestTypes: Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft-requests/type"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val stockDraftRequestTypes = mutableMapOf<String, Int>()
            val stockDraftRequestTypesJsonArray = it.getJSONArray("items")

            for (i in 0 until stockDraftRequestTypesJsonArray.length()) {

                stockDraftRequestTypes[stockDraftRequestTypesJsonArray.getJSONObject(i)
                    .getString("StockDraftRequestTypeTitle")] =
                    stockDraftRequestTypesJsonArray.getJSONObject(i)
                        .getInt("StockDraftRequestType_ID")
            }

            onSuccess(stockDraftRequestTypes)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun printCartonLabel(
        printer: Int,
        cartonNumber: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoon/print"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            showLog(
                "درخواست پرینت با موفقیت به پرینتر ارسال شد.",
                state,
                action = SnackBarActions.SUCCESS
            )
            onSuccess()

        }, {
            apiErrorProcess(state, it)

            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر خروجی پرینتر را بررسی کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }

            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()

                body.put("cartoonNumber", cartonNumber)
                body.put("printerId", printer)

                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun convertCartonToStockDraft(
        cartonIds: List<Long>,
        printerId: Int,
        onSuccess: (response: String) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/cartoon/convert-stock-draft/v2"

        val request = object : JsonArrayRequest(Method.POST, url, null, {
            onSuccess(it.getJSONObject(0).getString("MessageText"))

        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر خروجی پرینتر را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val cartons = JSONArray()
                cartonIds.forEach {
                    cartons.put(it)
                }
                body.put("PrinterModel_ID", printerId)
                body.put("CartoonIDs", cartons)
                Log.e("api", body.toString())
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfInventoryReport(
        warehouseCode: Int,
        shelfNumber: String,
        status: Boolean,
        products: List<ShelfItem>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/inventory"
        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("WareHouse_ID", warehouseCode)
                body.put("ShelfID", shelfNumber)
                body.put("Status", status)

                val jsonArray = JSONArray()
                products.forEach {
                    it.epcs.forEach { epc ->
                        if (epc !in it.product.scannedEPCs) {
                            val jsonObject = JSONObject()
                            jsonObject.put("BarcodeMain_ID", it.product.primaryKey)
                            jsonObject.put("EPC", epc)
                            jsonArray.put(jsonObject)
                        }
                    }
                }
                body.put("products", jsonArray)
                Log.e(this@API.tag, body.toString())
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun shelfContent(
        warehouseCode: Int,
        shelfNumber: String,
        onSuccess: (List<Product>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/contents"
        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val productsList = mutableListOf1<Product>()
            val productsJsonArray = it.getJSONArray("products")
            for (i in 0 until productsJsonArray.length()) {
                val product = Product()
                product.shelfCount = productsJsonArray.getJSONObject(i).getInt("Qty")
                product.imageUrl = productsJsonArray.getJSONObject(i).getString("ImgUrl")
                product.size = productsJsonArray.getJSONObject(i).getString("Size")
                product.name = productsJsonArray.getJSONObject(i).getString("ItemName")
                product.KBarCode = productsJsonArray.getJSONObject(i).getString("KBarCode")
                val searchAndBarcodes =
                    productsJsonArray.getJSONObject(i).getJSONArray("SearchCodes")
                if (searchAndBarcodes.length() != 0) {
                    for (a in 0 until searchAndBarcodes.length()) {
                        product.searchCodes.add(searchAndBarcodes.getString(a))
                    }
                }
                productsList.add(product)
            }
            onSuccess(productsList)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("WareHouseID", warehouseCode.toLong())
                body.put("ShelfCode", shelfNumber)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun shelfContent1(
        warehouseCode: Int,
        shelfNumber: String,
        onSuccess: (List<ShelfItem>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/contents"
        val request = object : JsonObjectRequest(
            Method.POST, url, null, {
                val productsList = mutableListOf<ShelfItem>()
                val productsJsonArray = it.getJSONArray("products")
                for (i in 0 until productsJsonArray.length()) {
                    val product = Product(
                        imageUrl = productsJsonArray.getJSONObject(i).getString("ImgUrl"),
                        size = productsJsonArray.getJSONObject(i).getString("Size"),
                        name = productsJsonArray.getJSONObject(i).getString("ItemName"),
                        KBarCode = productsJsonArray.getJSONObject(i).getString("KBarCode"),
                        shelfCount = productsJsonArray.getJSONObject(i).getInt("Qty"),
                        primaryKey = productsJsonArray.getJSONObject(i).getLong("BarcodeMain_ID"),
                    )
                    val searchAndBarcodes =
                        productsJsonArray.getJSONObject(i).getJSONArray("SearchCodes")
                    if (searchAndBarcodes.length() != 0) {
                        for (a in 0 until searchAndBarcodes.length()) {
                            product.searchCodes.add(searchAndBarcodes.getString(a))
                        }
                    }
                    val shelfProductEpc = mutableListOf<String>()
                    val epcsJsonArray =
                        productsJsonArray.getJSONObject(i).getJSONArray("epcs")
                    if (epcsJsonArray.length() != 0) {
                        for (a in 0 until epcsJsonArray.length()) {
                            shelfProductEpc.add(epcsJsonArray.getString(a))
                        }
                    }
                    val shelfItem = ShelfItem(
                        qtyInShelf = productsJsonArray.getJSONObject(i).getInt("Qty"),
                        product = product,
                        shelfNumber = shelfNumber,
                        epcs = shelfProductEpc
                    )
                    productsList.add(shelfItem)
                }
                onSuccess(productsList)
            },
            {
                apiErrorProcess(state, it)
                onError()
            }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("WareHouseID", warehouseCode.toLong())
                body.put("ShelfCode", shelfNumber)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun shelfContentCarton(
        shelfNumber: String,
        warehouseId: String,
        onSuccess: (List<Carton>) -> Unit,
        onError: () -> Unit,
    ) {
        val url =
            "$serverAddress/shelf/cartoon?CartonShelfID=$shelfNumber&WareHouse_ID=$warehouseId"
        val request = object : JsonArrayRequest(Method.GET, url, null, {
            val cartonsList = mutableListOf1<Carton>()
            for (i in 0 until it.length()) {
                val cartonsJsonObject = it.getJSONObject(i)
                val carton = Carton()
                carton.id = cartonsJsonObject.getLong("Cartoons_ID")
                carton.number = cartonsJsonObject.getString("CartoonNum")
                carton.cartonDes = cartonsJsonObject.getString("CartoonDesc")
                carton.isConfirmedByRFID = cartonsJsonObject.getBoolean("FromRFID")
                carton.numberOfItems = cartonsJsonObject.getInt("SumProductQty")
                carton.cartonSource = cartonsJsonObject.getInt("WareHouse_ID").toString()
                cartonsList.add(carton)
            }
            onSuccess(cartonsList)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun shelfEntry(
        sourceWareHouseID: Int,
        shelfCode: String,
        products: Map<String, Int>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/product/shelf-in"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر مدتی بعد محتوی قفسه را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val productList = JSONArray()
                body.put("SourceWareHouseID", sourceWareHouseID)
                body.put("ToShelfCode", shelfCode)
                products.forEach {
                    val jObject = JSONObject()
                    jObject.put("KBarCode", it.key)
                    jObject.put("qty", it.value)
                    productList.put(jObject)
                }
                body.put("products", productList)
                Log.e(this@API.tag, body.toString())
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfEntryWithEpcs(
        sourceWareHouseID: Int,
        shelfCode: String,
        products: StockDraftRequestItem,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/product/shelf-in"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر مدتی بعد محتوی قفسه را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val productList = JSONArray()
                body.put("SourceWareHouseID", sourceWareHouseID)
                body.put("ToShelfCode", shelfCode)
                if (products.epcs.isNotEmpty()) {
                    products.epcs.forEach { epc ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", products.product.primaryKey)
                        productJson.put("KBarCode", products.product.KBarCode)
                        productJson.put("qty", 1)
                        productJson.put("epc", epc)
                        productList.put(productJson)
                    }
                }

                if (products.product.scannedBarcodeNumber > 0) {
                    val productJson = JSONObject()
                    productJson.put("BarcodeMain_ID", products.product.primaryKey)
                    productJson.put("KBarCode", products.product.KBarCode)
                    productJson.put("qty", products.product.scannedBarcodeNumber)
                    productList.put(productJson)
                }

                body.put("products", productList)
                Log.e(this@API.tag, body.toString())
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfEnterEpcAndUpdateStockDraft(
        stockDraftRequestID: Long,
        reasonID: Int,
        shelfCode: String,
        products: StockDraftRequestItem,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ){
        val url = "$serverAddress/stock-draft-requests/$stockDraftRequestID/shelf-in"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر مدتی بعد محتوی قفسه را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getDefault()

                val body = JSONObject()
                val productList = JSONArray()
                body.put("ToShelfCode", shelfCode)
                body.put("WareHouse_ID", 44)
                body.put("JoorSelectTime", sdf.format(Date()))
                body.put("ReasonID", reasonID)
                if (products.epcs.isNotEmpty()) {
                    products.epcs.forEach { epc ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", products.product.primaryKey)
                        productJson.put("KBarCode", products.product.KBarCode)
                        productJson.put("qty", 1)
                        productJson.put("epc", epc)
                        productList.put(productJson)
                    }
                }else{
                    val productJson = JSONObject()
                    productJson.put("BarcodeMain_ID", products.product.primaryKey)
                    productJson.put("KBarCode", products.product.KBarCode)
                    productJson.put("qty", products.product.scannedBarcodeNumber)
                    productJson.put("epc", "")
                    productList.put(productJson)
                }
                body.put("products", productList)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }


    fun shelfExit(
        currentWareHouseId: Int,
        shelfCode: String,
        shelfEnterCode: String? = "",
        products: Map<String, Int>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/shelf/product/shelf-out"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر مدتی بعد محتوی قفسه را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val productList = JSONArray()
                body.put("SourceWareHouseID", currentWareHouseId)
                body.put("FromShelfCode", shelfCode)
                products.forEach {
                    val jObject = JSONObject()
                    jObject.put("KBarCode", it.key)
                    jObject.put("qty", it.value)
                    productList.put(jObject)
                }
                body.put("products", productList)
                if (shelfEnterCode != null) {
                    body.put("ToShelfCode", shelfEnterCode)
                }
                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfExitWithEpcs(
        currentWareHouseId: Int,
        shelfCode: String,
        shelfEnterCode: String? = "",
        products: List<ShelfItem>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/shelf/product/shelf-out"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر مدتی بعد محتوی قفسه را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val productList = JSONArray()
                body.put("SourceWareHouseID", currentWareHouseId)
                body.put("FromShelfCode", shelfCode)
                for (elements in products) {
                    for (i in 0 until elements.epcs.size) {
                        val jObject = JSONObject()
                        jObject.put("KBarCode", elements.product.KBarCode)
                        jObject.put("qty", 1)
                        jObject.put("epc", elements.epcs[i])
                        jObject.put("BarcodeMain_ID", elements.product.primaryKey)
                        productList.put(jObject)
                    }
                }
                body.put("products", productList)
                if (shelfEnterCode != null) {
                    body.put("ToShelfCode", shelfEnterCode)
                }
                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }


    fun getShelfType(
        onSuccess: (Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/deaprtment/shelf-type"

        val request = object : JsonArrayRequest(Method.GET, url, null, {
            val shelfJsonArray = mutableMapOf<String, Int>()
            for (i in 0 until it.length()) {
                shelfJsonArray[it.getJSONObject(i).getString("Title")] =
                    it.getJSONObject(i).getInt("DepartmentShelfType_ID")
            }
            onSuccess(shelfJsonArray)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun addSkuToShelfInStore(
        sku: List<String>,
        shelfId: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/department/product"

        val request = object : JsonArrayRequest(Method.POST, url, null, {

            onSuccess()
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val skuList = JSONArray()
                for (element in sku) {
                    skuList.put(element)
                }
                body.put("Sku", skuList)
                body.put("DepartmentShelf_ID", shelfId)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun printShelfInStore(
        depCode: Int,
        shelfTitle: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/department/print"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            onSuccess()
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("Department_ID", depCode)
                body.put("ShelfTile", shelfTitle)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun createShelfStore(
        depCode: Int,
        wareCode: Int,
        shelfTypeId: Int,
        description: String,
        onSuccess: (shelfCode: String) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/department"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            onSuccess(it.getString("Title"))
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("Department_ID", depCode)
                body.put("WareHouse_ID", wareCode)
                body.put("DepartmentShelfType_ID", shelfTypeId)
                body.put("Description", description)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getDepShelfs(
        wareCode: Int?,
        onSuccess: (shelfs: List<StoreShelf>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/shelf/department/product"

        var fullUrl = Uri.parse(url).buildUpon().build().toString()
        val where = mutableMapOf<String, Any?>()
        where["WareHouse_ID"] = wareCode
        val whereJson = JSONObject(where).toString()
        fullUrl = Uri.parse(fullUrl).buildUpon().appendQueryParameter("where", whereJson).build()
            .toString()

        val request = object : JsonArrayRequest(Method.GET, fullUrl, null, {
            val shelfInfo = mutableListOf1<StoreShelf>()
            for (i in 0 until it.length()) {
                val shelfData = StoreShelf()
                val response = it.getJSONObject(i)
                shelfData.depShelfId = response.getInt("DepartmentShelf_ID")
                shelfData.shelfTitle = response.getString("ShelfTitle")
                shelfData.shelfTypeId = response.getInt("DepartmentShelfType_ID")
                shelfData.shelfTitleType = response.getString("ShelfTypeTitle")
                shelfData.wareId = response.getInt("WareHouse_ID")
                shelfData.shelfDes = response.getString("ShelfDescription")
                val skus = response.getJSONArray("Sku")
                for (i in 0 until skus.length()) {
                    shelfData.shelfSkus.add(skus.getString(i))
                }
                shelfInfo.add(shelfData)
            }
            onSuccess(shelfInfo)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getStoreShelfProducts(
        shelfCode: String,
        onSuccess: (shelfInfo: StoreShelf) -> Unit,
        onError: (isValidShelf: Boolean) -> Unit,
    ) {


        val url = "$serverAddress/shelf/department/product"

        var fullUrl = Uri.parse(url).buildUpon().build().toString()

        val where = mutableMapOf<String, Any?>()

        where["ShelfTitle"] = shelfCode
        val whereJson = JSONObject(where).toString()
        fullUrl = Uri.parse(fullUrl).buildUpon().appendQueryParameter("where", whereJson).build()
            .toString()

        val request = object : JsonArrayRequest(Method.GET, fullUrl, null, {

            if (it.length() != 0) {
                val shelfData = StoreShelf()
                val response = it.getJSONObject(0)
                shelfData.depShelfId = response.getInt("DepartmentShelf_ID")
                shelfData.shelfTitle = response.getString("ShelfTitle")
                shelfData.shelfTypeId = response.getInt("DepartmentShelfType_ID")
                shelfData.shelfTitleType = response.getString("ShelfTypeTitle")
                shelfData.wareId = response.getInt("WareHouse_ID")
                shelfData.shelfDes = response.getString("ShelfDescription")
                val skus = response.getJSONArray("Sku")
                for (i in 0 until skus.length()) {
                    shelfData.shelfSkus.add(skus.getString(i))
                }
                onSuccess(shelfData)
            } else {
                onError(false)
            }
        }, {
            apiErrorProcess(state, it)
            onError(true)
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getProductShelfAddressStore(
        sku: String,
        onSuccess: (shelfInfo: List<StoreShelf>) -> Unit,
        onError: (isValidShelf: Boolean) -> Unit,
    ) {
        val url = "$serverAddress/shelf/department/product"

        var fullUrl = Uri.parse(url).buildUpon().build().toString()

        val where = mutableMapOf<String, Any?>()
        where["Sku"] = sku
        val whereJson = JSONObject(where).toString()
        fullUrl = Uri.parse(fullUrl).buildUpon().appendQueryParameter("where", whereJson).build()
            .toString()

        val request = object : JsonArrayRequest(Method.GET, fullUrl, null, {
            val shelfInfo = mutableListOf1<StoreShelf>()
            if (it.length() != 0) {
                for (i in 0 until it.length()) {
                    val shelfData = StoreShelf()
                    val response = it.getJSONObject(i)
                    shelfData.depShelfId = response.getInt("DepartmentShelf_ID")
                    shelfData.shelfTitle = response.getString("ShelfTitle")
                    shelfData.shelfTypeId = response.getInt("DepartmentShelfType_ID")
                    shelfData.shelfTitleType = response.getString("ShelfTypeTitle")
                    shelfData.wareId = response.getInt("WareHouse_ID")
                    shelfData.shelfDes = response.getString("ShelfDescription")
                    val skus = response.getJSONArray("Sku")
                    for (a in 0 until skus.length()) {
                        shelfData.shelfSkus.add(skus.getString(a))
                    }
                    shelfInfo.add(shelfData)
                }
                onSuccess(shelfInfo)
            } else {
                onError(false)
            }
        }, {
            apiErrorProcess(state, it)
            onError(true)
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfContentStore(
        shelfNumber: String,
        onSuccess: (shelfInfo: StoreShelf) -> Unit,
        onError: (isValidShelf: Boolean) -> Unit,
    ) {
        val url = "$serverAddress/shelf/department/product"

        var fullUrl = Uri.parse(url).buildUpon().build().toString()

        val where = mutableMapOf<String, Any?>()
        where["ShelfTitle"] = shelfNumber
        val whereJson = JSONObject(where).toString()
        fullUrl = Uri.parse(fullUrl).buildUpon().appendQueryParameter("where", whereJson).build()
            .toString()
        val request = object : JsonArrayRequest(Method.GET, fullUrl, null, {
            val shelfData = StoreShelf()
            if (it.length() != 0) {
                for (i in 0 until it.length()) {

                    val response = it.getJSONObject(i)
                    shelfData.depShelfId = response.getInt("DepartmentShelf_ID")
                    shelfData.shelfTitle = response.getString("ShelfTitle")
                    shelfData.shelfTypeId = response.getInt("DepartmentShelfType_ID")
                    shelfData.shelfTitleType = response.getString("ShelfTypeTitle")
                    shelfData.wareId = response.getInt("WareHouse_ID")
                    shelfData.shelfDes = response.getString("ShelfDescription")
                    val skus = response.getJSONArray("Sku")
                    for (a in 0 until skus.length()) {
                        shelfData.shelfSkus.add(skus.getString(a))
                    }
                }
                onSuccess(shelfData)
            } else {
                onError(false)
            }
        }, {
            apiErrorProcess(state, it)
            onError(true)
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun shelfBarcodeAddress(
        wareHouseId: Int,
        barcode: String,
        onSuccess: (List<ShelfBarcodeAddress>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/shelf/product/address?WareHouse_ID=$wareHouseId&KBarcode=$barcode"

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val item = mutableListOf1<ShelfBarcodeAddress>()

            for (i in 0 until it.length()) {
                val shelfStockID = it.getJSONObject(i).getInt("ID")
                val shelfID = it.getJSONObject(i).getString("ShelfID")
                val wareHouseTitle = it.getJSONObject(i).getString("WareHouseTitle")
                val qty = it.getJSONObject(i).getInt("Qty")
                val color = it.getJSONObject(i).getString("Color")
                val size = it.getJSONObject(i).getString("Size")
                val shelfProduct =
                    ShelfBarcodeAddress(shelfStockID, shelfID, wareHouseTitle, qty, color, size)
                item.add(shelfProduct)
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getCargoReceiveLocations(
        freightNumber: String,
        onSuccess: (Map<String, String>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/foreign-checking/receive-shipment/details"

        val request = object : JsonArrayRequest(Method.POST, url, null, {
            val response = mutableMapOf<String, String>()
            for (i in 0 until it.length()) {
                response[it.getJSONObject(i).getString("Title")] =
                    it.getJSONObject(i).getString("ID")
            }
            onSuccess(response)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("freightNumber", freightNumber)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getCargoRegistrationLocations(
        freightNumber: String,
        onSuccess: (Map<String, String>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/foreign-checking/master-shipment/details"

        val request = object : JsonArrayRequest(Method.POST, url, null, {
            val response = mutableMapOf<String, String>()
            for (i in 0 until it.length()) {
                response[it.getJSONObject(i).getString("Title")] =
                    it.getJSONObject(i).getString("ID")
            }
            onSuccess(response)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("freightNumber", freightNumber)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun finalCargoRegistration(
        freightNumber: String,
        styleCode: String,
        qty: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/foreign-checking/master-shipment/save-shipment"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("freightNumber", freightNumber)
                body.put("StyleCode", styleCode)
                body.put("Qty", qty)
                return body.toString().toByteArray()
            }

        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun finalCargoReceive(
        freightNumber: String,
        styleCode: String,
        qty: Int,
        packingID: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/foreign-checking/receive-shipment/save-shipment"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("freightNumber", freightNumber)
                body.put("StyleCode", styleCode)
                body.put("Qty", qty)
                body.put("PackingID", packingID)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun finalBanimodeReturn(
        id: Int,
        sourceWarehouse: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/order-banimode/finalize-order"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید یا موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("ID", id)
                body.put("SourceWareHouseID", sourceWarehouse)
                return body.toString().toByteArray()
            }

        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getBaniReturnList(
        onSuccess: (List<BaniReturn>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/order-banimode/collection-list"

        val request = object : JsonArrayRequest(Method.GET, url, null, {
            val response = mutableListOf1<BaniReturn>()
            for (i in 0 until it.length()) {
                val baniResponse = BaniReturn()
                baniResponse.id = it.getJSONObject(i).getString("ID")
                baniResponse.saleDate = it.getJSONObject(i).getString("SaleDate")
                baniResponse.mobile = it.getJSONObject(i).getString("Mobile")
                baniResponse.address = it.getJSONObject(i).getString("Address")
                baniResponse.isCollectable = it.getJSONObject(i).getInt("IsCollectable")
                baniResponse.isReleasable = it.getJSONObject(i).getInt("IsReleasable")
                baniResponse.canFinalize = it.getJSONObject(i).getInt("IsFinishable")
                baniResponse.color = it.getJSONObject(i).getString("Color")
                baniResponse.fullName = it.getJSONObject(i).getString("FullName")
                baniResponse.saleDesc = it.getJSONObject(i).getString("SaleDesc")
                baniResponse.paymentPrice = it.getJSONObject(i).getLong("PaymentPrice")
                response.add(baniResponse)
            }
            onSuccess(response)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }


    fun getBaniReceivrReturnList(
        stockDraftId: Long,
        onSuccess: (stockDraft: StockDraft) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/receive-order-banimode/$stockDraftId/details"

        val request = object : JsonArrayRequest(Method.GET, url, null, {
            val response = mutableMapOf<String, Product>()
            for (i in 0 until it.length()) {
                val baniResponse = Product()
                baniResponse.name = it.getJSONObject(i).getString("ItemName")
                baniResponse.KBarCode = it.getJSONObject(i).getString("ItemBarcode")
                baniResponse.primaryKey = it.getJSONObject(i).getLong("BarcodeMain_ID")
                baniResponse.imageUrl = it.getJSONObject(i).getString("ImgUrl")
                baniResponse.draftNumber = it.getJSONObject(i).getInt("SentQty")
                baniResponse.color = it.getJSONObject(i).getString("Color")
                baniResponse.size = it.getJSONObject(i).getString("Size")
                response[baniResponse.KBarCode] = baniResponse
            }
            val stockDraft = StockDraft(number = stockDraftId, items = response)
            onSuccess(stockDraft)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun baniReceiveReturnFinal(
        stockDraftId: String,
        products: List<Product>,
        warehouseCode: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/receive-order-banimode/$stockDraftId/finalize"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید یا موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONArray()
                for (i in products.indices) {
                    val product = JSONObject()
                    product.put("BarcodeMain_ID", products[i].primaryKey)
                    product.put("ItemBarcode", products[i].KBarCode)
                    product.put("ItemName", products[i].name)
                    product.put("Qty", products[i].scannedNumber)
                    product.put("WareHouseID", warehouseCode)
                    body.put(product)
                }
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun cartonBarcodeAddress(
        warehouseCode: String,
        barcode: String,
        onSuccess: (products: List<CartonItem>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/products/carton-address?WareHouse_ID=$warehouseCode&SKU=$barcode"

        val request = object : JsonObjectRequest(Method.GET, url, null, {
            val products = mutableListOf1<CartonItem>()
            val barcodesJsonArray = it.getJSONArray("items")
            for (i in 0 until barcodesJsonArray.length()) {
                val shelfId = barcodesJsonArray.getJSONObject(i).getString("ShelfID")
                val qty = barcodesJsonArray.getJSONObject(i).getInt("Qty")
                val color = barcodesJsonArray.getJSONObject(i).getString("Color")
                val size = barcodesJsonArray.getJSONObject(i).getString("Size")
                val wareTitle = barcodesJsonArray.getJSONObject(i).getString("WareHouseTitle")
                val cartonNum = barcodesJsonArray.getJSONObject(i).getString("CartoonNum")
                val itemBarcode = barcodesJsonArray.getJSONObject(i).getString("ItemBarcode")
                val product = CartonItem(
                    product = Product(
                        size = size,
                        color = color,
                        shelfAddress = shelfId,
                        wareHouseNumber = qty,
                        warehouseCode = wareTitle,
                        KBarCode = itemBarcode,
                        draftNumber = qty
                    ),
                    cartonNumber = cartonNum,
                    epcs = listOf(),
                    qtyInCarton = qty
                )
                products.add(product)
            }
            onSuccess(products)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }/*
        fun shelfValidation(
            warehouseCode: String,
            shelfCode: String,
            onSuccess: (isExists: Boolean) -> Unit,
            onError: (errorString: String) -> Unit,
        ) {
            val url = "$serverAddress/shelf/is-exist"

            val request = object : JsonObjectRequest(Method.POST, url, null, {
                val isExists = it.getBoolean("isExist")
                onSuccess(isExists)
            }, {
                apiErrorProcess(state, it)
                val errorString: String = if (it is NoConnectionError) {
                    "internet is disconnected"
                } else {

                    it?.networkResponse?.data?.decodeToString()?.let { it1 ->
                        try {
                            JSONObject(it1).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            commonCatchHandler(e)
                            it1
                        }
                    } ?: "internet is disconnected"
                }
                onError(errorString)
            }) {
                override fun getHeaders(): Map<String, String> {
    
                    
                    
                    
                    
                    return header
                }

                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("WareHouseID", warehouseCode.toLong())
                    body.put("ShelfCode", shelfCode)
                    return body.toString().toByteArray()
                }
            }
            val apiTimeout = 30000
            request.retryPolicy = DefaultRetryPolicy(
                apiTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            queue.add(request)
        }*/

    fun cardex(
        warehouseId: Int,
        barcodeMainId: Long,
        onSuccess: (List<Cardex>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/products/history/$warehouseId/$barcodeMainId"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val item = mutableListOf1<Cardex>()

            val barcodesJsonArray = it.getJSONArray("items")
            for (i in 0 until barcodesJsonArray.length()) {
                val cardex = Cardex()
                cardex.deliveryType = barcodesJsonArray.getJSONObject(i).getString("Title")
                cardex.deliveryDetails =
                    barcodesJsonArray.getJSONObject(i).getString("WareHouseTitle")
                cardex.deliveryDateShamsi =
                    barcodesJsonArray.getJSONObject(i).getString("DeliveryDateShamsi")
                cardex.deliveryWareHouseTitle =
                    barcodesJsonArray.getJSONObject(i).getString("DeliveryWareHouseTitle")
                cardex.itemName = barcodesJsonArray.getJSONObject(i).getString("ItemName")
                cardex.deliveryQty = barcodesJsonArray.getJSONObject(i).getInt("Qty")
                cardex.deliveryID = barcodesJsonArray.getJSONObject(i).getString("SrcHeaderID")
                cardex.qtyAfterDelivery = barcodesJsonArray.getJSONObject(i).getInt("RunningMojodi")
                item.add(cardex)
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }
        queue.add(request)
    }

    fun getCars(
        onSuccess: (Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/cars"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val item = mutableMapOf<String, Int>()
            val barcodesJsonArray = it.getJSONArray("cars")
            for (i in 0 until barcodesJsonArray.length()) {
                val driversName = barcodesJsonArray.getJSONObject(i).getString("CarTitle")
                val driverId = barcodesJsonArray.getJSONObject(i).getInt("ID")
                item[driversName] = driverId
            }
            onSuccess(item)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun getAppVersions(
        onSuccess: (Map<Int, String>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/apk"

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val versions = mutableMapOf<Int, String>()
            Log.e(tag, it.toString())
            for (i in 0 until it.length()) {
                val fileName = it.getString(i)
                fileName.fileNameToVersionIntFormat()?.also { versionInt ->
                    versions[versionInt] = fileName
                }
            }

            Log.e(tag, versions.toMap().toString())
            onSuccess(versions)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(request)
    }

    fun addOrRemoveCarton(
        cartonIDs: List<String>,
        shelfNumber: String,
        operation: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoon/inout/v2"

        val request = object : StringRequest(Method.POST, url, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                val cartonIDsJsonArray = JSONArray()

                cartonIDs.forEach {
                    cartonIDsJsonArray.put(it)
                }

                body.put("cartoonNums", cartonIDsJsonArray)
                body.put("operation", operation)
                body.put("shelfCode", shelfNumber)

                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun printStockDraftLabel(
        printer: Int,
        cartonNumber: String,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/print"

        val request = object : JsonArrayRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر خروجی پرینتر را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()

                body.put("stockDraftID", cartonNumber)
                body.put("printerId", printer)

                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun stockDraftsHistory(
        source: Int?,
        destination: Int?,
        startDate: String?,
        endDate: String?,
        stateId: Int?,
        showPendStocks: Boolean?,
        perPage: String?,
        onSuccess: (stockDrafts: List<StockDraftHistory>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/stock-draft/localdb"

        var fullUrl = Uri.parse(url).buildUpon()
            .appendQueryParameter("page", "1")
            .appendQueryParameter("perPage", perPage)
            .build()
            .toString()

        val where = mutableMapOf<String, Any?>()

        if (startDate != null || endDate != null) {
            val createDate = mutableMapOf<String, String>()
            startDate?.let { createDate["gte"] = it }
            endDate?.let { createDate["lte"] = it }
            where["CreateDate"] = createDate
        }

        source?.let { where["FromWareHouse_ID"] = it }
        destination?.let { where["ToWareHouse_ID"] = it }
        stateId?.let { where["StateID"] = it }

        if (showPendStocks != null && showPendStocks) {
            where["StateID"] = mutableMapOf(
                "nin" to listOf(2, 9)
            )
        }
        // Convert the 'where' object into a JSON string
        val whereJson = JSONObject(where).toString()

        // Use Uri.Builder to construct the URL
        fullUrl = Uri.parse(fullUrl).buildUpon().appendQueryParameter("where", whereJson).build()
            .toString()

        Log.e(tag, url)
        val request = object : JsonObjectRequest(Method.GET, fullUrl, null, {
            val item = mutableListOf1<StockDraftHistory>()
            val barcodesJsonArray = it.getJSONArray("items")
            for (i in 0 until barcodesJsonArray.length()) {
                val stockDraftID = barcodesJsonArray.getJSONObject(i).getString("StockDraft_ID")
                val fromWareHouseID =
                    barcodesJsonArray.getJSONObject(i).getString("FromWareHouse_ID")
                val toWareHouseID = barcodesJsonArray.getJSONObject(i).getString("ToWareHouse_ID")
                val sendDate = barcodesJsonArray.getJSONObject(i).getString("SendDate")
                val createDate = barcodesJsonArray.getJSONObject(i).getString("CreateDate")
                val updateDate = barcodesJsonArray.getJSONObject(i).getString("UpdateDate")
                val stockDraftStatusID =
                    barcodesJsonArray.getJSONObject(i).getString("StockDraftStatus_ID")
                val statusTitle = barcodesJsonArray.getJSONObject(i).getString("StatusTitle")
                val sumProductQrt = barcodesJsonArray.getJSONObject(i).getInt("SumProductQty")
                val positionId = barcodesJsonArray.getJSONObject(i).getInt("PositionID")
                val stateID = barcodesJsonArray.getJSONObject(i).getInt("StateID")
                val stockDraftDescription =
                    barcodesJsonArray.getJSONObject(i).getString("StockDraftDescription")
                val deliveryCode = barcodesJsonArray.getJSONObject(i).getString("LogesticKey")
                val stockDraft = StockDraftHistory(
                    stockDraftID,
                    fromWareHouseID,
                    toWareHouseID,
                    sendDate,
                    createDate,
                    updateDate,
                    stockDraftStatusID,
                    statusTitle,
                    sumProductQrt,
                    positionId,
                    stateID,
                    stockDraftDescription,
                    deliveryCode
                )
                item.add(stockDraft)
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getWarehousesLists(
        onSuccess: (locations: Map<String, String>, sortedLocations: List<String>, departmentWarehousesLists: Map<String, MutableList<String>>, departmentTitles: Map<String, String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/department-infos"
        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val locations = mutableMapOf<String, String>()
            val departmentWarehousesLists = mutableMapOf<String, MutableList<String>>()
            val departmentTitles = mutableMapOf<String, String>()

            Log.e(this@API.tag, it.toString())

            for (i in 0 until it.length()) {

                //some of departments does not have wareHouses array
                try {
                    val warehouses = it.getJSONObject(i).getJSONArray("wareHouses")
                    val departmentWarehousesList = mutableListOf1<String>()
                    for (j in 0 until warehouses.length()) {
                        val warehouse = warehouses.getJSONObject(j)
                        locations[warehouse.getString("WareHouse_ID")] =
                            warehouse.getString("WareHouseTitle")
                        departmentWarehousesList.add(warehouse.getString("WareHouse_ID"))
                    }
                    departmentWarehousesLists[it.getJSONObject(i).getString("DepartmentInfo_ID")] =
                        departmentWarehousesList
                    departmentTitles[it.getJSONObject(i).getString("DepartmentInfo_ID")] =
                        it.getJSONObject(i).getString("DepName")
                } catch (_: Exception) {

                }
            }

            val sortedLocations = locations.values.toMutableList()
            sortedLocations.sortBy { it1 ->
                it1
            }
            onSuccess(
                locations, sortedLocations, departmentWarehousesLists, departmentTitles
            )

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return this@API.header
            }
        }

        queue.add(request)
    }


    fun getDriversLogistics(
        onSuccess: (Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/drivers"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val item = mutableMapOf<String, Int>()
            val barcodesJsonArray = it.getJSONArray("drivers")
            for (i in 0 until barcodesJsonArray.length()) {
                val driversName = barcodesJsonArray.getJSONObject(i).getString("FullName")
                val driverId = barcodesJsonArray.getJSONObject(i).getInt("ID")
                item[driversName] = driverId
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }
        queue.add(request)
    }

    fun driverLogisticList(
        deliveryCode: Long,
        onSuccess: (stockDrafts: List<StockDraft>) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/stock-draft/validate"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val item = mutableListOf1<StockDraft>()
            val barcodesJsonArray = it.getJSONArray("items")
            for (i in 0 until barcodesJsonArray.length()) {
                val number = barcodesJsonArray.getJSONObject(i).getString("StockDraftID")
                val numberOfItems = barcodesJsonArray.getJSONObject(i).getInt("Qty")
                val sourceId = barcodesJsonArray.getJSONObject(i).getInt("SourceWarehouseID")
                val sourceTitle =
                    barcodesJsonArray.getJSONObject(i).getString("SourceWarehouseTitle")
                val date = barcodesJsonArray.getJSONObject(i).getString("CreateDate")
                val des = barcodesJsonArray.getJSONObject(i).getString("StockDraftDescription")
                val stockDraft = StockDraft(
                    number = number.toLong(),
                    createDate = date,
                    numberOfItems = numberOfItems,
                    source = sourceId,
                    sourceTitle = sourceTitle,
                    specification = des,
                )
                item.add(stockDraft)
            }
            onSuccess(item)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("DeliveryCode", deliveryCode)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }

    fun driverLogisticListFinal(
        deliveryCode: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/stock-draft/deliver"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("DeliveryCode", deliveryCode)
                return body.toString().toByteArray()
            }
        }
        queue.add(request)
    }


    fun receiveLogistics(
        stockDraftIds: ArrayList<Long>,
        driverId: Int,
        carId: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/stock-draft"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val stockDrafts = JSONArray()
                stockDraftIds.forEach {
                    stockDrafts.put(it)
                }
                val body = JSONObject()
                body.put("StockDraftIDs", stockDrafts)
                body.put("DriverID", driverId)
                body.put("CarID", carId)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun returnLogistics(
        stockDraftIds: ArrayList<Long>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/logistic/stock-draft/return"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess()
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }

            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val stockDrafts = JSONArray()
                stockDraftIds.forEach { stockDrafts.put(it) }
                val body = JSONObject()
                body.put("StockDraftIDs", stockDrafts)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getDriversCartonsListsV2(
        iSDeliverToDest: Boolean,
        iSConfirmedByRFID: Boolean,
        onSuccess: (driversCartons: Map<String, MutableList<String>>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoon/delivery-transfer/driver-cartoonsIds/v2"
        val request = object : JsonArrayRequest(Method.POST, url, null, {

            val driversCartons = mutableMapOf<String, MutableList<String>>()

            for (i in 0 until it.length()) {
                val assignedCartons = it.getJSONObject(i).getJSONArray("CartoonIDs")
                driversCartons[it.getJSONObject(i).getString("FullName")] = mutableListOf1()

                for (j in 0 until assignedCartons.length()) {
                    driversCartons[it.getJSONObject(i).getString("FullName")]?.add(/*"CN3" + */
                        assignedCartons.getString(j)
                    )
                }
            }
            onSuccess(driversCartons)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("ISDeliverToDest", iSDeliverToDest)
                body.put("ISConfirmedByRFID", iSConfirmedByRFID)
                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getDriversStockDraftsLists(
        onSuccess: (driversCartons: Map<String, MutableList<String>>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/delivery-transfer/driver-stockDraftIds"
        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val driversStockDrafts = mutableMapOf<String, MutableList<String>>()

            for (i in 0 until it.length()) {
                val assignedCartons = it.getJSONObject(i).getJSONArray("StockDraftIDs")
                driversStockDrafts[it.getJSONObject(i).getString("FullName")] = mutableListOf1()

                for (j in 0 until assignedCartons.length()) {
                    driversStockDrafts[it.getJSONObject(i).getString("FullName")]?.add(
                        assignedCartons.getString(j)
                    )
                }
            }

            onSuccess(driversStockDrafts)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(request)
    }

    fun getDriversStockDraftsListsLogisics(
        onSuccess: (logistics: Map<Int, Logistic>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/logistic"
        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val logistics = mutableMapOf<Int, Logistic>()

            val barcodesJsonArray = it.getJSONArray("logistics")
            for (i in 0 until barcodesJsonArray.length()) {

                val logistic = Logistic()
                logistic.name = (barcodesJsonArray.getJSONObject(i).getString("FullName"))
                logistic.destination =
                    barcodesJsonArray.getJSONObject(i).getString("DestWarehouseTitle")
                logistic.numberOfItems = barcodesJsonArray.getJSONObject(i).getInt("LogesticCount")
                logistics[logistic.hashCode()] = logistic
            }
            onSuccess(logistics)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getPrintersList(
        onSuccess: (printers: Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/printers"
        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val printers = mutableMapOf<String, Int>()

            for (i in 0 until it.length()) {
                printers[it.getJSONObject(i).getString("DataModelType")] =
                    it.getJSONObject(i).getInt("PrinterModels_ID")
            }
            onSuccess(printers)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getPackageTypes(
        onSuccess: (packageTypes: Map<String, Int>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/printers/packing-types"
        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val packageTypes = mutableMapOf<String, Int>()

            for (i in 0 until it.length()) {
                packageTypes[it.getJSONObject(i).getString("Title")] =
                    it.getJSONObject(i).getInt("ID")
            }
            onSuccess(packageTypes)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun createStockDraft(
        products: List<Product> = mutableListOf1(),
        desc: String,
        source: Int,
        destination: Int,
        onSuccess: (stockDraftID: Long) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/v2"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val stockDraftNumber = it.getLong("StockDraft_ID")
            onSuccess(stockDraftNumber)
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val barcodeArray = JSONArray()
                val epcArray = JSONArray()

                products.forEach {
                    repeat(it.scannedEPCNumber) { i ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("K_Name", it.kName)
                        productJson.put("epc", it.scannedEPCs[i])
                        epcArray.put(productJson)
                    }
                    repeat(it.scannedBarcodeNumber) { _ ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("K_Name", it.kName)
                        barcodeArray.put(productJson)
                    }
                    repeat(it.manualScannedNumber) { _ ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("K_Name", it.kName)
                        barcodeArray.put(productJson)
                    }
                }

                body.put("desc", desc)
                body.put("fromWarehouseId", source)
                body.put("toWarehouseId", destination)
                body.put("kbarcodes", barcodeArray)
                body.put("epcs", epcArray)

                return body.toString().toByteArray()
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun createStockDraftByStockDraftRequest(
        requestID: String,
        products: List<StockDraftRequestItem> = mutableListOf(),
        desc: String,
        source: Int,
        destination: Int,
        packageType: Int,
        onSuccess: (stockDraftID: String) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft-requests/$requestID/create-stock-draft"

        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val stockDraftNumber = it.getString("MessageText") ?: "0"
            onSuccess(stockDraftNumber)
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر موجودی را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val barcodeArray = JSONArray()
                val epcArray = JSONArray()

                products.forEach {
                    repeat(it.product.scannedEPCNumber) { i ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.product.primaryKey)
                        productJson.put("kbarcode", it.product.KBarCode)
                        productJson.put("K_Name", it.product.kName)
                        productJson.put("epc", it.product.scannedEPCs[i])
                        epcArray.put(productJson)
                    }
                    repeat(it.product.scannedBarcodeNumber) { _ ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.product.primaryKey)
                        productJson.put("kbarcode", it.product.KBarCode)
                        productJson.put("K_Name", it.product.kName)
                        barcodeArray.put(productJson)
                    }
                }

                body.put("desc", desc)
                body.put("fromWarehouseId", source)
                body.put("toWarehouseId", destination)
                body.put("packingTypeId", packageType)
                body.put("kbarcodes", barcodeArray)
                body.put("epcs", epcArray)

                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }


    fun createStockDraftByCarton(
        products: List<Carton> = mutableListOf1(),
        desc: String,
        source: Int,
        destination: Int,
        driver: Int,
        onSuccess: (message: String) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoons/transfer/v2"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            val message = it.getLong("StockDraft_ID").toString()
            onSuccess(message)
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر دوباره امتحان کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val cartonNumberArray = JSONArray()

                products.forEach {
                    cartonNumberArray.put(it.number)
                }

                body.put("description", desc)
                body.put("sourceWareHouseID", source)
                body.put("destWareHouseID", destination)
                body.put("driverID", driver)
                body.put("cartoonsNum", cartonNumberArray)

                Log.e(this@API.tag, body.toString())

                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }


    fun createCarton(
        products: List<Product> = mutableListOf1(),
        source: Int,
        printer: Int,
        onSuccess: (cartonNumber: String) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/cartoons/v2"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess(it.getString("CartoonNum"))
        }, {
            when (it?.networkResponse?.statusCode) {
                504 -> {
                    showLog(
                        "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر خروجی پرینتر را چک کنید.",
                        state,
                        action = SnackBarActions.WARNING
                    )
                }

                409 -> {
                    val error = it.networkResponse?.data?.decodeToString()?.let { errorString ->
                        try {
                            JSONObject(errorString).getJSONObject("error").getString("message")
                        } catch (_: Exception) {

                        }
                    }
                    // Parse the string into a JSONArray
                    val jsonArray = JSONArray(error.toString())
                    if (jsonArray.length() > 0) {
                        // Get the first element as a JSONObject
                        val firstElement = jsonArray.getJSONObject(0)

                        // Extract the fields
                        val cartoonsId = firstElement.getString("Cartoons_ID")
                        val epc = firstElement.getString("epc")
                        val kbarcode = firstElement.getString("kbarcode")
                        showLog(
                            "کالا با بارکد: $kbarcode و epc: $epc قبلا با در کارتن شماره: $cartoonsId ایجاد کارتن شده است.",
                            state,
                            action = SnackBarActions.ERROR
                        )
                    } else {
                        showLog(
                            "کالاهای موردنظر قبلا در همین انبار ایجاد کارتن شدند",
                            state,
                            action = SnackBarActions.ERROR
                        )
                    }
                }

                else -> {
                    apiErrorProcess(state, it)
                }
            }
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val productsArray = JSONArray()

                products.forEach {
                    it.scannedEPCs.forEach { epc ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("epc", epc)
                        productJson.put("qty", 1)
                        productsArray.put(productJson)
                    }
                    if (it.scannedBarcodeNumber != 0) {
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("qty", it.scannedBarcodeNumber)
                        productsArray.put(productJson)
                    }
                    if (it.manualScannedNumber != 0) {
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.primaryKey)
                        productJson.put("kbarcode", it.KBarCode)
                        productJson.put("qty", it.manualScannedNumber)
                        productsArray.put(productJson)
                    }
                }
                body.put("WareHouseID", source)
                body.put("products", productsArray)
                body.put("printerId", printer)

                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun createCartonByRequest(
        stockDraftRequestNumber: String,
        printer: Int,
        packageType: Int,
        desc: String,
        products: List<StockDraftRequestItem> = mutableListOf(),
        onSuccess: (cartonNumber: String) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft-requests/$stockDraftRequestNumber/create-cartoon"

        val request = object : JsonObjectRequest(Method.POST, url, null, {
            onSuccess(it.getString("MessageText"))
        }, {
            if (it?.networkResponse?.statusCode == 504) {
                showLog(
                    "درخواست انجام شده است اما سرور پاسخ نمی دهد. جهت اطمینان بیشتر خروجی پرینتر را چک کنید.",
                    state,
                    action = SnackBarActions.WARNING
                )
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val productsArray = JSONArray()

                products.forEach {

                    it.product.scannedEPCs.forEach { epc ->
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.product.primaryKey)
                        productJson.put("kbarcode", it.product.KBarCode)
                        productJson.put("epc", epc)
                        productJson.put("qty", 1)
                        productsArray.put(productJson)
                    }
                    if (it.product.scannedBarcodeNumber != 0) {
                        val productJson = JSONObject()
                        productJson.put("BarcodeMain_ID", it.product.primaryKey)
                        productJson.put("kbarcode", it.product.KBarCode)
                        productJson.put("qty", it.product.scannedBarcodeNumber)
                        productsArray.put(productJson)
                    }
                }

                body.put("products", productsArray)
                body.put("Description", desc)
                body.put("PrinterModels_ID", printer)
                body.put("packingTypeId", packageType)

                Log.e("api", body.toString())

                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun getWarehouseProducts(
        warehouseCode: String,
        onSuccess: (barcodes: List<String>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/products/$warehouseCode"

        val request = object : JsonObjectRequest(url, {

            val products = it.getJSONArray("products")

            val barcodes = mutableListOf1<String>()

            barcodes.clear()
            for (i in 0 until products.length()) {

                try {
                    barcodes.add(products.getJSONObject(i).getString("KBarCode"))
                } catch (e: Exception) {
                    commonCatchHandler(e)
                    showLog("مشکلی در پردازش برخی اطلاعات کالاهای انبار وجود دارد.", state)
                }
            }
            onSuccess(barcodes)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        val apiTimeout = 60000
        request.retryPolicy = DefaultRetryPolicy(
            apiTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(request)
    }

    fun operatorLogin(
        username: String,
        password: String,
        onSuccess: (token: String) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/login/operators"
        val request = object : JsonObjectRequest(Method.POST, url, null, fun(it) {
            val token = it.getString("accessToken")
            onSuccess(token)
        }, {

            if (it?.networkResponse?.statusCode == 401) {
                showLog("نام کاربری یا رمز عبور اشتباه است.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {

            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("username", username)
                body.put("password", password)
                return body.toString().toByteArray()
            }
        }

        queue.add(request)
    }

    fun registerDevice(
        token: String = "",
        deviceSerialNumber: String,
        onSuccess: (deviceId: String, iotToken: String) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/devices/handheld"
        val request = object : JsonObjectRequest(Method.POST, url, null, {
            showLog("دستگاه با موفقیت رجیستر شد", state, action = SnackBarActions.SUCCESS)
            val deviceId = it.getString("deviceId")
            val iotToken = it.getJSONObject("authentication").getJSONObject("symmetricKey")
                .getString("primaryKey")
            onSuccess(deviceId, iotToken)
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {

                val header = mutableMapOf<String, String>()
                header.putAll(this@API.header)
                header["Authorization"] = "Bearer $token"
                Log.e(this@API.tag, "header is empty: $header")
                return header
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("serialNumber", deviceSerialNumber)
                Log.e(this@API.tag, "body: $body")
                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    override fun userLogin(
        username: String,
        password: String,
        locationCode: Int,
        onSuccess: (user: User) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/login"

        val jsonRequest = object : JsonObjectRequest(Method.POST, url, null, { response ->

            val token = response.getString("accessToken")
            val fullName = response.getString("fullName")
            val usernameFromServer = response.getInt("personInfoID")
            val assignedLocation = response.getInt("locationCode")
            val assignedWarehouse = response.getJSONObject("location").getInt("warehouseCode")
            val accessLevel = response.getJSONArray("permissions")
            val warehouses = mutableMapOf<String, String>()
            val sortedDestinationsTitles = mutableListOf1<String>()
            val mappedDestination = mutableMapOf<String, Int>()
            val accessLevelList = mutableListOf1<String>()
            val warehousesJsonArray = response.getJSONArray("warehouses")
            val destinationsJsonArray = response.getJSONArray("destinationWarehouses")
            var storeWarehouseCode = 0
            var storeShopCode = 0
            val assignedDepartment = response.getJSONObject("location").getInt("departmentInfo_ID")
            val assignedDepartmentType =
                response.getJSONObject("location").getInt("departmentType_ID")
            val assignedWarehouseToDepartmentMap = mutableMapOf<Int, Int>()

            for (i in 0 until warehousesJsonArray.length()) {

                val warehouseCode = warehousesJsonArray.getJSONObject(i).getString("WareHouse_ID")
                warehouses[warehouseCode] =
                    warehousesJsonArray.getJSONObject(i).getString("WareHouseTitle")

                val warehouseDepartmentCode =
                    warehousesJsonArray.getJSONObject(i).getString("DepartmentInfo_ID")
                        .toIntOrNull() ?: 0
                if (warehouseDepartmentCode == assignedLocation) {
                    val warehouseType =
                        warehousesJsonArray.getJSONObject(i).getString("WareHouseTypes_ID")
                            .toIntOrNull() ?: 0
                    if (warehouseType == 1) {
                        storeShopCode = warehouseCode.toIntOrNull() ?: 0
                    } else if (warehouseType == 2 || warehouseType == 4) {
                        storeWarehouseCode = warehouseCode.toIntOrNull() ?: 0
                    }
                }

                if (warehouseDepartmentCode != 0 && warehouseCode.toIntOrNull() != null) {
                    assignedWarehouseToDepartmentMap[warehouseCode.toInt()] =
                        warehouseDepartmentCode
                }
            }

            for (i in 0 until accessLevel.length()) {
                accessLevelList.add(accessLevel.get(i).toString().dropLast(3))
            }

            for (i in 0 until destinationsJsonArray.length()) {
                sortedDestinationsTitles.add(
                    destinationsJsonArray.getJSONObject(i).getString("WareHouseTitle")
                )
                mappedDestination[destinationsJsonArray.getJSONObject(i)
                    .getString("WareHouseTitle")] =
                    destinationsJsonArray.getJSONObject(i).getInt("WareHouse_ID")
            }

            val sortedWarehouseTitles = warehouses.values.toMutableList()
            sortedWarehouseTitles.sortBy { it1 ->
                it1
            }

            sortedDestinationsTitles.sortBy { it1 ->
                it1
            }

            val user = User(
                token = token,
                name = fullName,
                username = usernameFromServer,
                locationCode = assignedLocation,
                warehouseCode = assignedWarehouse,
                warehouses = warehouses,
                warehousesTitlesSorted = sortedWarehouseTitles,
                destinationTitles = sortedDestinationsTitles,
                access = accessLevelList,
                destinationMapWithId = mappedDestination,
                storeWarehouseCode = storeWarehouseCode,
                storeShopCode = storeShopCode,
                assignedDepartment = assignedDepartment,
                assignedDepartmentType = assignedDepartmentType,
                warehouseToDepartmentMap = assignedWarehouseToDepartmentMap,
            )

            onSuccess(user)

        }, {

            if (it?.networkResponse?.statusCode == 401) {
                showLog("نام کاربری یا رمز عبور اشتباه است.", state)
            } else {
                apiErrorProcess(state, it)
            }
            onError()
        }) {

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("username", username)
                body.put("password", password)
                return body.toString().toByteArray()
            }

            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        queue.add(jsonRequest)
    }

    fun saveInventoryDataGetId(
        warehouseCode: String,
        onSuccess: (id: String) -> Unit,
        onError: () -> Unit,
    ) {
        val url = "$serverAddress/mojodi-review/header"
        val request = object : JsonObjectRequest(Method.POST, url, null, {

            val id = it.getString("MojodiReviewInfo_ID")
            onSuccess(id)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }

            override fun getBody(): ByteArray {

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getDefault()
                val body = JSONObject()
                body.put("CreateDate", sdf.format(Date()))
                body.put("desc", "انبارگردانی با RFID")
                body.put("Warehouse_ID", warehouseCode.toInt())

                return body.toString().toByteArray()
            }
        }

        val apiTimeout = 60000
        request.retryPolicy = DefaultRetryPolicy(
            apiTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(request)
    }

    fun getStockDraftIDs(
        onSuccess: (stockDrafts: List<Long>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/stock-draft/pending"

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val stockDraftsIDs = mutableListOf1<Long>()

            for (i in 0 until it.length()) {

                stockDraftsIDs.add(
                    it.getJSONObject(i).getString("StockDraft_ID").toLong(),
                )
            }

            onSuccess(stockDraftsIDs)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        queue.add(request)
    }

    fun getAllStockDraftRequestsList(
        sourceWarehouse: Int,
        createUserId: Int?,
        isJoorComplete: Boolean?,
        requestTpeId: Int?,
        onSuccess: (stockDrafts: List<StockDraftRequest>) -> Unit,
        onError: () -> Unit,
    ) {

        // Base URL
        val baseUrl = "$serverAddress/stock-draft-requests/$sourceWarehouse"

        // Create a list of query parameters
        val queryParams = mutableListOf<String>()

        if (createUserId != null) {
            queryParams.add("CreateUserID=$createUserId")
        }
        if (isJoorComplete != null) {
            queryParams.add("IsJoorComplete=$isJoorComplete")
        }
        if (requestTpeId != null) {
            queryParams.add("RequestTypeID=$requestTpeId")
        }

        // Append query parameters to URL if any exist
        val url = if (queryParams.isNotEmpty()) {
            "$baseUrl?${queryParams.joinToString("&")}"
        } else {
            baseUrl
        }

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val stockDraftRequests = mutableListOf1<StockDraftRequest>()

            for (i in 0 until it.length()) {

                val number = it.getJSONObject(i).getString("StockDraftRequest_ID")
                mutableListOf1<String>()
                mutableListOf1<String>()
                val collectorName = it.getJSONObject(i).getString("FullName")
                val source = it.getJSONObject(i).getString("SourceWareHouseTitle")
                val destination = it.getJSONObject(i).getString("WareHouseTitle")
                val jalaliCreateDate = it.getJSONObject(i).getString("RequestDate")
                val numberOfItems = it.getJSONObject(i).getInt("Qty")
                val foundNumber = it.getJSONObject(i).getInt("QtyJoor")
                val controlNumber = it.getJSONObject(i).getInt("QtyControl")
                val userCode = it.getJSONObject(i).getString("JoorPersonInfo_ID").toIntOrNull()
                val specification = it.getJSONObject(i).getString("Description")

                val stockDraftRequest = StockDraftRequest(
                    number = number.toLong(),
                    sumOfRequestedItems = numberOfItems,
                    date = jalaliCreateDate,
                    source = source,
                    destination = destination,
                    collectorName = collectorName,
                    specification = specification,
                    sumOfFoundItems = foundNumber,
                    sumOfControlledItems = controlNumber,
                    user = userCode,
                    items = listOf(),
                    itemsDistinctByKBarcode = mutableMapOf()
                )
                stockDraftRequests.add(stockDraftRequest)
            }

            onSuccess(stockDraftRequests)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun getAllStockDraftRequestsListV2(
        sourceWarehouse: Int?,
        createUserId: Int?,
        isJoorComplete: Boolean?,
        requestTypeId: Int?,
        onSuccess: (stockDrafts: List<StockDraftRequest>) -> Unit,
        onError: () -> Unit,
    ) {

        // Base URL
        val baseUrl = "$serverAddress/stock-draft-requests"

        // Create a list of query parameters
        val queryParams = mutableListOf<String>()

        if (sourceWarehouse != null) {
            queryParams.add("SourceWareHouseID=$sourceWarehouse")
        }
        if (createUserId != null) {
            queryParams.add("CreateUserID=$createUserId")
        }
        if (isJoorComplete != null) {
            queryParams.add("IsJoorComplete=$isJoorComplete")
        }
        if (requestTypeId != null) {
            queryParams.add("RequestTypeID=$requestTypeId")
        }

        // Append query parameters to URL if any exist
        val url = if (queryParams.isNotEmpty()) {
            "$baseUrl?${queryParams.joinToString("&")}"
        } else {
            baseUrl
        }

        val request = object : JsonArrayRequest(Method.GET, url, null, {

            val stockDraftRequests = mutableListOf1<StockDraftRequest>()

            for (i in 0 until it.length()) {

                val number = it.getJSONObject(i).getString("StockDraftRequest_ID")
                mutableListOf1<String>()
                mutableListOf1<String>()
                val collectorName = it.getJSONObject(i).getString("FullName")
                val source = it.getJSONObject(i).getString("SourceWareHouseTitle")
                val destination = it.getJSONObject(i).getString("WareHouseTitle")
                val jalaliCreateDate = it.getJSONObject(i).getString("RequestDate")
                val numberOfItems = it.getJSONObject(i).getInt("Qty")
                val foundNumber = it.getJSONObject(i).getInt("QtyJoor")
                val controlNumber = it.getJSONObject(i).getInt("QtyControl")
                val userCode = it.getJSONObject(i).getString("JoorPersonInfo_ID").toIntOrNull()
                val specification = it.getJSONObject(i).getString("Description")

                val stockDraftRequest = StockDraftRequest(
                    number = number.toLong(),
                    sumOfRequestedItems = numberOfItems,
                    date = jalaliCreateDate,
                    source = source,
                    destination = destination,
                    collectorName = collectorName,
                    specification = specification,
                    sumOfFoundItems = foundNumber,
                    sumOfControlledItems = controlNumber,
                    user = userCode,
                    items = listOf(),
                    itemsDistinctByKBarcode = mutableMapOf()
                )
                stockDraftRequests.add(stockDraftRequest)
            }

            onSuccess(stockDraftRequests)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }
        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun saveInventoryDataSendPackets(
        mojodiReviewId: String,
        productListForSend: List<Product>,
        scannedEpcs: Map<Long, List<String>>,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/mojodi-review/products"
        val request = object : StringRequest(Method.POST, url, {

            onSuccess()
        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }

            override fun getBody(): ByteArray {

                val body = JSONObject()
                val products = JSONArray()

                productListForSend.forEach {
                    val productJson = JSONObject()

                    productJson.put("BarcodeMain_ID", it.primaryKey)
                    productJson.put("kbarcode", it.KBarCode)
                    productJson.put("K_Name", it.kName)
                    productJson.put("diffCount", it.manualScannedNumber)
                    productJson.put(
                        "epcs", JSONArray(scannedEpcs[it.primaryKey] ?: mutableListOf1<String>())
                    )
                    products.put(productJson)
                }
                Log.e("products", products.toString())
                body.put("MojodiReviewInfo_ID", mojodiReviewId.toLong())
                body.put("products", products)
                return body.toString().toByteArray()
            }
        }

        request.retryPolicy = requestSetting
        queue.add(request)
    }

    fun saveInventoryDataConfirm(
        id: String,
        onSuccess: () -> Unit,
        onError: (it: VolleyError) -> Unit,
    ) {

        val url = "$serverAddress/mojodi-review/$id/submit"
        val request = object : StringRequest(Method.POST, url, {
            showLog(
                "اطلاعات انبارگردانی با موفقیت ثبت شدند.", state, action = SnackBarActions.SUCCESS
            )
            onSuccess()
        }, {

            when (it) {
                is NoConnectionError -> {
                    showLog("اینترنت قطع است. شبکه وای فای را بررسی کنید.", state)
                }

                is TimeoutError -> {
                    showLog("اطلاعات انبارگردانی با موفقیت ثبت شدند.", state)
                }

                else -> {
                    val error = it?.networkResponse?.data?.decodeToString()?.let { it1 ->
                        try {
                            JSONObject(it1).getJSONObject("error").getString("message")
                        } catch (e: Exception) {
                            commonCatchHandler(e)
                            it1
                        }
                    } ?: "مشکلی در ارتباط با سرور به وجود آمده است."

                    showLog(error, state)
                }
            }
            onError(it)
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.add(request)
    }

    fun getInventoryResult(
        sourceWarehouse: Int,
        onSuccess: (results: List<Inventory>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/mojodi-review/info?DepartmentID=$sourceWarehouse"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val items = it.getJSONArray("items")
            val result = mutableListOf1<Inventory>()
            for (i in 0 until items.length()) {
                val inventoryId = items.getJSONObject(i).getString("MojodiReviewInfo_ID")
                val creatorName = items.getJSONObject(i).getString("CreateUserFullName")
                val createDate = items.getJSONObject(i).getString("CreateDate")
                val miladiCreateDate =
                    items.getJSONObject(i).getString("CreateDate").substring(0, 10)
                val intArrayFormatJalaliCreateDate = JalaliDateConverter.gregorian_to_jalali(
                    miladiCreateDate.substring(0, 4).toInt(),
                    miladiCreateDate.substring(5, 7).toInt(),
                    miladiCreateDate.substring(8, 10).toInt()
                )
                val jalaliCreateDate =
                    "${intArrayFormatJalaliCreateDate[0]}/${intArrayFormatJalaliCreateDate[1]}/${intArrayFormatJalaliCreateDate[2]}"
                val des = items.getJSONObject(i).getString("ReviewDesc")
                val wareHouse = items.getJSONObject(i).getString("WareHouse_ID")
                val item = Inventory(
                    inventoryId, creatorName, createDate, jalaliCreateDate, des, false, wareHouse
                )
                result.add(item)
            }
            onSuccess(result)

        }, {
            Log.e("inventory error", it.message.toString())
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        queue.add(request)
    }

    fun getAllInventoryDetailsItems(
        inventoryID: String,
        onSuccess: (allInventoryItems: List<InventoryItem>) -> Unit,
        onError: () -> Unit,
    ) {
        allInventoryItems.clear()
        getAllInventoryData(inventoryID, 1, {
            onSuccess(allInventoryItems)
        }, {
            onError()
        })
    }

    private fun getAllInventoryData(
        inventoryID: String,
        page: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {

        val perPage = 500

        getInventoryDataById(
            inventoryID = inventoryID,
            page = page,
            perPage = perPage,
            { inventoryDetailsItems ->

                allInventoryItems.addAll(inventoryDetailsItems)
                if (inventoryDetailsItems.size == perPage) {
                    getAllInventoryData(inventoryID = inventoryID, page = page + 1, {
                        onSuccess()
                    }, {
                        onError()
                    })
                } else {
                    onSuccess()
                }
            },
            {
                onError()
            })
    }

    private fun getInventoryDataById(
        inventoryID: String,
        page: Int,
        perPage: Int,
        onSuccess: (results: List<InventoryItem>) -> Unit,
        onError: () -> Unit,
    ) {

        val url = "$serverAddress/mojodi-review/$inventoryID?page=$page&perPage=$perPage"

        val request = object : JsonObjectRequest(Method.GET, url, null, {

            val items = it.getJSONArray("items")
            val result = mutableListOf1<InventoryItem>()
            for (i in 0 until items.length()) {
                val mojodiReviewId = items.getJSONObject(i).getString("MojodiReview_ID")
                val mojodiReviewInfoID = items.getJSONObject(i).getString("MojodiReviewInfo_ID")
                val barcodeMainID = items.getJSONObject(i).getLong("BarcodeMain_ID")
                val productCount = items.getJSONObject(i).getInt("ProductCount")
                val itemBarcode = items.getJSONObject(i).getString("ItemBarcode")
                val currentMojodi = items.getJSONObject(i).getInt("CurrentMojodi")
                val diffMojodi = items.getJSONObject(i).getInt("DiffMojodi")
                val epcsJsonArray = items.getJSONObject(i).getJSONArray("epcs")
                val epcs = mutableListOf1<String>()
                for (index in 0 until epcsJsonArray.length()) {
                    epcs.add(epcsJsonArray.getString(index))
                }
                val item = InventoryItem(
                    mojodiReviewId,
                    mojodiReviewInfoID,
                    barcodeMainID,
                    productCount,
                    itemBarcode,
                    currentMojodi,
                    diffMojodi,
                    epcs
                )
                result.add(item)
            }

            onSuccess(result)

        }, {
            apiErrorProcess(state, it)
            onError()
        }) {
            override fun getHeaders(): Map<String, String> {


                return header
            }
        }

        request.retryPolicy = requestSetting

        queue.add(request)
    }

    fun apiErrorProcess(state: SnackbarHostState, it: VolleyError?) {

        if (it is NoConnectionError) {
            showLog("اینترنت قطع است. شبکه وای فای را بررسی کنید.", state)
        } else if (it?.networkResponse?.statusCode == 504) {
            showLog(
                "سرور مشغول است و پاسخ نمی دهد. لطفا دوباره امتحان کنید.",
                state,
                action = SnackBarActions.WARNING
            )

        } else if (it?.networkResponse?.statusCode == 404) {
            showLog(
                "درخواست یا کالا مورد نظر در سرور یافت نشد.",
                state,
                action = SnackBarActions.WARNING
            )
        } else {

            val error = it?.networkResponse?.data?.decodeToString()?.let { errorString ->
                try {
                    JSONObject(errorString).getJSONObject("error").getString("message")

                } catch (e: Exception) {
                    try {
                        JSONObject(errorString).getJSONObject("error").getString("details")
                    } catch (e1: Exception) {
                        //commonCatchHandler(e1)
                        errorString
                    }
                }
            } ?: "مشکلی در ارتباط با سرور به وجود آمده است."

            showLog(error, state)
        }
    }
}