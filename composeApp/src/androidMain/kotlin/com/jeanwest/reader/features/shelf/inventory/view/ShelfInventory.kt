package com.jeanwest.reader.features.shelf.inventory.view

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.ScanOrTypeNumberScreen
import com.jeanwest.reader.features.shelf.inventory.viewmodel.ShelfInventoryViewModel
import com.jeanwest.reader.features.shelf.view.ShelfItemsScreen
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShelfInventory : ComponentActivity() {

    private val viewModel: ShelfInventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            ShelfInventoryNavigation(
                finish = { finish() },
                viewModel = viewModel,
                navigationEvent = viewModel.navigationEvent
            )
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
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

@Composable
fun ShelfInventoryNavigation(
    finish: () -> Unit,
    viewModel: ShelfInventoryViewModel,
    navigationEvent: NavigationEvent,
) {
    val navController = rememberNavController()

    if (navigationEvent == NavigationEvent.GoToShelfInventoryEvent) {
        navController.navigate("shelfItemScreen")
    } else if (navigationEvent == NavigationEvent.BackToEnterShelfNumberEvent) {
        Log.e("navigationEvent", "back called")
        navController.popBackStack()
    }

    // Start the navigation flow from the scan screen
    NavHost(
        navController = navController,
        startDestination = "scanOrTypeShelfNumber"
    ) {
        composable("scanOrTypeShelfNumber") {
            BackHandler {
                finish()
            }
            ScanOrTypeNumberScreen(
                loading = viewModel.loading,
                value = viewModel.shelfCode,
                onClick = {
                    viewModel.getShelfDetails()
                },
                onValueChange = { viewModel.shelfCode = it },
                item = "قفسه",
                topBarTitle = stringResource(R.string.ShelfInventory),
                topBarOnClick = finish,
                state = viewModel.state
            )
        }

        composable("shelfItemScreen") {
            BackHandler {
                viewModel.backToScanShelfPage()
            }
            ShelfItemsScreen(
                topBarTitle = "اسکن قفسه",
                topBarOnClick = {
                    viewModel.backToScanShelfPage()
                },
                loading = viewModel.loading,
                uiListProduct = viewModel.uiList,
                state = viewModel.state,
                onBottomBarClick = {
                    viewModel.onScanFinished()
                },
                popupState = viewModel.popupState,
                bottomBarText = "ثبت گزارش اسکن",
                isSecondPage = true,
                itemOnClick = {},
                signedKBarCode = mutableListOf(),
                completeRfScan = true,
                rfid = viewModel.rfid,
                text1 = "مجموع کالاها: ${viewModel.numberOfItemsInShelf}",
                text2 = "کالاهای پیدا نشده: ${viewModel.sumOfNotFound}",
                specText = "",
            )
        }
    }
}