package com.jeanwest.reader.features.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.shared.BigButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.useCases.Barcode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
    private var deviceId by mutableStateOf("")
    private var iotToken by mutableStateOf("")
    private var deviceLocation by mutableStateOf("انتخاب انبار")
    private val warehousesList = mutableStateMapOf<String, String>()
    var loading by mutableStateOf(false)
    private var sortedLocations = mutableListOf<String>()
    private lateinit var barcode: Barcode

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        state.currentSnackbarData?.dismiss()
        requestPermissions()
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
            getLocations()
        }, {
            loading = false
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerDeviceToIotHub() {

        loading = true

        api.registerDevice(
            advanceSettingToken,
            deviceSerialNumber,
            { deviceId, iotToken ->
                this.deviceId = deviceId
                this.iotToken = iotToken
                saveToMemory()
                loading = false
                val nextActivityIntent = Intent(this, MainActivity::class.java)
                intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(nextActivityIntent)
            },
            {
                loading = false
            })
    }

    private fun getLocations() {
        loading = true
        api.getWarehousesLists(
            { destination, sortedDestinations, _, _ ->
                warehousesList.clear()
                warehousesList.putAll(destination)
                sortedLocations = sortedDestinations.toMutableList()
                loading = false
            },
            {
                loading = false
            },
            advanceSettingToken
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveToMemory() {

        val memory = PreferenceManager.getDefaultSharedPreferences(this)
        val memoryEditor = memory.edit()

        memoryEditor.putString("deviceId", deviceId)
        memoryEditor.putInt(
            "deviceLocationCode",
            (warehousesList.entries.firstOrNull { it.value == deviceLocation }?.key?.toInt() ?: 0)
        )
        memoryEditor.putString("deviceLocation", deviceLocation)
        memoryEditor.putString("iotToken", iotToken)
        memoryEditor.putString("deviceSerialNumber", deviceSerialNumber)
        memoryEditor.apply()
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                0
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
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

    @RequiresApi(Build.VERSION_CODES.O)
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {

                    FilterDropDownListWithSearch(
                        modifier = Modifier
                            .padding(start = 24.dp, bottom = 24.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_location_city_24),
                                contentDescription = "",
                                tint = primaryLight,
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        text = {
                            Text(
                                text = deviceLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        onClick = {
                            deviceLocation = it
                        },
                        values = sortedLocations
                    )
                }

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
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.deviceRegister), textAlign = TextAlign.Center,
                    )
                }
            },
        )
    }
}