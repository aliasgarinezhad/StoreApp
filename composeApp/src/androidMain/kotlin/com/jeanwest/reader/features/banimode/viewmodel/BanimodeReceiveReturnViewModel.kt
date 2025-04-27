package com.jeanwest.reader.features.banimode.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for managing the "Bani Mode Receive Return" process.
 *
 * This ViewModel handles the logic for receiving and returning items in "Bani Mode",
 * including scanning barcodes, managing item lists, calculating discrepancies, and
 * interacting with the API.
 *
 * @property state The state for managing snackbar messages.
 * @property memory SharedPreference for persisting data.
 * @property api The API interface for making network requests.
 * @property context The application context.
 */
@HiltViewModel
class BanimodeReceiveReturnViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    var back: () -> Unit = {}
    private var itemTable = mutableListOf<Product>()
    var loading by mutableStateOf(false)
        private set
    var step by mutableIntStateOf(0)
    var stockDraftId by mutableStateOf("")
    val barcode: Barcode
    var shortageNumber by mutableIntStateOf(0)
    var additionalNumber by mutableIntStateOf(0)
    var validatedNumber by mutableIntStateOf(0)
    var scanFilter by mutableStateOf("کسری")
        private set
    var productConflicts = mutableStateListOf<Product>()
    var filteredUiList = mutableStateListOf<Product>()
    private var stockDraft = StockDraft()
    var popupState = NotificationPopupHost()
    private var previousUsername by mutableStateOf(0)

    init {
        barcode = Barcode(context) {
            when (step) {
                0 -> {
                    if (it.isDigitsOnly() && it != "") {
                        stockDraftId = it
                        getStockDraftDetails()
                    } else {
                        showLog("حواله وارد شده نامعتبر است.", state)
                    }
                }
                1 -> {
                    getAdditionalItemData(it)
                }
            }
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.enable()
        barcode.disconnectFromContext()
        saveToMemory()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    fun getStockDraftDetails() {
        loading = true
        api.getBaniReceivrReturnList(stockDraftId.toLong(), {
            stockDraft = it
            itemTable.addAll(it.items.values)
            calculateConflicts()
            step = 1
            loading = false
        }, {
            stockDraftId = ""
            loading = false
        })
    }

    private fun calculateConflicts() {
        shortageNumber = 0
        additionalNumber = 0
        validatedNumber = 0
        for (elements in itemTable) {
            validatedNumber += elements.scannedBarcodeNumber
            if (elements.conflictType == "اضافی") {
                additionalNumber += elements.conflictNumber
            } else if (elements.conflictType == "کسری") {
                shortageNumber += elements.conflictNumber
            }
        }
        filterUiList()
    }

    private fun getAdditionalItemData(barcode: String) {
        loading = true
        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, products, _, invalidBarcodes ->
                if (invalidBarcodes.length() != 0) {
                    showLog("بارکد وارد شده نامعتبر است", state)
                    loading = false
                } else {
                    for (element in itemTable) {
                        if (products[0].primaryKey == element.primaryKey) {
                            element.scannedBarcode = barcode
                            element.scannedBarcodeNumber++
                            calculateConflicts()
                            filteredUiList
                            loading = false
                            return@getItemDetails
                        }
                    }
                    products[0].scannedBarcodeNumber++
                    itemTable.add(products[0].copy())
                    loading = false
                    calculateConflicts()
                    filteredUiList
                    return@getItemDetails
                }
            },
            {
                loading = false
            }
        )
    }

    fun changeFilterValue(filterValue: String) {
        scanFilter = filterValue
        filterUiList()
    }

    private fun filterUiList() {
        filteredUiList.clear()
        filteredUiList.addAll(itemTable.filter {
            it.conflictType == scanFilter
        })
    }

    fun finalizeBaniStocks() {
        loading = true
        val sendedList = itemTable.filter {
            it.scannedNumber > 0
        }
        if (sendedList.isEmpty()) {
            showLog("هیچ کالایی برای ارسال اسکن نکرده اید", state)
            loading = false
        } else {
            api.baniReceiveReturnFinal(stockDraft.number.toString(), sendedList, memory.user.warehouseCode, {
                showLog("کالاهای موردنظر با موفقیت ثبت شدند", state, action = SnackBarActions.SUCCESS)
                itemTable.clear()
                filteredUiList.clear()
                step = 0
                loading = false
            }, {
                loading = false
            })
        }
    }

    fun clearItem(product: Product) {
        if (product.KBarCode in stockDraft.items.keys) {
            val index = filteredUiList.indexOf(product)
            filteredUiList[index].scannedBarcode = ""
            filteredUiList[index].scannedBarcodeNumber = 0
            calculateConflicts()
        } else {
            itemTable.remove(product)
            filteredUiList.remove(product)
            calculateConflicts()
        }
    }

    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = memory.edit()

        edit.putString(
            "BanimodeReceiveReturnViewModelProducts",
            Gson().toJson(stockDraft).toString()
        )

        edit.putInt(
            "BanimodeReceiveReturnPreviousUser",
            this.memory.user.username
        )
        edit.apply()
    }

    fun loadMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(context)

        val type = object : TypeToken<StockDraft>() {}.type

        stockDraft = Gson().fromJson(
            memory.getString("BanimodeReceiveReturnViewModelProducts", ""),
            type
        ) ?: StockDraft()

        try {
            previousUsername = memory.getInt("BanimodeReceiveReturnPreviousUser", 0)
        } catch (_:Exception) {

        }

    }

    fun checkLoadMemoryData() {
        loading = true
        var isValidScannedCount = false
        stockDraft.items.values.forEach {
            if (it.scannedBarcodeNumber > 0) {
                isValidScannedCount = true
                return@forEach
            }
        }
        loading = false
        if (previousUsername == memory.user.username && isValidScannedCount) {
            popupState.showPopupWith2Button(
                message = "شما در حال اسکن حواله ${stockDraft.number}بودید، ادامه میدهید یا حواله جدید اسکن میکنید؟ ",
                okButtonTitle = "ادامه قبلی",
                cancelButtonTitle = "شروع جدید",
                onOkClick = {
                    step = 1
                    itemTable.addAll(stockDraft.items.values)
                    calculateConflicts()
                },
                onCancelClick = {
                    stockDraft = StockDraft()
                    saveToMemory()
                }
            )
        } else {
            stockDraft = StockDraft()
            saveToMemory()
        }
    }

}