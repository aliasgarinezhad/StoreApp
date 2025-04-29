package com.jeanwest.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import barcodeScannerPageAddress
import com.jeanwest.reader.factory.addTaskFeature.data.FactoryUser
import com.jeanwest.reader.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import com.jeanwest.reader.factory.main.viewModel.FactoryMainViewModel
import com.jeanwest.reader.factory.stopActivityFeature.viewModel.StopActivityViewModel
import com.jeanwest.reader.login.viewModel.LoginViewModel
import com.jeanwest.reader.shop.viewModel.KioskViewModel
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.jeanwest.reader.shop.data.StoreUser
import org.w3c.dom.get
import org.w3c.dom.set


@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val barcode = window.location.href.substringAfter("keyword=", "")
    println("web page ran")

    ComposeViewport(document.body!!) {

        App(
            mainViewModel = FactoryMainViewModel(factoryUser = FactoryUser()),
            addTaskViewModel = FactoryAddTaskViewModel(),
            stopActivityViewModel = StopActivityViewModel(),
        )
    }
}

private fun openScanner() {
    window.open(barcodeScannerPageAddress, target = "_self")
}

actual fun saveERPUserData(storeUser: StoreUser) {
    window.localStorage["userKey"] = Json.encodeToString(storeUser)
}

actual fun saveFactoryUserData(factoryUser: FactoryUser) {
    window.localStorage["factoryUserKey"] = Json.encodeToString(factoryUser)
}

actual fun getERPUserData(): StoreUser {
    return Json.decodeFromString(window.localStorage["userKey"] ?: Json.encodeToString(StoreUser()))
}

actual fun getFactoryUserData(): FactoryUser {
    return Json.decodeFromString(
        window.localStorage["factoryUserKey"] ?: Json.encodeToString(
            StoreUser()
        )
    )
}

@Composable
actual fun barcodeScanner() {
    openScanner()
}



