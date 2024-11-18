package io.domil.store.viewModel

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import networking.GetProductData
import networking.Product
import networking.User
import networking.createHttpClient
import util.onError
import util.onSuccess

class AppViewModel(
    val saveUserData: (user: User) -> Unit,
    val loadUserData: (onDataReceived: (user: User) -> Unit) -> Unit,
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

    var uilistColorFiltered = mutableMapOf<String, String>()

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
        println("filteredFirst"+value)
        colorFilterValue = value
        println("filtered2"+colorFilterValue)
        filterUiList()
    }

    fun onAccountBtnClick(){
        isAccountDialogOpen = !isAccountDialogOpen
    }

    fun onStoreFilterValueChange(value: String) {
        storeFilterValue = value
        user.locationCode = storeFilterValues[value]?.toInt() ?: 0
        saveUserData(user)
    }

    fun onImeAction() {
        isCameraOn = false
        getSimilarProducts()
    }

    fun openCamera() {
        isCameraOn = !isCameraOn
    }

    fun checkUserAuth(navHostController: NavHostController) {
        loadUserData {
            println(it.toString())
            if (it.username.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    routeScreen.value = MainScreen
                    navHostController.clearBackStack<MainScreen>()
                    user = it
                    username = it.username
                    storeFilterValues.clear()
                    it.warehouses.forEach {
                        storeFilterValues[it.WareHouseTitle] = it.DepartmentInfo_ID
                    }
                    storeFilterValue =
                        storeFilterValues.entries.find { it.value == user.locationCode.toString() }?.key
                            ?: ""
                }
            }
        }
    }

    fun onLogoutClick(navHostController: NavHostController) {
        loading = true
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
        CoroutineScope(Dispatchers.Default).launch {
            if (username.isEmpty() || password.isEmpty()) {
                showLog("لطفا تمامی مقادیر را وارد کنید", state)
            } else {
                loading = true
                client.loginUser(username, password).onSuccess {
                    user = it
                    withContext(Dispatchers.Main) {
                        navHostController.navigate(MainScreen)
                        routeScreen.value = MainScreen
                        navHostController.clearBackStack<MainScreen>()
                    }
                    saveUserData(it)
                }.onError {
                    if (it.name == "UNAUTHORIZED") {
                        showLog("نام کاربری یا رمزعبور اشتباه است", state)
                    } else {
                        showLog(it.toString(), state)
                    }
                }
                loading = false
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
            if (!uilistColorFiltered.keys.toMutableList().contains(it.Color)) {
                uilistColorFiltered[it.Color] = it.ImgUrl
            }
        }

        println("filteredcolorValue: $colorFilterValue")
        if (colorFilterValue == "") {
            colorFilterValue = uilistColorFiltered.keys.toList()[0]
        }
        println("filteredcolorValue: $colorFilterValue")
        filteredUiList.clear()
        searchUiList.forEach {
            if (it.Color == colorFilterValue){
                filteredUiList.add(it)
            }
        }
        println("filtered: ${filteredUiList.toList()}")
    }

    private fun clear() {
        searchUiList.clear()
        filteredUiList.clear()
        productCode = ""
        colorFilterValue = ""
        sizeFilterValue = ""
    }

    private fun getSimilarProducts() {
        if (productCode == "") {
            showLog("لطفا کد محصول را وارد کنید.", state)
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            loading = true
            try {
                client.getSimilarProductsByBarcode(productCode.trim(), user.locationCode)
                    .onSuccess {
                        if (it.isNotEmpty()) {
                            handleResponse(it)
                        } else {
                            client.getSimilarProductsBySearchCode(
                                productCode.trim(),
                                user.locationCode
                            )
                                .onSuccess { it1 ->
                                    if (it1.isEmpty()) {
                                        showLog(
                                            "این کد فرعی در هیچ موجودی در فروشگاه شما ندارد",
                                            state
                                        )
                                    } else {
                                        handleResponse(it1)
                                    }
                                }.onError {
                                    showLog(it.name, state)
                                }
                        }
                    }.onError { e2 ->
                        client.getSimilarProductsBySearchCode(productCode.trim(), user.locationCode)
                            .onSuccess { it1 ->
                                if (it1.isEmpty()) {
                                    showLog("این کد فرعی در هیچ موجودی در فروشگاه شما ندارد", state)
                                } else {
                                    handleResponse(it1)
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

    private fun getImgAlbum(barcode: String){
        CoroutineScope(Dispatchers.Default).launch {
            loading = true
            try {
                client.getImageAlbumUrl(barcode.trim())
                    .onSuccess {
                        if (it.isNotEmpty()) {
                            imgUrls.addAll(it)
                        }
                    }.onError {
                        println("errorIs: $it")
                        showLog("مشکلی در دریافت عکس ها پیش آمده است",state)
                    }

            } catch (e: Exception) {
            } finally {
                loading = false
            }
        }

    }

    private fun handleResponse(response: List<Product>) {
        if (response.isEmpty()) {
            clear()
        } else {
            clear()
            searchUiList.apply {
                clear()
                addAll(response)
            }
            filterUiList()
        }
    }

    fun barcodeScanner(scannedBarcode: String) {
        isCameraOn = false
        productCode = scannedBarcode
        getSimilarProducts()
    }
}