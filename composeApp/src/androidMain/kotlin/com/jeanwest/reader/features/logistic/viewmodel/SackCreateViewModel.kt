package com.jeanwest.reader.features.logistic.viewmodel

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * this view model class responsible
 * for SackCreate feature screen processing,
 * barcode scanning, api calls, button actions, etc.
 */

@HiltViewModel
class SackCreateViewModel @Inject constructor(
    var state: SnackbarHostState,
    var memory: SharedPreference,
    val api: API,
    val popupState: NotificationPopupHost,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var barcode: Barcode
    var rf: RFID

    private var draftsMap = mutableStateMapOf<String, StockDraft>()
    val listState = LazyListState(0)
    val title: String = "ایجاد گونی"
    val groupPrintersList: Map<String, Int>

    var loading by mutableStateOf(false)
        private set
    var scanningMode by mutableStateOf(true)
        private set
    var stockDraftUiList = mutableStateListOf<StockDraft>()
        private set
    var creatingStockDraft by mutableStateOf(false)
        private set
    var stockDraftIDs = mutableListOf<String>()
        private set
    var stockDraftNumber by mutableStateOf("")
        private set
    var printer by mutableStateOf("")
        private set

    init {
        exceptionHandler()
        rf = RFID(context, state) {
            scanTrigger()
        }
        barcode = Barcode(context) {
            getStockDraftsDetails(it)
        }
        groupPrintersList = memory.erpData.printers.filter {
            it.key.contains("Group")
        }.toMap()
        printer = groupPrintersList.keys.toList()[0]
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    fun onPause() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    fun onResume() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
    }

    fun getStockDraftsDetails(stockDraft: String) {
        loading = true
        if (stockDraft.isDigitsOnly()) {
            if (stockDraft in stockDraftIDs) {
                showLog("حواله قبلا اسکن شده است.", state)
                loading = false
                errorBeep(state)
            } else {
                api.getStockDraftDetails(stockDraft, {
                    if (it.stateID == "9") {
                        showLog("حواله ابطال شده است", state)
                        stockDraftNumber = ""
                        loading = false
                        errorBeep(state)
                    } else if (it.stateID == "2") {
                        showLog("حواله نهایی شده است", state)
                        stockDraftNumber = ""
                        loading = false
                        errorBeep(state)
                    } else if (it.logisticKey != "null") {
                        showLog("حواله قبلا به راننده تحویل داده شده است", state)
                        stockDraftNumber = ""
                        loading = false
                        errorBeep(state)
                    } else {
                        stockDraftNumber = ""
                        stockDraftIDs.add(stockDraft)
                        stockDraftUiList.add(it)
                        loading = false
                        successBeep(state)
                    }
                }, {
                    stockDraftIDs.remove(stockDraft)
                    loading = false
                    errorBeep(state)
                })
            }
        } else {
            showLog("فرمت حواله ورودی اشتباه است", state)
            loading = false
            stockDraftNumber = ""
            errorBeep(state)
        }
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun clear() {
        stockDraftIDs.clear()
        stockDraftUiList.clear()
        draftsMap.clear()
        //saveToMemory()
    }

    fun clear(stockDraftID: String) {
        stockDraftIDs.remove(stockDraftID)
        stockDraftUiList.removeAll {
            it.number.toString() == stockDraftID
        }
        draftsMap.remove(stockDraftID)
        //saveToMemory()
    }

    fun createSack() {
        creatingStockDraft = true
        loading = true

        val stockDrafts = mutableListOf<Long>()
        stockDraftIDs.forEach {
            stockDrafts.add(it.toLong())
        }
        api.createSack(groupPrintersList[printer] ?: 0, stockDrafts, {
            loading = false
            creatingStockDraft = false
            clear()
            showLog("حواله های موردنظر با موفقیت ثبت شدند", state)
            popupState.showPopupWithAButton(message = " تعداد ${stockDraftUiList.size} حواله با موفقیت ایجاد گونی شد.")
        }, {
            loading = false
            creatingStockDraft = false
        })
    }

    fun onTextFieldValueChanged(value: String) {
        stockDraftNumber = value
    }

    fun showClearPopup() {

        popupState.showPopupWith2Button(
            message = "حواله های اسکن شده پاک شوند؟",
            onOkClick = { clear() },
        )
    }

    fun showPrintPopUp() {

        popupState.showPopupWitheadlineMediumButtonDropDownList(
            message = "لطفا پرینتر مورد نظر خود را مشخص کنید",
            onDoneClick = {
                printer = it
                createSack()
            },
            dropDownText = printer,
            dropDownList = groupPrintersList.keys.toList(),
        )
    }
}
