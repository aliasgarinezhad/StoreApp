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
 *  ViewModel for handling cargo registration related operations.
 *  This class manages UI state, interacts with the API for cargo registration and location retrieval,
 *  and handles barcode scanning functionality.
 *
 *  @property state [SnackbarHostState] for displaying snackbar messages to the user.
 *  @property memory [SharedPreference] for persisting data locally.  (While not directly used in the provided code, it suggests potential future use for caching or persisting registration data).
 *  @property api [API] interface for making network requests to the backend.
 *  @property context Application context for accessing resources and system services.
 */
@HiltViewModel
class CargoRegistrationViewModel @Inject constructor(
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

    fun cargoRegistration() {
        if (qty != "0" && qty != "" && invoiceNumber != "" && styleCode != "استایل کد") {
            loading = true
            api.finalCargoRegistration(invoiceNumber, styleCode, Integer.valueOf(qty), {
                showLog("کالاهای مورد نظر با موفقیت ثبت شد", state, action = SnackBarActions.SUCCESS)
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
        api.getCargoRegistrationLocations(invoiceNumber, {
            styleCode = "استایل کد"
            this.styleCodes = it.toMutableMap()
            loading = false
        }, {
            loading = false
        })
    }
}
