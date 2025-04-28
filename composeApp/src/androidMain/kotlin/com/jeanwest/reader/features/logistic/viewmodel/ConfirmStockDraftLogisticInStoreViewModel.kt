package com.jeanwest.reader.features.logistic.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Logistic
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

/**
 * this view model class responsible
 * for ConfirmStockDraftLogisticInStore feature screen processing,
 * barcode scanning, RFID scanning, api calls, button actions, etc.
 */

@HiltViewModel
class ConfirmStockDraftLogisticInStoreViewModel @Inject constructor(
    val state: SnackbarHostState,
    val popupState: NotificationPopupHost,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    lateinit var barcode: Barcode
    var rf: RFID

    //ui parameters
    var loading by mutableStateOf(false)

    private var deliveryCode by mutableStateOf("")
    private var stockDrafts = mutableStateMapOf<String, StockDraft>()
    private var selectedStockDraftTransferHashcode = 0
    private var previousScannedBarcodes = mutableListOf<String>()

    var logistics = mutableStateMapOf<Int, Logistic>()
        private set
    var uiList = mutableStateListOf<StockDraft>()
        private set
    var scanningMode by mutableStateOf(false)
        private set
    var stockDraftNumber by mutableStateOf("")
        private set
    var isCameraOn by mutableStateOf(false)


    init {
        rf = RFID(context, state) {
            scanTrigger()
        }
        barcode = Barcode(context) { scannedBarcode ->
            onBarcodeScanResult(scannedBarcode)
        }

        loadMemory()
        getLogisticsDetails()
        exceptionHandler()
    }

    fun onBarcodeScanResult(scannedBarcode: String) {

        val it = if (scannedBarcode.uppercase()
                .startsWith("GN")
        ) scannedBarcode.substring(2) else scannedBarcode

        if (scanningMode) {
            this.barcode.scannedBarcodes.add(it)
            this.barcode.scannedBarcodes =
                this.barcode.scannedBarcodes.distinct().toMutableStateList()
            processScannedStockDrafts(it)
            saveToMemory()
        }

        if (isCameraOn) {
            isCameraOn = false
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
        if (!scanningMode) {
            barcode.disable()
        }
    }

    fun onLogisticClick(logistic: Logistic) {
        if (logistic.name != logistics[selectedStockDraftTransferHashcode]?.name) {
            selectedStockDraftTransferHashcode =
                logistic.hashCode()
            clear()
        }

        popupState.showPopUpWithAButtonAndTextField(
            message = "کد تحویل را وارد کنید",
            onDoneClick = {
                deliveryCode = it
                getDraftIDs()
            },
            value = deliveryCode,
        )
    }

    fun onTextFieldImeAction() {
        if (stockDraftNumber.isNotBlank()) {
            processScannedStockDrafts(stockDraftNumber, true)
        }
    }

    fun onTextFieldValueChange(newValue: String) {
        stockDraftNumber = newValue
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun getLogisticsDetails() {

        loading = true
        api.getDriversStockDraftsListsLogisics({ logisticsDetails ->
            logistics.clear()
            logistics.putAll(logisticsDetails)
            loading = false
        }, {
            loading = false
        })
    }

    private fun getDraftIDs() {

        loading = true

        if (deliveryCode.toLongOrNull() == null) {
            showLog("کد لجستیک اشتباه است.", state)
            selectedStockDraftTransferHashcode = 0
            loading = false
            return
        }

        api.driverLogisticList(deliveryCode.toLong(), {

            it.forEach { stockDraft ->
                logistics[selectedStockDraftTransferHashcode]?.items?.add(stockDraft.number.toString())
            }
            syncInputCartonsToServer()
        }, {
            loading = false
            deliveryCode = ""
            showLog("کد لجستیک اشتباه است.", state)
        })
    }

    fun confirmCheckIns() {

        loading = true

        run breakForEach@{
            stockDrafts.values.forEach {
                if (!it.isScanned) {
                    showLog("لطفا کسری لیست را برطرف کنید.", state)
                    loading = false
                    return
                }
            }
        }

        api.driverLogisticListFinal(deliveryCode.toLong(), {
            showLog("با موفقیت انجام شد.", state, action = SnackBarActions.SUCCESS)
            loading = false
            clear()
            scanningMode = false
            if (barcode.isEnabled) {
                barcode.disable()
            }
            getLogisticsDetails()
        }, {
            loading = false
        })
    }

    private fun syncInputCartonsToServer() {

        loading = true

        var cartonCode = ""
        run breakForEach@{
            logistics[selectedStockDraftTransferHashcode]?.items?.forEach {
                if (it !in stockDrafts.keys) {
                    cartonCode = it
                    return@breakForEach
                }
            }
        }

        if (cartonCode == "") {
            uiList.clear()
            uiList.addAll(stockDrafts.values)
            scanningMode = true
            if (!barcode.isEnabled) {
                barcode.enable()
            }
            if (barcode.scannedBarcodes.isNotEmpty()) {
                barcode.scannedBarcodes.forEach {
                    processScannedStockDrafts(it, false)
                }
            }
            loading = false
            return
        }

        api.getStockDraftDetails(
            cartonCode,
            {
                stockDrafts[cartonCode] = it
                uiList.clear()
                uiList.addAll(stockDrafts.values)
                syncInputCartonsToServer()
            },
            {
                loading = false
            })
    }

    private fun processScannedStockDrafts(barcode: String, enableBeep: Boolean = true) {
        if (barcode in stockDrafts.keys) {
            stockDrafts[barcode]!!.isScanned = true
            uiList.clear()
            uiList.addAll(stockDrafts.values)
            if (enableBeep) successBeep(state)
            uiList.sortBy {
                this.barcode.scannedBarcodes.indexOf(it.number.toString())
            }
            saveToMemory()
        } else {
            if (enableBeep) errorBeep(state)
            showLog("حواله در لیست ارسالی وجود ندارد!", state)
            this.barcode.scannedBarcodes.remove(barcode)
        }
    }

    private fun saveToMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = memory.edit()
        edit.putString(
            "StockDraftTransferToStoreConfirmationBarcodeTable",
            JSONArray(barcode.scannedBarcodes).toString()
        )
        edit.putInt(
            "StockDraftTransferToStoreConfirmationDriverName",
            selectedStockDraftTransferHashcode
        )
        edit.putString(
            "StockDraftTransferToStoreConfirmationLogisticKey",
            deliveryCode
        )
        edit.apply()
    }

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        previousScannedBarcodes = Gson().fromJson(
            memory.getString("StockDraftTransferToStoreConfirmationBarcodeTable", ""),
            previousScannedBarcodes.javaClass
        ) ?: mutableListOf()

        if (previousScannedBarcodes.isNotEmpty()) {
            selectedStockDraftTransferHashcode =
                memory.getInt("StockDraftTransferToStoreConfirmationDriverName", 0)

            deliveryCode =
                memory.getString("StockDraftTransferToStoreConfirmationLogisticKey", "0") ?: "0"

            Log.e("memory", deliveryCode)

            popupState.showPopupWith2Button(
                message = "شما حواله هایی دارید که اسکن کردید ولی نهایی نشده است، ادامه اسکن قبل را ادامه میدهید؟",
                onCancelClick = {
                    loading = true
                    barcode.scannedBarcodes.clear()
                    uiList.clear()
                    saveToMemory()
                    scanningMode = false
                    loading = false
                },
                onOkClick = {
                    getDraftIDs()
                    barcode.scannedBarcodes.clear()
                    barcode.scannedBarcodes.addAll(previousScannedBarcodes)
                    barcode.scannedBarcodes =
                        barcode.scannedBarcodes.distinct().toMutableStateList()
                    previousScannedBarcodes.clear()
                }
            )
        }
    }

    fun back() {
        if (isCameraOn) {
            isCameraOn = false
        } else if (scanningMode) {
            scanningMode = false
            if (barcode.isEnabled) {
                barcode.disable()
            }
            getLogisticsDetails()
        }
    }

    fun clear() {
        stockDrafts.clear()
        barcode.scannedBarcodes.clear()
        barcode.barcode = ""
        saveToMemory()
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun onScanButtonClick() {
        isCameraOn = true
    }
}