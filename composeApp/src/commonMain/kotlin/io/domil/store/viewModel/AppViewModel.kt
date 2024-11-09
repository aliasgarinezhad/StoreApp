package io.domil.store.viewModel

import androidx.compose.material.SnackbarDuration
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

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set

    // search ui parameters
    var productCode by mutableStateOf("")
        private set
    var filteredUiList = mutableStateListOf<Product>()
        private set
    var colorFilterValues = mutableStateListOf("همه رنگ ها")
        private set
    var sizeFilterValues = mutableStateListOf("همه سایز ها")
        private set
    var colorFilterValue by mutableStateOf("همه رنگ ها")
        private set
    var sizeFilterValue by mutableStateOf("همه سایز ها")
        private set
    var isCameraOn by mutableStateOf(false)
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var routeScreen: MutableState<Any> = mutableStateOf(LoginScreen)
        private set

    fun onTextValueChange(value: String) {
        productCode = value
    }

    fun onSizeFilterValueChange(value: String) {
        sizeFilterValue = value
    }

    fun onColorFilterValueChange(value: String) {
        colorFilterValue = value
    }

    fun onImeAction() {
        isCameraOn = false
        getSimilarProducts()
    }

    fun openCamera() {
        isCameraOn = true
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
                }
            }
        }
    }

    fun signIn(navHostController: NavHostController) {
        CoroutineScope(Dispatchers.Default).launch {
            client.loginUser(username, password).onSuccess {
                user = it
                withContext(Dispatchers.Main) {
                    navHostController.navigate(MainScreen)
                    routeScreen.value = MainScreen
                    navHostController.clearBackStack<MainScreen>()
                }
                saveUserData(it)
            }.onError {
                showLog(it.toString(),state)
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

        val sizeFilterOutput = if (sizeFilterValue == "همه سایز ها") {
            searchUiList
        } else {
            searchUiList.filter {
                it.Size == sizeFilterValue
            }
        }

        val colorFilterOutput = if (colorFilterValue == "همه رنگ ها") {
            sizeFilterOutput
        } else {
            sizeFilterOutput.filter {
                it.Color == colorFilterValue
            }
        }
        filteredUiList.clear()
        filteredUiList.addAll(colorFilterOutput)

    }

    private fun clear() {
        searchUiList.clear()
        filteredUiList.clear()
        colorFilterValues = mutableStateListOf("همه رنگ ها")
        sizeFilterValues = mutableStateListOf("همه سایز ها")
        productCode = ""
    }

    private fun getSimilarProducts() {

        if (productCode == "") {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "لطفا کد محصول را وارد کنید.",
                    null,
                    SnackbarDuration.Long
                )
            }
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
                                    handleResponse(it1)
                                }.onError { e1 ->
                                    println("Request error1: ${e1.name}")
                                    client
                                }
                        }
                    }.onError { e2 ->
                        client.getSimilarProductsBySearchCode(productCode.trim(), user.locationCode)
                            .onSuccess { it1 ->
                                handleResponse(it1)
                            }.onError { e1 ->
                                println("Request error1: ${e1.name}")
                                clear()
                            }
                        println("Request error: $e2")
                    }

            } catch (e: Exception) {
                println("Request error: $e")
                // You could also attempt a retry here if needed
            } finally {
                loading = false
            }
        }
    }

    private fun handleResponse(response: List<Product>) {
        if (response.isEmpty()) {
            clear()
            println("request body = 1st body is empty")
        } else {
            clear()
            searchUiList.apply {
                clear()
                addAll(response)
            }
            filterUiList()
        }
    }

    fun baroceScanner(scannedBarcode: String) {
        isCameraOn = false
        productCode = scannedBarcode
        getSimilarProducts()
    }
}