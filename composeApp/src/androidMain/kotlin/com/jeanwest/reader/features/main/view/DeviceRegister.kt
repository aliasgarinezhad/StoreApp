package com.jeanwest.reader.features.main.view

import android.os.Build
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.view.BigButton
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.SimpleTextField
import com.jeanwest.reader.models.Device
import com.jeanwest.reader.useCases.Barcode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity responsible for registering a device to an IoT hub.
 *
 * This activity handles user authentication, device registration, and saving device information locally.
 * It utilizes Jetpack Compose for UI rendering and interacts with an API for backend operations.
 *
 * The registration process involves two main steps:
 * 1. **User Authentication:** The user enters their username and password to obtain an authentication token.
 * 2. **Device Registration:** The user enters the device serial number and selects the device's location (warehouse)
 *    from a list. The device is then registered with the IoT hub, and its ID, IoT token, serial number, and location
 *    are saved to shared preferences for future use.
 *
 * The UI is built using Jetpack Compose and consists of two main screens:
 * - **Login Screen:** Prompts the user for their username and password.
 * - */
@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class DeviceRegister : ComponentActivity() {

    private var password by mutableStateOf("")
    private var username by mutableStateOf("")

    @Inject
    lateinit var state: SnackbarHostState
    private var advanceSettingToken = ""
    private var loginMode by mutableStateOf(true)
    private var deviceSerialNumber by mutableStateOf("")
    var loading by mutableStateOf(false)
    private lateinit var barcode: Barcode

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        init()
        setContent { Page() }
    }

    private fun init() {


        barcode = Barcode(this)
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    override fun onPause() {
        super.onPause()
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    private fun advanceUserAuthenticate() {
        loading = true
        api.operatorLogin(username, password, {
            advanceSettingToken = it
            loginMode = false
            loading = false
        }, {
            loading = false
        })
    }

    private fun registerDeviceToIotHub() {

        loading = true

        api.registerDevice(
            advanceSettingToken,
            deviceSerialNumber,
            { deviceId, iotToken ->
                memory.device = Device(
                    id = deviceId,
                    token = iotToken,
                    serialNumber = deviceSerialNumber
                )
                memory.setAppDataImmediately()
                loading = false
                finish()
            },
            {
                loading = false
            })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            back()
        }
        return true
    }

    private fun back() {
        if (loginMode) {
            finish()
        } else {
            loginMode = true
        }
    }

    @Composable
    fun Page() {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = { if (loginMode) AppBar() else AppBar2() },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            if (loginMode) Content() else Content2()
                        }
                    },
                    snackbarHost = { ErrorSnackBar(state) },
                )
            }
        }
    }

    @Composable
    fun Content() {

        Column {
            if (loading) {
                LoadingCircularProgressIndicator(isDataLoading = loading)
            } else {
                SimpleTextField(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                        .fillMaxWidth(),
                    hint = "نام کاربری خود را وارد کنید",
                    onValueChange = { username = it },
                    value = username,
                )

                SimpleTextField(
                    modifier = Modifier
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = 24.dp
                        )
                        .fillMaxWidth(),
                    hint = "رمز عبور خود را وارد کنید",
                    onValueChange = { password = it },
                    value = password,
                    visualTransformation = PasswordVisualTransformation()
                )

                BigButton(text = "ورود") {
                    advanceUserAuthenticate()
                }
            }
        }
    }

    @Composable
    fun Content2() {

        Column {

            if (loading) {
                LoadingCircularProgressIndicator(isDataLoading = loading)
            } else {
                //deviceSerialNumber = Build.getSerial().toString()
                SimpleTextField(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    hint = "شماره سریال دستگاه را وارد کنید",
                    onValueChange = { deviceSerialNumber = it },
                    value = deviceSerialNumber,
                )

                BigButton(text = "ثبت اطلاعات") {
                    registerDeviceToIotHub()
                }
            }
        }
    }

    @Composable
    fun AppBar2() {

        TopAppBar(

            title = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 0.dp, end = 60.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.deviceRegister), textAlign = TextAlign.Center,
                    )
                }
            },
            navigationIcon = {
                Box(
                    modifier = Modifier.width(60.dp)
                ) {
                    IconButton(
                        onClick = { back() },
                        modifier = Modifier.testTag("back")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                            contentDescription = ""
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun AppBar() {

        TopAppBar(

            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, end = 60.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.deviceRegister), textAlign = TextAlign.Center,
                    )
                }
            },
            navigationIcon = {
                Box(
                    modifier = Modifier.width(60.dp)
                ) {
                    IconButton(
                        onClick = { back() },
                        modifier = Modifier.testTag("back")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                            contentDescription = ""
                        )
                    }
                }
            }
        )
    }
}