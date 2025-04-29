
package com.jeanwest.reader.login.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jeanwest.reader.data.createHttpClient
import com.jeanwest.reader.data.onError
import com.jeanwest.reader.data.onSuccess
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.factory.addTaskFeature.data.RemoteConnection
import com.jeanwest.reader.getERPUserData
import com.jeanwest.reader.getFactoryUserData
import com.jeanwest.reader.login.view.NavigationEvents
import com.jeanwest.reader.saveERPUserData
import com.jeanwest.reader.saveFactoryUserData
import com.jeanwest.reader.shop.data.GetProductData
import com.jeanwest.reader.shop.data.StoreUser
import com.jeanwest.reader.view.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel: ViewModel() {

    private var storeUser = StoreUser()
    private var factoryUser = FactoryUser()

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    val state = SnackbarHostState()
    var isFactoryMode by mutableStateOf(false)
    private var _navigationEvents = MutableSharedFlow<NavigationEvents>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    val client = GetProductData(httpClient = createHttpClient(), storeUser = storeUser)

    init {

        println("hello")

        loading = true
        CoroutineScope(Default).launch {
            storeUser = getERPUserData()
            factoryUser = getFactoryUserData()
            println(storeUser.toString())
            println(factoryUser.toString())
            if (storeUser.username.isNotEmpty()) {
                CoroutineScope(Main).launch {
                    _navigationEvents.emit(NavigationEvents.OpenStoreModuleEvent)
                    loading = false
                }
            } else if (factoryUser.accessToken.isNotEmpty()) {
                CoroutineScope(Main).launch {
                    _navigationEvents.emit(NavigationEvents.OpenFactoryModuleEvent)
                    loading = false
                }
            } else {
                CoroutineScope(Main).launch {
                    loading = false
                }
            }
            println("scope finished")
        }
    }

    fun login() {

        if (username.isEmpty() || password.isEmpty()) {
            showLog("لطفا تمامی مقادیر را وارد کنید", state)
        } else {

            if (isFactoryMode) {
                loginToFactory()
            } else {
                loginToStore()
            }
        }
    }

    private fun loginToFactory() {
        CoroutineScope(Default).launch {
            if (!username.all { it.isDigit() } || !password.all { it.isDigit() }) {
                showLog("تمام مقادیر وارد شده باید عددی باشد", state)
            } else {
                loading = true
                RemoteConnection.loginUserFactory(username.toLong(), password.toLong()).onSuccess {
                    factoryUser = it
                    saveFactoryUserData(factoryUser = factoryUser)
                    saveERPUserData(storeUser = StoreUser())
                    CoroutineScope(Main).launch {
                        _navigationEvents.emit(NavigationEvents.OpenFactoryModuleEvent)
                        loading = false
                    }
                }.onError {
                    if (it.name == "UNKNOWN" || it.name == "UNAUTHORIZED") {
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

    private fun loginToStore() {

        loading = true

        CoroutineScope(Default).launch {
            client.loginUser(username, password).onSuccess {
                storeUser = it
                saveERPUserData(storeUser = storeUser)
                saveFactoryUserData(factoryUser = FactoryUser())
                _navigationEvents.emit(NavigationEvents.OpenStoreModuleEvent)
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

    fun onUsernameValueChanges(value: String) {
        username = value
    }

    fun onIsFactoryModeChanged(value: Boolean) {
        isFactoryMode = value
    }

    fun onPasswordValueChanges(value: String) {
        password = value
    }
}