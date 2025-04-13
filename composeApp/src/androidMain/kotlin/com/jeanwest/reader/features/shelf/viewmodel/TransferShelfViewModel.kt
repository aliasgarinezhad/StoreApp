package com.jeanwest.reader.features.shelf.viewmodel

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.RepositoryImpl
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransferShelfViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val memory: SharedPreference,
    val api: API,
    val repository: RepositoryImpl,
    val state: SnackbarHostState,
    val popupHost: NotificationPopupHost,
) : ViewModel() {

    // Current screen values
    var currentScreen = "ScanShelfToExit"
    var screen by mutableStateOf("ScanShelfToExit")
        private set

    // UI lists and state holders
    val uiList = mutableStateListOf<ShelfItem>()
    val scanAndCompareProductsUiList = mutableStateListOf<ShelfItem>()

    var shelfToExitItems by mutableStateOf("")
    var shelfToEnterItems by mutableStateOf("")
    private var isSecondScanningPage by mutableStateOf(false)
    var loading by mutableStateOf(false)

    private var barcode: Barcode
    var rfid: RFID

    init {
        setupExceptionHandler()
        // Initialize barcode scanning with a callback.
        barcode = Barcode(context) { scannedText ->
            if (scannedText.trim().uppercase(Locale.getDefault()).startsWith("SH")) {
                when (screen) {
                    "ScanShelfToExit" -> {
                        shelfToExitItems = scannedText
                        onFirstTextFieldImeAction()
                    }

                    "ScanShelfToEnter" -> {
                        shelfToEnterItems = scannedText
                        onSecondTextFieldImeAction()
                    }
                }
            } else {
                showLog("قفسه معتبر نیست", state)
            }
        }

        // Initialize RFID scanning with its callback.
        rfid = RFID(context, state) { scanTrigger() }
    }

    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()!!)
        )
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.disconnectFromContext()
        barcode.enable()
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()
        setBarcodeScan()
    }

    fun changeScreen(newScreen: String) {
        screen = newScreen
    }

    fun onScreenChanged() {
        currentScreen = screen
        setBarcodeScan()
    }

    fun onFirstTextFieldImeAction() {
        if (shelfToExitItems.isNotEmpty() &&
            shelfToExitItems.uppercase(Locale.getDefault()).trim().startsWith("SH")
        ) {
            getShelfDetails()
        } else {
            showLog("قفسه وارد شده معتبر نیست", state)
            shelfToExitItems = ""
        }
    }

    private fun getShelfDetails() {

        loading = true
        repository.api.shelfContent1(
            warehouseCode = memory.user.warehouseCode,
            shelfNumber = shelfToExitItems,
            onSuccess = { shelfItems ->
                for (elements in shelfItems) {
                    if (elements.qtyInShelf != elements.epcs.size) {
                        showLog(
                            "کالاهای این قفسه با rfid وارد نشده اند و امکان انتقال با rfid را ندارند",
                            state
                        )
                        loading = false
                        shelfToExitItems = ""
                        return@shelfContent1
                    }
                }
                uiList.clear()
                uiList.addAll(shelfItems)
                changeScreen("SelectProductsScreen")
                isSecondScanningPage = true
                loading = false
            },
            onError = {
                shelfToExitItems = ""
                loading = false
            }
        )
    }

    fun onSecondTextFieldImeAction() {
        changeScreen("ScanProductsScreen")
    }

    fun transferShelf() {
        loading = true
        api.shelfExitWithEpcs(
            currentWareHouseId = memory.user.warehouseCode,
            shelfCode = shelfToExitItems,
            shelfEnterCode = shelfToEnterItems,
            products = scanAndCompareProductsUiList,
            onSuccess = {
                popupHost.showPopupWithAButton(
                    message = "جنس ها با موفقیت به قفسه $shelfToEnterItems انتقال داده شدند. ",
                    onDismiss = { clearAll() },
                    onDoneButtonClick = { clearAll() },
                )
                loading = false
            },
            onError = {
                loading = false
            }
        )
    }

    private fun clearAll() {
        shelfToExitItems = ""
        shelfToEnterItems = ""
        uiList.clear()
        scanAndCompareProductsUiList.clear()
        changeScreen("ScanOrTypeNumberScreen")
    }

    private fun setBarcodeScan() {
        if (screen == "ScanShelfToExit" || screen == "ScanShelfToEnter") barcode.enable() else barcode.disable()
    }


    fun scanTrigger() {
        if (screen == "ScanProductsScreen") {
            if (rfid.scanning) {
                rfid.stopScanning()
                updateShelfItemsWithScannedEpcs(uiList, rfid.epcs)
            } else {
                rfid.startBulkScan(
                    justFindInputEPCs = true,
                    inputEPCs = scanAndCompareProductsUiList.flatMap { it.epcs }
                )
            }
        }
    }

    // Function to update all shelf items using a scanned EPC list.
    private fun updateShelfItemsWithScannedEpcs(
        shelfItems: List<ShelfItem>,
        scannedEpcList: List<String>,
    ) {
        shelfItems.forEach { shelfItem ->
            shelfItem.updateScannedEpcs(scannedEpcList)
        }
    }

    // Extension function on ShelfItem to update its product's scanned EPCs.
    private fun ShelfItem.updateScannedEpcs(scannedEpcList: List<String>) {
        // Find EPCs that are present in both lists.
        val matchingEpcs = epcs.intersect(scannedEpcList.toSet())
        matchingEpcs.forEach { epc ->
            // Add the EPC to scannedEPCs if not already added.
            if (epc !in product.scannedEPCs) {
                product.scannedEPCs.add(epc)
            }
        }
    }
}