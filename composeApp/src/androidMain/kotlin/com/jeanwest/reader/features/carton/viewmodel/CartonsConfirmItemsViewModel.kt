package com.jeanwest.reader.features.carton.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * this file holds CartonsConfirmItems
 * feature viewmodel and manage screen changes,
 * button actions,
 * api call and barcode scanner of CartonsConfirmItems screens.
 */

@HiltViewModel
class CartonsConfirmItemsViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context
) : ViewModel() {

    val popupState = NotificationPopupHost()

    var scanFilterValue by mutableStateOf("کسری")
        private set
    var uiList0 = mutableStateMapOf<String, MutableList<String>>()
        private set
    var uiList1 = mutableStateListOf<Carton>()
        private set
    var uiList2 = mutableStateListOf<Product>()
        private set
    var shortagesNumber by mutableIntStateOf(0)
        private set
    var additionalNumber by mutableIntStateOf(0)
        private set
    var scannedNumber by mutableIntStateOf(0)
        private set
    var uiState by mutableIntStateOf(0)
        private set
    var loading by mutableStateOf(false)
        private set
    var isScanning by mutableStateOf(false)
        private set

    private val cartons = mutableStateMapOf<String, Carton>()
    private val rf: RFID
    private val barcode: Barcode
    private val conflictResults = mutableMapOf<Long, Product>()
    private val validatedScannedEpcToPrimaryKey = mutableMapOf<String, Long>()
    private val productDetailsTable = mutableMapOf<Long, Product>()
    private val epcsToPrimaryKeyMap = mutableMapOf<String, Long>()
    private var selectedStockDraft = ""
    private var selectedCarton = Carton()
    private val tag = "cartonDetailsConfirm"

    var selectedCartonID by mutableStateOf("")

    init {

        rf = RFID(context, state) {
            scanTrigger()
        }
        barcode = Barcode(context) {
            onSelectCarton(it)
        }

        uiState = 0
        scannedNumber = rf.epcs.size
        getDraftIDs()
    }

    fun onConfirmCartonButtonClick() {

        if (shortagesNumber == 0 && additionalNumber == 0) {
            popupState.showPopupWith2Button(
                message = "کارتن را تایید می کنید؟",
                onOkClick = { confirmCartonWithoutConflict(selectedCartonID) },
                onCancelClick = { clear() })
        } else {
            popupState.showPopupWith2Button(
                message = "کارتن مغایرت دارد. کارتن جدیدی ایجاد و کارتن فعلی برگشت داده شود؟",
                onOkClick = { selectPrinterForCreatingNewCarton() },
                onCancelClick = { clear() })
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.enable()
        barcode.disconnectFromContext()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()

        if (uiState == 1) {
            barcode.enable()

        } else {
            barcode.disable()
        }
    }

    fun scanTrigger() {
        if (uiState == 2) {
            if (!rf.scanning) {
                isScanning = true
                loading = true
                rf.startBulkScan(
                    justFindInputEPCs = selectedCarton.everyProductHaveEpc,
                    selectedCarton.epcs
                )
            } else {
                isScanning = false
                loading = false
                rf.stopScanning()
                calculateConflicts()
            }
        } else if (uiState == 1) {
            barcode.startBarcodeScan()
        }
    }

    fun onFilterValueChange(filterValue: String) {
        this.scanFilterValue = filterValue
        filterResult()
    }

    fun onSelectCarton(cartonCode: String) {
        selectedCartonID = cartonCode
        clear()
        if (selectedCartonID in cartons.keys) {

            val carton = cartons[cartonCode]!!
            selectedCarton = carton

            if (!carton.isConfirmedByRFID) {

                if (uiState == 1) {
                    uiState = 2
                    barcode.disable()

                    carton.products.forEach { product ->
                        conflictResults[product.product.primaryKey] = product.product
                    }

                    if (!carton.everyProductHaveEpc) {
                        productDetailsTable.putAll(conflictResults)
                        epcsToPrimaryKeyMap.putAll(selectedCarton.epcsToPrimaryKeyMap)
                    }
                    calculateConflicts()

                    if (!carton.everyProductHaveEpc) {
                        popupState.showPopupWithAButton("کارتن انتخابی شما با استفاده از rfid ایجاد نشده است و باید در اتاق ایزوله تایید گردد.")
                    }
                }
            } else {
                showLog("کارتن قبلا تایید شده است.", state)
            }
        } else {
            showLog("کارتن در لیست موجود نیست.", state)
        }
    }

    fun onSelectLogistic(logisticCode: String) {
        selectedStockDraft = logisticCode
        uiList1.clear()
        cartons.clear()
        barcode.enable()
        syncInputCartonsToServer()
        uiState = 1
    }

    private fun confirmCartonWithoutConflict(cartonNumber: String) {
        confirmCarton(cartonNumber) {
            showLog("کالاهای کارتن با موفقیت تایید شد.", state)
        }
    }

    private fun confirmCarton(cartonNumber: String, onSuccess: () -> Unit) {
        loading = true
        isScanning = false
        api.confirmCartonByRfid(cartonNumber, {
            loading = false
            uiState = 1
            barcode.enable()
            uiList1[uiList1.indexOf(cartons[cartonNumber])].isConfirmed = true
            uiList1[uiList1.indexOf(cartons[cartonNumber])].isConfirmedByRFID =
                true
            uiList2.clear()
            rf.inputEPCs.clear()
            rf.epcs.clear()
            onSuccess()
        }, {
            loading = false
            showLog("مشکلی در تایید کالاهای کارتن پیش آمده است.", state)
        })
    }

    private fun selectPrinterForCreatingNewCarton() {
        popupState.showPopupWitheadlineMediumButtonDropDownList(
            onDoneClick = { printer ->
                confirmCartonWithConflict(selectedCartonID, memory.erpData.printers[printer] ?: 0)
            },
            message = "لطفا پرینتر را انتخاب کنید",
            dropDownText = memory.erpData.printers.keys.toList()[0],
            dropDownList = memory.erpData.printers.keys.toList(),
        )
    }

    private fun confirmCartonWithConflict(cartonNumber: String, printerID: Int) {

        loading = true
        isScanning = false
        api.createCarton(
            products = conflictResults.values.toList(),
            source = memory.user.warehouseCode,
            printer = printerID,
            { cartonID ->

                val stockDraftItems = mutableListOf<Product>()
                stockDraftItems.addAll(selectedCarton.products.map { it.product })
                stockDraftItems.forEach { item ->
                    item.manualScannedNumber = item.draftNumber
                }

                api.createStockDraft(
                    products = stockDraftItems,
                    destination = 2273,
                    source = memory.user.warehouseCode,
                    desc = "برگشت کارتن $cartonNumber با rfid به دلیل مغایرت",
                    onSuccess = { stockDraftID ->
                        confirmCarton(cartonNumber) {
                            showLog(
                                "کارتن فعلی تایید و برگشت زده شد " +
                                        "و حواله برگشت با شماره " + stockDraftID + " با موفقیت ثبت شد." +
                                        " کارتن جدید با شماره " + cartonID + " با موفقیت ایجاد شد و دستور پرینت لیبل آن ارسال شد.",
                                state
                            )
                            popupState.showPopupWithAButton(
                                message = "کارتن فعلی تایید و برگشت زده شد " +
                                        "و حواله برگشت با شماره " + stockDraftID + " با موفقیت ثیت شد." +
                                        " کارتن جدید با شماره " + cartonID + " با موفقیت ایجاد شد و دستور پرینت لیبل آن ارسال شد.",
                            )
                        }
                    },
                    onError = {
                        loading = false
                        showLog("مشکلی در برگشت کارتن به وجود آمده است.", state)
                    }
                )
            },
            {
                loading = false
                showLog("مشکلی در ایجاد کارتن جدید پیش آمده است.", state)
            }
        )
    }

    private fun filterResult() {

        shortagesNumber = 0
        additionalNumber = 0
        scannedNumber = rf.epcs.size

        conflictResults.values.forEach {
            when (it.conflictType) {
                "کسری" -> {
                    shortagesNumber += it.conflictNumber
                }

                "اضافی" -> {
                    additionalNumber += it.conflictNumber
                }
            }
        }

        uiList2.clear()
        uiList2.addAll(conflictResults.values.filter {
            it.conflictType == scanFilterValue
        }.toMutableStateList())
    }

    private fun getDraftIDs() {
        loading = true
        api.getDriversCartonsListsV2(
            iSDeliverToDest = true,
            iSConfirmedByRFID = false,
            onSuccess = {
                uiList0.clear()
                uiList0.putAll(it)
                loading = false
            }, onError = {
                loading = false
            })
    }

    private fun syncInputCartonsToServer() {

        loading = true

        api.getCartonsDetails(
            codes = uiList0[selectedStockDraft]?.toList() ?: mutableListOf(), { cartons ->

                cartons.forEach { carton ->
                    this.cartons[carton.number] = carton
                }
                uiList1.clear()
                uiList1.addAll(this.cartons.values)
                loading = false

            }, {
                loading = false
            }
        )
    }

    private fun calculateConflicts() {

        Log.e(tag, "enter calculate conflicts")

        loading = true

        CoroutineScope(Dispatchers.Default).launch {

            if (rf.epcs.size == 0) {
                filterResult()
                loading = false
                return@launch
            }

            if (selectedCarton.everyProductHaveEpc) {

                rf.epcs = rf.epcs.filter {
                    it in selectedCarton.epcsToPrimaryKeyMap.keys
                }.toMutableStateList()

                rf.epcs.forEach { epc ->

                    if (epc !in validatedScannedEpcToPrimaryKey) {

                        val product =
                            conflictResults[selectedCarton.epcsToPrimaryKeyMap[epc]]

                        if (product != null) {
                            product.manualScannedNumber++
                            product.scannedBarcodeNumber = 0
                            product.scannedEPCs.clear()
                            validatedScannedEpcToPrimaryKey[epc] = product.primaryKey
                        } else {
                            showLog("مشکلی در پردازش اطلاعات به وجود آمده است.", state)
                        }
                    }
                }

                filterResult()

                loading = false

            } else {

                Log.e(tag, "enter method 2")

                var needServerData = false

                rf.epcs.forEach { epc ->

                    if (epc !in validatedScannedEpcToPrimaryKey) {

                        val product = productDetailsTable[epcsToPrimaryKeyMap[epc]]

                        if (product != null) {

                            if (product.primaryKey !in conflictResults) {
                                product.scannedBarcodeNumber = 0
                                product.scannedEPCs.clear()
                                product.manualScannedNumber = 1
                                product.draftNumber = 0
                                conflictResults[product.primaryKey] = product
                                validatedScannedEpcToPrimaryKey[epc] = product.primaryKey
                            } else {
                                product.scannedBarcodeNumber = 0
                                product.scannedEPCs.clear()
                                conflictResults[product.primaryKey]!!.manualScannedNumber++
                                validatedScannedEpcToPrimaryKey[epc] = product.primaryKey
                            }

                        } else {
                            needServerData = true
                        }
                    }
                }

                if (needServerData) {
                    getScannedItemsDetails()
                } else {
                    filterResult()
                    loading = false
                }
            }
        }
    }

    private fun getScannedItemsDetails() {

        loading = true

        Log.e(tag, "enter sync ")

        CoroutineScope(Dispatchers.IO).launch {

            val epcTableForV4 = mutableListOf<String>()

            run breakForEach@{
                rf.epcs.forEach {
                    if (it !in validatedScannedEpcToPrimaryKey) {
                        if (epcTableForV4.size < 100) {
                            epcTableForV4.add(it)
                        } else {
                            return@breakForEach
                        }
                    }
                }
            }

            if (epcTableForV4.size == 0) {
                filterResult()
                loading = false
                return@launch
            }

            Log.e(tag, epcTableForV4.toList().toString())

            api.getItemDetails(epcTableForV4, mutableListOf(), { epcs, _, invalidEpcs, _ ->

                epcs.forEach { product ->
                    productDetailsTable[product.primaryKey] = product
                    epcsToPrimaryKeyMap[product.scannedEPCs[0]] = product.primaryKey
                }

                for (i in 0 until invalidEpcs.length()) {
                    rf.epcs.remove(invalidEpcs[i])
                    Log.e(tag, invalidEpcs[i].toString())
                }

                calculateConflicts()

            }, {
                calculateConflicts()
            })
        }
    }


    fun clear() {
        conflictResults.clear()
        rf.epcs.clear()
        validatedScannedEpcToPrimaryKey.clear()
        productDetailsTable.clear()
        epcsToPrimaryKeyMap.clear()
    }

    fun back() {

        when (uiState) {

            1 -> {
                if (barcode.isEnabled) {
                    barcode.disable()
                }
                uiList1.clear()
                getDraftIDs()
                uiState = 0
            }

            2 -> {
                if (!barcode.isEnabled) {
                    barcode.enable()
                }
                conflictResults.clear()
                uiList2.clear()
                cartons.clear()
                syncInputCartonsToServer()
                uiState = 1
            }
        }
    }
}