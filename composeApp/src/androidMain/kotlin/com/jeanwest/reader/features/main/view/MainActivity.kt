@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.FeatureLocation
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.local.features
import com.jeanwest.reader.data.local.mainTitle
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.IotHub
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.features.shared.AlertDialogWith2ButtonAndAppVersion
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButton
import com.jeanwest.reader.features.shared.AppBarWithNavigationButton
import com.jeanwest.reader.features.shared.BigButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.ExpandableCard
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.OpenActivityButton
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.User
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.fileNameToVersion
import com.jeanwest.reader.useCases.versionToVersionIntFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : ComponentActivity() {

    private val limitedFeatures = mutableListOf(
        "Inventory",
        //"StockDraftRequestCentralWarehouseFind"
    )

    lateinit var rf: RFID
    private lateinit var barcode: Barcode
    private var popupState = NotificationPopupHost()

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    @Inject
    lateinit var state: SnackbarHostState

    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var localStoreDatabase: LocalStoreDatabase

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        exceptionHandler()
        clearAppCash()
        checkAndRequestPermissions()
        init()
        setContent {
            Page(viewModel = viewModel)
        }
        onBackPressedDispatcher.addCallback(this) {
            localStoreDatabase.disconnectFromServer()
            rf.disconnect()
            finish()
        }
    }

    private fun init() {

        viewModel.isOfflineMode = memory.user.isLocalMode
        barcode = Barcode(this)
        rf = RFID(this, viewModel.state) {}
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.loading = true
            delay(1000)
            rf.enable()
            viewModel.loading = false
        }

        if (!memory.device.isExist) {
            val intent = Intent(this@MainActivity, DeviceRegister::class.java)
            intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else if (!memory.user.isExist) {
            startService(Intent(this, IotHub::class.java))
            viewModel.loginMode = true
        } else {
            viewModel.userFullName = memory.user.name
            getUserFeaturesList()
            startService(Intent(this, IotHub::class.java))
        }

        viewModel.appVersion = packageManager.getPackageInfo(packageName, 0).versionName

        viewModel.userWarehouse =
            memory.user.warehouses[memory.user.warehouseCode.toString()] ?: "0"
        viewModel.currentWarehouseCodeIsDepo = memory.user.currentWarehouseCodeIsDepo

        viewModel.userWarehousesList.clear()
        viewModel.userWarehousesList.addAll(memory.user.warehousesTitlesSorted)

        viewModel.userWarehousesListsOnClick = { warehouseTitle ->
            memory.user.warehouseCode =
                memory.user.warehouses.entries.firstOrNull { it.value == warehouseTitle }?.key?.toInt()
                    ?: 0
            viewModel.userWarehouse = warehouseTitle
            viewModel.currentWarehouseCodeIsDepo = memory.user.currentWarehouseCodeIsDepo
            if (!memory.user.isLocalMode) {
                getUserFeaturesList()
            }
            viewModel.isStoreMode = memory.user.isStoreUser
            memory.setAppDataImmediately()
        }

        viewModel.onFeatureButtonClick = { feature, data ->
            Intent(this@MainActivity, feature).apply {
                if (!data.isNullOrEmpty()) {
                    putExtra("data", data)
                    Log.e("main", data)
                }
                this@MainActivity.startActivity(this)
            }
        }
        viewModel.onSyncButtonClick = {
            api.syncServerToLocalWarehouse(onSuccess = {
                viewModel.openSendAndReceiveDialog = true
            })
        }
        viewModel.onLogOutButtonClick = {
            memory.user = User()
            viewModel.password = ""
            memory.setAppDataImmediately()
            viewModel.loginMode = true
        }
        viewModel.onLogInButtonClick = {
            login()
        }
    }

    private fun getUserFeaturesList() {

        viewModel.isStoreMode = memory.user.isStoreUser

        if (memory.user.isLocalMode) {
            viewModel.featuresList.clear()
            viewModel.featuresList.addAll(features.filter {
                it.accessKey in listOf(
                    "PrintPriceLabel",
                    "CreateStockDraftFromStoreWarehouseToStore",
                    "ConfirmStockDraft"
                )
            })
        } else {

            viewModel.featuresList.clear()
            if (memory.user.isStoreUser) {
                if (memory.user.currentWarehouseCodeIsDepo) {
                    viewModel.featuresList.addAll(features.filter {
                        FeatureLocation.STORE_WAREHOUSE in it.featureLocationsArray && it.accessKey in memory.user.access
                    })
                } else {
                    viewModel.featuresList.addAll(features.filter {
                        FeatureLocation.STORE in it.featureLocationsArray && it.accessKey in memory.user.access
                    })
                }
            } else {
                viewModel.featuresList.addAll(features.filter {
                    FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                            it.accessKey in memory.user.access &&
                            it.accessKey !in limitedFeatures
                })
            }
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.state.currentSnackbarData?.dismiss()
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestPermissions() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                2
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                2
            )
        }

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
                3
            )
        }
    }

    private fun login() {

        if (viewModel.username == "" || viewModel.password == "" || (viewModel.locationCode.toIntOrNull() == null && viewModel.isOfflineMode)) {
            showLog("لطفا تمام مقادیر را به طور صحیح وارد کنید", state)
        } else {

            if (viewModel.isOfflineMode) {

                viewModel.loading = true

                localStoreDatabase.userLogin(
                    username = viewModel.username,
                    password = viewModel.password,
                    locationCode = viewModel.locationCode.toInt(),
                    onSuccess = { user ->

                        memory.user = user
                        memory.user.isLocalMode = true
                        viewModel.isStoreMode = memory.user.isStoreUser
                        memory.setAppDataImmediately()

                        viewModel.currentWarehouseCodeIsDepo =
                            memory.user.currentWarehouseCodeIsDepo

                        viewModel.userFullName = user.name
                        viewModel.userWarehousesList.clear()
                        viewModel.userWarehousesList.addAll(user.warehousesTitlesSorted)
                        viewModel.userWarehouse =
                            user.warehouses[user.warehouseCode.toString()] ?: "0"

                        viewModel.loginMode = false
                        getUserFeaturesList()
                        viewModel.loading = false
                    },
                    onError = {
                        localStoreDatabase.disconnectFromServer()
                        viewModel.loading = false
                    })
            } else {

                viewModel.loading = true

                api.userLogin(
                    username = viewModel.username,
                    password = viewModel.password,
                    onSuccess = { user ->

                        memory.user = user
                        viewModel.isStoreMode = memory.user.isStoreUser
                        memory.user.isLocalMode = false

                        if (memory.user.warehouseCode.toString() !in user.warehouses) {
                            memory.user.warehouseCode =
                                user.warehouses.keys.toList()[0].toIntOrNull() ?: 0
                        }

                        memory.setAppDataImmediately()

                        api.setHeader(user)
                        viewModel.userWarehouse =
                            user.warehouses[user.warehouseCode.toString()] ?: "0"
                        viewModel.currentWarehouseCodeIsDepo =
                            memory.user.currentWarehouseCodeIsDepo
                        viewModel.userFullName = user.name
                        viewModel.userWarehousesList.clear()
                        viewModel.userWarehousesList.addAll(user.warehousesTitlesSorted)
                        getUserFeaturesList()
                        getWarehouseLists()
                    },
                    onError = {
                        viewModel.loading = false
                    })
            }
        }
    }

    private fun getWarehouseLists() {
        viewModel.loading = true
        api.getWarehousesLists(
            { destination, sortedDestinations, departmentWarehousesLists, departmentTitles ->
                memory.erpData.warehousesIDsToTitles.clear()
                memory.erpData.warehousesIDsToTitles.putAll(destination)
                memory.erpData.sortedWarehousesList.clear()
                memory.erpData.sortedWarehousesList.addAll(sortedDestinations)
                memory.erpData.departmentWarehouses.clear()
                memory.erpData.departmentWarehouses.putAll(departmentWarehousesLists)
                memory.erpData.departments.clear()
                memory.erpData.departments.putAll(departmentTitles)
                memory.erpData.warehousesTitlesToIDs.clear()
                getDriversList()
            },
            {
                viewModel.loading = false
            })
    }

    private fun getDriversList() {
        viewModel.loading = true
        api.getDriversLogistics({
            memory.erpData.drivers.clear()
            memory.erpData.drivers.putAll(it)
            getPrinters()
        }, {
            viewModel.loading = false
        })
    }

    private fun getPrinters() {
        viewModel.loading = true
        api.getPrintersList({
            memory.erpData.printers.clear()
            memory.erpData.printers.putAll(it)
            getPackageTypes()
        }, {
            viewModel.loading = false
        })
    }

    private fun getPackageTypes() {
        viewModel.loading = true
        api.getPackageTypes({
            memory.erpData.packageTypes.clear()
            memory.erpData.packageTypes.putAll(it)
            getStockDraftRequestTypes()
        }, {
            viewModel.loading = false
        })
    }

    private fun getStockDraftRequestTypes() {

        viewModel.loading = true
        api.getStockDraftRequestType({
            memory.erpData.stockDraftRequestTypes.clear()
            memory.erpData.stockDraftRequestTypes.putAll(it)
            memory.setAppDataImmediately()
            checkUpdate()
        }, {
            viewModel.loading = false
        })
    }

    private fun checkUpdate() {
        api.getAppVersions(onSuccess = {

            val desiredVersionIntFormat = it.keys.toList().maxOrNull()
            val desiredVersion = it[it.keys.toList().maxOrNull()]?.fileNameToVersion()
            val currentVersionIntFormat = packageManager.getPackageInfo(
                packageName,
                0
            ).versionName.versionToVersionIntFormat()
            Log.e(
                "update:",
                "desired version: $desiredVersionIntFormat current version: $currentVersionIntFormat"
            )
            if (desiredVersion != null && desiredVersionIntFormat != null && currentVersionIntFormat != null) {
                if (currentVersionIntFormat < desiredVersionIntFormat) {
                    val intent = Intent(this, Update::class.java)
                    intent.putExtra("appVersion", desiredVersion)
                    startActivity(intent)
                }
            }

            viewModel.loginMode = false
            viewModel.loading = false
        }, onError = {
            viewModel.loading = false
        })
    }

    private fun clearAppCash() {
        viewModel.loading = true
        deleteRecursive(cacheDir)
        deleteRecursive(codeCacheDir)
        viewModel.loading = false
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
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun Page(viewModel: MainViewModel) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    if (viewModel.loginMode) LoginAppBar() else AppBarWithNavigationButton(
                        imageVector = Icons.Filled.Person,
                        title = mainTitle,
                        onNavigationButtonPressed = { viewModel.openAccountDialog = true }
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (viewModel.loginMode)
                            LoginContent(viewModel = viewModel)
                        else {
                            MainContent(viewModel = viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = { if (!viewModel.loginMode) MainBottomBar(viewModel = viewModel) }
            )
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {

    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else
            LazyColumn(
                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    if (viewModel.openAccountDialog) {
                        AlertDialogWith2ButtonAndAppVersion(
                            title = viewModel.userFullName,
                            appVersion = viewModel.appVersion,
                            btnConfirm = "خروج از حساب",
                            btnNotConfirm = "ارسال و دریافت",
                            btnNotConfirmOnClick = {
                                viewModel.openAccountDialog = false
                                viewModel.onSyncButtonClick()
                            },
                            btnConfirmOnClick = {
                                viewModel.openAccountDialog = false
                                viewModel.onLogOutButtonClick()
                            },
                            onDismiss = { viewModel.openAccountDialog = false })
                    }

                    if (viewModel.openSendAndReceiveDialog) {

                        AlertDialogWithHeadlineMediumButton(
                            title = "ارسال و دریافت با موفقیت انجام شد، همگام سازی اطلاعات چند دقیقه زمان می\u200Cبرد.",
                            btnTxt = "متوجه شدم",
                            btnOnClick = { viewModel.openSendAndReceiveDialog = false },
                            onDismiss = { viewModel.openSendAndReceiveDialog = false }
                        )
                    }

                    if (viewModel.isStoreMode) {

                        val numberOfRowsBeforeLastRow = (viewModel.featuresList.size / 4)
                        val numberOfFeaturesInLastRow = (viewModel.featuresList.size % 4)

                        for (rowIndex in 0 until numberOfRowsBeforeLastRow) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {

                                for (i in 0..3) {
                                    val it = viewModel.featuresList[rowIndex * 4 + i]
                                    OpenActivityButton(
                                        it.featureTitleResourceAddress,
                                        it.featureIconResourceAddress
                                    ) {
                                        viewModel.onFeatureButtonClick(it.featureClass, null)
                                    }
                                }
                            }
                        }

                        if (numberOfFeaturesInLastRow != 0) {

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {

                                for (i in 0 until numberOfFeaturesInLastRow) {
                                    val it =
                                        viewModel.featuresList[numberOfRowsBeforeLastRow * 4 + i]
                                    OpenActivityButton(
                                        it.featureTitleResourceAddress,
                                        it.featureIconResourceAddress
                                    ) {
                                        viewModel.onFeatureButtonClick(it.featureClass, null)
                                    }
                                }

                                for (i in 0 until (4 - numberOfFeaturesInLastRow)) {
                                    Box(modifier = Modifier.size(80.dp))
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(128.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        ExpandableCard("cartonsFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("stockDraftsFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("shelfFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("driverFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("transferFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("mojoodiReviewFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("requestFeatures", viewModel.featuresList, viewModel)
                        ExpandableCard("banimodeFeatures", viewModel.featuresList, viewModel)
                        Spacer(
                            modifier = Modifier
                                .height(128.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
    }
}

@Composable
fun MainBottomBar(viewModel: MainViewModel) {

    Box(
        modifier = Modifier
            //.shadow(6.dp, RoundedCornerShape(0.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .height(72.dp)
            //.align(Alignment.BottomCenter),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            FilterDropDownListWithSearch(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_location_city_24),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp)
                    )
                },
                text = {
                    Text(
                        text = (viewModel.userWarehouse),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 16.dp)
                    )
                },
                onClick = { warehouseTitle ->
                    viewModel.userWarehousesListsOnClick(warehouseTitle)
                },
                values = viewModel.userWarehousesList
            )
        }
    }
}

@Composable
fun LoginContent(viewModel: MainViewModel) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {

            SimpleTextField(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                    .fillMaxWidth(),
                hint = "نام کاربری خود را وارد کنید",
                onValueChange = { viewModel.username = it },
                value = viewModel.username,
            )

            SimpleTextField(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                hint = "رمز عبور خود را وارد کنید",
                onValueChange = { viewModel.password = it },
                value = viewModel.password,
                visualTransformation = PasswordVisualTransformation()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            ) {
                var isChecked by remember { mutableStateOf(false) }

                Text(
                    text = "ورود بدون اینترنت",
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .layoutId(Alignment.Start)
                        .padding(start = 24.dp, end = 24.dp)
                )
                Switch(
                    checked = viewModel.isOfflineMode,
                    onCheckedChange = { viewModel.isOfflineMode = it },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .alignByBaseline()
                )
                if (viewModel.isOfflineMode) {
                    SimpleTextField(
                        modifier = Modifier
                            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                        hint = "کد فروشگاه",
                        onValueChange = {
                            viewModel.locationCode = it
                        },
                        value = viewModel.locationCode,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }

            BigButton(text = "ورود") {
                viewModel.onLogInButtonClick()
            }
        }
    }
}

@Composable
fun LoginAppBar() {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ورود به حساب کاربری", textAlign = TextAlign.Center,
                )
            }
        },
    )
}

@Preview
@Composable
fun Preview() {
    Box(
        modifier = Modifier
            .height(20.dp)
            .width(60.dp)
    ) {
        Switch(
            checked = true,
            onCheckedChange = { },
        )
    }

}