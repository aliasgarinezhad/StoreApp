package com.jeanwest.reader.features.shelf.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
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
class CreateShelfStoreViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    val api: API,
    @ApplicationContext val context: Context,
) : ViewModel() {
    var title: String = "ایجاد قفسه"
    var shelfTypeText by mutableStateOf("نوع قفسه")
    var back: () -> Unit = {}
    var uiList = mutableStateListOf<StoreShelf>()
        private set
    var openAddDialog by mutableStateOf(false)
    var shelfTypes = mutableMapOf<String, Int>()
    var shelfDes by mutableStateOf("")
    var loading by mutableStateOf(false)
        private set
    val barcode: Barcode
    var filterValue by mutableStateOf("")


    init {
        barcode = Barcode(context) {
        }
        getDepShels()
        getShelfTypes()
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

    private fun getShelfTypes() {
        api.getShelfType({
            shelfTypes.putAll(it)
        }, {
            showLog("مشکلی در دریافت اطلاعات نوع قفسه ها پیش آمده است", state)
        })
    }

    fun createShelf() {
        if (shelfTypeText == "نوع قفسه") {
            openAddDialog = false
            showLog("نوع قفسه را مشخص کنید", state)
        } else {
            openAddDialog = false
            loading = true
            api.createShelfStore(
                memory.user.calculatedLocationCode,
                memory.user.warehouseCode,
                shelfTypes[shelfTypeText] ?: 0,
                shelfDes,
                {
                    showLog("قفسه با شماره $it ایجاد شد. ", state)
                    printShelf(it)
                    getDepShels()
                },
                {
                    loading = false
                })
        }
    }

    private fun getDepShels() {
        loading = true
        api.getDepShelfs(
            memory.user.warehouseCode,
            {
                this.uiList.clear()
                this.uiList.addAll(it)
                loading = false
            }, {
                loading = false
            })
    }

    fun printShelf(shelfCode: String) {
        loading = true
        api.printShelfInStore(
            memory.user.calculatedLocationCode,
            shelfCode,
            {
                loading = false
                showLog("دستور چاپ با موفقیت ارسال شد", state)
            }, {
                loading = false
                showLog("مشکلی در پرینت شماره قفسه به وجود آمده است", state)
            })
    }

}