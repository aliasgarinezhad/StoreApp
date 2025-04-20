package com.jeanwest.reader.features.shelf.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for the Shelf Enter Store screen.
 *
 * This ViewModel manages the UI state and logic for the screen where users enter a store shelf,
 * scan products, and add them to the shelf.
 *
 * @property state The state of the Snackbar host for displaying messages to the user.
 * @property memory Shared preferences for storing data.
 * @property api The API interface for interacting with the backend.
 * @property context The application context.
 */
@HiltViewModel
class ShelfEnterStoreViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    val popupState = NotificationPopupHost()
    var loading by mutableStateOf(false)
        private set
    private val barcode: Barcode
    var uiState by mutableIntStateOf(0)
    var productBarcode by mutableStateOf("")
    var shelfCode by mutableStateOf("")
    private var shefId by mutableIntStateOf(0)
    val uiListProduct = mutableStateListOf<Product>()
    val skuList = mutableListOf<String>()
    val barcodes = mutableListOf<String>()

    init {
        barcode = Barcode(context) {
            if (uiState == 0) {
                getShelProducts(it)
            } else if (uiState == 1) {
                if (barcodes.contains(it)) {
                    showLog("این کالا قبلا اسکن شده", state)
                } else {
                    getProductDetails(it)
                    barcodes.add(it)
                }
            }
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

    fun getShelProducts(shelfCode: String) {
        loading = true
        api.getStoreShelfProducts(shelfCode, {
            shefId = it.depShelfId
            loading = false
            uiState = 1
        }, {
            if (it) {
                loading = false
            } else {
                loading = false
                showLog("قفسه وارد شده نامعتبر است.", state)
            }
        })
    }

    fun getProductDetails(barcode: String) {
        loading = true
        api.getItemDetails(
            mutableListOf(),
            mutableListOf(barcode),
            { _, products, _, invalidBarcodes ->
                if (invalidBarcodes.length() == 0) {
                    val product = products[0]
                    uiListProduct.add(product)
                    loading = false
                } else {
                    loading = false
                    showLog("مشخصات کالا یافت نشد", state)
                }
            },
            {
                loading = false
            })
    }

    fun addProductToShelf() {
        loading = true
        calculateSkus()
        api.addSkuToShelfInStore(skuList, shefId, {
            loading = false
            uiListProduct.clear()
            barcodes.clear()
            skuList.clear()
            uiState = 0
            showLog("کالاهای موردنظر با موفقیت به قفسه اضافه شد", state)
        }, {
            loading = false
            return@addSkuToShelfInStore
        })
    }

    fun clear(item: Product) {
        uiListProduct.remove(item)
        barcodes.remove(item.KBarCode)
        barcodes.remove(item.scannedBarcode)
        calculateSkus()
    }

    private fun calculateSkus() {
        skuList.clear()
        for (i in 0 until uiListProduct.size) {
            val skuCode = uiListProduct[i].productCode + "-" + uiListProduct[i].color
            skuList.add(skuCode)
        }
    }

}