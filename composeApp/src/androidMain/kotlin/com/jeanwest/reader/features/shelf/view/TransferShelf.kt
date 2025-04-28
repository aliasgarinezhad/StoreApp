package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.view.ScanOrTypeNumberScreen
import com.jeanwest.reader.features.shelf.viewmodel.TransferShelfViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Activity responsible for handling the shelf transfer process.
 *  This process involves scanning items from one shelf and transferring them to another.
 *  The activity uses Jetpack Compose for UI and Navigation Compose for screen management.
 *
 *  Key Functionalities:
 *    - Scanning items from the source shelf ("ScanShelfToExit").
 *    - Selecting products to be transferred ("SelectProductsScreen").
 *    - Scanning the destination shelf ("ScanShelfToEnter").
 *    - Scanning the selected products on the destination shelf to confirm transfer ("ScanProductsScreen").
 *    - Managing RFID scanning for product identification.
 *    - Handling user input and navigation between screens.
 *    - Communicating with a [TransferShelfViewModel] to manage data and business logic.
 *
 *  Screens:
 *    - **ScanShelfToExit:**  Allows the user to scan or manually enter the identifier of the shelf from which items will be transferred.
 *    - **SelectProductsScreen:** Displays a list of products and allows the user to select the products they want to transfer.  Also initiates RFID scanning to identify products on the shelf.
 *    - **ScanShelfToEnter:** Allows the user to scan or manually enter the identifier of the shelf to which items will be transferred.
 *    - **ScanProductsScreen:**  Requires the user to scan the selected products on the destination shelf to verify the transfer.  Final RFID scan and transfer confirmation occurs here.
 *
 *  Dependencies:
 *    - Jetpack Compose: For building the UI.
 *    - Navigation Compose: For managing screen transitions.
 *    - Dagger Hilt: For dependency injection, providing the [TransferShelfViewModel].
 *    - [TransferShelfViewModel]:  Manages the state and logic for the shelf transfer process.  This includes handling user input, interacting with data sources, and controlling navigation.
 *
 *  Lifecycle Events:
 *    - **onCreate:** Sets up the Compose UI */
@AndroidEntryPoint
class TransferShelf : ComponentActivity() {

    val viewModel: TransferShelfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "ScanShelfToExit") {

                composable("ScanShelfToExit") {
                    BackHandler {
                        finish()
                    }
                    ScanOrTypeNumberScreen(
                        loading = viewModel.loading,
                        onClick = { viewModel.onFirstTextFieldImeAction() },
                        value = viewModel.shelfToExitItems,
                        onValueChange = { viewModel.shelfToExitItems = it },
                        item = "قفسه اول",
                        popupHost = viewModel.popupHost,
                        topBarTitle = "انتقال قفسه",
                        topBarOnClick = {
                            finish()
                        },
                        state = viewModel.state
                    )
                }

                composable("SelectProductsScreen") {
                    BackHandler {
                        viewModel.changeScreen("ScanShelfToExit")
                    }
                    ShelfItemsScreen(
                        topBarTitle = "انتخاب کالا",
                        topBarOnClick = {
                            viewModel.changeScreen("ScanShelfToExit")
                        },
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        state = viewModel.state,
                        popupState = viewModel.popupHost,
                        uiListProduct = viewModel.uiList,
                        bottomBarText = "اسکن قفسه دوم",
                        onBottomBarClick = { viewModel.changeScreen("ScanShelfToEnter") },
                        isSecondPage = false,
                        itemOnClick = {
                            if (it !in viewModel.scanAndCompareProductsUiList) {
                                viewModel.scanAndCompareProductsUiList.add(it)
                            } else {
                                viewModel.scanAndCompareProductsUiList.remove(it)
                            }
                        },
                        signedKBarCode = viewModel.scanAndCompareProductsUiList,
                        completeRfScan = viewModel.rfid.epcs.size == viewModel.scanAndCompareProductsUiList.flatMap { it.epcs }.size,
                        rfid = viewModel.rfid,
                        text1 = "مجموع اسکن: ${
                            viewModel.uiList.filter { it in viewModel.scanAndCompareProductsUiList }
                                .sumOf { it.product.scannedBarcodeNumber }
                        }",
                        text2 = "",
                        specText = "لطفا کالا های مورد نظر برای خروج از قفسه را انتخاب کنید",
                    )
                }

                composable("ScanShelfToEnter") {
                    BackHandler {
                        viewModel.changeScreen("SelectProductsScreen")
                    }
                    ScanOrTypeNumberScreen(
                        loading = viewModel.loading,
                        onClick = { viewModel.onSecondTextFieldImeAction() },
                        value = viewModel.shelfToEnterItems,
                        onValueChange = { viewModel.shelfToEnterItems = it },
                        item = "قفسه دوم",
                        popupHost = viewModel.popupHost,
                        topBarTitle = "انتقال قفسه",
                        topBarOnClick = {
                            viewModel.changeScreen("SelectProductsScreen")
                        },
                        state = viewModel.state
                    )
                }

                composable("ScanProductsScreen") {
                    BackHandler {
                        viewModel.changeScreen("ScanShelfToEnter")
                    }
                    ShelfItemsScreen(
                        topBarTitle = "اسکن کالا",
                        topBarOnClick = {
                            viewModel.changeScreen("ScanShelfToEnter")
                        },
                        loading = viewModel.loading || viewModel.rfid.scanning,
                        state = viewModel.state,
                        popupState = viewModel.popupHost,
                        uiListProduct = viewModel.uiList.filter {
                            it in viewModel.scanAndCompareProductsUiList
                        },
                        bottomBarText = "ورود به قفسه",
                        onBottomBarClick = { viewModel.transferShelf() },
                        isSecondPage = viewModel.shelfToEnterItems != "",
                        itemOnClick = {},
                        signedKBarCode = viewModel.scanAndCompareProductsUiList,
                        completeRfScan = (viewModel.rfid.epcs.size == viewModel.scanAndCompareProductsUiList.flatMap { it.epcs }.size && viewModel.scanAndCompareProductsUiList.flatMap { it.epcs }
                            .isNotEmpty()),
                        rfid = viewModel.rfid,
                        text1 = "مجموع اسکن: ${
                            viewModel.uiList.filter { it in viewModel.scanAndCompareProductsUiList }
                                .sumOf { it.product.scannedBarcodeNumber }
                        }",
                        text2 = "",
                        specText = "لطفا کالا های مورد نظر برای خروج از قفسه را اسکن کنید"
                    )
                }
            }

            LaunchedEffect(viewModel.screen) {
                navController.navigate(viewModel.screen)
                viewModel.onScreenChanged()
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