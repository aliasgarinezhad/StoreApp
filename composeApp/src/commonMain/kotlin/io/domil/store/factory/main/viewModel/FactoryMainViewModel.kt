package io.domil.store.factory.main.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.domil.store.factory.main.useCase.features
import io.domil.store.factory.main.view.FeatureListScreen
import io.domil.store.view.LoginScreen

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
    val featureList = features

    var destinationScreen: Any = LoginScreen
    var currentScreen: Any = LoginScreen
    var screenChangePending by mutableStateOf(false)
        private set

    fun signIn() {

        changeScreen(FeatureListScreen)
        //TODO
    }

    fun onFeatureIconClick(screen: Any) {
        changeScreen(screen)
    }

    private fun changeScreen(screen: Any) {
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