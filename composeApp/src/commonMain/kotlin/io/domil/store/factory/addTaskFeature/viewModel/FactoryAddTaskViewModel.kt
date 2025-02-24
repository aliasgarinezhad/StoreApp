package io.domil.store.factory.addTaskFeature.viewModel

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.domil.store.factory.addTaskFeature.model.ProductionLine
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesList

class FactoryAddTaskViewModel {

    //charge ui parameters
    var loading by mutableStateOf(false)
        private set
    var state = SnackbarHostState()
        private set
    var destinationScreen: Any = ShowProductionLinesList
    var currentScreen: Any = ShowProductionLinesList
    var screenChangePending by mutableStateOf(false)
        private set

    var productionLine by mutableStateOf(ProductionLine())
    var productionLines = mutableListOf<ProductionLine>()

    init {
        println("init")
        getProductionLines()
    }

    private fun getProductionLines() {

        println("getProductionLines")
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
        productionLines.add(ProductionLine())
    }

    fun onProductLineClick(productionLine: ProductionLine) {
        println("onProductLineClick")
        this.productionLine = productionLine
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

    }
}