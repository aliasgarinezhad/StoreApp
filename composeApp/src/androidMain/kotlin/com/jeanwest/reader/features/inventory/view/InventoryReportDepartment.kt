package com.jeanwest.reader.features.inventory.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.view.AlertDialogWithHeadlineMediumButton1InputText
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.AppBarWithFileButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.FilterDropDownList
import com.jeanwest.reader.view.InventoryReportItem
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotFound
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Inventory
import com.jeanwest.reader.models.InventoryItem
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.epcDecoder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalFoundationApi
class InventoryReportDepartment : ComponentActivity() {

    private var departmentInventoryWarehouses = mutableListOf<String>()
    private var departmentInventories = mutableListOf<Inventory>()
    private var inputBarcodes = mutableListOf<String>()
    private val warehouseProductsWithDetails = mutableMapOf<Long, Product>()
    private val scannedProducts = mutableMapOf<Long, Int>()
    private var validatedScannedEpcList = mutableListOf<String>()
    private var validatedScannedEpcWithPrimaryKeyMap = mutableMapOf<String, Long>()
    private var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    private var productDetailsTable = mutableListOf<Product>()
    private var lastInventoryTime = 0L
    private var duplicateScannedEpcs = mutableStateListOf<String>()
    private var inventory0EPCS = mutableListOf<String>()
    private val inventory0Shortages = mutableMapOf<Long, Int>()
    private val inventory0Additions = mutableMapOf<Long, Int>()
    private var inventory1EPCS = mutableListOf<String>()
    private val inventory1Shortages = mutableMapOf<Long, Int>()
    private val inventory1Additions = mutableMapOf<Long, Int>()
    private var fileName by mutableStateOf("خروجی")

    //ui parameters
    private var duplicatedProducts = mutableStateMapOf<Long, Product>()
    private var duplicatedProductsUiList = mutableStateListOf<Product>()
    private var duplicateErrorPercentage by mutableFloatStateOf(0F)
    private var duplicateErrorDescription by mutableStateOf("")

    var uiList = mutableStateListOf<Product>()
    private var inventoryResult = mutableStateMapOf<Long, Product>()
    var loading by mutableStateOf(false)

    @Inject
    lateinit var state: SnackbarHostState
    private var inventoryProgress by mutableFloatStateOf(0F)
    private var isInventoryStarted by mutableStateOf(false)
    var shortagesNumber by mutableIntStateOf(0)
    var additionalNumber by mutableIntStateOf(0)
    private var confirmedNumber by mutableIntStateOf(0)
    private var allInventoryNumber by mutableIntStateOf(0)
    private var numberOfScanned by mutableIntStateOf(0)
    private var navigationState by mutableIntStateOf(0)
    private var signedKBarCode = mutableStateListOf<String>()
    private var scanFilter by mutableStateOf("کسری")
    private lateinit var barcode: Barcode
    private var openFileDialog by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API
    lateinit var rf: RFID
    private var shouldLoadDataFromSearchProduct = false
    private var isValidInventoryData by mutableStateOf(true)
    private var notFoundText by mutableStateOf("")
    private val listState = LazyListState(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContent {
            Page()
        }
    }

