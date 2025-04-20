package com.jeanwest.reader.features.carton.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.CartonItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * this file holds SearchAnItemInCartons
 * feature viewmodel and manage screen changes,
 * button actions,
 * api call and barcode scanner of CartonsConfirmItems screens.
 */

@HiltViewModel
class SearchAnItemInCartonsViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    var title: String = "آدرس بارکد کارتن"
    var back: () -> Unit = {}
    var uiList = mutableStateListOf<CartonItem>()
        private set
    var skuNumber by mutableStateOf("")
    var scanningMode by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    val barcode: Barcode

    init {

        barcode = Barcode(context) {
            if (scanningMode) {
                clearData()
                searchInCartons(it)
            } else {
                skuNumber = it
                searchInCartons(it)
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
        if (scanningMode) {
            barcode.disable()
        }
    }

    private fun clearData() {
        uiList.clear()
    }

    fun searchInCartons(barcode: String) {
        loading = true
        api.cartonBarcodeAddress(
            memory.user.warehouseCode.toString(),
            barcode,
            {
                uiList.addAll(it)
                scanningMode = true
                loading = false
            },
            {
                skuNumber = ""
                showLog("کالا در کارتن یافت نشد", state)
                loading = false
            })
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    fun back() {
        if (scanningMode) {
            uiList.clear()
            skuNumber = ""
            scanningMode = false
            if (!barcode.isEnabled) {
                barcode.enable()
            }
        }
    }
}