package io.domil.store.factory.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import io.domil.store.factory.useCase.features
import io.domil.store.factory.view.FeatureListScreen
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainScreen

class FactoryViewModel {

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    val featureList = features

    var destinationScreen: Any = mutableStateOf(LoginScreen)
    var currentScreen: Any = LoginScreen
    var screenChangePending by mutableStateOf(false)
        private set

    fun signIn(navHostController: NavHostController) {

        changeScreen(FeatureListScreen)

        /*CoroutineScope(Default).launch {
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
        }*/
    }

    fun changeScreen(screen: Any) {
        destinationScreen = screen
        screenChangePending = true
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
}