package com.jeanwest.reader.features.shelf.inventory.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.showLog
import com.jeanwest.reader.features.shelf.inventory.view.NavigationEvent
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShelfInventoryViewModel @Inject constructor(
    private val repositoryImpl: RepositoryImpl,
    val state: SnackbarHostState,
    @ApplicationContext val context: Context,
    val memory: SharedPreference,
    val popupState: NotificationPopupHost,
) : ViewModel() {

    var shelfCode by mutableStateOf("")
    val uiList = mutableStateListOf<ShelfItem>()
    var loading by mutableStateOf(false)
        private set

    var numberOfItemsInShelf by mutableIntStateOf(0)
    var sumOfNotFound by mutableIntStateOf(0)
    var navigationEvent by mutableStateOf<NavigationEvent>(NavigationEvent.BackToEnterShelfNumberEvent)

    private var shelfEPCs = mutableListOf<String>()

    val rfid = RFID(context, state) {
        scanTrigger()
    }
    val barcode = Barcode(context) {
        shelfCode = it
        getShelfDetails()
    }

    fun scanTrigger() {
        if (navigationEvent == NavigationEvent.GoToShelfInventoryEvent) {
            if (rfid.scanning) {
                rfid.stopScanning()
                calculateShortages()
            } else {
                if (shelfEPCs.isNotEmpty()) {
                    rfid.startBulkScan(justFindInputEPCs = true, inputEPCs = shelfEPCs)
                } else {
                    showLog(data = "قفسه خالی است.", state = state)
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
    }

    private fun calculateShortages() {
        loading = true
        CoroutineScope(Dispatchers.Default).launch {
            uiList.forEach { shelfItem ->
                shelfItem.epcs.forEach { epc ->
                    if (rfid.epcs.contains(epc)) {
                        shelfItem.product.scannedEPCs.add(epc)
                    }
                }
            }
            sumOfNotFound = shelfEPCs.size - rfid.epcs.size
            loading = false
        }
    }

    fun getShelfDetails() {
        loading = true
        repositoryImpl.api.shelfContent1(
            warehouseCode = memory.user.warehouseCode,
            shelfNumber = shelfCode,
            onSuccess = { shelfItems ->
                checkShelfIsReadyForInventory(shelfItems)
            },
            onError = {
                showLog(state = state, data = "مشخصات قفسه یافت نشد.")
                loading = false
            }
        )
    }

    fun onScanFinished() {

        loading = true
        repositoryImpl.api.shelfInventoryReport(
            warehouseCode = memory.user.warehouseCode,
            shelfNumber = shelfCode,
            status = sumOfNotFound == 0,
            products = uiList,
            onSuccess = {
                showLog(data = "گزارش با موفقیت ثبت شد.", state = state)
                backToScanShelfPage()
                loading = false
            },
            onError = {
                showLog(data = "مشکلی در ثبت گزارش به وجود آمده است.", state = state)
                loading = false
            }
        )
    }

    fun backToScanShelfPage() {
        viewModelScope.launch {
            rfid.epcs.clear()
            shelfEPCs.clear()
            uiList.clear()
            numberOfItemsInShelf = 0
            sumOfNotFound = 0
            withContext(Dispatchers.Main) {
                navigationEvent = NavigationEvent.BackToEnterShelfNumberEvent
            }
        }
    }

    private fun checkShelfIsReadyForInventory(shelfItems: List<ShelfItem>) {
        loading = false
        CoroutineScope(Dispatchers.Default).launch {
            if (shelfItems.any { it.qtyInShelf != it.epcs.size }) {
                shelfCode = ""
                showLog(data = "قفسه مورد نظر شرایط انبارگردانی را ندارد.", state = state)
            } else if (shelfItems.isEmpty()) {
                shelfCode = ""
                showLog(data = "قفسه مورد نظر موجودی ندارد.", state = state)
            } else {
                shelfItems.forEach {
                    shelfEPCs.addAll(it.epcs)
                }
                uiList.clear()
                uiList.addAll(shelfItems)
                numberOfItemsInShelf = shelfEPCs.size
                sumOfNotFound = shelfEPCs.size - rfid.epcs.size
                navigationEvent = NavigationEvent.GoToShelfInventoryEvent
                checkBarcodeScannerStatus()
            }
            loading = false
        }
    }

    private fun checkBarcodeScannerStatus() {
        if (navigationEvent == NavigationEvent.BackToEnterShelfNumberEvent) {
            barcode.connectWithContext()
            barcode.enable()
        } else {
            barcode.disable()
        }
    }
}