package com.jeanwest.reader.shop.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.jeanwest.reader.view.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import com.jeanwest.reader.shop.data.GetProductData
import com.jeanwest.reader.shop.data.Product
import com.jeanwest.reader.shop.data.StoreUser
import com.jeanwest.reader.data.createHttpClient
import com.jeanwest.reader.data.onError
import com.jeanwest.reader.data.onSuccess
import com.jeanwest.reader.saveERPUserData
import com.jeanwest.reader.view.NotificationPopupHost

/**
 * ViewModel for the application, managing UI state and interactions related to product data, user authentication, and navigation.
 *
 * @property saveERPUserData A function to save user data to persistent storage.  It takes a [StoreUser] object as input.
 * @property webPageRequestBarcode  A barcode passed from a web page request, used to automatically search for a product upon login if not empty.
 * @property savedStoreUser The [StoreUser] object retrieved from persistent storage upon app launch.  If empty, the user is not logged in.
 */
class KioskViewModel(
    val webPageRequestBarcode: String,
    val savedStoreUser: StoreUser,
): ViewModel() {

    var popupHost = NotificationPopupHost()
    private var storeUser = StoreUser()
    private var client = GetProductData(storeUser, createHttpClient())
    private var searchUiList = mutableStateListOf<Product>()
    var imgUrls = mutableListOf<String>()

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var isFullScreenImage by mutableStateOf(false)
        private set
    var isAccountDialogOpen by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set

    // search ui parameters
    var productCode by mutableStateOf("")
        private set
    var filteredUiList = mutableStateListOf<Product>()
        private set
    var storeFilterValues = mutableMapOf<String, String>()
        private set
    var storeFilterValue by mutableStateOf("")
        private set
    var colorFilterValue by mutableStateOf("")
        private set
    var sizeFilterValue by mutableStateOf("")
        private set

    var isCameraOn by mutableStateOf(false)
        private set

    var uiListColorFiltered = mutableMapOf<String, String>()
    private var itemBarcode = ""

    private val sizes = listOf("S", "M", "L", "XL", "XXL", "XXXL")
    var colorFilterLazyRowState = mutableStateOf(LazyListState())


    init {

        loading = true

        CoroutineScope(Default).launch {

            println(savedStoreUser.toString())
            if (savedStoreUser.username.isNotEmpty()) {
                storeUser = savedStoreUser
                storeFilterValues.clear()
                savedStoreUser.warehouses.forEach {
                    storeFilterValues[it.WareHouseTitle] = it.DepartmentInfo_ID
                }
                storeFilterValue =
                    storeFilterValues.entries.find { it.value == storeUser.locationCode.toString() }?.key
                        ?: ""

                if (webPageRequestBarcode.isNotEmpty()) {
                    onScanResult(webPageRequestBarcode)
                }
                loading = false
            }
        }
    }

    fun changeFullScreenState() {
        isFullScreenImage = !isFullScreenImage
        println("kbarcode: ${filteredUiList[0].KBarCode}")
        getImgAlbum(filteredUiList[0].KBarCode)
    }

    fun onTextValueChange(value: String) {
        productCode = value
    }

    fun onColorFilterValueChange(value: String) {
        println("filteredFirst" + value)
        colorFilterValue = value
        println("filtered2" + colorFilterValue)
        filterUiList()
    }

    fun onAccountBtnClick(navHostController: NavHostController) {
        popupHost.showPopupWith2Button1DropDownList(
            message = "تنظیمات حساب کاربری",
            dropDownText = storeFilterValue,
            dropDownList = storeFilterValues.keys.toList(),
            onOkClick = {
                onStoreFilterValueChange(it)
            },
            okButtonTitle = "ذخیره",
            onCancelClick = { onLogoutClick(navHostController = navHostController) },
            cancelButtonTitle = "خروج از حساب",
        )
    }

    fun onStoreFilterValueChange(value: String) {
        storeFilterValue = value
        storeUser.locationCode = storeFilterValues[value]?.toInt() ?: 0
        saveERPUserData(storeUser)
        clear()
    }

    private fun clear() {
        itemBarcode = ""
        searchUiList.clear()
        filteredUiList.clear()
        productCode = ""
        colorFilterValue = ""
        sizeFilterValue = ""
        uiListColorFiltered.clear()
    }

    fun onImeAction() {
        isCameraOn = false
        getSimilarProducts()
    }

    fun openCamera() {
        isCameraOn = !isCameraOn
    }

    fun onLogoutClick(navHostController: NavHostController) {
        loading = true
        clear()
        storeUser = StoreUser()
        saveERPUserData(storeUser)
        storeFilterValues.clear()
        navHostController.popBackStack()
        isAccountDialogOpen = !isAccountDialogOpen
        loading = false
    }

    private fun filterUiList() {

        searchUiList.forEach {
            if (!uiListColorFiltered.keys.toMutableList().contains(it.Color)) {
                uiListColorFiltered[it.Color] = it.ImgUrl
            }
        }

        println("filteredcolorValue: $colorFilterValue")
        if (colorFilterValue == "") {
            colorFilterValue = if (itemBarcode.isNotEmpty()) {
                val color = uiListColorFiltered.keys.find {
                    it in itemBarcode
                }
                if (color != null) {
                    CoroutineScope(Main).launch {
                        colorFilterLazyRowState.value.requestScrollToItem(
                            uiListColorFiltered.keys.indexOf(
                                color
                            ), scrollOffset = -120
                        )
                    }
                    color
                } else {
                    uiListColorFiltered.keys.toList()[0]
                }
            } else {
                uiListColorFiltered.keys.toList()[0]
            }
        }
        println("filteredcolorValue: $colorFilterValue")
        filteredUiList.clear()
        searchUiList.forEach {
            if (it.Color == colorFilterValue) {
                filteredUiList.add(it)
            }
        }
        filteredUiList.sortBy {
            sizes.indexOf(it.Size)
        }
        println("filtered: ${filteredUiList.toList()}")
    }

    private fun getSimilarProducts() {
        if (productCode == "") {
            showLog("لطفا کد محصول را وارد کنید.", state)
            return
        }

        CoroutineScope(Default).launch {
            loading = true
            try {
                client.getSimilarProductsByBarcode(productCode.trim(), storeUser.locationCode)
                    .onSuccess {
                        if (it.isNotEmpty()) {
                            handleResponse(it, productCode)
                        } else {
                            client.getSimilarProductsBySearchCode(
                                productCode.trim(), storeUser.locationCode
                            ).onSuccess { it1 ->
                                if (it1.isEmpty()) {
                                    clear()
                                    showLog(
                                        "این کد فرعی هیچ موجودی در فروشگاه شما ندارد", state
                                    )
                                } else {
                                    handleResponse(it1, "")
                                }
                            }.onError {
                                clear()
                                showLog(it.name, state)
                            }
                        }
                    }.onError { e2 ->
                        client.getSimilarProductsBySearchCode(productCode.trim(), storeUser.locationCode)
                            .onSuccess { it1 ->
                                if (it1.isEmpty()) {
                                    showLog("این کد فرعی هیچ موجودی در فروشگاه شما ندارد", state)
                                    clear()
                                } else {
                                    handleResponse(it1, "")
                                }
                            }.onError { e1 ->
                                println("Request error1: ${e1.name}")
                                clear()
                            }
                        println("Request error: $e2")
                    }

            } catch (e: Exception) {
            } finally {
                loading = false
            }
        }
    }

    private fun getImgAlbum(barcode: String) {
        CoroutineScope(Default).launch {
            loading = true
            try {
                client.getImageAlbumUrl(barcode.trim()).onSuccess {
                    if (it.isNotEmpty()) {
                        imgUrls.addAll(it)
                    }
                }.onError {
                    println("errorIs: $it")
                    showLog("مشکلی در دریافت عکس ها پیش آمده است", state)
                }

            } catch (e: Exception) {
            } finally {
                loading = false
            }
        }

    }

    private fun handleResponse(response: List<Product>, searchCode: String) {
        if (response.isEmpty()) {
            clear()
        } else {
            clear()
            searchUiList.apply {
                clear()
                addAll(response)
            }
            productCode = response[0].K_Bar_Code
            if (searchCode.isNotEmpty()) {
                itemBarcode = searchCode
            }
            filterUiList()
        }
    }

    fun barcodeScanner(scannedBarcode: String) {
        isCameraOn = false
        productCode = scannedBarcode
        getSimilarProducts()
    }

    private fun onScanResult(scannedBarcode: String) {
        productCode = scannedBarcode
        getSimilarProducts()
    }
}