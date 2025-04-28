package com.jeanwest.reader.features.stockDraft.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.view.NotificationPopupHost
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import javax.inject.Inject

/**
 * This activity handles the reverse refill process, allowing users to scan items,
 * synchronize them with the server, and create stock drafts for transferring
 * items from the store to the warehouse.
 *
 * Key functionalities include:
 * - **Barcode Scanning:** Uses a [Barcode] object to scan barcodes and trigger
 *   item synchronization.
 * - **Item Synchronization:** Communicates with the server via an [API] to fetch
 *   item details based on scanned barcodes.  Manages a list of scanned and
 *   synchronized [Product]s.
 * - **Stock Draft Creation:** Allows users to create stock drafts, initiating the
 *   transfer of selected items to the warehouse using [LocalStoreDatabase].
 * - **Memory Management:** Persists scanned barcodes and product data locally using
 *   shared preferences ( */
@AndroidEntryPoint
class ReverseRefill : ComponentActivity() {

    @Inject
    lateinit var state: SnackbarHostState
    @Inject
    lateinit var viewModel: ReverseRefillViewModel

    private var scanningMode by mutableStateOf(true)

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var repository: RepositoryImpl
    var uiList = mutableStateListOf<Product>()
    var products = mutableListOf<Product>()
    lateinit var barcode: Barcode

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        loadMemory()
        exceptionHandler()
        setContent {
            Page(viewModel)
        }
    }

    private fun init() {
        barcode = Barcode(this) {
            syncScannedItemToServer(it)
        }
        viewModel.scanningMode = scanningMode
        viewModel.uiList = uiList
        viewModel.barcode = barcode
        viewModel.back = { back() }
        viewModel.popupState = NotificationPopupHost()
        viewModel.bottomBarOnClick = { createStockDraft() }
        viewModel.localServerLoading = localStoreDatabase.loading
        viewModel.uiListOnClick = {
            openSearchActivity(it)
        }
        viewModel.deleteItem = {
            clear(it)
        }

        viewModel.disableBarcode = {
            if (barcode.isEnabled) {
                barcode.disable()
            }
        }
        viewModel.clear = {
            barcode.scannedBarcodes.clear()
            uiList.clear()
            products.clear()
            saveToMemory()
        }
        viewModel.deleteAll = {
            if (!viewModel.loading) {
                viewModel.openClearDialog = true
            }
        }
        viewModel.state.currentSnackbarData?.dismiss()
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
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
        viewModel.state.currentSnackbarData?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        viewModel.state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
        if (!scanningMode) {
            barcode.disable()
        }
        loadMemory()
        uiList.addAllAndSort(products)
        syncScannedItemsToServer()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    private fun syncScannedItemsToServer() {

        viewModel.loading = true

        if (barcode.scannedBarcodes.size == 0) {
            uiList.addAllAndSort(products)
            viewModel.loading = false
            return
        }

        val barcodeArray = mutableListOf<String>()
        val alreadySyncedBarcodes = mutableListOf<String>()

        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        barcode.scannedBarcodes.forEach {
            if (it !in alreadySyncedBarcodes) {
                barcodeArray.add(it)
            } else {
                val productIndex = products.indexOf(products.last { it1 ->
                    it1.scannedBarcode == it
                })
                products[productIndex].scannedBarcodeNumber =
                    barcode.scannedBarcodes.count { it1 ->
                        it1 == it
                    }
            }
        }

        if (barcodeArray.size == 0) {
            uiList.addAllAndSort(products)
            viewModel.loading = false
            return
        }
        api.getItemDetailsAndInventory(
            mutableListOf(),
            barcodeArray,
            { _, barcodes, _, invalidBarcodes ->

                barcodes.forEach {
                    var isInRefillProductList = false

                    run forEacheadlineMedium@{
                        products.forEach { it1 ->
                            if (it1.primaryKey == it.primaryKey) {
                                it1.scannedBarcode = it.scannedBarcode
                                it1.scannedBarcodeNumber += 1
                                isInRefillProductList = true
                                return@forEacheadlineMedium
                            }
                        }
                    }
                    if (!isInRefillProductList) {
                        it.scannedBarcodeNumber = 1
                        products.add(it)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    barcode.scannedBarcodes.remove(invalidBarcodes[i])
                }

                uiList.addAllAndSort(products)
                viewModel.loading = false

            },
            {

                uiList.addAllAndSort(products)
                viewModel.loading = false
            }, true
        )
    }

    private fun createStockDraft() {
        viewModel.loading = true

        var destinationCode = 0
        var destinationString = ""

//        Log.e("userWareHouses", memory.user.warehouses.toList().toString())
//
//        for (elements in memory.user.warehouses.values) {
//            if (elements.contains("دپو")) {
//                destinationCode =
//                    memory.user.warehouses.entries.find { it.value == elements }?.key?.toInt() ?: 0
//                destinationString = elements
//            }
//        }

//        for (elements in memory.user.warehouses.values) {
//            if (elements == destinationString.replace("(دپو)", "")) {
//                sourceCode =
//                    memory.user.warehouses.entries.find { it.value == elements }?.key?.toInt() ?: 0
//                sourceString = elements
//            }
//        }
//
        val originCode: Int = memory.user.warehouseCode
        val originString: String =
            memory.user.warehouses[memory.user.warehouseCode.toString()] ?: ""

        for (elements in memory.user.warehouses.values) {
            if (elements.contains("دپو") && elements.contains(originString)) {
                destinationCode =
                    memory.user.warehouses.entries.find { it.value == elements }?.key?.toInt() ?: 0
                destinationString = elements
            }
        }

        if (originCode == 0) {
            viewModel.popupState.showPopupWithAButton("انبار مبدا به درستی انتخاب نمیشود")
            viewModel.loading = false
            return
        }
        if (destinationCode == 0) {
            viewModel.popupState.showPopupWithAButton("شما به انبار فروشگاه دسترسی ندارید.")
            viewModel.loading = false
            return
        }
        if (destinationCode.toString() == originCode.toString() || destinationString.length <= originString.length) {
            viewModel.popupState.showPopupWithAButton("خطای سیستمی رخ داده است.")
            viewModel.loading = false
            return
        }
        for (elements in uiList) {
            if (elements.scannedNumber > elements.storeNumber) {
                viewModel.popupState.showPopupWithAButton("موجودی فروشگاه برخی از کالا ها کافی نمیباشد.")
                viewModel.loading = false
                return
            }
        }

        Log.e(
            "sourceAndDes",
            "sourceCode:$originCode, sourceString:$originString \n desCode:$destinationCode, desString:$destinationString"
        )

        localStoreDatabase.createStockDraft(
            memory.user.username,
            originCode,
            destinationCode,
            uiList,
            "ارسال کالا به انبار با RFID",
            { stockDraftID ->
                viewModel.loading = false

                viewModel.popupState.showPopupWithAButton(
                    message = "اجناس با شماره حواله $stockDraftID به فروشگاه ارسال شدند.",
                    onDoneButtonClick = {
                        barcode.scannedBarcodes.clear()
                        products.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                    },
                    onDismiss = {
                        barcode.scannedBarcodes.clear()
                        products.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                    }
                )
            },
            {
                viewModel.loading = false
            }
        )
    }

    private fun syncScannedItemToServer(barcode: String) {

        viewModel.loading = true
        val alreadySyncedBarcodes = mutableListOf<String>()
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        if (barcode in alreadySyncedBarcodes) {
            successBeep(state = state)
            saveToMemory()
            val productIndex = products.indexOf(products.last { it1 ->
                it1.scannedBarcode == barcode
            })
            products[productIndex].scannedBarcodeNumber =
                this.barcode.scannedBarcodes.count { it1 ->
                    it1 == barcode
                }

            uiList.addAllAndSort(products)
            viewModel.loading = false
            return
        }

        repository.getBarcodeDetails(
            barcode = barcode, {
                successBeep(state = state)
                saveToMemory()
                var isInRefillProductList = false

                run forEacheadlineMedium@{
                    products.forEach { it1 ->
                        if (it1.primaryKey == it.primaryKey) {
                            it1.scannedBarcode = it.scannedBarcode
                            it1.scannedBarcodeNumber += 1
                            isInRefillProductList = true
                            return@forEacheadlineMedium
                        }
                    }
                }
                if (!isInRefillProductList) {
                    it.scannedBarcodeNumber = 1
                    products.add(it)
                }

                uiList.addAllAndSort(products)
                viewModel.loading = false
            }, {
                this.barcode.scannedBarcodes.remove(barcode)
                errorBeep(state = state)
                viewModel.loading = false
            }
        )
    }

    @SuppressLint("ApplySharedPref")
    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = memory.edit()

        edit.putString(
            "ReverseRefillWarehouseManagerBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )

        edit.putString(
            "ReverseRefillProducts",
            Gson().toJson(products).toString()
        )

        edit.apply()
    }

    private fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)

        val type = object : TypeToken<List<Product>>() {}.type

        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("ReverseRefillWarehouseManagerBarcodeTable", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()

        products = Gson().fromJson(
            memory.getString("ReverseRefillProducts", ""),
            type
        ) ?: mutableListOf()
    }

    fun clear(product: Product) {

        val removedRefillProducts = mutableListOf<Product>()

        products.forEach {
            if (it.KBarCode == product.KBarCode) {

                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode
                }
                removedRefillProducts.add(it)
            }
        }
        products.removeAll(removedRefillProducts.toSet())
        removedRefillProducts.clear()

        uiList.addAllAndSort(products)
        saveToMemory()
    }

    private fun back() {
        if (viewModel.scanningMode) {
            saveToMemory()
            finish()
        } else {
            saveToMemory()
            viewModel.scanningMode = true
            if (!viewModel.barcode.isEnabled) {
                viewModel.barcode.enable()
            } else {
                viewModel.barcode.disable()
            }
        }
    }

    private fun openSearchActivity(product: Product) {

        val intent = Intent(this, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        startActivity(intent)
    }

    private fun SnapshotStateList<Product>.addAllAndSort(products: MutableList<Product>): Boolean {

        this.clear()
        val returnVar = this.addAll(products)
        this.sortBy {
            it.productCode
        }
        this.sortBy {
            it.name
        }
        this.sortBy {
            it.scannedNumber > 0
        }
        return returnVar
    }
}