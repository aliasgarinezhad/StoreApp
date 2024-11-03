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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import networking.GetProductData
import networking.Product
import networking.createHttpClient
import util.onError
import util.onSuccess

class AppViewModel {

    private var fullName = ""
    private var token = ""
    private var storeFilterValue = 0
    private var client = GetProductData(token, createHttpClient())
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
    var routeScreen: MutableState<Any> = mutableStateOf(MainScreen)
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

    fun checkUserAuth() {
        if (token == "") {
            routeScreen.value = LoginScreen
        } else {
            routeScreen.value = MainScreen
        }
    }

    fun signIn(navHostController: NavHostController) {

        token = "fg"
        navHostController.navigate(MainScreen)
        routeScreen.value = MainScreen
        navHostController.clearBackStack<MainScreen>()

        /*private fun signIn() {

            val memory = PreferenceManager.getDefaultSharedPreferences(this)
            val editor = memory.edit()

            val logInRequest = Volley.newRequestQueue(this)
            val url = "https://rfid-api.avakatan.ir/login"

            val jsonRequest = object : JsonObjectRequest(Method.POST, url, null, { response ->

                editor.putString("accessToken", response.getString("accessToken"))
                editor.putString("username", username)
                editor.putString("userFullName", response.getString("fullName"))
                editor.putInt("userLocationCode", response.getInt("locationCode"))
                editor.putInt(
                    "userWarehouseCode",
                    response.getJSONObject("location").getInt("warehouseCode")
                )

                editor.apply()

                val intent =
                    Intent(this@UserLoginActivity, ManualRefillActivity::class.java)
                intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)


            }, {
                when {
                    it is NoConnectionError -> {
                        CoroutineScope(Dispatchers.Default).launch {
                            state.showSnackbar(
                                "اینترنت قطع است. شبکه وای فای را بررسی کنید.",
                                null,
                                SnackbarDuration.Long
                            )
                        }
                    }
                    it.networkResponse.statusCode == 401 -> {

                        CoroutineScope(Dispatchers.Default).launch {
                            state.showSnackbar(
                                "نام کاربری یا رمز عبور اشتباه است",
                                null,
                                SnackbarDuration.Long
                            )
                        }
                    }
                    else -> {
                        CoroutineScope(Dispatchers.Default).launch {
                            state.showSnackbar(
                                it.toString(),
                                null,
                                SnackbarDuration.Long
                            )
                        }
                    }
                }

                editor.putString("accessToken", "")
                editor.putString("username", "")
                editor.apply()

            }) {

                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("username", username)
                    body.put("password", password)
                    return body.toString().toByteArray()
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json;charset=UTF-8"
                    return params
                }
            }
            logInRequest.add(jsonRequest)

        */
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

        storeFilterValue = 68
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MDE2IiwibmFtZSI6Itiq2LPYqiBSRklEINiq2LPYqiBSRklEIiwiZGVwSWQiOjY4LCJ3YXJlaG91c2VzIjpbeyJXYXJlSG91c2VfSUQiOjE3MDYsIldhcmVIb3VzZVR5cGVzX0lEIjoxfSx7IldhcmVIb3VzZV9JRCI6MTcwNywiV2FyZUhvdXNlVHlwZXNfSUQiOjJ9LHsiV2FyZUhvdXNlX0lEIjoxOTE4LCJXYXJlSG91c2VUeXBlc19JRCI6Mn0seyJXYXJlSG91c2VfSUQiOjE5MTksIldhcmVIb3VzZVR5cGVzX0lEIjoxfV0sInJvbGVzIjpbInVzZXIiXSwic2NvcGVzIjpbImVycCJdLCJpYXQiOjE3MzAyODA0NDAsImV4cCI6MjMxMDg4ODQ0MCwiYXVkIjoiZXJwIn0.hsvOBrJO4Qsa64pjngdNEl82BcxwLT2Y3PBFNbIaE-s"
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
                client.getSimilarProductsByBarcode(productCode.trim(), storeFilterValue).onSuccess {
                    if (it.isNotEmpty()) {
                        handleResponse(it)
                    } else {
                        client.getSimilarProductsBySearchCode(productCode.trim(), storeFilterValue)
                            .onSuccess { it1 ->
                                handleResponse(it1)
                            }.onError { e1 ->
                                println("Request error1: ${e1.name}")
                                client
                            }
                    }
                }.onError { e2 ->
                    client.getSimilarProductsBySearchCode(productCode.trim(), storeFilterValue)
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
                loading = false // Ensure loading is set to false in all cases
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
}