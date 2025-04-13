package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.features.carton.view.ShelfEnterScreen
import com.jeanwest.reader.features.carton.view.StockDraftRequestItemScreen
import com.jeanwest.reader.features.shelf.viewmodel.NewShelfInViewModel
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewShelfIn : ComponentActivity() {

    val viewModel: NewShelfInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "StockDraftRequestScreen") {

                composable("StockDraftRequestScreen") {
                    StockDraftRequestScreen(
                        topBarTitle = "لیست درخواست ها",
                        topBarOnClick = { finish() },
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        stockDraftRequestsList = viewModel.requestsList,
                        uiListOnClick = { viewModel.onRequestClick(it) },
                        state = viewModel.state,
                        popupState = viewModel.popupHost
                    )
                }

                composable("StockDraftRequestItemsScreen") {

                    BackHandler {
                        viewModel.getUserRequestsList()
                    }
                    StockDraftRequestItemScreen(
                        topBarTitle = "انتخاب کالا",
                        topBarOnClick = {
                            viewModel.getUserRequestsList()
                        },
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        state = viewModel.state,
                        popupState = viewModel.popupHost,
                        stockDraftRequestNumber = viewModel.request.number,
                        uiListProduct = viewModel.requestItems,
                        bottomBarText = if (viewModel.requestItems.isEmpty()) "پایان درخواست" else "برگشت کالاها",
                        onBottomBarClick = { viewModel.onFinalizeRequestButtonClick() },
                        onProductClick = { viewModel.onRequestItemClick(it) },
                        shortageNumber = viewModel.requestItems.sumOf { it.notFoundNumber }
                    )
                }

                composable("ShelfEnterScreen") {

                    BackHandler { viewModel.changeScreen("StockDraftRequestItemsScreen") }
                    ShelfEnterScreen(
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        state = viewModel.state,
                        popupState = viewModel.popupHost,
                        uiListProduct = viewModel.selectedProductToEnterShelf,
                        topBarTitle = if (viewModel.selectedShelfToEnterProduct == "") "اسکن قفسه" else "اسکن کالا",
                        suggestedShelfList = viewModel.recommendedShelfsToEnter,
                        shelfNumber = viewModel.selectedShelfToEnterProduct,
                        bottomBarText = "ورود به قفسه",
                        onBottomBarClick = { viewModel.onEnterToShelfButtonClick() },
                        scannedNumber = viewModel.selectedProductToEnterShelfScannedNumber,
                        topBarOnClick = { viewModel.changeScreen("StockDraftRequestItemsScreen") },
                        inputNumberChange = {
                            viewModel.increaseSelectedProductScannedNumberByInputValue(
                                it
                            )
                        },
                        enableInputNumber = !viewModel.isStockDraftCreateByRfid.value
                    )
                }

                composable("ScanAndCompareProduct") {

                    BackHandler { viewModel.changeScreen("StockDraftRequestItemsScreen") }
                    ScanAndCompareProduct(
                        topBarOnClick = { viewModel.changeScreen("StockDraftRequestItemsScreen") },
                        topBarTitle = "برگشت کالاها",
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        popupState = viewModel.popupHost,
                        state = viewModel.state,
                        uiListProduct = viewModel.scanAndCompareUiList,
                        onBottomBarClick = { viewModel.createReturnStockDrafts() },
                        bottomBarText = "برگشت کالاها"
                    )
                }
            }

            if (viewModel.currentScreen != viewModel.screen) {
                when (viewModel.screen) {
                    "StockDraftRequestScreen" -> {
                        if (viewModel.currentScreen == "ScanAndCompareProduct") {
                            repeat(2) {
                                navController.popBackStack()
                            }
                        } else {
                            navController.popBackStack()
                        }
                        viewModel.onScreenChanged()
                    }

                    "StockDraftRequestItemsScreen" -> {
                        if (viewModel.currentScreen == "StockDraftRequestScreen") {
                            navController.navigate("StockDraftRequestItemsScreen")
                        } else {
                            navController.popBackStack()
                        }
                        viewModel.onScreenChanged()
                    }

                    else -> {
                        navController.navigate(viewModel.screen)
                        viewModel.onScreenChanged()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 280 || keyCode == 293) {
                viewModel.scanTrigger()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}