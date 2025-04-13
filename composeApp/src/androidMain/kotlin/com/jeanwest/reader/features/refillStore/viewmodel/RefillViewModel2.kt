package com.jeanwest.reader.features.refillStore.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import com.jeanwest.reader.features.kiosk.view.SearchProduct
import com.jeanwest.reader.features.print.view.PrintPricePerProduct
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class RefillViewModel2 @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    private val rf: RFID
    val barcode: Barcode

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase
    var loading by mutableStateOf(false)
        private set
    var refillProducts = mutableListOf<Product>()
    var uiList = mutableStateListOf<Product>()
    var departmentFilterList = mutableListOf<String>()
    var selectedDepartmentFilter by mutableStateOf("همه انواع کالا")
    private var foundProductsNumber by mutableIntStateOf(0)
    var popupState = NotificationPopupHost()
    val listState = LazyListState(0)

    init {
        barcode = Barcode(context) {
            syncScannedItemToServer(it)
        }
        rf = RFID(context, state) {
            scanTrigger()
        }
        getRefillProducts()
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.enable()
        barcode.disconnectFromContext()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
    }

    fun filterUiByDepName(depName: String) {
        selectedDepartmentFilter = depName
        uiList.addAllAndSort(refillProducts)
    }

    fun loadMemory() {
        uiList.clear()
        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        barcode.scannedBarcodes = Gson().fromJson(
            memory.getString("RefillBarcodeTable2", ""),
            barcode.scannedBarcodes.javaClass
        ) ?: mutableStateListOf()
    }

    fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = memory.edit()
        edit.putString("RefillBarcodeTable2", JSONArray(barcode.scannedBarcodes).toString())
        edit.apply()
    }

    fun openSearchActivity(product: Product) {
        val intent = Intent(context, SearchProduct::class.java)
        intent.putExtra("product", Gson().toJson(product).toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun getRefillProducts() {
        loading = true
        api.getRefillNew(memory.user.calculatedLocationCode,{
            loading = false
            refillProducts.addAll(it)
            uiList.addAllAndSort(it.toMutableList())
            syncScannedItemsToServer()
        }, {
            loading = false
        })
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun createStockDraft() {

        loading = true

        val source = memory.user.warehouseCode

        val sourceString: String
        if (memory.erpData.warehousesIDsToTitles[source.toString()].isNullOrBlank()) {
            loading = false
            popupState.showPopupWithAButton("لطفا انبار خود را درست انتخاب کنید. نوع انبار باید دپو باشد.")
            return
        } else {
            sourceString = memory.erpData.warehousesIDsToTitles[source.toString()]!!
        }

        var destination = 0
        memory.erpData.departmentWarehouses[memory.user.calculatedLocationCode.toString()]?.forEach { warehouseCode ->

            val warehouseString: String
            if (memory.erpData.warehousesIDsToTitles[warehouseCode].isNullOrBlank()) {
                loading = false
                popupState.showPopupWithAButton("کاربری شما به این فروشگاه دسترسی ندارد.")
                return
            } else {
                warehouseString = memory.erpData.warehousesIDsToTitles[warehouseCode]!!
            }

            if (warehouseCode == source.toString() || warehouseString.length >= sourceString.length) {
                return@forEach
            }

            if (warehouseString == sourceString.substring(0, warehouseString.length)
            ) {
                destination = warehouseCode.toInt()
            }
        }

        if (destination == 0) {
            popupState.showPopupWithAButton("شما به انبار فروشگاه دسترسی ندارید.")
            loading = false
            return
        }

        for (elements in uiList.filter {
            it.scannedNumber > 0
        }) {
            if (elements.scannedNumber > elements.wareHouseNumber) {
                popupState.showPopupWithAButton("موجودی انبار برخی از کالا ها کافی نمیباشد.")
                loading = false
                return
            }
        }
        Log.e("sendedItems", refillProducts.filter {
            it.scannedNumber > 0
        }.toMutableList().toString())
        localStoreDatabase.createStockDraft(
            memory.user.username,
            source,
            destination,
            refillProducts.filter {
                it.scannedNumber > 0
            }.toMutableList(),
            "خطی 2 با RFID",
            { stockDraftID ->

                popupState.showPopupWith2Button(
                    message = "اجناس با شماره حواله $stockDraftID به فروشگاه ارسال شدند، قیمت کالاها پرینت شود؟",
                    onOkClick = {
                        val intent = Intent(context, PrintPricePerProduct::class.java)
                        intent.putExtra(
                            "StockDraftId",
                            Gson().toJson(
                                StockDraft(
                                    number = stockDraftID.toLong(),
                                    numberOfItems = barcode.scannedBarcodes.size,
                                    barcodeTable = barcode.scannedBarcodes.toMutableList(),
                                    specification = "خطی 2 با RFID",
                                )
                            )
                        )
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    onCancelClick = {
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    },
                    onDismiss = {
                        barcode.scannedBarcodes.clear()
                        refillProducts.removeAll {
                            it.scannedBarcodeNumber > 0
                        }
                        saveToMemory()
                        syncScannedItemsToServer()
                        if (!barcode.isEnabled) {
                            barcode.enable()
                        }
                    }
                )
                loading = false
            },
            {
                loading = false
            }
        )
    }

    fun clear(product: Product) {

        refillProducts.forEach {
            if (it.KBarCode == product.KBarCode) {
                it.scannedBarcodeNumber = 0
                barcode.scannedBarcodes.removeAll { it1 ->
                    it1 == it.scannedBarcode
                }
                it.scannedBarcode = ""
            }
        }
        uiList.clear()
        uiList.addAllAndSort(refillProducts)
        foundProductsNumber = uiList.filter { refillProduct ->
            refillProduct.scannedBarcodeNumber > 0
        }.size
        saveToMemory()
    }

    private fun syncScannedItemsToServer() {

        loading = true

        if (barcode.scannedBarcodes.size == 0) {
            uiList.addAllAndSort(refillProducts)
            loading = false
            return
        }

        val barcodeTableForV4 = mutableListOf<String>()

        val alreadySyncedBarcodes = mutableListOf<String>()
        refillProducts.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        barcode.scannedBarcodes.forEach {
            if (it !in alreadySyncedBarcodes) {
                barcodeTableForV4.add(it)
            } else {
                val productIndex = refillProducts.indexOf(refillProducts.last { refillProduct ->
                    refillProduct.scannedBarcode == it
                })
                refillProducts[productIndex].scannedBarcodeNumber =
                    barcode.scannedBarcodes.count { it1 ->
                        it1 == it
                    }
            }
        }

        if (barcodeTableForV4.size == 0) {
            uiList.addAllAndSort(refillProducts)
            loading = false
            return
        }

        api.getItemDetailsAndInventory(
            mutableListOf(),
            barcodeTableForV4,
            { _, barcodes, _, invalidBarcodes ->

                val junkBarcodes = mutableListOf<String>()
                for (i in barcodes.indices) {

                    val isInRefillList = refillProducts.any { refillProduct ->
                        refillProduct.primaryKey == barcodes[i].primaryKey
                    }

                    if (isInRefillList) {

                        val productIndex =
                            refillProducts.indexOf(refillProducts.last { refillProduct ->
                                refillProduct.primaryKey == barcodes[i].primaryKey
                            })

                        refillProducts[productIndex].scannedBarcodeNumber =
                            barcode.scannedBarcodes.count { it1 ->
                                it1 == barcodes[i].scannedBarcode
                            }

                        refillProducts[productIndex].scannedBarcode = barcodes[i].scannedBarcode
                    } else {
                        junkBarcodes.add(barcodes[i].scannedBarcode)
                    }
                }

                for (i in 0 until invalidBarcodes.length()) {
                    junkBarcodes.add(invalidBarcodes[i].toString())
                }

                barcode.scannedBarcodes.removeAll(junkBarcodes.toSet())
                uiList.addAllAndSort(refillProducts)
                foundProductsNumber = uiList.filter { refillProduct ->
                    refillProduct.scannedBarcodeNumber > 0
                }.size
                loading = false

            },
            {
                uiList.addAllAndSort(refillProducts)
                loading = false
            }, true
        )
    }

    private fun syncScannedItemToServer(barcode: String) {

        loading = true

        val alreadySyncedBarcodes = mutableListOf<String>()
        refillProducts.forEach {
            if (it.scannedBarcodeNumber > 0) {
                alreadySyncedBarcodes.add(it.scannedBarcode)
            }
        }

        if (barcode in alreadySyncedBarcodes) {

            val productIndex = refillProducts.indexOf(refillProducts.last { refillProduct ->
                refillProduct.scannedBarcode == barcode
            })
            if (refillProducts[productIndex].scannedBarcodeNumber < refillProducts[productIndex].requestedNumber) {
                refillProducts[productIndex].scannedBarcodeNumber =
                    this.barcode.scannedBarcodes.count { it1 ->
                        it1 == barcode
                    }
                uiList.addAllAndSort(refillProducts)
                successBeep(state)
                saveToMemory()
                loading = false
                return
            } else {
                errorBeep(state)
                this.barcode.scannedBarcodes.remove(barcode)
                showLog("تعداد اسکن شده از تعداد درخواست نمی تواند بیشتر باشد.", state)
                loading = false
                return
            }
        }

        api.getItemDetailsAndInventory(
            mutableListOf(),
            mutableListOf(barcode),
            { _, barcodes, _, _ ->

                if (barcodes.size == 1) {

                    val isInRefillList = refillProducts.any { refillProduct ->
                        refillProduct.primaryKey == barcodes[0].primaryKey
                    }

                    if (isInRefillList) {
                        successBeep(state)
                        saveToMemory()

                        val productIndex =
                            refillProducts.indexOf(refillProducts.last { refillProduct ->
                                refillProduct.primaryKey == barcodes[0].primaryKey
                            })

                        refillProducts[productIndex].scannedBarcodeNumber =
                            this.barcode.scannedBarcodes.count { it1 ->
                                it1 == barcodes[0].scannedBarcode
                            }

                        refillProducts[productIndex].scannedBarcode = barcodes[0].scannedBarcode
                    } else {
                        errorBeep(state)
                        this.barcode.scannedBarcodes.remove(barcode)
                    }
                } else {
                    errorBeep(state)
                    this.barcode.scannedBarcodes.remove(barcode)
                }

                uiList.addAllAndSort(refillProducts)
                foundProductsNumber = uiList.filter { refillProduct ->
                    refillProduct.scannedBarcodeNumber > 0
                }.size
                loading = false
            },
            {
                errorBeep(state)
                uiList.addAllAndSort(refillProducts)
                loading = false
            }, true
        )
    }

    private fun SnapshotStateList<Product>.addAllAndSort(products: MutableList<Product>): Boolean {

        departmentFilterList.clear()
        departmentFilterList.add("همه انواع کالا")
        products.forEach {
            departmentFilterList.add(it.departmentName)
        }
        departmentFilterList = departmentFilterList.distinct().toMutableList()

        this.clear()
        val returnVar =
            this.addAll(if (selectedDepartmentFilter == "همه انواع کالا") products else products.filter {
                it.departmentName == selectedDepartmentFilter
            })

        this.sortBy {
            it.productCode
        }
        this.sortBy {
            it.name
        }

        return returnVar
    }
}