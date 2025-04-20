package com.jeanwest.reader.features.carton.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * this file holds Carton details feature viewmodel
 * and manage screen changes, button actions,
 * api call and barcode scanner of Carton details screens.
 */

@HiltViewModel
class CartonDetailsViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var barcode: Barcode
    val inputProducts = mutableMapOf<String, Product>()
    var inputBarcodeMapWithProperties = mutableMapOf<String, Product>()
    var cartonProperties = Carton(number = "0", numberOfItems = 0)
    var productConflicts = mutableStateListOf<Product>()
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    var cartonNumber by mutableStateOf("")
    var showDetailMode by mutableStateOf(false)
    var openPrintDialog by mutableStateOf(false)
    var printers = mutableStateMapOf<String, Int>()
    var printer by mutableStateOf("")
    var rf: RFID

    init {
        loadPrintersList()
        rf = RFID(context, state) {
            scanTrigger()
        }
        barcode = Barcode(context) {
            cartonNumber = it
            getCartonDetail(it)
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
        if (showDetailMode) {
            barcode.disable()
        }
    }

    fun getCartonDetail(code: String) {

        loading = true

        if (code.isEmpty()) {
            showLog("لطفا شماره کارتن را وارد کنید", state)
            loading = false
            return
        }

        api.getCartonsDetails(listOf(code), { cartons ->
            if (cartons.isNotEmpty()) {
                cartonProperties = cartons[0]
                showDetailMode = true
                if (barcode.isEnabled) {
                    barcode.disable()
                }
                syncInputItemsToServer()
            } else {
                loading = false
            }
        }, {
            loading = false
        })
    }

    fun openSearchActivity(product: Product) {

        val searchResultProduct = Product(
            name = product.name + " از حواله شماره " + cartonProperties.number,
            KBarCode = product.KBarCode,
            imageUrl = product.imageUrl,
            color = product.color,
            size = product.size,
            productCode = product.productCode,
            rfidKey = product.rfidKey,
            primaryKey = product.primaryKey,
            originalPrice = product.originalPrice,
            salePrice = product.salePrice,
            storeNumber = product.storeNumber,
            wareHouseNumber = product.wareHouseNumber,
        )

        val intent = Intent(context, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(searchResultProduct).toString())
        context.startActivity(intent)
    }

    fun back() {
        if (showDetailMode) {
            inputBarcodeMapWithProperties.clear()
            inputProducts.clear()
            productConflicts.clear()
            cartonProperties = Carton(number = "0", numberOfItems = 0)
            uiList.clear()
            cartonNumber = ""
            showDetailMode = false
            if (!barcode.isEnabled) {
                barcode.enable()
            }
        }
    }

    private fun makeInputProductMap() {

        inputProducts.clear()
        cartonProperties.barcodeTable.distinct().forEach {
            val product = inputBarcodeMapWithProperties[it]!!.copy()
            product.draftNumber = cartonProperties.barcodeTable.count { it1 ->
                it == it1
            }

            if (product.KBarCode !in inputProducts.keys) {
                inputProducts[product.KBarCode] = product.copy()
            } else {
                inputProducts[product.KBarCode]!!.draftNumber += product.draftNumber
            }
        }
    }


    fun syncInputItemsToServer() {

        loading = true

        val barcodeTableForV4 = mutableListOf<String>()
        var inputProductsBiggerThan1000 = false

        run breakForEach@{
            cartonProperties.barcodeTable.distinct().forEach {
                if (it !in inputBarcodeMapWithProperties.keys) {
                    if (barcodeTableForV4.size < 1000) {
                        barcodeTableForV4.add(it)
                    } else {
                        inputProductsBiggerThan1000 = true
                        return@breakForEach
                    }
                }
            }
        }

        if (barcodeTableForV4.size == 0) {
            calculateConflicts()
            loading = false
            return
        }

        api.getItemDetails(
            mutableListOf(),
            barcodeTableForV4,
            { _, barcodes, _, invalidBarcodes ->

                barcodes.forEach { product ->
                    inputBarcodeMapWithProperties[product.scannedBarcode] = product
                }

                if (inputProductsBiggerThan1000) {
                    syncInputItemsToServer()
                } else if (invalidBarcodes.length() != 0) {
                    loading = false
                    return@getItemDetails
                } else {
                    makeInputProductMap()
                    calculateConflicts()
                    loading = false
                }
            },
            {
                loading = false
            })
    }

    private fun filterResult(conflictResult: MutableList<Product>) {

        uiList.clear()
        uiList.addAll(conflictResult)
        uiList.sortBy {
            it.productCode
        }
        uiList.sortBy {
            it.name
        }
    }


    private fun calculateConflicts() {

        val conflicts = mutableListOf<Product>()

        conflicts.addAll(inputProducts.values)
        productConflicts.clear()
        productConflicts.addAll(conflicts)
        filterResult(conflicts)
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }


    fun printCarton() {

        loading = true

        api.printCartonLabel(printers[printer] ?: 0, cartonNumber, {
            loading = false
        }, {
            loading = false
        })
    }


    private fun loadPrintersList() {
        loading = true
        api.getPrintersList({
            printers.clear()
            printers.putAll(it)
            Log.e("printers list", it.keys.toString() + it.values.toString())
            printer = printers.keys.toMutableList()[0]
            loading = false
        }, {
            loading = false
        })
    }
}