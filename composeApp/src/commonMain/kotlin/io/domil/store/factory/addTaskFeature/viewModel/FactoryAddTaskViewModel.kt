package io.domil.store.factory.addTaskFeature.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import io.domil.store.factory.addTaskFeature.data.RemoteConnection
import io.domil.store.factory.addTaskFeature.model.Product
import io.domil.store.factory.addTaskFeature.model.UserTask
import io.domil.store.factory.addTaskFeature.view.EnterDateAndNumberScreen
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import io.domil.store.view.showLog
import io.domil.store.networking.createHttpClient
import io.domil.store.tools.onError
import io.domil.store.tools.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FactoryAddTaskViewModel {

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
    private var remoteConnection = RemoteConnection(createHttpClient())

//    init {
//        println("init")
//        getProductionLines()
//    }

    fun getProductionLines() {
        loading = true
        CoroutineScope(Default).launch {
            remoteConnection.getProductionOrders().onSuccess {
                println("navid body: $it")
                it.forEach { productLineApi ->
                    products.add(
                        Product(
                            name = productLineApi.partName,
                            style = productLineApi.styleN,
                            color = productLineApi.colorCodeF,
                            //uiColor = Color(productLineApi.colorHex.substring(1).toInt()),
                           // tasks = productLineApi.operationItems
                        )
                    )
                }
                withContext(Dispatchers.Main){
                    loading = false
                }
            }.onError {

                withContext(Dispatchers.Main){
                    loading = false
                }
            }
        }
//        //TODO
//        println("getProductionLines")
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
//        products.add(Product())
    }

    fun onProductLineClick(product: Product) {
        println("onProductLineClick")
        userTask = userTask.copy(product = product)
        changeScreen(SelectTaskScreen)
    }

    fun changeScreen(screen: Any) {
        destinationScreen = screen
        screenChangePending = true
    }

    fun onScreenChanged() {
        currentScreen = destinationScreen
        screenChangePending = false
    }

    fun onTaskClick(task: String) {
        userTask = userTask.copy(task = task)
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
        userTask = userTask.copy(size = size)
    }

    fun onNumberChanged(number: String) {
        textFieldValue = number
        if (number.toIntOrNull() == null) {
            if(textFieldValue != "") showLog("لطفا مقدار عددی وارد کنید.", state)
            userTask = userTask.copy(number = 0)
        } else {
            userTask = userTask.copy(number = number.toInt())
        }
    }

    fun onAddTaskButtonClick() {
        //TODO
    }
}