package io.domil.store

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import barcodeScannerPageAddress
import io.domil.store.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import io.domil.store.factory.main.viewModel.FactoryMainViewModel
import io.domil.store.viewModel.AppViewModel
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.domil.store.networking.User
import org.w3c.dom.get
import org.w3c.dom.set


@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val isFactoryAppRequested = window.location.href.contains("factory")
    val barcode = window.location.href.substringAfter("keyword=", "")
    val user = getUserData()

    println("web page ran")

    val viewModel = AppViewModel(
        saveUserData = {
            saveUserData(it)
        },
        savedUser = user,
        webPageRequestBarcode = barcode
    )

    ComposeViewport(document.body!!) {

        val barcodeScannerComposable: @Composable (
            onScanSuccess: (barcode: String) -> Unit
        ) -> Unit = {
            openScanner()
        }

        App(
            barcodeScanner = barcodeScannerComposable,
            viewModel = viewModel,
            factoryMainViewModel = FactoryMainViewModel(),
            isFactoryAppRequested = isFactoryAppRequested,
            factoryAddTaskViewModel = FactoryAddTaskViewModel()
        )
    }
}

private fun openScanner() {
    window.open(barcodeScannerPageAddress, target = "_self")
}

fun saveUserData(user: User) {
    window.localStorage["userKey"] = Json.encodeToString(user)
}

fun getUserData(): User {
    return Json.decodeFromString(window.localStorage["userKey"] ?: Json.encodeToString(User()))
}


