package io.domil.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.domil.store.viewModel.AppViewModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import networking.User
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

lateinit var user: User

fun MainViewController() = ComposeUIViewController {

    val dataStore = remember { createDataStore() }

    getUserData(dataStore = dataStore)

    val viewModel = AppViewModel(
        saveUserData = {
            saveUserData(dataStore = dataStore, user = it)
        },
        savedUser = user,
        webPageRequestBarcode = ""
    )

    val barcodeScannerComposable: @Composable (
        onScanSuccess: (barcode: String) -> Unit
    ) -> Unit = { onScanSuccess ->
        QrScanner(onScanSuccess = onScanSuccess)
    }

    App(
        barcodeScanner = barcodeScannerComposable,
        viewModel = viewModel
    )
}

private fun saveUserData(user: User, dataStore: DataStore<Preferences>) {

    CoroutineScope(Dispatchers.Default).launch {
        val userKey = stringPreferencesKey("userKey")
        dataStore.edit {
            it[userKey] = Json.encodeToString(user)
        }
    }
}

private fun getUserData(dataStore: DataStore<Preferences>) {

    runBlocking {
        val userKey = stringPreferencesKey("userKey")
        user =  dataStore.data.map {
            Json.decodeFromString<User>(it[userKey] ?: Json.encodeToString(User()))
        }.first()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun createDataStore(): DataStore<Preferences> {

    val dataStoreFileName = "storeAppLocalMemory.preferences_pb"

    return PreferenceDataStoreFactory.createWithPath {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        (requireNotNull(documentDirectory).path + "/$dataStoreFileName").toPath()
    }
}