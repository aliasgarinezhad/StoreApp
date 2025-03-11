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

    fun onNumberChanged(number: Int) {
        userTask = userTask.copy(number = number)
    }

    fun onAddTaskButtonClick() {
        //TODO
    }
}