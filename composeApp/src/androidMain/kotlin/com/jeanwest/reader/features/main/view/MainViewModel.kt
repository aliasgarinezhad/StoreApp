package com.jeanwest.reader.features.main.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.jeanwest.reader.models.Feature
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.local.features
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The main ViewModel for the application.  This ViewModel holds the application's
 * global state and handles user interactions that affect the UI across multiple screens.
 *
 * It uses Jetpack Compose's state management to observe and react to changes in the UI.
 *
 * @property state The [SnackbarHostState] used to display snackbars across the app. Injected using Dagger Hilt.
 * @property isStoreMode A [Boolean] flag indicating if the app is in "store" mode.  This likely
 *  controls which features are available or how data is processed.
 * @property appVersion A [String] representing the current version of the application.  Initialized to an empty string.
 * @property isOfflineMode A [Boolean] flag indicating whether the app is currently operating offline.
 * @property loginMode A [Boolean] flag indicating whether the app is in the login screen.
 * @property username A [String] holding the user's entered username during login.
 * @property password A [String] holding the user's entered password during login.
 * @property locationCode A [String] for a location identifier, likely used for data filtering or context.
 * @property loading A [Boolean] flag indicating whether a long-running operation (e.g., network request, database operation) is in progress.  Used to show loading indicators.
 * @property userFullName A [String] storing the full name of the logged-in user.
 * @property userWarehouse A [String] representing the user's assigned warehouse.
 * @property currentWarehouseCodeIsDepo A [Boolean] flag indicating if the current warehouse is a "depot".   */
@AndroidEntryPoint
class MainViewModel @Inject constructor(
    @set: Inject
    var state: SnackbarHostState

) : ComponentActivity() {
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
    var currentWarehouseCodeIsDepo by mutableStateOf(false)
    var openAccountDialog by mutableStateOf(false)
    var openSendAndReceiveDialog by mutableStateOf(false)
    var featuresList: SnapshotStateList<Feature> = features.toMutableStateList()
    var onFeatureButtonClick: (featureClass: Class<*>, data: String?) -> Unit = { _: Class<*>, _: String? -> }
    var onSyncButtonClick: () -> Unit = {}
    var onLogOutButtonClick: () -> Unit = {}
    var onLogInButtonClick: () -> Unit = {}
    var userWarehousesListsOnClick: (warehouseTitle: String) -> Unit = {}
    var userWarehousesList: SnapshotStateList<String> = mutableStateListOf()
    @Inject
    lateinit var memory: SharedPreference
}