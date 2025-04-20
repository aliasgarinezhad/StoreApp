package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.res.stringResource
import com.jeanwest.reader.R
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.data.local.SharedPreference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This activity is responsible for searching and displaying the shelf address of an item based on its barcode.
 *
 *  Key functionalities:
 *  - **Barcode Scanning:** Integrates with a barcode scanner to capture item barcodes.
 *  - **Search and Display:** Queries an API to retrieve shelf addresses for a given barcode and displays them in a list.
 *  - **Scanning Mode:** Operates in two modes:
 *     - *Normal Mode:*  Allows manual barcode input or single-scan search.
 *     - *Scanning Mode:*  Continuously scans and searches for barcodes until explicitly exited.
 *  - **Clear Data:**  Clears the displayed list of shelf addresses.
 *  - **Back Navigation:** Navigates back to the previous screen or exits the scanning mode.
 *  - **Exception Handling:** Implements a custom exception handler to manage uncaught exceptions.
 *
 *  **Dependencies:**
 *  - `Barcode`:  Manages barcode scanning functionality.
 *  - `SnackbarHostState`:  Controls the display of snackbar messages.
 *  - `SharedPreference`: Provides access to shared preferences for storing user data.
 *  - `API`:  Handles communication with the backend API for retrieving shelf address information.
 *  - `ShelfBarcodeAddress`: Data class representing the shelf address and quantity of an item.
 *
 *  **UI Composition:**
 *  Uses Jetpack Compose to build the user interface, with a dedicated composable function (`Page */
@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ShelfAddress : ComponentActivity() {

    lateinit var barcode: Barcode
    var loading by mutableStateOf(false)
    var uiList = mutableStateListOf<ShelfBarcodeAddress>()

    @Inject
    lateinit var state: SnackbarHostState
    private var itemBarcode by mutableStateOf("")
    var scanningMode by mutableStateOf(false)

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        exceptionHandler()
        setContent {
            com.jeanwest.reader.features.shelf.viewmodel.Page(
                scanningMode,
                { back() },
                stringResource(id = R.string.shelf_address),
                state,
                loading,
                uiList,
                itemBarcode,
                {
                    itemBarcode = it
                },
                {
                    searchInShelfs(itemBarcode)
                },
            )
        }
    }

    private fun init() {


        barcode = Barcode(this) {
            if (scanningMode) {
                clearData()
                searchInShelfs(it)
            } else {
                itemBarcode = it
                searchInShelfs(it)
            }
        }
    }

    private fun clearData() {
        uiList.clear()
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
        if (scanningMode) {
            barcode.disable()
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun searchInShelfs(barcode: String) {
        loading = true
        api.shelfBarcodeAddress(memory.user.warehouseCode,barcode, {
            uiList.clear()
            uiList.addAll(it)
            uiList = uiList.sortedBy { uiItem ->
                uiItem.qty == 0
            }.toMutableStateList()
            scanningMode = true
            loading = false
        }, {
            loading = false
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 4) {
                back()
            }
        }
        return true
    }

    fun scanTrigger() {
        barcode.startBarcodeScan()
    }

    private fun back() {
        if (scanningMode) {
            uiList.clear()
            itemBarcode = ""
            scanningMode = false
            if (!barcode.isEnabled) {
                barcode.enable()
            }
        } else {
            finish()
        }
    }
}