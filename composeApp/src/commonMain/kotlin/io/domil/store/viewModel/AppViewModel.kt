package io.domil.store.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainScreen
import io.domil.store.view.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.domil.store.networking.GetProductData
import io.domil.store.networking.Product
import io.domil.store.networking.User
import io.domil.store.networking.createHttpClient
import io.domil.store.util.onError
import io.domil.store.util.onSuccess

class AppViewModel(
    val saveUserData: (user: User) -> Unit,
    val webPageRequestBarcode: String,
    val savedUser: User,
) {

    private var user = User()
    private var client = GetProductData(user, createHttpClient())
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
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var routeScreen: MutableState<Any> = mutableStateOf(LoginScreen)
        private set

    var uiListColorFiltered = mutableMapOf<String, String>()
    private var loadDataRan = false
    private var itemBarcode = ""

    private val sizes = listOf("S", "M", "L", "XL", "XXL", "XXXL")
    var colorFilterLazyRowState = mutableStateOf(LazyListState())


    init {

        loading = true

        println(savedUser.toString())
        if (savedUser.username.isNotEmpty()) {
            println("signed in")
            user = savedUser
            storeFilterValues.clear()
            savedUser.warehouses.forEach {
                storeFilterValues[it.WareHouseTitle] = it.DepartmentInfo_ID
            }
            storeFilterValue =
                storeFilterValues.entries.find { it.value == user.locationCode.toString() }?.key
                    ?: ""

            if (webPageRequestBarcode.isNotEmpty()) {
                onScanResult(webPageRequestBarcode)
            }
            routeScreen.value = MainScreen
            loading = false

        } else {
            println("not signed in")
            CoroutineScope(Main).launch {
                loading = false
            }
        }
        loadDataRan = true
    }

    fun changeFullScreenState() {
        isFullScreenImage = !isFullScreenImage
        println("kbarcode: ${filteredUiList[0].KBarCode}")
        getImgAlbum(filteredUiList[0].KBarCode)
    }

    fun onTextValueChange(value: String) {
        productCode = value
    }

    fun onSizeFilterValueChange(value: String) {
        sizeFilterValue = value
    }

    fun onColorFilterValueChange(value: String) {
        println("filteredFirst" + value)
        colorFilterValue = value
        println("filtered2" + colorFilterValue)
        filterUiList()
    }

    fun onAccountBtnClick() {
        isAccountDialogOpen = !isAccountDialogOpen
    }

    fun onStoreFilterValueChange(value: String) {
        storeFilterValue = value
        user.locationCode = storeFilterValues[value]?.toInt() ?: 0
        saveUserData(user)
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

    private suspend fun delayScreen() {

        withContext(Main) {
            loading = true
        }
        CoroutineScope(Default).launch {
            delay(500)
            withContext(Main) {
                loading = false
            }
        }
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
        user = User()
        saveUserData(user)
        storeFilterValues.clear()
        navHostController.navigate(LoginScreen)
        routeScreen.value = LoginScreen
        navHostController.clearBackStack<LoginScreen>()
        isAccountDialogOpen = !isAccountDialogOpen
        loading = false
    }

    fun signIn(navHostController: NavHostController) {
        CoroutineScope(Default).launch {
            if (username.isEmpty() || password.isEmpty()) {
                showLog("لطفا تمامی مقادیر را وارد کنید", state)
            } else {
                loading = true
                client.loginUser(username, password).onSuccess {
                    user = it
                    storeFilterValues.clear()
                    it.warehouses.forEach { warehouse ->
                        storeFilterValues[warehouse.WareHouseTitle] = warehouse.DepartmentInfo_ID
                    }
                    storeFilterValue =
                        storeFilterValues.entries.find { it.value == user.locationCode.toString() }?.key
                            ?: ""
                    saveUserData(it)
                    withContext(Main) {
                        navHostController.navigate(MainScreen)
                        routeScreen.value = MainScreen
                        navHostController.clearBackStack<MainScreen>()
                        delayScreen()
                    }
                }.onError {
                    if (it.name == "UNAUTHORIZED") {
                        showLog("نام کاربری یا رمزعبور اشتباه است", state)
                    } else {
                        showLog(it.toString(), state)
                    }
                    withContext(Main) {
                        loading = false
                    }
                }
            }
        }
    }

    fun onUsernameValueChanges(value: String) {
        username = value
    }

    fun onPasswordValueChanges(value: String) {
        password = value
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
                client.getSimilarProductsByBarcode(productCode.trim(), user.locationCode)
                    .onSuccess {
                        if (it.isNotEmpty()) {
                            handleResponse(it, productCode)
                        } else {
                            client.getSimilarProductsBySearchCode(
                                productCode.trim(), user.locationCode
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
                        client.getSimilarProductsBySearchCode(productCode.trim(), user.locationCode)
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