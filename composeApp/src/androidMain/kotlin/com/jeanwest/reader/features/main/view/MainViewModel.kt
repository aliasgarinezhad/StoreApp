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