package com.jeanwest.reader.features.main.mainPage.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanwest.reader.FeatureLocation
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.local.features
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.data.remote.LocalStoreDatabase
import com.jeanwest.reader.factory.main.model.Feature
import com.jeanwest.reader.features.main.mainPage.view.NavigationEvents
import com.jeanwest.reader.features.main.view.Update
import com.jeanwest.reader.features.shared.showLog
import com.jeanwest.reader.models.User
import com.jeanwest.reader.useCases.Barcode
import com.jeanwest.reader.useCases.ExceptionHandler
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.useCases.fileNameToVersion
import com.jeanwest.reader.useCases.versionToVersionIntFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.reflect.KClass


/**
 * [MainViewModel] is the ViewModel for the main screen of the application.
 * It manages the application's core states, user authentication, feature access,
 * and communication with external services and databases.
 *
 * @property state The state of the SnackbarHost for displaying snackbar messages.
 * @property memory The shared preferences instance for persistent data storage.
 * @property context The application context.
 * @property localStoreDatabase The local database instance for offline mode operations.
 * @property api The API service for network communication.
 */
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(
    val state: SnackbarHostState,
    val memory: SharedPreference,
    @ApplicationContext val context: Context,
    val localStoreDatabase: LocalStoreDatabase,
    val api: API,
) : ViewModel() {
    var isStoreMode by mutableStateOf(false)
    var appVersion: String = ""
    var isOfflineMode by mutableStateOf(false)
    var loginMode by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var locationCode by mutableStateOf("")
    var loading by mutableStateOf(false)
    var userFullName by mutableStateOf("")
    var userWarehouse by mutableStateOf("")
    private var currentWarehouseCodeIsDepo by mutableStateOf(false)
    var openAccountDialog by mutableStateOf(false)
    var openSendAndReceiveDialog by mutableStateOf(false)
    var featuresList: SnapshotStateList<Feature> = features.toMutableStateList()
    var userWarehousesList: SnapshotStateList<String> = mutableStateListOf()
    private val limitedFeatures = mutableListOf("Inventory")
    var rf: RFID
    private val _navigationEvents = MutableSharedFlow<NavigationEvents>()
    val navigationEvents = _navigationEvents.asSharedFlow()
    private var barcode: Barcode

    init {

        exceptionHandler()

        isOfflineMode = memory.user.isLocalMode
        barcode = Barcode(context)
        rf = RFID(context, state) {}
        CoroutineScope(Dispatchers.Default).launch {
            loading = true
            delay(1000)
            rf.enable()
            clearAppCash()
            loading = false
        }

        if (!memory.user.isExist) {
            loginMode = true
        } else {
            userFullName = memory.user.name
            getUserFeaturesList()
        }

        appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

        userWarehouse =
            memory.user.warehouses[memory.user.warehouseCode.toString()] ?: "0"
        currentWarehouseCodeIsDepo = memory.user.currentWarehouseCodeIsDepo

        userWarehousesList.clear()
        userWarehousesList.addAll(memory.user.warehousesTitlesSorted)
    }

    fun userWarehousesListsOnClick(warehouseTitle: String) {
        memory.user.warehouseCode =
            memory.user.warehouses.entries.firstOrNull { it.value == warehouseTitle }?.key?.toInt()
                ?: 0
        userWarehouse = warehouseTitle
        currentWarehouseCodeIsDepo = memory.user.currentWarehouseCodeIsDepo
        if (!memory.user.isLocalMode) {
            getUserFeaturesList()
        }
        isStoreMode = memory.user.isStoreUser
        memory.setAppDataImmediately()
    }

    fun onFeatureButtonClick(feature: KClass<*>?, data: String?) {
        openActivity(feature!!, data)
    }

    fun onSyncButtonClick() {
        api.syncServerToLocalWarehouse(onSuccess = {
            openSendAndReceiveDialog = true
        })
    }

    fun onLogOutButtonClick() {
        memory.user = User()
        password = ""
        memory.setAppDataImmediately()
        loginMode = true
    }

    private fun openActivity(activity: KClass<*>, data: String?) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvents.OpenActivity(activity, data))
        }
    }

    private fun openService(service: Class<*>) {

        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvents.OpenService(service))
        }
    }

    private fun getUserFeaturesList() {

        isStoreMode = memory.user.isStoreUser

        if (memory.user.isLocalMode) {
            featuresList.clear()
            featuresList.addAll(features.filter {
                it.accessKey in listOf(
                    "PrintPriceLabel",
                    "CreateStockDraftFromStoreWarehouseToStore",
                    "ConfirmStockDraft"
                )
            })
        } else {

            featuresList.clear()
            if (memory.user.isStoreUser) {
                if (memory.user.currentWarehouseCodeIsDepo) {
                    featuresList.addAll(features.filter {
                        FeatureLocation.STORE_WAREHOUSE in it.locationsArray && it.accessKey in memory.user.access
                    })
                } else {
                    featuresList.addAll(features.filter {
                        FeatureLocation.STORE in it.locationsArray && it.accessKey in memory.user.access
                    })
                }
            } else {
                featuresList.addAll(features.filter {
                    FeatureLocation.CENTRAL_WAREHOUSE in it.locationsArray &&
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

    fun onPauseActivity() {
        state.currentSnackbarData?.dismiss()
        if (!barcode.isEnabled) {
            barcode.enable()
        }
    }

    fun onResumeActivity() {
        state.currentSnackbarData?.dismiss()
        if (barcode.isEnabled) {
            barcode.disable()
        }
    }

    override fun onCleared() {
        super.onCleared()
        rf.disconnect()
        localStoreDatabase.disconnectFromServer()
    }

    fun login() {

        if (username == "" || password == "" || (locationCode.toIntOrNull() == null && isOfflineMode)) {
            showLog("لطفا تمام مقادیر را به طور صحیح وارد کنید", state)
        } else {

            if (isOfflineMode) {

                loading = true

                localStoreDatabase.userLogin(
                    username = username,
                    password = password,
                    locationCode = locationCode.toInt(),
                    onSuccess = { user ->

                        memory.user = user
                        memory.user.isLocalMode = true
                        isStoreMode = memory.user.isStoreUser
                        memory.setAppDataImmediately()

                        currentWarehouseCodeIsDepo =
                            memory.user.currentWarehouseCodeIsDepo

                        userFullName = user.name
                        userWarehousesList.clear()
                        userWarehousesList.addAll(user.warehousesTitlesSorted)
                        userWarehouse =
                            user.warehouses[user.warehouseCode.toString()] ?: "0"

                        loginMode = false
                        getUserFeaturesList()
                        loading = false
                    },
                    onError = {
                        localStoreDatabase.disconnectFromServer()
                        loading = false
                    })
            } else {

                loading = true

                api.userLogin(
                    username = username,
                    password = password,
                    onSuccess = { user ->

                        memory.user = user
                        isStoreMode = memory.user.isStoreUser
                        memory.user.isLocalMode = false

                        if (memory.user.warehouseCode.toString() !in user.warehouses) {
                            memory.user.warehouseCode =
                                user.warehouses.keys.toList()[0].toIntOrNull() ?: 0
                        }

                        memory.setAppDataImmediately()

                        api.setHeader(user)
                        userWarehouse =
                            user.warehouses[user.warehouseCode.toString()] ?: "0"
                        currentWarehouseCodeIsDepo =
                            memory.user.currentWarehouseCodeIsDepo
                        userFullName = user.name
                        userWarehousesList.clear()
                        userWarehousesList.addAll(user.warehousesTitlesSorted)
                        getUserFeaturesList()
                        getWarehouseLists()
                    },
                    onError = {
                        loading = false
                    })
            }
        }
    }

    private fun getWarehouseLists() {
        loading = true
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
                loading = false
            })
    }

    private fun getDriversList() {
        loading = true
        api.getDriversLogistics({
            memory.erpData.drivers.clear()
            memory.erpData.drivers.putAll(it)
            getPrinters()
        }, {
            loading = false
        })
    }

    private fun getPrinters() {
        loading = true
        api.getPrintersList({
            memory.erpData.printers.clear()
            memory.erpData.printers.putAll(it)
            getPackageTypes()
        }, {
            loading = false
        })
    }

    private fun getPackageTypes() {
        loading = true
        api.getPackageTypes({
            memory.erpData.packageTypes.clear()
            memory.erpData.packageTypes.putAll(it)
            getStockDraftRequestTypes()
        }, {
            loading = false
        })
    }

    private fun getStockDraftRequestTypes() {

        loading = true
        api.getStockDraftRequestType({
            memory.erpData.stockDraftRequestTypes.clear()
            memory.erpData.stockDraftRequestTypes.putAll(it)
            memory.setAppDataImmediately()
            checkUpdate()
        }, {
            loading = false
        })
    }

    private fun checkUpdate() {
        api.getAppVersions(onSuccess = {

            val desiredVersionIntFormat = it.keys.toList().maxOrNull()
            val desiredVersion = it[it.keys.toList().maxOrNull()]?.fileNameToVersion()
            val currentVersionIntFormat = context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName.versionToVersionIntFormat()
            Log.e(
                "update:",
                "desired version: $desiredVersionIntFormat current version: $currentVersionIntFormat"
            )
            if (desiredVersion != null && desiredVersionIntFormat != null && currentVersionIntFormat != null) {
                if (currentVersionIntFormat < desiredVersionIntFormat) {
                    openActivity(Update::class, null)
                }
            }

            loginMode = false
            loading = false
        }, onError = {
            loading = false
        })
    }

    private fun clearAppCash() {
        loading = true
        deleteRecursive(context.cacheDir)
        deleteRecursive(context.codeCacheDir)
        loading = false
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