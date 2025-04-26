package com.jeanwest.reader.features.shelf.newShelfIn.view

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
import com.jeanwest.reader.features.shelf.newShelfIn.viewModel.NewShelfInViewModel
import com.jeanwest.reader.features.stockDraftRequest.view.StockDraftRequestScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity manages the "New Shelf In" workflow, which allows users to process stock draft requests,
 * select items, and manage shelf entries and returns.  It uses Jetpack Compose for the UI and Navigation
 * Compose for managing different screens within the workflow.  The activity interacts with a [NewShelfInViewModel]
 * to handle business logic and data management.
 *
 * Key features:
 *  - **Navigation:**  Manages transitions between different screens using a NavHost.  Screens include:
 *      - `StockDraftRequestScreen`: Displays a list of stock draft requests.
 *      - `StockDraftRequestItemsScreen`: Shows items associated with a selected stock draft request.
 *      - `ShelfEnterScreen`:  Handles entering items into a shelf, including shelf scanning and quantity input.
 *      - `ScanAndCompareProduct`: Facilitates the return of items, likely involving a scanning and comparison process.
 *  - **Data Handling:** Relies on the [NewShelfInViewModel] for data retrieval, updates, and state management.
 *  - **UI Interactions:**  Defines composable functions for each screen, handling user interactions like button clicks
 *    and list selections.  The UI reflects the state managed by the ViewModel (e.g., loading indicators, data lists).
 *  - **RFID Integration:** Potentially integrates with RFID scanning hardware, as evidenced by the `viewModel.rfid.scanning`
 *    property and the `onKeyDown` method handling specific key codes.
 *  - **Lifecycle Management:** Overrides `onResume` and `onPause` to potentially handle RFID scanner lifecycle or other
 *    resource management tasks.
 *  - **Back Navigation:** Uses `BackHandler` to customize back button behavior on each screen, often navigating back
 *    to a previous screen within the workflow.
 *
 * @property viewModel The [NewShelfInViewModel] instance, injected using Hilt.
 */
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