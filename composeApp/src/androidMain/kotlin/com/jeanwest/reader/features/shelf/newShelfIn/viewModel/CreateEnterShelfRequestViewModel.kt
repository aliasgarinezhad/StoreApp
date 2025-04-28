package com.jeanwest.reader.features.shelf.newShelfIn.viewModel

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
import com.jeanwest.reader.features.shelf.newShelfIn.model.RequestType
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.errorBeep
import com.jeanwest.reader.useCases.successBeep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel for managing the creation of "Enter Shelf" requests.  This request is used to move
 * stock from a central warehouse to a store's shelves.
 *
 * This ViewModel handles:
 * - User interaction for creating requests.
 * - Scanning barcodes (product or carton codes) using a [Barcode] scanner.
 * - Scanning RFID tags (EPCs) for products (when the `newFeature` is enabled).
 * - Retrieving product and carton details from a [RepositoryImpl].
 * - Managing UI state (loading indicators, lists of products/cartons, etc.).
 * - Submitting the request to the [RepositoryImpl].
 * - Displaying success/error messages using a [SnackbarHostState] and a [NotificationPopupHost].
 * - Handling different request types (Product or Carton).
 */
@HiltViewModel
class CreateEnterShelfRequestViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val memory: SharedPreference,
    val api: API,
    val repository: RepositoryImpl,
    val state: SnackbarHostState,
    val popupHost: NotificationPopupHost,
) : ViewModel() {

    val requestTypes = RequestType.entries.map { it.toString() }
    val barcode = Barcode(context) {
        getBarcodeDetails(it)
    }
    val rf = RFID(
        context = context,
        state = state,
    ) {
        scanTrigger()
    }

    private val scannedBarcodesWithDetails = mutableMapOf<String, Product>()
    private val scannedEpcsWithDetails = mutableMapOf<String, Product>()
    private val uiListWithBarcodeMainMap = mutableMapOf<Long, Product>()

    var loading by mutableStateOf(false)
        private set
    var textFieldValue by mutableStateOf("")
        private set

    val productsUiList = mutableStateListOf<Product>()
    val cartonsUiList = mutableStateListOf<Carton>()
    var newFeature by mutableStateOf(false)
    var requestType by mutableStateOf(RequestType.Carton)

    fun onTextFieldValueChange(value: String) {
        textFieldValue = value
    }

    fun scanTrigger() {
        if (requestType == RequestType.Product && newFeature) {
            if (rf.scanning) {
                rf.stopScanning()
                getEPCsDetails()
            } else {
                rf.startBulkScan()
            }
        }
    }

    fun onCreateShelfInRequestButtonClick() {

        loading = true

        if (memory.user.warehouseCode == 44) {
            showLog(
                data = "انبار جاری شما انبار مرکزی است. لطفا انبار جاری خود را به درستی انتخاب کنید.",
                state = state
            )
            loading = false
            return
        } else if (productsUiList.isNotEmpty() && cartonsUiList.isNotEmpty()) {
            showLog(data = "لیست باید فقط شامل کارتن یا فقط شامل کالا باشد.", state = state)
            loading = false
            return
        } else if (productsUiList.isNotEmpty()) {
            if (productsUiList.any { it.wareHouseNumber < it.scannedNumber }) {
                showLog(data = "موجودی برخی کالاها کافی نمیباشد.", state = state)
                loading = false
                return
            } else {
                repository.createShelfInRequest(
                    products = productsUiList,
                    onSuccess = { stockDraftNumber, requestNumber ->
                        popupHost.showPopupWithAButton(
                            message = "حواله به فروشگاه مرکزی به تعداد ${productsUiList.size} کالا با شماره $stockDraftNumber ایجاد و $requestNumber ",
                            onDismiss = { clearAll() },
                            onDoneButtonClick = { clearAll() }
                        )
                        loading = false
                    },
                    onError = {
                        showLog(data = it, state = state)
                        loading = false
                    }
                )
            }
        } else if (cartonsUiList.isNotEmpty()) {
            repository.createShelfInRequestByCartons(
                cartons = cartonsUiList,
                createRequestByRFID = newFeature,
                onSuccess = { stockDraftNumber, requestNumber ->
                    popupHost.showPopupWithAButton(
                        message = "حواله به فروشگاه مرکزی بامجموع کالا ${cartonsUiList.sumOf { it.numberOfItems }} با شماره $stockDraftNumber ایجاد و $requestNumber ",
                        onDismiss = { clearAll() },
                        onDoneButtonClick = { clearAll() }
                    )
                    loading = false
                },
                onError = {
                    showLog(data = it, state = state)
                    loading = false
                }
            )
        } else {
            showLog(data = "لیست خالی است.", state = state)
            loading = false
            return
        }
    }

    fun deleteProduct(index: Int) {
        if (cartonsUiList.isNotEmpty()) {
            barcode.scannedBarcodes.removeIf { it == cartonsUiList[index].number }
            cartonsUiList.removeAt(index)
        } else {
            uiListWithBarcodeMainMap.remove(productsUiList[index].primaryKey)
            barcode.scannedBarcodes.removeIf { it == productsUiList[index].scannedBarcode }
            for (i in 0 until productsUiList[index].scannedEPCs.size) {
                scannedEpcsWithDetails.remove(productsUiList[index].scannedEPCs[i])
            }
            productsUiList.removeAt(index)
        }
    }

    fun onImeAction() {
        getBarcodeDetails(textFieldValue)
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.connectWithContext()
        setBarcodeScanner()
    }

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        barcode.disconnectFromContext()
        barcode.enable()
    }

    private fun clearAll() {
        productsUiList.clear()
        cartonsUiList.clear()
        scannedBarcodesWithDetails.clear()
        uiListWithBarcodeMainMap.clear()
        textFieldValue = ""
        barcode.scannedBarcodes.clear()
    }

    private fun getBarcodeDetails(barcode: String) {

        if (newFeature) {

            if (requestType == RequestType.Carton) {
                if (barcode.startsWith("CN")) {
                    getCartonDetails(barcode)
                } else {
                    showLog(state = state, data = "در درخواست سایز بندی فقط اسکن کارتن مجاز است.")
                }
            } else {
                if (barcode.startsWith("CN")) {
                    showLog(state = state, data = "در درخواست مرجوعی اسکن کارتن مجاز نیست.")
                } else {
                    showLog(
                        state = state,
                        data = "لطفا کالا ها را در اتاق ایزوله با RFID اسکن کنید."
                    )
                }
            }
        } else {
            if (productsUiList.isEmpty() && barcode.startsWith("CN")) {
                getCartonDetails(barcode)
            } else if (cartonsUiList.isEmpty()) {
                if (newFeature) {
                    showLog(
                        data = "اسکن کالا مجاز نیست.",
                        state = state
                    )
                } else {
                    getProductDetails(barcode)
                }
            } else {
                showLog(data = "لیست باید فقط شامل کارتن یا فقط شامل کالا باشد.", state = state)
            }
            textFieldValue = ""
        }
    }

    private fun getProductDetails(productCode: String) {
        loading = true
        scannedBarcodesWithDetails[productCode].let { productDetails ->
            if (productDetails == null) {
                repository.getBarcodeDetails(productCode, onSuccess = {
                    scannedBarcodesWithDetails[it.scannedBarcode] = it.copy()
                    addToUiList(it)
                    loading = false
                }, onError = {
                    showLog(data = "مشخصات بارکد یافت نشد.", state = state)
                    errorBeep(state = state)
                    loading = false
                })
            } else {
                addToUiList(productDetails)
                loading = false
            }
        }
    }

    private fun getEPCsDetails() {
        loading = true
        rf.epcs.filter { it !in scannedEpcsWithDetails }.let { newEpcs ->
            if (newEpcs.isNotEmpty()) {
                repository.getItemDetailsAndInventory(
                    epcs = newEpcs,
                    barcodes = listOf(),
                    onSuccess = { epcs, _, invalidEpcs, _ ->
                        epcs.forEach {
                            scannedEpcsWithDetails[it.scannedEPCs[0]] = it
                            addEPCToUiList(it)
                        }
                        for (i in 0 until invalidEpcs.length()) {
                            rf.epcs.remove(invalidEpcs[i])
                        }
                        loading = false
                    }, onError = {
                        loading = false
                    }
                )
            } else {
                loading = false
            }
        }
    }

    private fun addEPCToUiList(product: Product) {
        uiListWithBarcodeMainMap[product.primaryKey].let { productDetails ->
            if (productDetails == null) {
                uiListWithBarcodeMainMap[product.primaryKey] = product
            } else {
                productDetails.scannedEPCs.add(product.scannedEPCs[0])
            }
            productsUiList.clear()
            productsUiList.addAll(uiListWithBarcodeMainMap.values)
        }
    }

    private fun addToUiList(product: Product) {

        uiListWithBarcodeMainMap[product.primaryKey].let { productDetails ->
            if (productDetails == null) {
                if (product.wareHouseNumber > 0) {
                    uiListWithBarcodeMainMap[product.primaryKey] =
                        product.copy(scannedBarcodeNumber = 1)
                    successBeep(state = state)
                } else {
                    showLog(data = "این کالا در انبار جاری شما موجودی ندارد.", state = state)
                    errorBeep(state = state)
                }
            } else {
                if (productDetails.wareHouseNumber > productDetails.scannedBarcodeNumber) {
                    successBeep(state = state)
                    productDetails.scannedBarcodeNumber++
                } else {
                    showLog(
                        data = "شما همه ی موجودی این کالا در انبار جاری را اسکن کرده اید.",
                        state = state
                    )
                    errorBeep(state = state)
                }
            }
            productsUiList.clear()
            productsUiList.addAll(uiListWithBarcodeMainMap.values)
        }
    }

    private fun setBarcodeScanner() {
        if (newFeature && requestType == RequestType.Product) {
            barcode.disable()
        } else {
            barcode.enable()
        }
    }

    private fun getCartonDetails(cartonCode: String) {

        loading = true
        repository.getCartonProducts(cartonCode = cartonCode, onSuccess = {
            if (it in cartonsUiList) {
                showLog(data = "کارتن در لیست موجود است.", state = state)
            } else {
                cartonsUiList.add(it)
            }
            loading = false
        }, onError = {
            showLog(data = "مشخصات کارتن یافت نشد.", state = state)
            loading = false
        })
    }

    fun onRequestTypeChange(value: String) {
        requestType = RequestType.entries.find { it.toString() == value } ?: RequestType.Carton
        setBarcodeScanner()
    }
}