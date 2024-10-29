package io.domil.store

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import networking.GetProductData
import networking.Product
import networking.createHttpClient
import util.onError
import util.onSuccess
import java.io.File

class ManualRefillActivity : ComponentActivity() {

    private var fullName = ""
    private var username = ""
    private var token = ""

    //charge ui parameters
    private var loading by mutableStateOf(false)
    private var state = SnackbarHostState()

    // search ui parameters
    private var productCode by mutableStateOf("")
    private var searchUiList = mutableStateListOf<Product>()
    private var filteredUiList = mutableStateListOf<Product>()
    private var colorFilterValues = mutableStateListOf("همه رنگ ها")
    private var sizeFilterValues = mutableStateListOf("همه سایز ها")
    private var colorFilterValue by mutableStateOf("همه رنگ ها")
    private var sizeFilterValue by mutableStateOf("همه سایز ها")
    private var storeFilterValue = 0
    private var isCameraOn by mutableStateOf(false)
    private lateinit var client: GetProductData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMemory()
        /*if (token == "") {
            val intent = Intent(this, UserLoginActivity::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }*/
        checkPermission()
        clearCash()
        client = GetProductData(token, createHttpClient())
        setContent {
            Page(
                state = state,
                sizeFilterValue = sizeFilterValue,
                sizeFilterValues = sizeFilterValues,
                colorFilterValue = colorFilterValue,
                colorFilterValues = colorFilterValues,
                onImeAction = { onImeAction() },
                onScanButtonClick = { openCamera() },
                onTextValueChange = { productCode = it },
                onSizeFilterValueChange = { sizeFilterValue = it },
                onColorFilterValueChange = { colorFilterValue = it },
                onBottomBarButtonClick = { openCamera() },
                loading = loading,
                isCameraOn = isCameraOn,
                textFieldValue = productCode,
                uiList = filteredUiList
            )
        }
    }

    private fun onImeAction() {
        isCameraOn = false
        getSimilarProducts()
    }

    private fun openCamera() {
        isCameraOn = true
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

    private fun loadMemory() {
        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        username = memory.getString("username", "") ?: ""
        token = memory.getString("accessToken", "") ?: ""
        fullName = memory.getString("userFullName", "") ?: ""
        storeFilterValue = memory.getInt("userLocationCode", 0)
        storeFilterValue = 68
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0MDE2IiwibmFtZSI6Itiq2LPYqiBSRklEINiq2LPYqiBSRklEIiwiZGVwSWQiOjY4LCJ3YXJlaG91c2VzIjpbeyJXYXJlSG91c2VfSUQiOjE3MDYsIldhcmVIb3VzZVR5cGVzX0lEIjoxfSx7IldhcmVIb3VzZV9JRCI6MTcwNywiV2FyZUhvdXNlVHlwZXNfSUQiOjJ9LHsiV2FyZUhvdXNlX0lEIjoxOTE4LCJXYXJlSG91c2VUeXBlc19JRCI6Mn0seyJXYXJlSG91c2VfSUQiOjE5MTksIldhcmVIb3VzZVR5cGVzX0lEIjoxfV0sInJvbGVzIjpbInVzZXIiXSwic2NvcGVzIjpbImVycCJdLCJpYXQiOjE3MzAxMzE1MzgsImV4cCI6MjMxMDczOTUzOCwiYXVkIjoiZXJwIn0.5_2b4yTWVGAv3LSuaERsdONtSUMRqEg-vZq6wxD1rMo"
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
        if (productCode == "کد محصول") {
            CoroutineScope(Dispatchers.Default).launch {
                state.showSnackbar(
                    "لطفا کد محصول را وارد کنید.",
                    null,
                    SnackbarDuration.Long
                )
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            loading = true
            client.getSimilarProductsByBarcode(productCode, storeFilterValue)
                .onSuccess { responseBarcode ->
                    if (responseBarcode.isEmpty()) {
                        client.getSimilarProductsBySearchCode(productCode, storeFilterValue)
                            .onSuccess { responseSearchCode ->
                                if (responseSearchCode.isEmpty()) {
                                    println("request body = 1st body is empty")
                                    loading = false
                                } else {
                                    clear()
                                    searchUiList.addAll(responseSearchCode)
                                    filterUiList()
                                    loading = false
                                }
                            }.onError {
                                println("request body = error1")
                                loading = false
                            }
                        loading = false
                    } else {
                        println("response: $responseBarcode")
                        clear()
                        searchUiList.clear()
                        searchUiList.addAll(responseBarcode)
                        filterUiList()
                        loading = false
                    }
                }.onError {
                    client.getSimilarProductsBySearchCode(productCode, storeFilterValue)
                        .onSuccess { responseSearchCode ->
                            if (responseSearchCode.isEmpty()) {
                                println("request body = 1st body is empty")
                                loading = false
                            } else {
                                clear()
                                searchUiList.addAll(responseSearchCode)
                                filterUiList()
                                loading = false
                            }
                        }.onError {
                            loading = false
                            println("request body = error1")
                        }
                    loading = false
                    println("request body = error2 $it")
                }
        }
    }

    private fun getScannedProductProperties() {
        CoroutineScope(Dispatchers.IO).launch {
            loading = true
            client.getScannedProductProperties(productCode)
                .onSuccess {
                    clear()
                    searchUiList.addAll(it)
                    filterUiList()
                }
                .onError {
                }
            loading = false
        }
    }
}