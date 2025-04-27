package com.jeanwest.reader

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.factory.FactoryApp
import com.jeanwest.reader.factory.addTaskFeature.view.EnterDateAndNumberScreen
import com.jeanwest.reader.factory.addTaskFeature.view.SelectTaskScreen
import com.jeanwest.reader.factory.addTaskFeature.view.ShowProductionLinesScreen
import com.jeanwest.reader.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import com.jeanwest.reader.factory.main.view.FeatureListScreen
import com.jeanwest.reader.factory.main.viewModel.FactoryMainViewModel
import com.jeanwest.reader.factory.stopActivityFeature.view.StopActivityScreen
import com.jeanwest.reader.factory.stopActivityFeature.viewModel.StopActivityViewModel
import com.jeanwest.reader.shop.view.LoginPage
import com.jeanwest.reader.shop.view.LoginScreen
import com.jeanwest.reader.shop.view.MainPage
import com.jeanwest.reader.shop.view.MainScreen
import com.jeanwest.reader.shop.viewModel.AppViewModel

/**
 * The main composable function for the application, handling navigation and screen changes.
 *
 * This function conditionally displays either the factory-specific app flow or the standard app flow
 * based on the `isFactoryAppRequested` flag. It also manages navigation between screens based on
 * pending screen change requests from the provided ViewModels.
 *
 * @param viewModel The main application ViewModel.
 * @param mainViewModel ViewModel for the main factory flow.
 * @param addTaskViewModel ViewModel for adding tasks in the factory flow.
 * @param stopActivityViewModel ViewModel for stopping activities in the factory flow.
 * @param isFactoryAppRequested Flag indicating whether the factory-specific app flow should be used. Defaults to false.
 * @param barcodeScanner Composable function for scanning barcodes.  It should take a callback function
 *  `onScanSuccess` as a parameter, which will be invoked with the scanned barcode string when a scan is successful.
 */
@Composable
fun App(
    viewModel: AppViewModel,
    mainViewModel: FactoryMainViewModel,
    addTaskViewModel: FactoryAddTaskViewModel,
    stopActivityViewModel: StopActivityViewModel,
    isFactoryAppRequested: Boolean = false,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
) {

    val navHostController = rememberNavController()
    if (isFactoryAppRequested) {
        FactoryApp(
            factoryMainViewModel = mainViewModel,
            factoryAddTaskViewModel = addTaskViewModel,
            navHostController = navHostController,
            factoryStopActivityViewModel = stopActivityViewModel
        )
    } else {
        ComposableHost(
            viewModel,
            navHostController = navHostController,
            barcodeScanner = barcodeScanner,
        )
    }

    if (mainViewModel.screenChangePending || addTaskViewModel.screenChangePending || stopActivityViewModel.screenChangePending) {

        println("screenChangePending")
        if (mainViewModel.destinationScreen == FeatureListScreen && mainViewModel.currentScreen == LoginScreen) {
            navHostController.navigate(FeatureListScreen)
            mainViewModel.onScreenChanged()
        } else if (mainViewModel.destinationScreen == LoginScreen && mainViewModel.currentScreen == FeatureListScreen) {
            navHostController.popBackStack()
            mainViewModel.onScreenChanged()
        } else if (mainViewModel.destinationScreen == ShowProductionLinesScreen && mainViewModel.currentScreen == FeatureListScreen) {
            navHostController.navigate(ShowProductionLinesScreen)
            mainViewModel.onScreenChanged()
            addTaskViewModel.getProductionLines()
        } else if (mainViewModel.destinationScreen == StopActivityScreen && mainViewModel.currentScreen == FeatureListScreen) {
            navHostController.navigate(StopActivityScreen)
            mainViewModel.onScreenChanged()
        } else if (stopActivityViewModel.destinationScreen == FeatureListScreen && stopActivityViewModel.currentScreen == StopActivityScreen) {
            navHostController.popBackStack()
            mainViewModel.destinationScreen = FeatureListScreen
            mainViewModel.onScreenChanged()
            stopActivityViewModel.onScreenChanged()
            stopActivityViewModel.currentScreen = StopActivityScreen
            stopActivityViewModel.destinationScreen = StopActivityScreen
        } else if (addTaskViewModel.destinationScreen == FeatureListScreen && addTaskViewModel.currentScreen == ShowProductionLinesScreen) {
            navHostController.popBackStack()
            mainViewModel.destinationScreen = FeatureListScreen
            mainViewModel.onScreenChanged()
            addTaskViewModel.onScreenChanged()
            addTaskViewModel.currentScreen = ShowProductionLinesScreen
        } else if (addTaskViewModel.destinationScreen == SelectTaskScreen && addTaskViewModel.currentScreen == ShowProductionLinesScreen) {
            println("SelectTaskScreen")
            navHostController.navigate(SelectTaskScreen)
            addTaskViewModel.onScreenChanged()
        } else if (addTaskViewModel.destinationScreen == ShowProductionLinesScreen && addTaskViewModel.currentScreen == SelectTaskScreen) {
            navHostController.popBackStack()
            addTaskViewModel.getProductionLines()
            addTaskViewModel.onScreenChanged()
        } else if (addTaskViewModel.destinationScreen == EnterDateAndNumberScreen && addTaskViewModel.currentScreen == SelectTaskScreen) {
            println("EnterDateAndNumberScreen")
            navHostController.navigate(EnterDateAndNumberScreen)
            addTaskViewModel.onScreenChanged()
        } else if (addTaskViewModel.destinationScreen == SelectTaskScreen && addTaskViewModel.currentScreen == EnterDateAndNumberScreen) {
            navHostController.popBackStack()
            addTaskViewModel.onScreenChanged()
        } else if (addTaskViewModel.destinationScreen == ShowProductionLinesScreen && addTaskViewModel.currentScreen == EnterDateAndNumberScreen) {
            navHostController.popBackStack()
            navHostController.popBackStack()
            addTaskViewModel.onScreenChanged()
            addTaskViewModel.getProductionLines()
        } else if (addTaskViewModel.destinationScreen == FeatureListScreen && addTaskViewModel.currentScreen == EnterDateAndNumberScreen) {
            navHostController.popBackStack()
            navHostController.popBackStack()
            navHostController.popBackStack()
            mainViewModel.destinationScreen = FeatureListScreen
            mainViewModel.onScreenChanged()
            addTaskViewModel.onScreenChanged()
            addTaskViewModel.currentScreen = ShowProductionLinesScreen
            addTaskViewModel.destinationScreen = ShowProductionLinesScreen
        }
    }
}

