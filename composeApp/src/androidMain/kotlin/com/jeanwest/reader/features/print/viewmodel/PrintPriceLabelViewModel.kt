package com.jeanwest.reader.features.print.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.print.view.PrintPricePerProduct
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for handling price label printing functionality.
 * Handles business logic and communication with data layers.
 */
@HiltViewModel
class PrintPriceLabelViewModel @Inject constructor(
    val state: SnackbarHostState,
    private val memory: SharedPreference,
    private val api: API,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // State variables
    var barcode: Barcode
    var rf: RFID
    var loading by mutableStateOf(false)
    var product by mutableStateOf<Product?>(null)
    var productBarcode by mutableStateOf("")
    var scanningMode by mutableStateOf(true)
    var uiList = mutableListOf<StockDraft>()
    private var stockDraftIDs = mutableListOf<Long>()
    private var draftsMap = mutableStateMapOf<Long, StockDraft>()
    private var popupState = NotificationPopupHost()
    private var userSelectedStoreWarehouse = 0

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    init {
        // Initialize Barcode with scanning callback
        barcode = Barcode(context) { barcodeResult ->
            handleBarcodeScan(barcodeResult)
        }

        // Initialize RFID with scanning trigger callback
        rf = RFID(context, state) {
            scanTrigger()
        }

        // Fetch user store warehouse code
        userSelectedStoreWarehouse = getUserStoreCode() ?: 0

        // Fetch draft IDs if not in local mode
        if (!memory.user.isLocalMode) {
            getDraftIDs()
        }
    }

    /**
     * Handles the barcode scan result.
     * @param barcodeResult The scanned barcode result.
     */
    private fun handleBarcodeScan(barcodeResult: String) {
        if (memory.user.isLocalMode) {
            localStoreDatabase.printPriceLabelWithBarcode(barcodeResult, 1, {
                showLog("دستور پرینت با موفقیت ارسال شد", state, SnackBarActions.SUCCESS)
                loading = false
            }, {
                showLog("مشکلی در پرینت قیمت بوحود آمده است", state, SnackBarActions.ERROR)
                loading = false
            })
        } else {
            productBarcode = barcodeResult
            getProductDetails()
        }
    }

    /**
     * Handles activity pause to clean up resources.
     */
    fun onPauseActivity() {
        barcode.disableIfEnabled()
        barcode.disconnectFromContextIfConnected()
        state.currentSnackbarData?.dismiss()
    }

    /**
     * Handles activity resume to reinitialize resources.
     */
    fun onResumeActivity() {
        barcode.connectWithContextIfNotConnected()
        state.currentSnackbarData?.dismiss()
    }

    /**
     * Updates the product barcode and filters the UI list based on input.
     */
    fun onTextValueChange(newValue: String) {
        productBarcode = newValue
        Log.e("newBarcode:", productBarcode)

        if (!memory.user.isLocalMode) {
            updateUIList(newValue)
        }
    }

    /**
     * Confirms the text field input and either prints or fetches product details.
     */
    fun onTextFieldConfirm() {
        if (memory.user.isLocalMode) {
            localStoreDatabase.printPriceLabelWithBarcode(productBarcode, 1, {}, {})
        } else {
            getProductDetails()
        }
    }

    /**
     * Handles UI list item click.
     */
    fun uiListOnClick(itemBarcode: String) {
        if (!memory.user.isLocalMode) {
            productBarcode = itemBarcode
            getProductDetails()
        }
    }

    /**
     * Fetches product details using the barcode.
     */
    private fun getProductDetails() {
        loading = true
        Log.e("productBarcode:", productBarcode)
        api.getItemDetails(
            mutableListOf(),
            mutableListOf(productBarcode),
            { _, products, _, invalidBarcodes ->
                Log.e("invalidBarcodes:", invalidBarcodes.toString())
                if (invalidBarcodes.length() > 0) {
                    handleInvalidBarcode()
                } else {
                    handleValidProductDetails(products)
                }
            },
            { loading = false }
        )
    }

    private fun handleInvalidBarcode() {
        Log.e("productBarcode:", productBarcode)
        api.getStockDraftDetails(productBarcode, { stockDraft ->
            scanningMode = false
            startPrintPricePerProductActivity(stockDraft)
            loading = false
        }, { loading = false })
    }

    private fun handleValidProductDetails(products: List<Product>) {
        if (products.size == 1) {
            product = products[0]
        }
        printPriceLabel()
    }

    /**
     * Starts the PrintPricePerProduct activity.
     */
    private fun startPrintPricePerProductActivity(stockDraft: StockDraft) {
        val intent = Intent(context, PrintPricePerProduct::class.java).apply {
            putExtra("StockDraftId", Gson().toJson(stockDraft))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Fetches draft IDs from the API.
     */
    private fun getDraftIDs() {
        loading = true
        api.stockDraftsHistory(null, null, null, null, null, null, perPage = "15", { drafts ->
            stockDraftIDs = drafts.map { it.stockDraftID.toLong() }.toMutableList()
            draftsMap.clear()
            getStockDraftsDetails()
        }, { loading = false })
    }

    /**
     * Fetches details for stock drafts using their IDs.
     * Populates the `draftsMap` and updates the UI list accordingly.
     */
    private fun getStockDraftsDetails() {
        loading = true
        val stockDraftID: Long? = stockDraftIDs.find { it !in draftsMap.keys }

        if (stockDraftID == null) {
            // If no new draft ID is found, update the UI list and stop loading
            uiList.clear()
            uiList.addAll(draftsMap.values)
            uiList = uiList.filter {
                it.destination == userSelectedStoreWarehouse && it.source == memory.user.warehouseCode
            }.sortedByDescending { it.date }.toMutableList()
            loading = false
            return
        }

        // Fetch details for the current draft ID
        api.getStockDraftDetails(stockDraftID.toString(), { stockDraft ->
            draftsMap[stockDraft.number] = stockDraft
            getStockDraftsDetails() // Recursively fetch the next draft
        }, {
            loading = false
        })
    }

    /**
     * Updates the filtered UI list based on the input value.
     */
    private fun updateUIList(newValue: String) {
        uiList = draftsMap.values.filter { stockDraft ->
            stockDraft.matchesFilters(
                newValue,
                userSelectedStoreWarehouse,
                memory.user.warehouseCode
            )
        }.sortedByDescending { it.date }.toMutableList()
    }

    /**
     * Prints the price label for the selected product.
     */
    private fun printPriceLabel() {
        loading = true
        localStoreDatabase.printPriceLabel(product?.primaryKey ?: 0, 1, {
            showLog("دستور پرینت با موفقیت ارسال شد", state, SnackBarActions.SUCCESS)
            loading = false
        }, {
            showLog("مشکلی در پرینت قیمت بوجود آمده است", state, SnackBarActions.ERROR)
            loading = false
        })
    }

    /**
     * Gets the user store code based on the current warehouse setup.
     */
    /**
     * Gets the user's store warehouse code based on the current warehouse configuration.
     * Displays error messages if the configuration is invalid.
     */
    private fun getUserStoreCode(): Int? {
        if (memory.user.currentWarehouseCodeIsDepo) {
            val source = memory.user.warehouseCode

            val sourceString: String = memory.erpData.warehousesIDsToTitles[source.toString()]
                ?: run {
                    popupState.showPopupWithAButton("لطفا انبار خود را درست انتخاب کنید. نوع انبار باید دپو باشد.")
                    return null
                }

            var destination = 0
            memory.erpData.departmentWarehouses[memory.user.calculatedLocationCode.toString()]?.forEach { warehouseCode ->
                val warehouseString: String = memory.erpData.warehousesIDsToTitles[warehouseCode]
                    ?: run {
                        popupState.showPopupWithAButton("کاربری شما به این فروشگاه دسترسی ندارد.")
                        return null
                    }

                if (warehouseCode == source.toString() || warehouseString.length >= sourceString.length) {
                    return@forEach
                }

                if (warehouseString == sourceString.substring(0, warehouseString.length)) {
                    destination = warehouseCode.toInt()
                }
            }

            if (destination == 0) {
                popupState.showPopupWithAButton("شما به انبار فروشگاه دسترسی ندارید.")
                return null
            }

            return destination
        }
        return null
    }

    /**
     * Converts a warehouse code to its corresponding text representation.
     * @param code The warehouse code to convert.
     * @return The text representation of the warehouse code or "نامشخص" if not found.
     */
    fun convertWarehouseCodeToText(code: String): String {
        return memory.erpData.warehousesIDsToTitles[code] ?: "نامشخص"
    }


    /**
     * Triggers barcode scanning.
     */
    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    /**
     * Helper function for Barcode to enable if not enabled.
     */
    private fun Barcode.disableIfEnabled() {
        if (!isEnabled) enable()
    }

    /**
     * Helper function for Barcode to disconnect if connected.
     */
    private fun Barcode.disconnectFromContextIfConnected() {
        if (isConnectedToContext) disconnectFromContext()
    }

    /**
     * Helper function for Barcode to connect if not connected.
     */
    private fun Barcode.connectWithContextIfNotConnected() {
        if (!isConnectedToContext) connectWithContext()
    }

    /**
     * Helper function for StockDraft to check filters.
     */
    private fun StockDraft.matchesFilters(barcode: String, destination: Int, source: Int): Boolean {
        return (barcode.isEmpty() || number.toString().startsWith(barcode)) &&
                this.destination == destination &&
                this.source == source
    }
}