package io.domil.store.factory.addTaskFeature.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.domil.store.factory.addTaskFeature.data.RemoteConnection
import io.domil.store.factory.addTaskFeature.model.Product
import io.domil.store.factory.addTaskFeature.model.UserTask
import io.domil.store.factory.addTaskFeature.view.EnterDateAndNumberScreen
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import io.domil.store.factory.main.view.FeatureListScreen
import io.domil.store.view.showLog
import io.domil.store.data.onError
import io.domil.store.data.onSuccess
import io.domil.store.view.NotificationPopupHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object SharedRepository {
    var machineCode: Int? = 0
}

/**
 * ViewModel for the "Add Task" feature in the factory application.
 *
 * This class manages the UI state and logic for adding a new task to a user's schedule.
 * It interacts with the remote API to fetch product lines and submit the user's task.
 *
 * @property popupHost Manages the display of pop-up notifications.
 * @property textFieldValue Holds the current value of the text field for numeric input (e.g., number of items).
 * @property loading Indicates whether a network request is in progress.
 * @property state The state of the SnackbarHost for displaying temporary messages.
 * @property destinationScreen Represents the screen to navigate to.
 * @property currentScreen Represents the currently displayed screen.
 * @property screenChangePending Flags whether a screen change is pending.
 * @property userTask The data model representing the user's task details.
 * @property products A list of available product lines fetched from the API.
 */
class FactoryAddTaskViewModel {

    val popupHost = NotificationPopupHost()
    var textFieldValue by mutableStateOf("")

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    var destinationScreen: Any = ShowProductionLinesScreen
    var currentScreen: Any = ShowProductionLinesScreen
    var screenChangePending by mutableStateOf(false)
        private set

    var userTask by mutableStateOf(UserTask())
    var products = mutableListOf<Product>()

    fun getProductionLines() {
        loading = true
        CoroutineScope(Default).launch {
            RemoteConnection.getProductionOrders().onSuccess {
                println("navid body: $it")
                products.clear()
                it.forEach { productLineApi ->

                    val tasks = mutableMapOf<String, Long>()
                    val sizes = mutableMapOf<String, Int>()
                    productLineApi.operationItems.forEach { operationItem ->
                        tasks[operationItem.operation] = operationItem.productionOrderOperationId
                        operationItem.pieces.forEach { size ->
                            sizes[size.size] = size.sizeCode
                        }
                    }

                    products.add(
                        Product(
                            lineID = productLineApi.productionOrderId,
                            name = productLineApi.partName,
                            style = productLineApi.styleN,
                            color = productLineApi.colorCodeF,
                            colorHex = productLineApi.colorHex,
                            tasks = tasks,
                            part = productLineApi.part,
                            sizes = sizes,
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    loading = false
                }
            }.onError {

                withContext(Dispatchers.Main) {
                    loading = false
                }
            }
        }
    }

    fun onProductLineClick(product: Product) {
        println("onProductLineClick")
        userTask = UserTask(product = product)
        textFieldValue = ""
        changeScreen(SelectTaskScreen)
    }

    fun changeScreen(screen: Any) {
        if(!screenChangePending) {
            destinationScreen = screen
            screenChangePending = true
        }
    }

    fun onScreenChanged() {
        currentScreen = destinationScreen
        screenChangePending = false
    }

    fun onTaskClick(task: String) {
        userTask = userTask.copy(task = task, taskId = userTask.product.tasks[task] ?: 0L)
        changeScreen(EnterDateAndNumberScreen)
    }

    fun onStartHourChanged(hour: Int) {
        userTask = userTask.copy(startHour = hour)
    }

    fun onStartMinuteChanged(minute: Int) {
        userTask = userTask.copy(startMinute = minute)
    }

    fun onEndHourChanged(hour: Int) {
        userTask = userTask.copy(endHour = hour)
    }

    fun onEndMinuteChanged(minute: Int) {
        userTask = userTask.copy(endMinute = minute)
    }

    fun onSizeChanged(size: String) {
        userTask = userTask.copy(size = size, sizeCode = userTask.product.sizes[size] ?: 0)
    }

    fun onNumberChanged(number: String) {
        textFieldValue = number
        if (number.toIntOrNull() == null) {
            if (textFieldValue != "") showLog("لطفا مقدار عددی وارد کنید.", state)
            userTask = userTask.copy(number = 0)
        } else {
            userTask = userTask.copy(number = number.toInt())
        }
    }

    fun onAddTaskButtonClick() {

        if (userTask.sizeCode == 0) {
            showLog("لطفا سایز را انتخاب کنید.", state = state)
        } else if(SharedRepository.machineCode == null) {
            showLog("شماره چرخ نامعتبر است.", state = state)
        } else {
            userTask = userTask.copy(machineCode = SharedRepository.machineCode!!)
            loading = true
            CoroutineScope(Default).launch {
                RemoteConnection.finalUserAction(userTask = userTask).onSuccess {
                    println("navid body: $it")
                    println("request success")
                    popupHost.showPopupWithAButton("ثبت فعالیت با موفقیت انجام شد.", onDoneButtonClick = {
                        changeScreen(FeatureListScreen)
                    }, onDismiss = {
                        changeScreen(FeatureListScreen)
                    })
                    withContext(Dispatchers.Main) {
                        loading = false
                    }
                }.onError {

                    showLog("مشکلی پیش آمده است.", state)
                    withContext(Dispatchers.Main) {
                        loading = false
                    }
                }
            }
        }
    }
}