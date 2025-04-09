package io.domil.store.factory.stopActivityFeature.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.domil.store.factory.addTaskFeature.viewModel.SharedRepository
import io.domil.store.factory.main.view.FeatureListScreen
import io.domil.store.factory.stopActivityFeature.data.RemoteConnection
import io.domil.store.factory.stopActivityFeature.model.Reason
import io.domil.store.factory.stopActivityFeature.view.StopActivityScreen
import io.domil.store.data.Result
import io.domil.store.view.SnackBarActions
import io.domil.store.view.showLog
import kotlinx.coroutines.launch

/**
 * ViewModel for the Stop Activity screen.
 *
 * This ViewModel manages the state and logic for stopping an activity, including:
 *  - Fetching and displaying a list of reasons for stopping an activity.
 *  - Handling user selection of a reason.
 *  - Communicating with the remote server to stop the activity.
 *  - Managing loading state during the server call.
 *  - Displaying success or error messages via a Snackbar.
 *  - Handling screen navigation after a successful stop.
 */
class StopActivityViewModel : ViewModel() {
    // State holding the list of reasons and a loading flag
    val reasons = listOf(Reason(1, "تعمیر"), Reason(2, "تنظیم"), Reason(3, "نبودن کالا"))
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    var destinationScreen: Any = StopActivityScreen
    var currentScreen: Any = StopActivityScreen
    var screenChangePending by mutableStateOf(false)
        private set

    fun confirmStopActivity(
        selectedReasonId: Int
    ) {
        viewModelScope.launch {
            loading = true
            val result =
                RemoteConnection.stopActivity(SharedRepository.machineCode ?: -1, selectedReasonId)
            when (result) {
                is Result.Success -> {
                    loading = false
                    showLog("توقف با موفقیت ثبت شد", state, SnackBarActions.SUCCESS)
                    changeScreen(FeatureListScreen)
                }

                is Result.Error -> {
                    loading = false
                    showLog("مشکلی در ثبت توقف بوجود آمده است", state)
                }
            }
        }
    }

    fun changeScreen(screen: Any) {
        if (!screenChangePending) {
            destinationScreen = screen
            screenChangePending = true
        }
    }

    fun onScreenChanged() {
        currentScreen = destinationScreen
        screenChangePending = false
    }

}
