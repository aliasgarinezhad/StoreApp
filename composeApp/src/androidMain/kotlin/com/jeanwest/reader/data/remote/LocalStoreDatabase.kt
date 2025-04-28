package com.jeanwest.reader.data.remote

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.models.User
import com.jeanwest.reader.useCases.commonCatchHandler
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.showLog
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.SentryLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/*
*
* this class used for call stored procedures or sql
* query from local store server. it finds database
* address using user default store. it implements
* Requests interface same as API class.
 */
@Singleton
class LocalStoreDatabase @Inject constructor(

    @ApplicationContext val context: Context,
    var state: SnackbarHostState,
    var popupState: NotificationPopupHost,
    var memory: SharedPreference,
) : Requests {
    private var host = "DBLSNR-1.avakatan.ir"

    private val database = "PTCERP_DEP"
    private val username = "RFID"
    private val password = "kfKXno35peu4cMJtzWN4e0NdmRpNV1"
    private var url = "jdbc:jtds:sqlserver://$host/$database"
    private var connection: Connection? = null
    var loading by mutableStateOf(false)

    init {
        if (memory.user.isStoreUser) {
            connectToServer(locationCode = memory.user.calculatedLocationCode)
        }
    }

    private fun findServerAddress(locationCode: Int) {
        host = "DB-${locationCode}-1.avakatan.ir"
        url = "jdbc:jtds:sqlserver://$host/$database"
    }

    private fun connectToServer(locationCode: Int) {

        loading = true
        CoroutineScope(IO).launch {
            connectToDB(locationCode)
            loading = false
        }
    }

    private fun connectToDB(locationCode: Int): Boolean {
        findServerAddress(locationCode)
        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            return true
        } catch (e: Exception) {
            commonCatchHandler(exception = e)
            return false
        }
    }

    private fun runQueryWithConnectionCheck(query: String): MutableMap<Int, MutableMap<Int, String>>? {

        val result: MutableMap<Int, MutableMap<Int, String>>?
        loading = true
        if (runQuery("SELECT 1").isEmpty()) {

            if (connectToDB(memory.user.locationCode)) {
                Log.e("local server", "connection retried")
                result = runQuery(query = query)
            } else {
                Log.e("local server", "error connection")
                showLog(state = state, data = "سرور قطع شده است. لطفا دوباره امتحان کنید.")
                result = null
            }
        } else {
            Log.e("local server", "connection ok")
            result = runQuery(query = query)
        }

        loading = false
        return result
    }

    private fun runQueryWithoutResultWithConnectionCheck(sp: String, qty: Int = 0): Boolean? {

        val result: Boolean?
        loading = true
        if (runQuery("SELECT 1").isEmpty()) {

            if (connectToDB(memory.user.locationCode)) {
                result = runQueryWithoutResult(sp = sp, qty = qty)
            } else {
                showLog(state = state, data = "سرور قطع شده است. لطفا دوباره امتحان کنید.")
                result = null
            }
        } else {
            result = runQueryWithoutResult(sp = sp, qty = qty)
        }

        loading = false
        return result
    }

    fun disconnectFromServer() {

        loading = true
        CoroutineScope(IO).launch {

            try {
                if (connection?.isClosed == false) {
                    connection!!.close()
                }
            } catch (e: Exception) {
                popupState.showPopupWithAButton("مشکلی در اجرا دستورات در سرور فروشگاه وجود دارد.")
            }
            loading = false
        }
    }

    private fun runQuery(query: String): MutableMap<Int, MutableMap<Int, String>> {

        val resultSet: ResultSet
        try {
            val statement = connection!!.createStatement()
            resultSet = statement.executeQuery(query)
        } catch (e: Exception) {
            popupState.showPopupWithAButton("مشکلی در اجرا دستورات در سرور فروشگاه وجود دارد.")
            saveLog(logContent = "$query\nresult: ${false}")
            return mutableMapOf()
        }

        val result = mutableMapOf<Int, MutableMap<Int, String>>()
        var rowIndex = 0
        while (resultSet.next()) {
            val rowItems = mutableMapOf<Int, String>()
            var columnIndex = 1
            while (columnIndex <= resultSet.metaData.columnCount) {
                try {
                    resultSet.getString(columnIndex)?.let {
                        rowItems[columnIndex] = it
                        columnIndex++
                    } ?: run {
                        rowItems[columnIndex] = "null"
                        columnIndex++
                    }
                } catch (e: Exception) {
                    break
                }
            }
            result[rowIndex] = rowItems
            rowIndex++
        }
        saveLog(logContent = "$query\nresult: $result")
        return result
    }

    private fun runQueryWithoutResult(sp: String, qty: Int = 0): Boolean {

        var result = false
        try {
            val statement = connection!!.createStatement()
            statement.execute(sp)
            result = true
        } catch (e: Exception) {
            popupState.showPopupWithAButton("مشکلی در اجرا دستورات در سرور فروشگاه وجود دارد.")
            saveLog(logContent = "$sp\nresult: ${false}")
            result = false
        } finally {
            saveLog("$sp\nresult: $result", qty = qty)
        }
        return result
    }

    fun printPriceLabel(
        primaryKey: Long,
        qty: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,

        ) {
        CoroutineScope(IO).launch {

            if (!runPrintPriceSP(primaryKey, "Barcode", qty)) {
                onError()
                return@launch
            }

            onSuccess()
        }
    }

    override fun getStockDraftDetails(
        code: String,
        onSuccess: (draftProperties: StockDraft) -> Unit,
        onError: () -> Unit,
    ) {
        CoroutineScope(IO).launch {
            val query =
                "SELECT\n" + "sd.BarcodeMain_ID,\n" + "sd.ItemName,\n" + "sd.ItemBarcode, \n" + "Qty,\n" + "StockDraft_ID\n" + "FROM\n" + "[WM].[StockDraftDetails] sd\n" + "WHERE [StockDraft_ID] = $code\n"
            val result = runQueryWithConnectionCheck(query)

            if (!result.isNullOrEmpty()) {

                val productList = mutableMapOf<String, Product>()
                for (i in 0 until result.keys.size) {
                    val product = Product()
                    product.primaryKey = result[i]?.get(1)?.toLong() ?: 0L
                    product.name = result[i]?.get(2).toString()
                    product.KBarCode = result[i]?.get(3).toString()
                    product.searchCodes.add(result[i]?.get(3).toString())
                    product.draftNumber = convertStringToInt(result[i]?.get(4).toString())

                    if (product.searchCodes[0] in productList) {
                        productList[product.searchCodes[0]]!!.draftNumber += product.draftNumber
                    } else {
                        productList[product.searchCodes[0]] = product.copy()
                    }
                }

                val query1 =
                    "SELECT\n" + "FromWareHouse_ID,\n" + "ToWareHouse_ID\n" + "FROM\n" + "[WM].[StockDraft] sd\n" + "WHERE StockDraft_ID = $code\n"
                val result2 = runQueryWithConnectionCheck(query1)
                val stockDraft: StockDraft
                if (!result2.isNullOrEmpty()) {
                    stockDraft = StockDraft(
                        number = code.toLong(),
                        source = result2[0]?.get(1)?.toInt() ?: 0,
                        destination = result2[0]?.get(2)?.toInt() ?: 0,
                        items = productList
                    )
                    stockDraft.items.forEach { product ->
                        repeat(product.value.draftNumber) {
                            stockDraft.barcodeTable.add(product.value.searchCodes[0])
                        }
                    }
                    stockDraft.numberOfItems = stockDraft.barcodeTable.size
                    onSuccess(stockDraft)

                } else {
                    if (result2 != null) {
                        showLog("مشخصات حواله یافت نشد.", state)
                    }
                    onError()
                }
            } else {
                if (result != null) {
                    showLog("مشخصات حواله یافت نشد.", state)
                }
                onError()
            }

        }
    }

    override fun getItemDetails(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
    ) {
        getItemDetailsAndInventory(epcs = epcs,
            barcodes = barcodes,
            onSuccess = { resultEpcs, resultBarcodes, invalidEpcs, invalidBarcodes ->
                onSuccess(resultEpcs, resultBarcodes, invalidEpcs, invalidBarcodes)
            },
            onError = { onError() })
    }

    override fun getItemDetailsAndInventory(
        epcs: List<String>,
        barcodes: List<String>,
        onSuccess: (epcs: List<Product>, barcodes: List<Product>, invalidEpcs: JSONArray, invalidBarcodes: JSONArray) -> Unit,
        onError: () -> Unit,
        local: Boolean,
    ) {

        val invalidBarcodes = JSONArray()
        val barcodesDetails = mutableListOf<Product>()
        val barcode = barcodes[0]

        getProductDetails(barcode = barcode, onSuccess = { product ->
            if (product.KBarCode == "") {
                invalidBarcodes.put(barcode)
            } else {
                barcodesDetails.add(product)
            }
            onSuccess(listOf(), barcodesDetails, JSONArray(), invalidBarcodes)
        }, onError = {
            onError()
        })
    }

    private fun getProductDetails(
        barcode: String,
        onSuccess: (Product) -> Unit,
        onError: () -> Unit,
    ) {
        CoroutineScope(IO).launch {

            val query = if (!memory.user.isLocalMode) {
                """
                EXEC RFID_FindProducts
                @WarehouseID = '${memory.user.storeWarehouseCode}, ${memory.user.storeShopCode}'
                ,@State = 'KBarcode'
                ,@KBarcode = '$barcode'
            """.trimIndent()
            } else {
                """
                EXEC RFID_FindProducts
                @WarehouseID = '${memory.user.warehouseCode}'
                ,@State = 'KBarcode'
                ,@KBarcode = '$barcode'
            """.trimIndent()
            }

            val result = runQueryWithConnectionCheck(query)

            if (!result.isNullOrEmpty()) {
                val product = Product()
                product.primaryKey = result[0]?.get(1)?.toLong() ?: 0
                product.KBarCode = result[0]?.get(2).toString()
                product.scannedBarcode = barcode
                product.searchCodes = mutableListOf(barcode, result[0]?.get(3).toString())
                product.productCode = result[0]?.get(4).toString()
                product.kName = result[0]?.get(5).toString()
                product.name = result[0]?.get(6).toString()
                product.originalPrice = result[0]?.get(7).toString()
                product.salePrice = result[0]?.get(8).toString()
                product.departmentName = result[0]?.get(11).toString()
                product.imageUrl = result[0]?.get(13).toString()
                product.rfidKey = result[0]?.get(14)?.toLongOrNull() ?: 0L
                product.color = result[0]?.get(16).toString()
                product.size = result[0]?.get(17).toString()
                product.brandName = result[0]?.get(18).toString()

                if (!memory.user.isLocalMode) {
                    if (result[0]?.get(19)?.toInt() == memory.user.storeWarehouseCode) {
                        product.wareHouseNumber = result[0]?.get(20)?.toFloat()?.toInt() ?: 0
                        product.storeNumber = result[1]?.get(20)?.toFloat()?.toInt() ?: 0
                    } else {
                        product.storeNumber = result[0]?.get(20)?.toFloat()?.toInt() ?: 0
                        product.wareHouseNumber = result[1]?.get(20)?.toFloat()?.toInt() ?: 0
                    }
                } else {
                    product.wareHouseNumber = result[0]?.get(20)?.toFloat()?.toInt() ?: 0
                }

                onSuccess(product)
            } else {
                if (result != null) {
                    onSuccess(Product())
                    showLog("مشخصات بارکد $barcode یافت نشد.", state)
                } else {
                    onError()
                }
            }
        }
    }

    fun printPriceLabelWithBarcode(
        barcode: String,
        qty: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit,
    ) {
        CoroutineScope(IO).launch {
            if (!runPrintPriceSPForBarcode(barcode, "Barcode", qty)) {
                onError()
                return@launch
            }

            onSuccess()
        }
    }

    override fun userLogin(
        username: String,
        password: String,
        locationCode: Int,
        onSuccess: (user: User) -> Unit,
        onError: () -> Unit,
    ) {
        CoroutineScope(IO).launch {

            connectToServer(locationCode = locationCode)

            while (true) {
                if (!loading) {
                    break
                }
                delay(1000)
            }

            val hashedOPass = password.toMD5()

            val query =
                "SELECT u.Users_ID, u.UserName, u.UserPassword, u.PersonInfoID, u.WareHouseDef,\n" + "        u.DepCodeDef, p.FullName, wh.WareHouseTitle, di.DepName, wh.WareHouseTypes_ID\n" + "      FROM Person.Users u\n" + "      INNER JOIN Person.PersonInfo p ON u.PersonInfoID = p.PersonInfo_ID\n" + "      INNER JOIN Accounting.WareHouse wh ON wh.WareHouse_ID = u.WareHouseDef\n" + "      INNER JOIN Accounting.DepartmentInfo di ON di.DepartmentInfo_ID  = u.DepCodeDef\n" + "      WHERE (\n" + "        u.UserName = TRY_CONVERT(VARCHAR, $username) OR u.PersonInfoID = TRY_CONVERT(BIGINT, $username)\n" + "      )     AND u.IsActive = 1 AND u.IsDeleted = 0"
            val credentialResult = runQueryWithConnectionCheck(query)
            if (credentialResult.isNullOrEmpty()) {
                if (credentialResult != null) {
                    showLog("مشکلی هنگام ورود به حساب کاریری پیش آمده است.", state)
                }
                onError()
                return@launch
            } else {
                if (hashedOPass == credentialResult[0]?.get(3).toString()) {

                    val userFullName = credentialResult[0]?.get(7).toString()
                    val department = credentialResult[0]?.get(6)?.toInt()
                    val warehouseCode = credentialResult[0]?.get(5)?.toInt()
                    val dataBaseUserName = credentialResult[0]?.get(4)!!.toInt()

                    getLoginInfo(username, { warehousesTitlesSorted, warehouses ->

                        val user = User(
                            username = dataBaseUserName,
                            name = userFullName,
                            token = "offline mode",
                            warehouses = warehouses,
                            warehousesTitlesSorted = warehousesTitlesSorted,
                            locationCode = department ?: 0,
                            warehouseCode = warehouseCode ?: 0,
                        )

                        onSuccess(user)

                    }, {
                        onError()
                    })

                } else {
                    showLog("نام کاربری یا رمز عبور اشتباه است.", state)
                    onError()
                    return@launch
                }
            }
        }
    }

    private fun getLoginInfo(
        username: String,
        onSuccess: (warehousesTitlesSorted: MutableList<String>, warehouses: MutableMap<String, String>) -> Unit,
        onError: () -> Unit,
    ) {
        CoroutineScope(IO).launch {
            val query =
                " DECLARE @PersonID BIGINT = $username\n" + "      SELECT\n" + "        wh.WareHouse_ID AS WareHouse_ID,\n" + "        wh.WareHouseTitle AS WareHouseTitle,\n" + "        wh.WareHouseTypes_ID AS WareHouseTypes_ID,\n" + "        wh.DepartmentInfo_ID AS DepartmentInfo_ID\n" + "      FROM Accounting.WareHouse wh WITH (Nolock)\n" + "      WHERE wh.WareHouse_ID IN(\n" + "        SELECT ua.WareHouseDef\n" + "        FROM Person.UserAnbs ua\n" + "        WHERE ua.PersonInfo_ID = @PersonID\n" + "      )"
            val result = runQueryWithConnectionCheck(query)
            if (result.isNullOrEmpty()) {
                if (result != null) {
                    showLog("مشکلی هنگام ورود به حساب کاریری پیش آمده است.", state)
                }
                onError()
                return@launch
            } else {
                val warehouses = mutableMapOf<String, String>()
                for (i in 0 until result.keys.size) {
                    val wareId = result[i]?.get(1)
                    val wareTitle = result[i]?.get(2)
                    warehouses[wareId.toString()] = wareTitle.toString()
                }

                val warehousesTitlesSorted = warehouses.values.toMutableList()
                warehousesTitlesSorted.sortBy { it1 ->
                    it1
                }
                onSuccess(warehousesTitlesSorted, warehouses)
            }
        }
    }

    private fun runPrintPriceSP(primaryKey: Long, printerModel: String, qty: Int): Boolean {

        val query =
            "EXEC [dbo].[PrintModelByRecordId] " + "@RecordID = $primaryKey, " + "@DataModelType = \"$printerModel\"," + "@QTY = $qty"

        Log.e("query", query)

        return runQueryWithoutResultWithConnectionCheck(query) == true
    }

    private fun runPrintPriceSPForBarcode(
        barcode: String,
        printerModel: String,
        qty: Int,
    ): Boolean {

        val query =
            "DECLARE\n" + "@kbarcode varchar(100) = '$barcode'\n" + ", @barcodeMainID BIGINT = null\n" + ", @qty INT = $qty\n" + ", @dataModelType varchar (100) = '$printerModel'\n" + "IF @kbarcode is not NULL AND @barcodeMainID is NULL\n" + "BEGIN\n" + "SELECT @barcodeMainID = BarcodeMain_ID from ViewKalabarcodeExtraSearch vkes\n" + "WHERE kbarcode = @kbarcode\n" + "END\n" + "EXEC [dbo].[PrintModelByRecordId]\n" + "@RecordID = @barcodeMainID\n" + ", @DataModelType = @dataModelType\n" + ", @QTY = @qty"

        return runQueryWithoutResultWithConnectionCheck(query) == true
    }

    private fun clearStockDraftTempTable(user: Int): Boolean {
        return runQueryWithoutResultWithConnectionCheck("DELETE WM.StackDraftTemp WHERE [CreateUserID] = $user") == true
    }

    private fun insertIntoStockDraftTemp(
        user: Int,
        source: Int,
        destination: Int,
        products: MutableList<Product> = mutableListOf(),
    ): Boolean {

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        val date = sdf.format(Date())
        var query =
            "INSERT INTO WM.StackDraftTemp(" + "[IsDeleted], " + "[IsEditable], " + "[CreateUserID], " + "[CreateDate], " + "[UpdateUserID], " + "[UpdateDate], " + "[RFIDEPC], " + "[BarcodeMain_ID], " + "[ItemName], " + "[ItemBarcode], " + "[SourceWareHouse_ID], " + "[DestWareHouse_ID], " + "[Qty] " + ") " + "VALUES "

        var itemsSize = 0

        products.forEachIndexed { index, it ->

            it.scannedEPCs.forEachIndexed { index2, epc ->
                query += "(" + "0, 1, " + "$user, '$date', $user, '$date', " + "'$epc', " + "${it.primaryKey}, " + "'${it.kName}', " + "'${it.KBarCode}', $source, $destination, 1" + if (index2 != it.scannedEPCs.size - 1) "), " else if (it.scannedBarcodeNumber > 0) "), " else ""
                itemsSize += 1
            }
            if (it.scannedBarcodeNumber > 0) {
                query += "(" + "0, 1, " + "$user, '$date', $user, '$date', " + "null, " + "${it.primaryKey}, " + "'${it.kName}', " + "'${it.scannedBarcode}', $source, $destination, ${it.scannedBarcodeNumber}"

                itemsSize += it.scannedBarcodeNumber
            }

            if (it.scannedNumber != 0) {
                query += if (index != products.size - 1) "), " else ")"
            }
        }

        return runQueryWithoutResultWithConnectionCheck(query, qty = itemsSize) == true
    }

    fun createStockDraft(
        user: Int,
        source: Int,
        destination: Int,
        products: MutableList<Product> = mutableListOf(),
        desc: String,
        onSuccess: (stockDraftID: String) -> Unit,
        onError: () -> Unit,
    ) {

        if (products.filter { it1 ->
                it1.scannedNumber > 0
            }.toList().isEmpty()) {
            popupState.showPopupWithAButton("کالایی برای ارسال وجود ندارد")
            onError()
            return
        }

        CoroutineScope(IO).launch {

            if (!clearStockDraftTempTable(user)) {
                onError()
                return@launch
            }

            if (!insertIntoStockDraftTemp(user, source, destination, products)) {
                onError()
                return@launch
            }

            val query =
                "DECLARE @StockDraft_ID BIGINT " + "EXEC RFID_CreateStockDraft " + "@PersonInfoID = $user, " + "@FromWareHouseID = $source, " + "@ToWareHouse_ID = $destination, " + "@StockDraftDescription = \"$desc\", " + "@StockDraftID = @StockDraft_ID OUTPUT " + "SELECT @StockDraft_ID"

            val stockDraftCreateSPResults = runQueryWithConnectionCheck(query)
            if (stockDraftCreateSPResults.isNullOrEmpty()) {
                onError()
                return@launch
            }

            if (stockDraftCreateSPResults[0]?.get(1) == "OK") {
                onSuccess(stockDraftCreateSPResults[0]?.get(2).toString())
            } else {
                onError()
                popupState.showPopupWithAButton(stockDraftCreateSPResults[0]?.get(2).toString())
            }
        }
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

        if (products.filter { it1 ->
                it1.scannedNumber > 0
            }.toList().isEmpty()) {
            popupState.showPopupWithAButton("کالایی برای ارسال وجود ندارد")
            onError()
            return
        }

        CoroutineScope(IO).launch {

            var query =
                "EXEC [dbo].[RFID_CreateRequestStock] " + "@State = \"DelProductList\", " + "@PersonID = $user, " + "@FromWareHouse_ID = $source, " + "@ToWareHouse_ID = $destination, " + "@SotckDraftRequestType_ID = $stockDraftRequestType, " + "@QTY = 0, " + "@SearchCode = \" \" "

            if (runQueryWithoutResultWithConnectionCheck(query) != true) {
                onError()
                return@launch
            }

            products.forEach {
                query =
                    "EXEC [dbo].[RFID_CreateRequestStock] " + "@State = \"InsProductList\", " + "@PersonID = $user, " + "@FromWareHouse_ID = $source, " + "@ToWareHouse_ID = $destination, " + "@SotckDraftRequestType_ID = $stockDraftRequestType, " + "@QTY = ${it.scannedNumber}, " + "@SearchCode = \"${it.scannedBarcode}\" "

                if (runQueryWithoutResultWithConnectionCheck(
                        query,
                        qty = it.scannedNumber
                    ) != true
                ) {
                    onError()
                    return@launch
                }
            }

            query =
                "EXEC [dbo].[RFID_CreateRequestStock] " + "@State = \"CreateProductRequest\", " + "@PersonID = $user, " + "@FromWareHouse_ID = $source, " + "@ToWareHouse_ID = $destination, " + "@SotckDraftRequestType_ID = $stockDraftRequestType, " + "@QTY = 0, " + "@SearchCode = \" \" "

            val stockDraftCreateSPResults = runQueryWithConnectionCheck(query)
            if (stockDraftCreateSPResults.isNullOrEmpty()) {
                onError()
                return@launch
            }

            if (stockDraftCreateSPResults[0]?.get(1) == "OK") {
                onSuccess(stockDraftCreateSPResults[0]?.get(2).toString())
            } else {
                onError()
                popupState.showPopupWithAButton(stockDraftCreateSPResults[0]?.get(2).toString())
            }
        }
    }

    fun confirmStockDraft(
        user: Int,
        source: Int,
        destination: Int,
        code: Long,
        products: MutableList<Product> = mutableListOf(),
        onSuccess: (stockDraftID: String) -> Unit,
        onError: (errorMessage: String) -> Unit,
    ) {
        CoroutineScope(IO).launch {

            if (!clearStockDraftTempTable(user)) {
                onError("مشکلی در پاک کردن جدول تمپ پیش آمده است")
                return@launch
            }

            if (products.isNotEmpty()) {
                if (!insertIntoStockDraftTemp(user, source, destination, products)) {
                    onError("مشکلی در وارد کردن اطلاعات به جدول تمپ به وجود آمده است")
                    return@launch
                }
            }
            val query =
                "EXEC RFID_StockDraftAdditionSubtract " + "@StockDraft_ID = $code, " + "@CurrentWareHouse_ID = $source, " + "@CurrentUserID = $user, " + "@FromWareHouse_ID = $source, " + "@ToWareHouse_ID = $destination "

            val stockDraftCreateSPResults = runQueryWithConnectionCheck(query)
            if (stockDraftCreateSPResults.isNullOrEmpty()) {
                onError("مشکلی در تایید حواله پیش آمده است")
                return@launch
            }

            if (stockDraftCreateSPResults[0]?.get(1) == "OK" || stockDraftCreateSPResults[0]?.get(
                    2
                ).toString().contains("حواله نهايي شده است")
            ) {
                onSuccess(stockDraftCreateSPResults[0]?.get(2).toString())
            } else {
                onError(stockDraftCreateSPResults[0]?.get(2).toString())
                popupState.showPopupWithAButton(stockDraftCreateSPResults[0]?.get(2).toString())
            }
        }
    }

    private fun String.toMD5(): String {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

    private fun convertStringToInt(input: String): Int {
        return input.toDouble().toInt()
    }

    private fun saveLog(logContent: String, qty: Int = 0) {

        val additionalInfo = """
            user: ${memory.user.username}
            serialNumber: ${memory.device.serialNumber}
            qty: $qty
        
        """.trimIndent()
        Sentry.captureMessage(additionalInfo + logContent, SentryLevel.INFO)
    }
}