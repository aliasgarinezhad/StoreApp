package com.jeanwest.reader.features.carton.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class CartonCreateViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var cartonNumber = ""
    var barcode: Barcode
    var rf: RFID
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    var products = mutableListOf<Product>()
    var scanTypeValue by mutableStateOf("RFID")
    var printer by mutableStateOf("")
    var openPrintDialog by mutableStateOf(false)
    var popupState = NotificationPopupHost()
    var scannedNumber by mutableIntStateOf(0)
    var printerList: MutableList<String>

    init {
        rf = RFID(context, state) {
            scanTrigger()
        }
        barcode = Barcode(context) {
            syncScannedItemToServer(it)
        }
        printerList = memory.erpData.printers.keys.toMutableList()
        printer = memory.erpData.printers.keys.toMutableList()[0]
        syncScannedItemsToServer()
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
        if (scanTypeValue == "RFID") {
            barcode.disable()
        }
    }

    private fun createNewCarton() {
        loading = true
        api.createCarton(
            uiList,
            memory.user.warehouseCode,
            memory.erpData.printers[printer] ?: 0,
            {
                cartonNumber = it
                barcode.scannedBarcodes.clear()
                rf.epcs.clear()
                products.clear()
                syncScannedItemsToServer()
                popupState.showPopupWithAButton("کارتن با شماره $cartonNumber ایجاد و دستور پرینت لیبل آن ارسال شد.")
                loading = false
            },
            {
                loading = false
            }
        )
    }

    fun scanTrigger() {
        if (scanTypeValue == "بارکد") {
            rf.stopScanning()
            barcode.startBarcodeScan()
        } else {
            if (!rf.scanning && !loading) {
                rf.startBulkScan()
            } else {
                rf.stopScanning()
                if ((rf.epcs.size + barcode.scannedBarcodes.size != 0) && !loading) {
                    syncScannedItemsToServer()
                }
            }
        }
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (barcode.scannedBarcodes.size + rf.epcs.size == 0) {
            uiList.clear()
            uiList.addAll(products)
            uiList.sortBy {
                it.productCode
            }
            uiList.sortBy {
                it.name
            }
            scannedNumber = 0
            uiList.forEach {
                scannedNumber += it.scannedNumber
            }
            loading = false
            return
        }

        val barcodeArray = mutableListOf<String>()
        val epcArray = mutableListOf<String>()
        val alreadySyncedBarcodes = mutableListOf<String>()
        val alreadySyncedEpcs = mutableListOf<String>()

        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
            if (it.scannedEPCNumber > 0) {
                alreadySyncedEpcs.addAll(it.scannedEPCs)
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

        rf.epcs.forEach {
            if (it !in alreadySyncedEpcs) {
                epcArray.add(it)
            }
        }

        if (barcodeArray.size + epcArray.size == 0) {
            uiList.clear()
            uiList.addAll(products)
            uiList.sortBy {
                it.productCode
            }
            uiList.sortBy {
                it.name
            }
            scannedNumber = 0
            uiList.forEach {
                scannedNumber += it.scannedNumber
            }
            loading = false
            return
        }

        api.getItemDetails(
            epcArray,
            barcodeArray,
            { epcs, barcodes, invalidEpcs, invalidBarcodes ->

                barcodes.forEach {
                    var isInRefillProductList = false

                    run forEach1@{
                        products.forEach { it1 ->
                            if (it1.KBarCode == it.KBarCode) {
                                it1.scannedBarcode = it.scannedBarcode
                                it1.scannedBarcodeNumber += 1
                                isInRefillProductList = true
                                return@forEach1
                            }
                        }
                    }
                    if (!isInRefillProductList) {
                        it.scannedBarcodeNumber = 1
                        products.add(it)
                    }
                }

                epcs.forEach {
                    var isInRefillProductList = false

                    run forEach1@{
                        products.forEach { it1 ->
                            if (it1.KBarCode == it.KBarCode) {
                                it1.scannedEPCs.addAll(it.scannedEPCs)
                                isInRefillProductList = true
                                return@forEach1
                            }
                        }
                    }
                    if (!isInRefillProductList) {
                        products.add(it)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    barcode.scannedBarcodes.remove(invalidBarcodes[i])
                }
                for (i in 0 until invalidEpcs.length()) {
                    rf.epcs.remove(invalidEpcs[i])
                }

                uiList.clear()
                uiList.addAll(products)
                uiList.sortBy {
                    it.productCode
                }
                uiList.sortBy {
                    it.name
                }
                scannedNumber = 0
                uiList.forEach {
                    scannedNumber += it.scannedNumber
                }
                loading = false
            },
            {
                loading = false
            }
        )
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        val alreadySyncedBarcodes = mutableListOf<String>()
        products.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        if (barcode in alreadySyncedBarcodes) {
            successBeep(state)
            val productIndex = products.indexOf(products.last { it1 ->
                it1.scannedBarcode == barcode
            })
            products[productIndex].scannedBarcodeNumber =
                this.barcode.scannedBarcodes.count { it1 ->
                    it1 == barcode
                }

            uiList.clear()
            uiList.addAll(products)
            uiList.sortBy {
                it.productCode
            }
            uiList.sortBy {
                it.name
            }
            uiList.sortBy { it1 ->
                it1.scannedEPCNumber + it1.scannedBarcodeNumber > 0
            }
            scannedNumber = 0
            uiList.forEach {
                scannedNumber += it.scannedNumber
            }
            loading = false
            return
        }

        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size == 1) {
                    successBeep(state)
                    var isInRefillProductList = false

                    run forEach1@{
                        products.forEach { it1 ->
                            if (it1.KBarCode == barcodes[0].KBarCode) {
                                it1.scannedBarcode = barcodes[0].scannedBarcode
                                it1.scannedBarcodeNumber += 1
                                isInRefillProductList = true
                                return@forEach1
                            }
                        }
                    }
                    if (!isInRefillProductList) {
                        barcodes[0].scannedBarcodeNumber = 1
                        products.add(barcodes[0])
                    }
                } else {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                }

                uiList.clear()
                uiList.addAll(products)
                uiList.sortBy {
                    it.productCode
                }
                uiList.sortBy {
                    it.name
                }
                uiList.sortBy { it1 ->
                    it1.scannedEPCNumber + it1.scannedBarcodeNumber > 0
                }
                scannedNumber = 0
                uiList.forEach {
                    scannedNumber += it.scannedNumber
                }
                loading = false

            },
            {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                loading = false
            })
    }

    fun clear(product: Product) {

        rf.epcs = rf.epcs.filter {
            it !in product.scannedEPCs
        }.toMutableStateList()

        barcode.scannedBarcodes.removeAll(listOf(product.scannedBarcode))

        repeat(product.scannedBarcodeNumber - 1) {
            barcode.scannedBarcodes.add(product.scannedBarcode)
        }

        val productIndex = products.indexOf(products.find {
            it.primaryKey == product.primaryKey
        })

        products[productIndex].scannedBarcodeNumber =
            barcode.scannedBarcodes.count {
                it == product.scannedBarcode
            }

        products[productIndex].scannedEPCs.clear()

        if (products[productIndex].scannedNumber == 0) {
            products.removeAt(productIndex)
        }

        uiList.clear()
        uiList.addAll(products)

        uiList.sortBy { it1 ->
            it1.name
        }

        scannedNumber = 0
        uiList.forEach {
            scannedNumber += it.scannedNumber
        }
    }

    fun openSearchActivity(product: Product, context: Context) {
        val intent = Intent(context, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        context.startActivity(intent)
    }

    fun onBottomBarClick() {
        openPrintDialog = true
    }

    fun onPrinterSelected(printer: String) {
        this.printer = printer
    }

    fun onPrintConfirm() {
        openPrintDialog = false
        createNewCarton()
    }

    fun onPopupDismiss() {
        openPrintDialog = false
    }

    fun onScanTypeChanged(type: String) {
        scanTypeValue = type
        if (!barcode.isEnabled && scanTypeValue == "بارکد") {
            barcode.enable()
        } else if (barcode.isEnabled && scanTypeValue == "RFID") {
            barcode.disable()
        }
    }

}