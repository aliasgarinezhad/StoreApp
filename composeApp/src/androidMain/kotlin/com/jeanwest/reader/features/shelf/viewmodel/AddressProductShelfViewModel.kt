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
import com.jeanwest.reader.models.StoreShelf
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AddressProductShelfViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    var loading by mutableStateOf(false)
        private set
    private val barcode: Barcode
    var uiState by mutableIntStateOf(0)
        private set
    var productBarcode by mutableStateOf("")
    var uiList = mutableStateListOf<StoreShelf>()

    init {
        barcode = Barcode(context) {
            productBarcode = it
            getProductShelfs()
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

    private fun getProductShelfs() {
        loading = true
        Log.e("حroduct barcode", productBarcode)
        api.getItemDetails(
            mutableListOf(),
            mutableListOf(productBarcode),
            { _, products, _, invalidBarcodes ->
                if (invalidBarcodes.length() == 0) {
                    val product = products[0]
                    val skuCode = product.productCode + "-" + product.color
                    api.getProductShelfAddressStore(skuCode, {
                        uiState = 1
                        uiList.clear()
                        uiList.addAll(it)
                        loading = false
                    }, {
                        if (it) {
                            loading = false
                        } else {
                            loading = false
                            showLog("کالای وارد شده در قفسه ای وجود ندارد", state)
                        }
                    })
                } else {
                    loading = false
                    showLog("مشخصات کالا یافت نشد", state)
                }
            },
            {
                loading = false
            })

    }
}