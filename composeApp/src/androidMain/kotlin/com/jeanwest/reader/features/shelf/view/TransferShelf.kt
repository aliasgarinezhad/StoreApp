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
import com.jeanwest.reader.features.shared.ScanOrTypeNumberScreen
import com.jeanwest.reader.features.shelf.viewmodel.TransferShelfViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                        rfid = viewModel.rfid
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
                        rfid = viewModel.rfid
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