package io.domil.store.factory.addTaskFeature.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.domil.store.factory.addTaskFeature.model.Product
import io.domil.store.factory.addTaskFeature.model.UserTask
import io.domil.store.factory.addTaskFeature.view.EnterDateAndNumberScreen
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen

class FactoryAddTaskViewModel {

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

    init {
        println("init")
        getProductionLines()
    }

    private fun getProductionLines() {

        //TODO
        println("getProductionLines")
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
        products.add(Product())
    }

    fun onProductLineClick(product: Product) {
        println("onProductLineClick")
        userTask.product = product
        changeScreen(SelectTaskScreen)
    }

    private fun changeScreen(screen: Any) {
        destinationScreen = screen
        screenChangePending = true
    }

    fun onScreenChanged() {
        currentScreen = destinationScreen
        screenChangePending = false
    }

    fun onTaskClick(task: String) {
        userTask.task = task
        changeScreen(EnterDateAndNumberScreen)
    }

    fun onStartHourChanged(hour: Int) {
        userTask.startHour = hour
    }

    fun onStartMinuteChanged(minute: Int) {
        userTask.startMinute = minute
    }

    fun onEndHourChanged(hour: Int) {
        userTask.endHour = hour
    }

    fun onEndMinuteChanged(minute: Int) {
        userTask.endMinute = minute
    }

    fun onSizeChanged(size: String) {
        userTask.size = size
    }

    fun onNumberChanged(number: Int) {
        userTask.number = number
    }

    fun onAddTaskButtonClick() {
        //TODO
    }
}