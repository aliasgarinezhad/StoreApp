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
import com.jeanwest.reader.getFactoryUserData
import com.jeanwest.reader.login.view.LoginScreen
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
class FactoryMainViewModel(factoryUser: FactoryUser) {

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    val featureList = mutableStateListOf<Feature>()

    var destinationScreen: Any = LoginScreen
    var currentScreen: Any = LoginScreen
    var screenChangePending by mutableStateOf(false)
        private set
    var machineCodeTextFieldValue by mutableStateOf("")
    var userFullName by mutableStateOf("")
        private set

    init {
        userFullName = factoryUser.fullName
        val userFeatures = factoryUser.icons.map { feature -> feature.iconLatinName }
        features.forEach { feature ->
            if (feature.accessKey in userFeatures) {
                featureList.add(feature)
            }
        }
        SharedRepository.machineCode = factoryUser.machineCode
        machineCodeTextFieldValue = SharedRepository.machineCode.toString()
    }

    fun onFeatureIconClick(screen: Any) {
        if (machineCodeTextFieldValue == "") {
            showLog(data = "لطفا شماره چرخ را وارد کنید.", state = state)
        } else {
            changeScreen(screen)
        }
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