/**
 *  This Composable function hosts the navigation graph for the application.
 *  It uses Jetpack Compose Navigation to manage transitions between different screens.
 *
 * @param viewModel The shared [AppViewModel] instance containing the application's state and logic.
 * @param navHostController The [NavHostController] that manages navigation within the NavHost.
 * @param barcodeScanner A composable function that displays a barcode scanner UI.  It takes a lambda
 *  `onScanSuccess` which is called when a barcode is successfully scanned, passing the scanned barcode string.
 *
 *  The NavHost defines two routes:
 *  - **LoginScreen**: Displays the login page, handling user authentication via the [AppViewModel].
 *  - **MainScreen**: Displays the main application screen after successful login. This screen includes functionality for
 *     viewing data, filtering, interacting with the camera, and user logout, all managed by the [AppViewModel].  The barcode scanner UI
 *     is integrated within the MainPage composable, utilizing the provided [barcodeScanner] composable.
 */
@Composable
fun ComposableHost(
    viewModel: AppViewModel,
    navHostController: NavHostController,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
) {

    NavHost(navController = navHostController, startDestination = viewModel.routeScreen.value) {

        composable<LoginScreen> {
            LoginPage(
                username = viewModel.username,
                password = viewModel.password,
                onSignInButtonClick = { viewModel.signIn(navHostController = navHostController) },
                onPasswordValueChanged = { viewModel.onPasswordValueChanges(it) },
                onUsernameValueChanged = { viewModel.onUsernameValueChanges(it) },
                state = viewModel.state,
                loading = viewModel.loading,
            )
        }

        composable<MainScreen> {
            MainPage(
                state = viewModel.state,
                isCameraOn = viewModel.isCameraOn,
                colorFilterValue = viewModel.colorFilterValue,
                onBottomBarButtonClick = { viewModel.openCamera() },
                loading = viewModel.loading,
                uiList = viewModel.filteredUiList,
                onColorFilterValueChange = { viewModel.onColorFilterValueChange(it) },
                textFieldValue = viewModel.productCode,
                onTextValueChange = { viewModel.onTextValueChange(it) },
                onImeAction = { viewModel.onImeAction() },
                onScanSuccess = {
                    viewModel.barcodeScanner(it)
                },
                barcodeScanner = barcodeScanner,
                isFullScreenImage = viewModel.isFullScreenImage,
                changeImageFullScreen = { viewModel.changeFullScreenState() },
                colorFilterList = viewModel.uiListColorFiltered,
                filteredUiList = viewModel.filteredUiList,
                onAccountBtnClick = { viewModel.onAccountBtnClick(navHostController) },
                imgAlbumUrl = viewModel.imgUrls,
                colorFilterLazyRowState = viewModel.colorFilterLazyRowState.value,
                popupState = viewModel.popupHost
            )
        }
    }
}