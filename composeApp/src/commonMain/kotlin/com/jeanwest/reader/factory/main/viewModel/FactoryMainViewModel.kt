package com.jeanwest.reader.factory.main.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.factory.addTaskFeature.data.RemoteConnection
import com.jeanwest.reader.factory.addTaskFeature.viewModel.SharedRepository
import com.jeanwest.reader.factory.main.model.Feature
import com.jeanwest.reader.factory.main.useCase.features
import com.jeanwest.reader.factory.main.view.FeatureListScreen
import com.jeanwest.reader.data.onError
import com.jeanwest.reader.data.onSuccess
import com.jeanwest.reader.shop.view.LoginScreen
import com.jeanwest.reader.view.showLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Factory Main screen, handling user authentication,
 * feature access, and navigation within the application.
 */
class FactoryMainViewModel {

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    val featureList = mutableStateListOf<Feature>()

    var destinationScreen: Any = LoginScreen
    var currentScreen: Any = LoginScreen
    var screenChangePending by mutableStateOf(false)
        private set
    var machineCodeTextFieldValue by mutableStateOf("")
    private var factoryUser = FactoryUser()


    fun signIn() {
        CoroutineScope(Default).launch {
            if (username.isEmpty() || password.isEmpty()) {
                showLog("لطفا تمامی مقادیر را وارد کنید", state)
            } else if (!username.all { it.isDigit() } || !password.all { it.isDigit() }) {
                showLog("تمام مقادیر وارد شده باید عددی باشد", state)
            } else {
                loading = true
                RemoteConnection.loginUserFactory(username.toLong(), password.toLong()).onSuccess {
                    factoryUser = it
                    val userFeatures = factoryUser.icons.map { feature -> feature.iconLatinName }
                    features.forEach { feature ->
                        if (feature.accessKey in userFeatures) {
                            featureList.add(feature)
                        }
                    }
                    SharedRepository.machineCode = factoryUser.machineCode
                    machineCodeTextFieldValue = SharedRepository.machineCode.toString()
                    withContext(Main) {
                        changeScreen(FeatureListScreen)
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

    fun onFeatureIconClick(screen: Any) {
        changeScreen(screen)
    }

    private fun changeScreen(screen: Any) {
        if (!screenChangePending) {
            destinationScreen = screen
            screenChangePending = true
        }
    }

    fun onScreenChanged() {
        currentScreen = destinationScreen
        screenChangePending = false
    }

    fun onUsernameValueChanges(value: String) {
        username = value
    }

    fun onPasswordValueChanges(value: String) {
        password = value
    }

    fun onTextFieldFocused() {
        machineCodeTextFieldValue = ""
    }

    fun changeMachineCode(value: String) {
        machineCodeTextFieldValue = value
        if (value.toIntOrNull() == null) {
            if (value != "") showLog("لطفا مقدار عددی وارد کنید.", state)
        } else {
            SharedRepository.machineCode = value.toInt()
        }
    }
}