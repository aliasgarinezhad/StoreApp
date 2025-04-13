package com.jeanwest.reader.features.print.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraft
import com.jeanwest.reader.useCases.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class PrintPricePerProductViewModel @Inject constructor(
    val snackBarHostState: SnackbarHostState, // SnackBar state for displaying error or success messages
    private val api: API,                            // API for fetching product details
    private val localStoreDatabase: LocalStoreDatabase, // Local database for printing labels
    @ApplicationContext private val context: Context, // Application context for initializing dependencies
) : ViewModel() {

    // Barcode scanner utility
    var barcode = Barcode(context)
        private set

    // Screen title
    var title: String = "پرینت لیبل قیمت"
        private set

    // UI state variables
    var loading by mutableStateOf(false)                // Indicates whether a loading spinner is visible
    var stockDraftSpecification by mutableStateOf("")   // Specification details of the current stock draft
    var stockDraftId by mutableStateOf("")              // Identifier for the current stock draft
    var uiList = mutableStateListOf<Product>()                // List of products to display in the UI
    var products = mapOf<String, List<Product>>()
    val listState = LazyListState(0)         // State for managing the LazyColumn scroll position

    // Click listener for product items in the UI
    var onProductClick: (Product, Int) -> Unit = { _, _ -> }
        private set

    // Properties of the current stock draft
    private lateinit var stockDraftProperties: StockDraft

    /**
     * Initializes the ViewModel with the given stock draft ID.
     * Parses the stock draft data and loads product details.
     *
     * @param stockDraftId ID of the stock draft to initialize.
     */
    fun initialize(stockDraftId: String) {
        if (stockDraftId.isBlank()) {
            showSnackBar("کالایی جهت بررسی وجود ندارد")
            return
        }

        parseStockDraftProperties(stockDraftId)
        getInputProductDetails()
        setupProductClickListener()
    }

    /**
     * Parses the stock draft data from the provided JSON string.
     *
     * @param stockDraftId JSON string containing stock draft details.
     */
    private fun parseStockDraftProperties(stockDraftId: String) {
        stockDraftProperties = Gson().fromJson(stockDraftId, StockDraft::class.java).apply {
            this@PrintPricePerProductViewModel.stockDraftId = number.toString()
            this@PrintPricePerProductViewModel.stockDraftSpecification = specification
        }
    }

    /**
     * Handles actions when the activity resumes.
     * Disables the barcode scanner if enabled and dismisses any snackbar.
     */
    fun handleResume() {
        barcode.disableIfEnabled()
        snackBarHostState.currentSnackbarData?.dismiss()
    }

    /**
     * Handles actions when the activity pauses.
     * Enables the barcode scanner if disabled and disconnects it from the context.
     */
    fun handlePause() {
        barcode.enableIfDisabled()
        snackBarHostState.currentSnackbarData?.dismiss()
    }

    /**
     * Handles cleanup when the activity is destroyed.
     * Clears UI data and resets the loading state.
     */
    fun handleDestroy() {
        uiList.clear()
        loading = false
        stockDraftId = ""
    }

    /**
     * Fetches product details for the stock draft and updates the UI.
     */
    private fun getInputProductDetails() {
        loading = true
        api.getItemDetails(
            mutableListOf(),
            stockDraftProperties.barcodeTable.distinct().toMutableList(),
            { _, barcodes, _, invalidBarcodes ->
                Log.e("invalidBarcodes:", invalidBarcodes.toString())
                handleBarcodeDetails(barcodes, invalidBarcodes)
            },
            {
                loading = false
                showSnackBar("در دریافت جزییات حواله مشکلی پیش امده است")
            }
        )
    }

    /**
     * Processes barcode details and groups products for the UI.
     *
     * @param barcodes List of valid barcodes returned from the API.
     * @param invalidBarcodes Details of invalid barcodes, if any.
     */
    private fun handleBarcodeDetails(barcodes: List<Product>, invalidBarcodes: JSONArray) {
        if (invalidBarcodes.length() != 0) {
            loading = false
            showSnackBar("مشخصات برخی از بارکد ها یافت نشد")
            return
        }

        uiList.clear()

        // Count scanned barcodes for each product
        val barcodeCounts = stockDraftProperties.barcodeTable.groupingBy { it }.eachCount()
        barcodes.forEach { product ->
            product.scannedBarcodeNumber = barcodeCounts[product.scannedBarcode] ?: 0
        }

        products = barcodes.groupBy { it.productCode }.toMap()

        // Group barcodes by product code and process each group
        barcodes.groupBy { it.productCode }.forEach { (productCode, productList) ->
            val product = productList.first().copy()
            val price = product.salePrice

            if (productList.any { it.salePrice != price }) {
                uiList.clear()
                uiList.addAll(barcodes)
                barcodes.forEach { product1 ->
                    product1.scannedBarcodeNumber = barcodeCounts[product1.scannedBarcode] ?: 0
                }
                return@forEach
            }

            //product.KBarCode = productCode
            product.scannedBarcodeNumber = productList.sumOf { it.scannedBarcodeNumber }
            uiList.add(product)
        }
        loading = false

    }

    /**
     * Sets up the click listener for products in the UI.
     * Updates the product's printed state and sends a print request.
     */
    private fun setupProductClickListener() {
        onProductClick = { product, index  ->

            product.isPrinted = true
            val printListProducts = products[product.productCode]
            val printList = mutableListOf<String>()

            printListProducts?.sortedBy {
                it.size
            }?.forEach {
                repeat(it.scannedBarcodeNumber) { _ ->
                    printList.add(it.KBarCode)
                }
            }
            // Send the print request and update UI on success
            printPriceLabel(printList) {
                uiList[index] = uiList[index].copy(isPrinted = true) // Update after successful print
            }
        }
    }

    /**
     * Sends a print request for the specified product.
     *
     * @param printList Barcode list for print.
     */
    private fun printPriceLabel(printList: List<String>, onSuccess: () -> Unit) {
        loading = true
        var allPrintedSuccessfully = true
        for (element in printList) {
            localStoreDatabase.printPriceLabelWithBarcode(element, 1, {

            }, {
                allPrintedSuccessfully = false
                showSnackBar("مشکلی در دستور پرینت بوجود آمده است")
            })
        }
        if (allPrintedSuccessfully) {
            onSuccess() // Trigger the callback if successful
        }
        showLog("دستور پرینت با موفقیت ارسال شد", snackBarHostState, SnackBarActions.SUCCESS)
        loading = false
    }

    /**
     * Displays a snackBar message.
     *
     * @param message The message to display.
     */
    private fun showSnackBar(message: String) {
        showLog(message, snackBarHostState)
    }

    /**
     * Helper methods for managing the barcode scanner state.
     */
    private fun Barcode.disableIfEnabled() {
        if (isEnabled) disable()
        if (isConnectedToContext) disconnectFromContext()
    }

    private fun Barcode.enableIfDisabled() {
        if (!isEnabled) enable()
        if (isConnectedToContext) disconnectFromContext()
    }
}