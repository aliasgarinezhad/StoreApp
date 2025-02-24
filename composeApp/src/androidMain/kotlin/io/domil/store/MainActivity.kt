package io.domil.store

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.domil.store.viewModel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.domil.store.networking.User
import okio.Path.Companion.toPath
import java.io.File

class MainActivity : ComponentActivity() {

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
        clearCash()

        val barcodeScannerComposable: @Composable (
            onScanSuccess: (barcode: String) -> Unit
        ) -> Unit = { onScanSuccess ->
            QrScanner(onScanSuccess = onScanSuccess)
        }
        setContent {
            val dataStore = remember { createDataStore(this) }

            getUserData(dataStore = dataStore)

            val viewModel = AppViewModel(
                saveUserData = {
                    saveUserData(dataStore = dataStore, user = it)
                },
                savedUser = user,
                webPageRequestBarcode = ""
            )

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    //window.statusBarColor = colorScheme.primary.toArgb()
                    window.statusBarColor = Color(red = 255, green = 255, blue = 255).toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        true
                }
            }
            App(barcodeScanner = barcodeScannerComposable, viewModel = viewModel)
        }
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

    private fun createDataStore(context: Context): DataStore<Preferences> {

        return PreferenceDataStoreFactory.createWithPath {
            val dataStoreFileName = "storeAppLocalMemory.preferences_pb"
            context.filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
    }

    private fun clearCash() {
        deleteRecursive(cacheDir)
        deleteRecursive(codeCacheDir)
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.let {
                for (child in it) {
                    deleteRecursive(child)
                }
            }
        }
        fileOrDirectory.delete()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                0
            )
        }
    }
}