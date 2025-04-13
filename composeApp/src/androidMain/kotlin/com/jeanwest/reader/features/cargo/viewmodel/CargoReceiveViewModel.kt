package com.jeanwest.reader.features.cargo.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for managing the cargo receiving process.
 *
 * This ViewModel handles user interactions and data management related to receiving cargo,
 * including barcode scanning, API communication, and UI state updates.
 *
 * @property state The state of the SnackbarHost, used to display messages to the user.
 * @property memory An instance of SharedPreference for persistent data storage (if needed).
 * @property api The API interface for communication with the backend.
 * @property context The application context.
 */
@HiltViewModel
class CargoReceiveViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set
    private val barcode: Barcode
    var styleCode by mutableStateOf("استایل کد")
    var invoiceNumber by mutableStateOf("")
    var qty by mutableStateOf("")
    var styleCodes = mutableMapOf<String, String>()
    var packingID by mutableStateOf("")

    init {
        barcode = Barcode(context) {
            invoiceNumber = ""
            invoiceNumber = it
            getLocations()
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

    fun cargoReceive() {
        if (qty != "" && qty != "0" && invoiceNumber != "" && styleCode != "استایل کد") {
            loading = true
            api.finalCargoReceive(invoiceNumber, styleCode, Integer.valueOf(qty),
                Integer.valueOf(packingID),
                {
                    showLog("کالاهای مورد نظر با موفقیت دریافت شدند", state, action = SnackBarActions.SUCCESS)
                    loading = false
                }, {
                    loading = false
                })
        } else {
            showLog("تمامی مقادیر را پر کنید", state)
        }
    }

    fun getLocations() {
        loading = true
        this.styleCodes.clear()
        styleCode = ""
        packingID = ""
        api.getCargoReceiveLocations(invoiceNumber, {
            styleCode = "استایل کد"
            this.styleCodes = it.toMutableMap()
            loading = false
        }, {
            loading = false
        })
    }
}
