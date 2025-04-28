package com.jeanwest.reader.features.cardex.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.models.Cardex
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * this file holds Cardex feature viewmodel
 * and manage screen changes, button actions,
 * api call and barcode scanner of Cardex screens.
 */
@HiltViewModel
class CardexViewModel @Inject constructor(
    var state: SnackbarHostState,
    var memory: SharedPreference,
    var api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var title: String = "کاردکس"
    var loading by mutableStateOf(false)
    var uiListCardex = mutableStateListOf<Cardex>()
    var scanningMode by mutableStateOf(true)
    var scannedBarcode by mutableStateOf("")
    var productDetails by mutableStateOf(Product(""))

    var barcode: Barcode

    init {

        exceptionHandler()
        barcode = Barcode(context) {
            getProductDetails(it)
        }
    }

    fun onItemClick(cardex: Cardex) {
        val clipboard: ClipboardManager =
            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("stockDraftId", cardex.deliveryID)
        clipboard.setPrimaryClip(clip)
        showLog("شماره حواله کپی شد.", state)
    }

    fun onPauseActivity() {

        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
        loading = false
    }

    fun onResumeActivity() {
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
        state.currentSnackbarData?.dismiss()
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    fun getProductDetails(barcode: String) {
        loading = true
        api.getItemDetails(mutableListOf(), mutableListOf(barcode), { _, product, _, _ ->
            if (product.isNotEmpty()) {
                productDetails = product[0]
                api.cardex(memory.user.warehouseCode, product[0].primaryKey, {
                    scannedBarcode = barcode
                    uiListCardex.clear()
                    uiListCardex.addAll(it)
                    scanningMode = false
                    loading = false
                }, {
                    loading = false
                    showLog("مشکلی در دریافت اطلاعات کاردکس بوجود امده است.", state)
                })
            } else {
                loading = false
            }
        }, {
            loading = false
            showLog("مشکلی در دریافت اطلاعات کالا به وجود امده است.", state)
        })
    }
}
