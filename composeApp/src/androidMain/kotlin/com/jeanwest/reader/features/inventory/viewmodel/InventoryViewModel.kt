package com.jeanwest.reader.features.inventory.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.android.volley.TimeoutError
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.useCases.epcDecoder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    private val api: API,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private var saveToServerId = ""
    private var inputBarcodes = mutableListOf<String>()
    private val warehouseProductsWithDetails = mutableMapOf<Long, Product>()
    private val scannedProducts = mutableMapOf<Long, Int>()
    private var validatedScannedEpcList = mutableListOf<String>()
    private var validatedMainIdWithEpcsMap = mutableMapOf<Long, MutableList<String>>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var productDetailsTable = mutableListOf<Product>()
    private var resultsSentToServer = mutableStateMapOf<Long, Product>()
    private var lastInventoryTime = 0L
    private val listState = LazyListState(0)
    var popupHost = NotificationPopupHost()
        private set

    //ui parameters
    var uiList = mutableStateListOf<Product>()
    var inventoryResult = mutableStateMapOf<Long, Product>()
    var loading by mutableStateOf(false)

    var inventoryProgress by androidx.compose.runtime.mutableFloatStateOf(0F)
    var isInventoryStarted by mutableStateOf(false)
        private set
    var shortagesNumber by mutableIntStateOf(0)
    var additionalNumber by mutableIntStateOf(0)
    var confirmedNumber by mutableIntStateOf(0)
    var allInventoryNumber by mutableIntStateOf(0)
    private var numberOfScanned by mutableIntStateOf(0)
    var isInShortageAdditionalPage by mutableStateOf(false)
        private set
    var signedKBarCode = mutableStateListOf<String>()
        private set
    var scanFilter by mutableStateOf("کسری")
        private set
    private var barcode: Barcode = Barcode(context)
    var sexTileFilterValue = ""
        private set
    var sexTileFilterValues = mutableListOf<String>()
        private set
    private val tag = "scan time"
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    lateinit var rf: RFID
    private var shouldLoadDataFromSearchProduct = false

    init {
        rf = RFID(context, state) {
            scanTrigger()
        }
        Log.e(tag, "start program")
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        if (!loading) {
            loadMemory()
        }
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    fun onProductLongClick(productIndexInUiList: Int): Unit {

        if (uiList[productIndexInUiList].KBarCode !in signedKBarCode) {
            signedKBarCode.add(uiList[productIndexInUiList].KBarCode)
        } else {
            signedKBarCode.remove(uiList[productIndexInUiList].KBarCode)
        }
        saveToMemory()
    }

    fun onInventoryConflictButtonClick() {
        isInShortageAdditionalPage = true
    }

    fun onFilterValueChanged(value: String): Unit {
        scanFilter = value
        calculateInventoryResults()
    }

    fun onSexFilterValueChanged(value: String): Unit {
        sexTileFilterValue = value
        calculateInventoryResults()
    }

    fun onProductClick(productIndexInUiList: Int): Unit {
        shouldLoadDataFromSearchProduct = true
    }

    fun startInventory():Unit {
        checkServerIsReady()
    }

    private fun getWarehouseBarcodes() {

        Log.e("scan time", "start getting warehouse barcodes")

        loading = true

        api.getWarehouseProducts(
            memory.user.warehouseCode.toString(),
            { barcodes ->
                inputBarcodes.clear()
                inputBarcodeMapWithProperties.clear()
                warehouseProductsWithDetails.clear()
                inputBarcodes.addAll(barcodes.distinct())
                Log.e("scan time", "finish getting warehouse barcodes")
                syncInputItemsToServer()
            },
            {
                loading = false
            })
    }

    private fun calculateInventoryResults() {
        Log.e("scan time", "start calculating conflicts")

        loading = true
        viewModelScope.launch {

            val scannedProductsPrimaryKeysList = scannedProducts.keys.sorted()

            val conflicts = mutableStateMapOf<Long, Product>()
            inventoryResult.clear()

            warehouseProductsWithDetails.forEach { warehouseProduct ->

                if ((warehouseProduct.value.brandName == "primaryLight" || warehouseProduct.value.brandName == "JootiJeans" || warehouseProduct.value.brandName == "Baleno") &&
                    !warehouseProduct.value.name.contains("جوراب") &&
                    !warehouseProduct.value.name.contains("عينك") &&
                    !warehouseProduct.value.name.contains("ساعت") &&
                    !warehouseProduct.value.name.contains("شاپينگ")
                ) {

                    warehouseProduct.value.countedWarehouseNumber =
                        -1 * warehouseProduct.value.wareHouseNumber

                    if (scannedProductsPrimaryKeysList.binarySearch(warehouseProduct.key) >= 0) {
                        //if (warehouseProduct.key in scannedProducts) {
                        warehouseProduct.value.manualScannedNumber =
                            scannedProducts[warehouseProduct.key]!!
                    }
                    conflicts[warehouseProduct.key] = warehouseProduct.value
                }
            }

            val conflictsPrimaryKeysList = conflicts.keys.sorted()

            scannedProducts.forEach {
                if (conflictsPrimaryKeysList.binarySearch(it.key) < 0) {
                    //if (it.key !in conflicts) {
                    val scannedProduct: Pair<Long, Product> =
                        it.key to productDetailsTable.findLast { productDetails ->
                            productDetails.primaryKey == it.key
                        }!!

                    if (!scannedProduct.second.name.contains("جوراب") &&
                        !scannedProduct.second.name.contains("عينك") &&
                        !scannedProduct.second.name.contains("ساعت") &&
                        !scannedProduct.second.name.contains("شاپينگ")
                    ) {

                        scannedProduct.second.countedWarehouseNumber =
                            -1 * scannedProduct.second.wareHouseNumber

                        scannedProduct.second.manualScannedNumber = it.value
                        conflicts[scannedProduct.first] = scannedProduct.second
                    }
                }
            }

            inventoryResult.putAll(conflicts)

            inventoryProgress = if (warehouseProductsWithDetails.isEmpty()) {
                0F
            } else {
                (inventoryResult.filter {
                    it.value.inventoryConflictType == "تایید شده"
                }.size.toFloat()) / inventoryResult.size.toFloat()
            }

            Log.e("inventoryResult", inventoryResult.filter {
                it.value.inventoryConflictType == "تایید شده"
            }.toList().toString())

            shortagesNumber = 0
            additionalNumber = 0
            numberOfScanned = 0
            allInventoryNumber = 0
            confirmedNumber = 0


            var log = 0
            inventoryResult.forEach {
                log += it.value.wareHouseNumber
            }
            Log.e("inventoryResult", log.toString())

            inventoryResult.values.toList()
                .filter { it.sexTile == sexTileFilterValue || sexTileFilterValue == "همه" }
                .forEach {
                    when (it.inventoryConflictType) {
                        "کسری" -> {
                            shortagesNumber += it.inventoryConflictAbs
                            confirmedNumber += it.inventoryNumber - it.inventoryConflictAbs
                        }

                        "اضافی" -> {
                            additionalNumber += it.inventoryConflictAbs
                            confirmedNumber += it.inventoryNumber
                        }

                        "تایید شده" -> {
                            confirmedNumber += it.inventoryNumber
                        }
                    }
                    numberOfScanned += it.scannedNumber
                    allInventoryNumber += it.inventoryNumber
                }
            Log.e("scan time", "finish calculating conflicts")
            filterUiList()
        }
    }

    private fun filterUiList() {

        Log.e("scan time", "start filtering results")

        loading = true

        viewModelScope.launch {

            val shortageAndAdditional = inventoryResult.filter { it1 ->
                it1.value.inventoryConflictType == "کسری" || it1.value.inventoryConflictType == "اضافی"
            }.toMutableMap()

            val uiListTemp = mutableListOf<Product>()
            uiListTemp.addAll(shortageAndAdditional.values.toMutableStateList())

            uiListTemp.sortBy {
                it.productCode
            }

            uiListTemp.sortBy {
                it.name
            }

            signedKBarCode.removeAll {
                uiListTemp.indexOfLast { it1 ->
                    it1.KBarCode == it
                } == -1
            }

            uiList.clear()
            uiList.addAll(uiListTemp)
            saveToMemory()


            val uiListParameters = if (sexTileFilterValue == "همه") {
                uiList.filter {
                    it.inventoryConflictType == scanFilter
                } as MutableList<Product>
            } else {
                uiList.filter {
                    it.inventoryConflictType == scanFilter && it.sexTile == sexTileFilterValue
                } as MutableList<Product>
            }

            uiList.clear()
            uiList.addAll(uiListParameters)
            loading = false
            Log.e("scan time", "finish filtering results")
        }
    }

    fun scanTrigger() {

        if (isInventoryStarted) {

            if (!rf.scanning) {

                rf.startBulkScan()
            } else {

                rf.stopScanning()
                processScannedProducts()
            }
        }
    }

    private fun syncInputItemsToServer() {

        val barcodeTableForV4 = mutableListOf<String>()

        Log.e("scan time", "start getting warehouse barcodes details")

        loading = true
        var inputProductsBiggerThan100 = false

        Log.e("scan time", "start processing request inputs")

        run breakForEach@{
            inputBarcodes.forEach {
                if (it !in inputBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 100) {
                        barcodeTableForV4.add(it)
                    } else {
                        inputProductsBiggerThan100 = true
                        return@breakForEach
                    }
                }
            }
        }

        Log.e("scan time", "finish processing request inputs")

        if (barcodeTableForV4.size == 0) {
            loading = false
            return
        }

        Log.e("scan time", "start request")


        api.getProductsV5(
            warehouses = listOf(memory.user.warehouseCode.toString()),
            epcs = mutableListOf(),
            barcodes = barcodeTableForV4,
            { _, barcodes, _, _ ->

                Log.e("scan time", "finish request")
                Log.e("scan time", "start processing response")

                barcodes.forEach { product ->
                    inputBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                if (inputProductsBiggerThan100) {
                    Log.e("scan time", "finish processing response")
                    syncInputItemsToServer()
                } else {

                    var log = 0

                    inputBarcodeMapWithProperties.forEach {
                        log += it.value.wareHouseNumber
                    }
                    Log.e("scan time", "finish getting warehouse barcodes details")
                    processWarehouseProducts()
                }

            },
            {
                loading = false
            },
        )
    }

    private fun processWarehouseProducts() {

        loading = true
        viewModelScope.launch {

            inputBarcodes.distinct().forEach {

                val product = inputBarcodeMapWithProperties[it]!!

                if (product.primaryKey !in warehouseProductsWithDetails.keys) {
                    warehouseProductsWithDetails[product.primaryKey] = product.copy()
                }
            }

            productDetailsTable.clear()
            productDetailsTable.addAll(warehouseProductsWithDetails.values)
            sexTileFilterValues.clear()
            sexTileFilterValues.add("همه")
            sexTileFilterValue = "همه"
            productDetailsTable.sortBy { it.rfidKey }
            productDetailsTable.forEach {
                if (it.sexTile !in sexTileFilterValues) {
                    sexTileFilterValues.add(it.sexTile)
                }
            }
            if (rf.epcs.size > 0) {
                processScannedProducts()
            } else {
                calculateInventoryResults()
            }
        }
    }

    private fun syncScannedItemsToServer() {

        loading = true

        Log.e("scan time", "start syncing to server")

        viewModelScope.launch {

            Log.e("scan time", "start processing request inputs")

            Log.e(tag, "${rf.epcs.size}, ${validatedScannedEpcList.size}")

            val epcTableForV4 = mutableListOf<String>()

            run breakForEach@{
                rf.epcs.forEach {
                    if (validatedScannedEpcList.binarySearch(it) < 0) {
                        if (epcTableForV4.size < 100) {
                            epcTableForV4.add(it)
                        } else {
                            return@breakForEach
                        }
                    }
                }
            }

            if (epcTableForV4.size == 0) {
                calculateInventoryResults()
                loading = false
                return@launch
            }

            Log.e("scan time", "finish processing request inputs")
            Log.e("scan time", "start request")
            Log.e("scan time", "request epcs: ${epcTableForV4.toList()}")

            api.getProductsV5(
                warehouses = listOf(memory.user.warehouseCode.toString()),
                epcs = epcTableForV4,
                barcodes = mutableListOf(),
                { epcs, _, invalidEpcs, _ ->

                    Log.e("scan time", "finish request")
                    Log.e("scan time", "start response process")

                    epcs.forEach { product ->
                        productDetailsTable.add(product)
                        if (product.sexTile !in sexTileFilterValues) {
                            sexTileFilterValues.add(product.sexTile)
                        }
                    }

                    productDetailsTable.sortBy { it.rfidKey }

                    Log.e("scan time", "finish response process")

                    for (i in 0 until invalidEpcs.length()) {
                        rf.epcs.remove(invalidEpcs[i])
                    }
                    processScannedProducts()
                },
                {
                    processScannedProducts()
                })
        }
    }

    private fun addNewScannedProduct(epc: String, productDetails: Product) {
        if (productDetails.primaryKey !in scannedProducts.keys) {
            scannedProducts[productDetails.primaryKey] = 1
        } else {
            scannedProducts[productDetails.primaryKey] =
                scannedProducts[productDetails.primaryKey]!! + 1
        }
        validatedScannedEpcList.add(epc)

        if (productDetails.primaryKey !in validatedMainIdWithEpcsMap.keys) {
            validatedMainIdWithEpcsMap[productDetails.primaryKey] = mutableListOf(epc)
        } else {
            validatedMainIdWithEpcsMap[productDetails.primaryKey]?.add(epc)
        }
    }

    private fun processScannedProducts() {

        loading = true

        viewModelScope.launch {

            Log.e("scan time", "start processing")

            validatedScannedEpcList.sort()

            if (rf.epcs.size == 0) {
                loading = false
                return@launch
            }

            rf.epcs.forEach { epc ->

                //if (validatedScannedEpcList.binarySearch(epc) < 0)

                if (validatedScannedEpcList.binarySearch(epc) < 0) {

                    val epcData = epcDecoder(epc)

                    if (epcData != null) {

                        when (epcData.encodingTypeString) {

                            "avakatan" -> {

                                if (epcData.company == 100) {
                                    productDetailsTable.findLast {
                                        (it.primaryKey == epcData.item)
                                    }?.let { addNewScannedProduct(epc, it) }

                                } else if (epcData.company == 101) {
                                    productDetailsTable.binarySearch {
                                        it.rfidKey.toInt() - epcData.item.toInt()
                                    }.let { index ->
                                        if (index >= 0)
                                            addNewScannedProduct(epc, productDetailsTable[index])
                                    }
                                }
                            }

                            "jootijeans" -> {
                                productDetailsTable.findLast {
                                    it.color == epcData.color &&
                                            it.size == epcData.size &&
                                            it.productCode.contains(epcData.styleCode)
                                }?.let { addNewScannedProduct(epc, it) }
                            }

                            "primaryLight" -> {
                                productDetailsTable.findLast {
                                    epc in it.scannedEPCs
                                }?.let {
                                    addNewScannedProduct(epc, it)
                                }
                            }

                            else -> {
                                showLog("مشخصات برخی تگ ها یافت نشد.", state)
                            }
                        }
                    } else {
                        showLog("مشخصات برخی تگ ها یافت نشد.", state)
                    }
                }
            }

            Log.e("scan time", "finish processing")

            validatedScannedEpcList.distinct()
            if (validatedScannedEpcList.size != rf.epcs.size) {
                validatedScannedEpcList.sort()
                syncScannedItemsToServer()
            } else {
                calculateInventoryResults()
            }
        }
    }

    private fun saveResultsToServer() {
        loading = true

        api.saveInventoryDataGetId(memory.user.warehouseCode.toString(), {
            saveToServerId = it
            saveApi2()
        }, {
            loading = false
        })
    }

    private fun saveApi2() {

        loading = true
        var resultsBiggerThan500 = false
        val productListForSend = mutableListOf<Product>()

        run breakForEach@{
            inventoryResult.keys.forEach {
                if (it !in resultsSentToServer.keys) {
                    if (productListForSend.size < 500) {
                        productListForSend.add(inventoryResult[it]!!)
                        resultsSentToServer[it] = inventoryResult[it]!!.copy()
                    } else {
                        resultsBiggerThan500 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (productListForSend.isEmpty()) {
            saveApi3()
            return
        }

        api.saveInventoryDataSendPackets(
            saveToServerId,
            productListForSend,
            validatedMainIdWithEpcsMap,
            {
                if (resultsBiggerThan500) {
                    saveApi2()
                } else {
                    saveApi3()
                }
            },
            {
                loading = false
            })
    }

    private fun saveApi3() {

        loading = true

        api.saveInventoryDataConfirm(saveToServerId, {
            isInventoryStarted = false
            inputBarcodes.clear()
            warehouseProductsWithDetails.clear()
            inputBarcodeMapWithProperties.clear()
            clearAll()
            lastInventoryTime = System.currentTimeMillis()
            saveToMemory()
            isInShortageAdditionalPage = false
            loading = false
        }, {
            if (it is TimeoutError) {
                isInventoryStarted = false
                inputBarcodes.clear()
                warehouseProductsWithDetails.clear()
                inputBarcodeMapWithProperties.clear()
                clearAll()
                lastInventoryTime = System.currentTimeMillis()
                saveToMemory()
                isInShortageAdditionalPage = false
            }
            loading = false
        })
    }

    private fun checkServerIsReady() {

        if (System.currentTimeMillis() <= lastInventoryTime + 150000) {

            CoroutineScope(Dispatchers.IO).launch {
                state.showSnackbar(
                    "سرور مشغول ثبت اطلاعات انبارگردانی قبلی است. لطفا بعدا امتحان کنید.",
                    null,
                    duration = SnackbarDuration.Long
                )
            }
        } else {

            signedKBarCode.clear()
            saveToMemory()
            loading = false

            isInventoryStarted = true
            getWarehouseBarcodes()
        }
    }

    fun onInventoryFinishButtonClick() {

        popupHost.showPopupWith2Button(
            message = "کالاهای اسکن شده ثبت شوند؟ برای بازگشت جایی از صفحه را لمس کنید.",
            cancelButtonTitle = "خیر، نتایج پاک شوند",
            onOkClick = {
                saveResultsToServer()
            },
            onCancelClick = {
                isInventoryStarted = false
                loading = false
                inputBarcodes.clear()
                warehouseProductsWithDetails.clear()
                inputBarcodeMapWithProperties.clear()
                clearAll()
                isInShortageAdditionalPage = false
            }
        )
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = memory.edit()
        edit.putLong("lastInventoryTime", lastInventoryTime)
        edit.putBoolean("InventoryIsInventoryStarted", isInventoryStarted)
        edit.putString("InventoryEPCTable", JSONArray(rf.epcs).toString())
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        lastInventoryTime = memory.getLong("lastInventoryTime", 0L)

        Log.e(tag, "start loading")

        if (!isInventoryStarted) {

            isInventoryStarted = memory.getBoolean("InventoryIsInventoryStarted", false)

            rf.epcs = Gson().fromJson(
                memory.getString("InventoryEPCTable", ""),
                rf.epcs.javaClass
            ) ?: mutableStateListOf()

            if (isInventoryStarted) {
                if (rf.epcs.size > 0) {
                    getWarehouseBarcodes()
                } else {
                    isInventoryStarted = false
                }
            }
        }

        if (shouldLoadDataFromSearchProduct) {
            val searchProductScannedEPCs = Gson().fromJson(
                memory.getString("searchProductEPCTable", ""),
                rf.epcs.javaClass
            ) ?: mutableStateListOf()
            rf.epcs.addAll(searchProductScannedEPCs)
            rf.epcs = rf.epcs.distinct().toMutableStateList()
            syncScannedItemsToServer()
            shouldLoadDataFromSearchProduct = false
        }

        Log.e(tag, "finish loading")
    }

    private fun clearAll() {

        rf.epcs.clear()
        scannedProducts.clear()
        validatedScannedEpcList.clear()
        inventoryResult.clear()
        uiList.clear()
        inputBarcodes.clear()
        inputBarcodeMapWithProperties.clear()
        warehouseProductsWithDetails.clear()
        inventoryResult.clear()
        productDetailsTable.clear()
        validatedScannedEpcList.clear()
        inventoryProgress = 0F
        shortagesNumber = 0
        additionalNumber = 0
        allInventoryNumber = 0
        numberOfScanned = 0
        confirmedNumber = 0
        scanFilter = "کسری"
        saveToMemory()
    }

    fun back() {

        if (isInShortageAdditionalPage) {
            isInShortageAdditionalPage = false
        } else {
            saveToMemory()
            loading = false
        }
    }
}