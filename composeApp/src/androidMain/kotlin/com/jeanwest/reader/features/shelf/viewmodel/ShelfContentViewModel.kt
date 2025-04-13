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
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.SnackBarActions
import com.jeanwest.reader.features.shared.showLog
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class ShelfContentViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {

    private var openEditShelfDialog by mutableStateOf(false)
    var clearShelfDialog by mutableStateOf(false)
    var editShelfMode by mutableStateOf(false)
    var editShelfCount by mutableIntStateOf(0)
    var cageNumber by mutableStateOf("")
    var loading by mutableStateOf(false)
    var uiListProduct = mutableStateListOf<Product>()
    var uiListCarton = mutableStateListOf<Carton>()
    var barcodes = mutableListOf<String>()
    private var currentWarehouse by Delegates.notNull<Int>()
    var barcode: Barcode
    private var clearProductsList = mutableMapOf<String, Int>()
    private var editProductsList = mutableMapOf<String, Int>()
    var popupState = NotificationPopupHost()
    var pageState = mutableIntStateOf(0)

    init {
        currentWarehouse = memory.user.warehouseCode
        barcode = Barcode(context) {
            cageNumber = it
            getShelfProducts()
        }
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        if (barcode.isConnectedToContext) {
            barcode.disconnectFromContext()
        }
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isConnectedToContext) {
            barcode.connectWithContext()
        }
    }

    fun back() {
        uiListProduct.clear()
        cageNumber = ""
        barcodes.clear()
        pageState.intValue = 0
        if (!barcode.isEnabled) {
            barcode.enable()
        }
        clearShelfDialog = false
        openEditShelfDialog = false
        editShelfMode = false
        editShelfCount = 0
        clearProductsList.clear()
        editProductsList.clear()
    }

    private fun updateEditedProductCount() {
        editShelfCount = 0
        editProductsList.forEach {
            editShelfCount += it.value
        }
    }

    fun clear() {
        uiListProduct.clear()
        editShelfCount = 0
        cageNumber = ""
        barcodes.clear()
        editShelfMode = false
        editProductsList.clear()
    }

    fun getShelfProducts() {
        loading = true
        api.shelfContent(currentWarehouse, cageNumber, {
            if (it.isNotEmpty()) {
                uiListProduct.clear()
                uiListProduct.addAll(it)
                pageState.intValue = 1
                loading = false
            }else{
                api.shelfContentCarton(cageNumber,currentWarehouse.toString(), { cartons ->
                    Log.e("cartonsList: ", it.toList().toString())
                    uiListCarton.clear()
                    uiListCarton.addAll(cartons)
                    loading = false
                    pageState.intValue = 2
                }, {
                    pageState.intValue = 0
                    loading = false
                })
            }
        }, {
            loading = false
        })
    }

    fun clearShelf() {
        loading = true
        uiListProduct.forEach {
            clearProductsList[it.KBarCode] = it.shelfCount
        }
        api.shelfExit(currentWarehouse, cageNumber, null, clearProductsList, {
            showLog("تمام کالاهای قفسه خارج شدند", state, action = SnackBarActions.SUCCESS)
            getShelfProducts()
        }, {
            loading = false
        })
    }

    fun editShelf(product: Product) {
        if (editProductsList.contains(product.KBarCode)) {
            var previousScannedNumber = editProductsList.getValue(product.KBarCode)
            editProductsList[product.KBarCode] = previousScannedNumber.plus(1)
            updateEditedProductCount()
        } else {
            editProductsList[product.KBarCode] = 1
            updateEditedProductCount()
        }
    }

    fun editShelfConfirm() {
        loading = true
        api.shelfExit(currentWarehouse, cageNumber, null, editProductsList, {
            loading = false
            showLog(
                "کالاهای موردنظر با موفقیت ویرایش شدند",
                state,
                action = SnackBarActions.SUCCESS
            )
            clear()
            pageState.intValue = 0
        }, {
            loading = false
        })
    }

}