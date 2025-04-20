package com.jeanwest.reader.features.shelf.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for managing the display of shelf content in the store.
 *
 * This ViewModel handles barcode scanning, fetching shelf details from the API,
 * retrieving product information based on SKUs, and managing the UI state for displaying
 * products associated with a specific shelf (cage).
 *
 * @property state The state of the Snackbar host for displaying messages to the user.
 * @property memory Shared preferences for storing user-related data.  Currently unused in this class but kept for potential future use.
 * @property api The API interface for interacting with the backend service.
 * @property context The application context.
 */
@HiltViewModel
class ShelfContentStoreViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    var loading by mutableStateOf(false)
        private set
    private val barcode: Barcode
    var pageState by mutableIntStateOf(0)
    var cageNumber by mutableStateOf("")
    var uiList = mutableStateListOf<Product>()
    private var shelfDetails = mutableStateOf(StoreShelf())
    var popupState = NotificationPopupHost()

    init {
        barcode = Barcode(context) {
            cageNumber = it
            getShelfProducts()
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
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    fun getShelfProducts() {
        loading = true
        api.shelfContentStore(cageNumber, { shelf ->
            Log.e("Debug", "Shelf Details: $shelf")
            shelfDetails.value = shelf

            if (shelf.shelfSkus.isNotEmpty()) {
                getProductDetails(shelf.shelfSkus)
            } else {
                Log.e("Error", "No SKUs found for shelf")
                showLog("محتوای قفسه یافت نشد", state)
                loading = false
            }
        }, { error ->
            Log.e("API Error", "Failed to fetch shelf content: $error")
            showLog("محتوای قفسه یافت نشد", state)
            loading = false
        })
    }

    fun getProductDetails(skuList: List<String>) {
        loading = true
        for (sku in skuList) {
            val skuParts = sku.split("-")

            if (skuParts.size < 2) {
                Log.e("Error", "Invalid SKU format: $sku")
                continue
            }

            val styleCode = skuParts[0]
            val colorCode = skuParts[1]
            api.getProductsSimilarWithStyleAndWareCode(
                shelfDetails.value.wareId,
                styleCode,
                { products ->
                    // Filter products safely
                    val filteredProducts = products.filter { it.color == colorCode }

                    if (filteredProducts.isNotEmpty()) {
                        Log.e("Debug", "Product Added: ${filteredProducts[0]}")
                        uiList.add(filteredProducts[0])
                    } else {
                        Log.e("Error", "No matching product found for SKU: $sku")
                    }
                },
                {
                }
            )
        }
        pageState = 1
        loading = false
    }
}
