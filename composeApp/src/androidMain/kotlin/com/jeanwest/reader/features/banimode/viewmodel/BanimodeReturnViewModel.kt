package com.jeanwest.reader.features.banimode.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.models.BaniReturn
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.view.SnackBarActions
import com.jeanwest.reader.view.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for handling Banimode returns.
 *
 * This ViewModel manages the UI state and interactions for the Banimode return process,
 * including fetching return data, handling user input, and confirming returns.  It utilizes
 * dependency injection via Hilt for accessing necessary components.
 *
 * @property state The state of the SnackbarHost for displaying messages to the user.
 * @property memory Access to shared preferences for user information and settings.
 * @property api The API service for communicating with the backend.
 * @property context The application context.
 */
@HiltViewModel
class BanimodeReturnViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext
    val context: Context,
) : ViewModel() {
    var back: () -> Unit = {}
    var uiList = mutableStateListOf<BaniReturn>()
        private set
    var openAddDialog by mutableStateOf(false)
    var loading by mutableStateOf(false)
        private set
    val barcode: Barcode
    var stockDraftId by mutableStateOf("")

    init {
        barcode = Barcode(context) {
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
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    fun getUiList() {
        loading = true
        api.getBaniReturnList({
            loading = false
            uiList.clear()
            uiList.addAll(it)
        }, {
            loading = false
        })
    }

    fun finalConfirm(id: String) {
        loading = true
        api.finalBanimodeReturn(id.toInt(),
            memory.user.warehouseCode, {
                showLog("سفارش با موفقیت نهایی شد", state, action = SnackBarActions.SUCCESS)
                getUiList()
            }, {
                loading = false
            })
    }
}