    private fun init() {

        barcode = Barcode(this)
        rf = RFID(this, state) {}
        isInventoryStarted = true
        loadMemory()
        checkServerIsReady()
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (!loading) {
            loadMemory()
        }
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    private fun getInventoryReportHistory() {
        loading = true

        departmentInventoryWarehouses =
            memory.erpData.departmentWarehouses[memory.user.calculatedLocationCode.toString()]?.toMutableList()
                ?: mutableListOf()

        departmentInventoryWarehouses.removeAll {
            it !in memory.user.warehouses.keys.toString()
        }

        if (departmentInventoryWarehouses.isEmpty()) {
            notFoundText =
                "در فروشگاه شما هیچ انباری وجود ندارد. لطفا از درست بودن حساب کاربری و دسترسی های آن اطمینان حاصل کنید."
            loading = false
            isValidInventoryData = false
            return
        } else if (departmentInventoryWarehouses.size > 2) {
            notFoundText =
                "در فروشگاه شما بیش از ۲ انبار انباری وجود دارد. لطفا از درست بودن حساب کاربری و دسترسی های آن اطمینان حاصل کنید."
            loading = false
            isValidInventoryData = false
            return
        } else if (memory.user.warehouses[departmentInventoryWarehouses[0]]?.contains("دپو") != true &&
            memory.user.warehouses[departmentInventoryWarehouses[1]]?.contains("دپو") != true
        ) {
            notFoundText =
                "نوع انبار های شما فروشگاهی نیست دارد. لطفا از درست بودن حساب کاربری و دسترسی های آن اطمینان حاصل کنید."
            loading = false
            isValidInventoryData = false
            return
        }

        api.getInventoryResult(memory.user.calculatedLocationCode, {

            departmentInventories.clear()

            var inventoryItemTemp = it.firstOrNull { inventoryItem ->
                inventoryItem.warehouse == departmentInventoryWarehouses[0] &&
                        inventoryItem.des.contains("RFID")
//                inventoryItem.inventoryId == "1041100000258"
            }

            if (inventoryItemTemp != null) {
                departmentInventories.add(inventoryItemTemp)
            } else {
                notFoundText = "گزارش انبارگردانی های انجام شده کامل نیست."
                loading = false
                isValidInventoryData = false
                return@getInventoryResult
            }

            inventoryItemTemp = it.firstOrNull { inventoryItem ->
                inventoryItem.warehouse == departmentInventoryWarehouses[1]
//                inventoryItem.inventoryId == "1041100000259"
            }

            if (inventoryItemTemp != null) {
                departmentInventories.add(inventoryItemTemp)
            } else {
                notFoundText = "گزارش انبارگردانی های انچام شده کامل نیست."
                loading = false
                isValidInventoryData = false
                return@getInventoryResult
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getDefault()

            val warehouse1Time = sdf.parse(departmentInventories[0].standardDate)?.time
            val warehouse2Time = sdf.parse(departmentInventories[1].standardDate)?.time

            if (warehouse1Time == null || warehouse2Time == null) {
                notFoundText = "گزارش انبارگردانی های انچام شده کامل نیست."
                loading = false
                isValidInventoryData = false
                return@getInventoryResult
            } else if ((warehouse1Time > warehouse2Time + 86400000) || (warehouse2Time > warehouse1Time + 86400000)) {
                notFoundText =
                    "تاریخ گزارش انبارگردانی انبار فاصله زیادی با تاریخ انبارگردانی فروشگاه دارد."
                loading = false
                isValidInventoryData = false
                return@getInventoryResult
            }

            Log.e("checkResults", "get history")


            getDepartmentInventoriesDetails()
        }, {
            loading = false
        })
    }

    private fun getDepartmentInventoriesDetails() {

        loading = true

        api.getAllInventoryDetailsItems(
            inventoryID = departmentInventories[0].inventoryId,
            { allInventoryItems ->

                departmentInventories[0].inventoryItem.clear()

                val distinctInventory0Items = mutableListOf<InventoryItem>()
                val doubledInventory0Items = mutableListOf<InventoryItem>()
                val alreadyAddedBarcodeMainIDs0 = mutableListOf<Long>()
                allInventoryItems.forEach {
                    if (it.barcodeMainID !in alreadyAddedBarcodeMainIDs0) {
                        distinctInventory0Items.add(it)
                        alreadyAddedBarcodeMainIDs0.add(it.barcodeMainID)
                    } else {
                        doubledInventory0Items.add(it)
                    }
                }
                Log.e("epcs", "inventory0Distinct: " + distinctInventory0Items.size.toString())
                Log.e("epcs", "inventory0double: " + doubledInventory0Items.size.toString())
                departmentInventories[0].inventoryItem.addAll(distinctInventory0Items)

                api.getAllInventoryDetailsItems(
                    inventoryID = departmentInventories[1].inventoryId,
                    { allInventoryItems1 ->

                        departmentInventories[1].inventoryItem.clear()

                        val distinctInventory1Items = mutableListOf<InventoryItem>()
                        val doubledInventory1Items = mutableListOf<InventoryItem>()
                        val alreadyAddedBarcodeMainIDs1 = mutableListOf<Long>()
                        allInventoryItems1.forEach {
                            if (it.barcodeMainID !in alreadyAddedBarcodeMainIDs1) {
                                distinctInventory1Items.add(it)
                                alreadyAddedBarcodeMainIDs1.add(it.barcodeMainID)
                            } else {
                                doubledInventory1Items.add(it)
                            }
                        }
                        Log.e(
                            "epcs",
                            "inventory1Distinct: " + distinctInventory1Items.size.toString()
                        )
                        Log.e("epcs", "inventory1double: " + doubledInventory1Items.size.toString())
                        departmentInventories[1].inventoryItem.addAll(distinctInventory1Items)
                        processInventoriesData()
                    }, {
                        loading = false
                    })
            },
            {
                loading = false
            })
    }

    private fun processInventoriesData() {

        loading = true
        CoroutineScope(Dispatchers.Default).launch {
            Log.e("epcs", "processInventoriesData")


            inputBarcodes.clear()
            inventory0EPCS.clear()
            inventory0Shortages.clear()
            inventory0Additions.clear()
            inventory1EPCS.clear()
            inventory1Shortages.clear()
            inventory1Additions.clear()
            duplicateScannedEpcs.clear()

            departmentInventories[0].inventoryItem.forEach {
                repeat(it.currentMojodi) { _ ->
                    inputBarcodes.add(it.itemBarcode)
                }
                inventory0EPCS.addAll(it.epcs)
                if (it.diffMojodi > 0) {
                    inventory0Additions[it.barcodeMainID] = it.diffMojodi
                } else if (it.diffMojodi < 0) {
                    inventory0Shortages[it.barcodeMainID] = it.diffMojodi
                }
            }

            departmentInventories[1].inventoryItem.forEach {
                repeat(it.currentMojodi) { _ ->
                    inputBarcodes.add(it.itemBarcode)
                }
                inventory1EPCS.addAll(it.epcs)
                if (it.diffMojodi > 0) {
                    inventory1Additions[it.barcodeMainID] = it.diffMojodi
                } else if (it.diffMojodi < 0) {
                    inventory1Shortages[it.barcodeMainID] = it.diffMojodi
                }
            }


            Log.e("epcs", "inventory0: " + inventory0EPCS.size.toString())
            Log.e("epcs", "inventory1: " + inventory1EPCS.size.toString())
            Log.e("epcs", "inventory0Shortages: " + inventory0Shortages.size.toString())
            Log.e("epcs", "inventory1Shortages: " + inventory1Shortages.size.toString())


            val inventory1DuplicatedEPCs = mutableListOf<String>()
            val inventory1NotDuplicatedEPCs = mutableListOf<String>()
            inventory1EPCS.forEach {
                if (it !in inventory1NotDuplicatedEPCs) {
                    inventory1NotDuplicatedEPCs.add(it)
                } else {
                    inventory1DuplicatedEPCs.add(it)
                }
            }

            Log.e("inventory1DuplicatedEPCs", inventory1DuplicatedEPCs.size.toString())
            Log.e("inventory1DuplicatedEPCs", inventory1NotDuplicatedEPCs.size.toString())
            Log.e("inventory1DuplicatedEPCs", inventory1DuplicatedEPCs.toList().toString())
            Log.e("inventory1DuplicatedEPCs", inventory1NotDuplicatedEPCs.toList().toString())

            inventory0EPCS = inventory0EPCS.distinct().toMutableList()
            inventory1EPCS = inventory1EPCS.distinct().toMutableList()

            Log.e("epcs", "inventory0: " + inventory0EPCS.size.toString())
            Log.e("epcs", "inventory1: " + inventory1EPCS.size.toString())

            rf.epcs.clear()
            rf.epcs.addAll(inventory0EPCS)

            inventory1EPCS.forEach {
                if (it !in inventory0EPCS) {
                    rf.epcs.add(it)
                } else {
                    duplicateScannedEpcs.add(it)
                }
            }

            Log.e("epcs", "duplicated: " + duplicateScannedEpcs.size.toString())
            Log.e("epcs", "finish processInventoriesData")


            syncInputItemsToServer()
        }
    }

    private fun calculateInventoryResults() {

        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            Log.e("epcs", "calculateInventoryResults")

            val isInDepo = true

            val conflicts = mutableStateMapOf<Long, Product>()
            inventoryResult.clear()

            Log.e("epcs", "warehouseProducts: " + warehouseProductsWithDetails.size.toString())
            Log.e("epcs", "inputBarcodes: " + inputBarcodes.size.toString())


            warehouseProductsWithDetails.forEach {

                if ((it.value.brandName == "JeansWest" || it.value.brandName == "JootiJeans" || it.value.brandName == "Baleno") &&
                    !it.value.name.contains("جوراب") &&
                    !it.value.name.contains("عينك") &&
                    !it.value.name.contains("شاپينگ")
                ) {
                    val warehouseProduct = it
                    warehouseProduct.value.inventoryOnDepo = isInDepo

                    warehouseProduct.value.scannedEPCs.clear()
                    warehouseProduct.value.scannedBarcodeNumber = 0

                    warehouseProduct.value.draftNumber =
                        inputBarcodes.count { inputBarcode ->
                            it.value.scannedBarcode == inputBarcode || it.value.KBarCode == inputBarcode
                        }

                    if (warehouseProduct.key in scannedProducts.keys) {
                        warehouseProduct.value.manualScannedNumber =
                            scannedProducts[warehouseProduct.key]!!
                    }
                    conflicts[warehouseProduct.key] = warehouseProduct.value
                }
            }

            scannedProducts.forEach {
                if (it.key !in conflicts) {

                    val scannedProduct: Pair<Long, Product> =
                        it.key to productDetailsTable.findLast { productDetails ->
                            productDetails.primaryKey == it.key
                        }!!

                    if (!scannedProduct.second.name.contains("جوراب") &&
                        !scannedProduct.second.name.contains("عينك") &&
                        !scannedProduct.second.name.contains("شاپينگ")
                    ) {
                        scannedProduct.second.inventoryOnDepo = isInDepo

                        scannedProduct.second.scannedEPCs.clear()
                        scannedProduct.second.scannedBarcodeNumber = 0

                        scannedProduct.second.draftNumber = 0

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
                    it.value.conflictType == "تایید شده"
                }.size.toFloat()) / inventoryResult.size.toFloat()
            }

            shortagesNumber = 0
            additionalNumber = 0
            numberOfScanned = 0
            allInventoryNumber = 0
            confirmedNumber = 0

            inventoryResult.values.toMutableStateList().forEach {

                when (it.conflictType) {
                    "کسری" -> {
                        shortagesNumber += it.conflictNumber
                        confirmedNumber += it.draftNumber - it.conflictNumber
                    }

                    "اضافی" -> {
                        additionalNumber += it.conflictNumber
                        confirmedNumber += it.draftNumber
                    }

                    "تایید شده" -> {
                        confirmedNumber += it.draftNumber
                    }
                }
                numberOfScanned += it.scannedNumber
                allInventoryNumber += it.draftNumber
            }

            duplicateScannedEpcs.forEach { epc ->

                validatedScannedEpcWithPrimaryKeyMap[epc]?.let { barcodeMainID ->
                    productDetailsTable.forEach { product ->
                        if (product.primaryKey == barcodeMainID) {

                            if (barcodeMainID !in duplicatedProducts) {
                                duplicatedProducts[barcodeMainID] =
                                    product.copy(
                                        manualScannedNumber = 1,
                                        scannedBarcodeNumber = 0,
                                        scannedEPCs = mutableListOf()
                                    )
                            } else {
                                duplicatedProducts[barcodeMainID]!!.manualScannedNumber += 1
                            }
                            return@let
                        }
                    }
                }
            }

            duplicatedProductsUiList.clear()
            duplicatedProductsUiList.addAll(duplicatedProducts.values)

            duplicateErrorPercentage =
                duplicateScannedEpcs.size.toFloat() / (rf.epcs.size + duplicateScannedEpcs.size)
            duplicateErrorDescription =
                "${duplicateScannedEpcs.size}/${rf.epcs.size + duplicateScannedEpcs.size}"

            inventoryResult.forEach {

                if (it.value.conflictType == "کسری") {
                    if (it.key in inventory0Shortages || it.key in inventory1Shortages) {

                        if (it.key in inventory0Shortages && it.key in inventory1Shortages) {
                            it.value.spec =
                                "کسری سطح: " + (inventory0Shortages[it.key] ?: 0).toString()
                            it.value.spec2 =
                                "کسری دپو: " + (inventory1Shortages[it.key] ?: 0).toString()

                        } else if (it.key in inventory0Shortages) {
                            it.value.spec =
                                "کسری سطح: " + (inventory0Shortages[it.key] ?: 0).toString()
                            it.value.spec2 =
                                "کسری کل: " + it.value.conflictNumber
                        } else if (it.key in inventory1Shortages) {
                            it.value.spec =
                                "کسری دپو: " + (inventory1Shortages[it.key] ?: 0).toString()
                            it.value.spec2 =
                                "کسری کل: " + it.value.conflictNumber
                        }
                    } else {
                        it.value.spec = "موجودی: " + it.value.conflictNumber
                        it.value.spec2 =
                            it.value.conflictType + ":" + " " + it.value.conflictNumber
                    }
                } else {
                    it.value.spec = "موجودی: " + it.value.conflictNumber
                    it.value.spec2 =
                        it.value.conflictType + ":" + " " + it.value.conflictNumber
                }
            }

            filterUiList()
        }
    }

    private fun filterUiList() {
        loading = true

        CoroutineScope(Dispatchers.IO).launch {

            val shortageAndAdditional = inventoryResult.filter { it1 ->
                it1.value.conflictType == "کسری" || it1.value.conflictType == "اضافی"
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

            val uiListParameters = uiList.filter {
                it.conflictType == scanFilter
            } as MutableList<Product>

            uiList.clear()
            uiList.addAll(uiListParameters)
            loading = false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    private fun syncInputItemsToServer() {

        val barcodeTableForV4 = mutableListOf<String>()

        loading = true
        var inputProductsBiggerThan100 = false

        run breakForEach@{
            inputBarcodes.distinct().forEach {
                if (it !in inputBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 250) {
                        barcodeTableForV4.add(it)
                    } else {
                        inputProductsBiggerThan100 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (barcodeTableForV4.size == 0) {
            loading = false
            return
        }
        Log.e("epcs", "syncInputItemsToServer")

        api.getItemDetailsAndInventory(mutableListOf(), barcodeTableForV4, { _, barcodes, _, _ ->

            barcodes.forEach { product ->
                inputBarcodeMapWithProperties[product.scannedBarcode] = product
            }

            if (inputProductsBiggerThan100) {
                syncInputItemsToServer()
            } else {
                processWarehouseProducts()
            }

        }, {
            loading = false
        }, true)
    }

    private fun processWarehouseProducts() {

        loading = true
        Log.e("epcs", "processWarehouseProducts")

        CoroutineScope(Dispatchers.Default).launch {

            inputBarcodes.distinct().forEach {

                val product = inputBarcodeMapWithProperties[it]!!

                if (product.primaryKey !in warehouseProductsWithDetails.keys) {
                    warehouseProductsWithDetails[product.primaryKey] = product.copy()
                }
            }

            productDetailsTable.clear()
            productDetailsTable.addAll(warehouseProductsWithDetails.values)
            if (rf.epcs.size > 0) {
                processScannedProducts()
            } else {
                calculateInventoryResults()
            }
        }
    }

    private fun exportFile() {

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("conflicts")

        val headerRow = sheet.createRow(sheet.physicalNumberOfRows)
        headerRow.createCell(0).setCellValue("کد جست و جو")
        headerRow.createCell(1).setCellValue("تعداد")
        headerRow.createCell(2).setCellValue("دسته")
        headerRow.createCell(3).setCellValue("کسری")
        headerRow.createCell(4).setCellValue("اضافی")
        headerRow.createCell(5).setCellValue("نشانه")
        headerRow.createCell(6).setCellValue("وضعیت کسری")
        headerRow.createCell(7).setCellValue("وضعیت کسری")

        inventoryResult.values.forEach {
            val row = sheet.createRow(sheet.physicalNumberOfRows)
            row.createCell(0).setCellValue(it.KBarCode)
            row.createCell(1).setCellValue(it.draftNumber.toDouble())
            row.createCell(2).setCellValue(it.departmentName)

            if (it.conflictType == "کسری") {
                row.createCell(3).setCellValue(it.conflictNumber.toDouble())
            } else if (it.conflictType == "اضافی") {
                row.createCell(4).setCellValue(it.conflictNumber.toDouble())
            }

            if (it.KBarCode in signedKBarCode) {
                row.createCell(5).setCellValue("نشانه دار")
            }

            row.createCell(6).setCellValue(it.spec)
            row.createCell(7).setCellValue(it.spec2)
        }

        val row = sheet.createRow(sheet.physicalNumberOfRows)
        row.createCell(0).setCellValue("مجموع")
        row.createCell(1).setCellValue(numberOfScanned.toDouble())
        row.createCell(3).setCellValue(shortagesNumber.toDouble())
        row.createCell(4).setCellValue(additionalNumber.toDouble())
        /*
                val sheet2 = workbook.createSheet("کسری")

                val header2Row = sheet2.createRow(sheet2.physicalNumberOfRows)
                header2Row.createCell(0).setCellValue("کد جست و جو")
                header2Row.createCell(1).setCellValue("موجودی")
                header2Row.createCell(2).setCellValue("دسته")
                header2Row.createCell(3).setCellValue("کسری")
                header2Row.createCell(4).setCellValue("نشانه")

                uiList.forEach {

                    if (it.conflictNumber == "کسری") {
                        val shortageRow = sheet2.createRow(sheet2.physicalNumberOfRows)
                        shortageRow.createCell(0).setCellValue(it.KBarCode)
                        shortageRow.createCell(1)
                            .setCellValue(it.scannedNumber.toDouble() + it.matchedNumber.toDouble())
                        shortageRow.createCell(2).setCellValue(it.category)
                        shortageRow.createCell(3).setCellValue(it.matchedNumber.toDouble())

                        if (it.KBarCode in signedKBarCode) {
                            shortageRow.createCell(4).setCellValue("نشانه دار")
                        }
                    }
                }

                val sheet3 = workbook.createSheet("اضافی")

                val header3Row = sheet3.createRow(sheet3.physicalNumberOfRows)
                header3Row.createCell(0).setCellValue("کد جست و جو")
                header3Row.createCell(1).setCellValue("موجودی")
                header3Row.createCell(2).setCellValue("دسته")
                header3Row.createCell(3).setCellValue("اضافی")
                header3Row.createCell(4).setCellValue("نشانه")

                uiList.forEach {

                    if (it.scan == "اضافی") {
                        val additionalRow = sheet3.createRow(sheet3.physicalNumberOfRows)
                        additionalRow.createCell(0).setCellValue(it.KBarCode)
                        additionalRow.createCell(1)
                            .setCellValue(it.matchedNumber - it.scannedNumber.toDouble())
                        additionalRow.createCell(2).setCellValue(it.category)
                        additionalRow.createCell(3).setCellValue(it.matchedNumber.toDouble())

                        if (it.KBarCode in signedKBarCode) {
                            additionalRow.createCell(4).setCellValue("نشانه دار")
                        }
                    }
                }*/

        val dir = File(this.getExternalFilesDir(null), "/")

        val outFile = File(dir, "$fileName.xlsx")

        val outputStream = FileOutputStream(outFile.absolutePath)
        workbook.write(outputStream)
        outputStream.flush()
        outputStream.close()

        val uri = FileProvider.getUriForFile(
            this,
            this.applicationContext.packageName + ".provider",
            outFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "application/octet-stream"
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(shareIntent)
    }

    private fun syncScannedItemsToServer() {

        loading = true

        Log.e("epcs", "syncScannedItemsToServer")


        CoroutineScope(Dispatchers.IO).launch {

            val epcTableForV4 = mutableListOf<String>()

            run breakForEach@{
                rf.epcs.forEach {
                    if (it !in validatedScannedEpcList) {
                        if (epcTableForV4.size < 500) {
                            epcTableForV4.add(it)
                        } else {
                            return@breakForEach
                        }
                    }
                }
            }

            if (epcTableForV4.size == 0) {
                loading = false
                return@launch
            }

            api.getItemDetailsAndInventory(
                epcTableForV4,
                mutableListOf(),
                { epcs, _, invalidEpcs, _ ->

                    epcs.forEach { product ->
                        productDetailsTable.add(product)
                    }

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
        validatedScannedEpcWithPrimaryKeyMap[epc] = productDetails.primaryKey
    }

    private fun processScannedProducts() {

        loading = true
        Log.e("epcs", "processScannedProducts")

        CoroutineScope(Dispatchers.Default).launch {

            if (rf.epcs.size == 0) {
                loading = false
                return@launch
            }

            rf.epcs.forEach { epc ->

                if (epc !in validatedScannedEpcList) {

                    val epcData = epcDecoder(epc)

                    if (epcData != null) {
                        when (epcData.encodingTypeString) {

                            "avakatan" -> {
                                productDetailsTable.firstOrNull {
                                    (it.rfidKey == epcData.item && epcData.company == 101) ||
                                            (it.primaryKey == epcData.item && epcData.company == 100)
                                }?.let { addNewScannedProduct(epc, it) }
                            }

                            "jootijeans" -> {
                                productDetailsTable.firstOrNull {
                                    it.color == epcData.color &&
                                            it.size == epcData.size &&
                                            (it.productCode == epcData.styleCode ||
                                                    it.productCode.uppercase()
                                                        .endsWith(epcData.styleCode.uppercase()) ||
                                                    it.productCode.uppercase() == "S${epcData.styleCode}".uppercase() ||
                                                    it.productCode.uppercase() == "W${epcData.styleCode}".uppercase() ||
                                                    it.productCode.uppercase() == "${epcData.styleCode}S".uppercase())
                                }?.let { addNewScannedProduct(epc, it) }
                            }

                            "jeanswest" -> {
                                productDetailsTable.firstOrNull {
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

            var needServerData = false

            run breakForEach@{

                rf.epcs.forEach {
                    if (it !in validatedScannedEpcList) {
                        needServerData = true
                        return@breakForEach
                    }
                }
            }

            if (needServerData) {
                syncScannedItemsToServer()
            } else {
                calculateInventoryResults()
            }
        }
    }

    private fun checkServerIsReady() {

        if (System.currentTimeMillis() <= lastInventoryTime + 150000) {
            notFoundText = "سرور مشغول ثبت اطلاعات انبارگردانی قبلی است. لطفا بعدا امتحان کنید."
            isValidInventoryData = false
        } else {
            signedKBarCode.clear()
            loading = false
            isInventoryStarted = true
            getInventoryReportHistory()
        }
    }

    fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "FileAttachmentFileSignedCodesTable",
            JSONArray(signedKBarCode).toString()
        )
        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        lastInventoryTime = memory.getLong("lastInventoryTime", 0L)

        signedKBarCode = Gson().fromJson(
            memory.getString("FileAttachmentFileSignedCodesTable", ""),
            signedKBarCode.javaClass
        ) ?: SnapshotStateList()
    }

    private fun back() {

        if (navigationState != 0) {
            navigationState = 0
        } else {
            loading = false
            finish()
        }
    }
    
    @OptIn(ExperimentalCoilApi::class)
    @ExperimentalFoundationApi
    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = {
                        if (navigationState != 2) {
                            AppBarWithBack(
                                title = stringResource(id = R.string.inventoryReportDepartment),
                                onBackPressed = { back() }
                            )
                        } else {
                            AppBarWithFileButton(
                                title = stringResource(id = R.string.inventoryReportDepartment),
                                onBackPressed = { back() },
                                onDeletePressed = { openFileDialog = true }
                            )
                        }
                    },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            when (navigationState) {

                                0 -> Content()
                                2 -> DepartmentInventoryDetailsContent()
                                4 -> DuplicateEPCsReportContent(uiList = duplicatedProductsUiList)
                                else -> Content()
                            }
                        }
                    },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = loading)
        } else if (!isValidInventoryData) {
            NotFound(text = notFoundText)
        } else {

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .weight(2F)
                        .fillMaxSize()
                ) {
                    InventoryReportItem(
                        modifier = Modifier
                            .weight(1F)
                            .padding(16.dp)
                            .fillMaxSize(),
                        title = "مغایرت داخلی دپارتمان",
                        description = "10/100",
                        itemPercentage = 0.1F,
                        buttonTitle = "جزئیات",
                        buttonOnClick = {}
                    )
                    InventoryReportItem(
                        modifier = Modifier
                            .weight(1F)
                            .padding(16.dp)
                            .fillMaxSize(),
                        title = "مغایرت کلی دپارتمان",
                        description = "$confirmedNumber/$allInventoryNumber",
                        itemPercentage = inventoryProgress,
                        buttonTitle = "جزئیات",
                        buttonOnClick = {
                            navigationState = 2
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .weight(2F)
                        .fillMaxSize()
                ) {
                    InventoryReportItem(
                        modifier = Modifier
                            .weight(1F)
                            .padding(16.dp)
                            .fillMaxSize(),
                        title = "کل مغایرت ها",
                        description = "15/100",
                        itemPercentage = 0.15F,
                        buttonTitle = "جزئیات",
                        buttonOnClick = {}
                    )
                    InventoryReportItem(
                        modifier = Modifier
                            .weight(1F)
                            .padding(16.dp)
                            .fillMaxSize(),
                        title = "کالاهای تکراری",
                        description = duplicateErrorDescription,
                        itemPercentage = duplicateErrorPercentage,
                        buttonTitle = "جزئیات",
                        buttonOnClick = {
                            navigationState = 4
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun DuplicateEPCsReportContent(uiList: MutableList<Product>) {

        Column(Modifier.fillMaxSize()) {

            if (loading || rf.scanning) {
                LoadingCircularProgressIndicator(
                    rf.scanning, loading
                )
            } else {

                LazyColumn {

                    items(uiList.size) { i ->

                        Item(
                            i,
                            uiList,
                            text3 = "تعداد: " + uiList[i].manualScannedNumber,
                            text4 = "رنگ" + ":" + " " + uiList[i].color,
                            clickable = true,
                            onClick = {
                                Intent(
                                    this@InventoryReportDepartment, SearchProduct::class.java
                                ).apply {
                                    this.putExtra(
                                        "product", Gson().toJson(uiList[i]).toString()
                                    )
                                    startActivity(this)
                                }
                            })
                    }
                }
            }
        }
    }

    @ExperimentalCoilApi
    @ExperimentalFoundationApi
    @Composable
    fun DepartmentInventoryDetailsContent() {

        Column(Modifier.fillMaxSize()) {

            if (loading) {
                LoadingCircularProgressIndicator(
                    isDataLoading = loading
                )
            } else {

                if (openFileDialog) {
                    AlertDialogWithHeadlineMediumButton1InputText(
                        title = "اسم فایل را وارد کنید",
                        btnTxt = "ذخیره",
                        defaultText = fileName,
                        btnOnClick = {
                            openFileDialog = false
                            exportFile()
                        },
                        onValueChange = {
                            fileName = it
                        },
                        onDismiss = { openFileDialog = false }
                    )
                }

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                ) {

                    Text(
                        text = "کسری: $shortagesNumber",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp)
                            .weight(1F)
                    )

                    Text(
                        text = "اضافی: $additionalNumber",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp)
                            .weight(1F)
                    )
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F)
                    ) {
                        FilterDropDownList(icon = { }, text = {
                            Text(
                                text = scanFilter,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        }, values = mutableListOf("اضافی", "کسری"), onClick = {
                            scanFilter = it
                            calculateInventoryResults()
                        })
                    }
                }

                LazyColumn {

                    items(uiList.size) { i ->

                        Item(
                            i,
                            uiList,
                            text3 = uiList[i].spec,
                            text4 = uiList[i].spec2,
                            clickable = true,
                            onClick = {
                                Intent(
                                    this@InventoryReportDepartment, SearchProduct::class.java
                                ).apply {
                                    this.putExtra(
                                        "product", Gson().toJson(uiList[i]).toString()
                                    )
                                    shouldLoadDataFromSearchProduct = true
                                    startActivity(this)
                                }
                            },
                            colorFull = uiList[i].KBarCode in signedKBarCode,
                            onLongClick = {
                                if (uiList[i].KBarCode !in signedKBarCode) {
                                    signedKBarCode.add(uiList[i].KBarCode)
                                } else {
                                    signedKBarCode.remove(uiList[i].KBarCode)
                                }
                                saveToMemory()
                            })
                    }
                }
            }
        }
    }
}