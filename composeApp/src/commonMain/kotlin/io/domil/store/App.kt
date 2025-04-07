package io.domil.store

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.domil.store.factory.FactoryApp
import io.domil.store.factory.addTaskFeature.view.EnterDateAndNumberScreen
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesScreen
import io.domil.store.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import io.domil.store.factory.main.view.FeatureListScreen
import io.domil.store.factory.main.viewModel.FactoryMainViewModel
import io.domil.store.factory.stopActivityFeature.view.StopActivityScreen
import io.domil.store.factory.stopActivityFeature.viewModel.StopActivityViewModel
import io.domil.store.view.LoginPage
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainPage
import io.domil.store.view.MainScreen
import io.domil.store.viewModel.AppViewModel

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
        }
    }
}

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
                sizeFilterValue = viewModel.sizeFilterValue,
                onBottomBarButtonClick = { viewModel.openCamera() },
                loading = viewModel.loading,
                uiList = viewModel.filteredUiList,
                onScanButtonClick = { viewModel.openCamera() },
                onColorFilterValueChange = { viewModel.onColorFilterValueChange(it) },
                onSizeFilterValueChange = { viewModel.onSizeFilterValueChange(it) },
                textFieldValue = viewModel.productCode,
                onTextValueChange = { viewModel.onTextValueChange(it) },
                onImeAction = { viewModel.onImeAction() },
                onScanSuccess = {
                    viewModel.barcodeScanner(it)
                },
                barcodeScanner = barcodeScanner,
                onLogoutClick = { viewModel.onLogoutClick(navHostController) },
                storesFilterValue = viewModel.storeFilterValue,
                storesFilterValues = viewModel.storeFilterValues.keys.toMutableList(),
                onStoreFilterValueChange = { viewModel.onStoreFilterValueChange(it) },
                isFullScreenImage = viewModel.isFullScreenImage,
                changeImageFullScreen = { viewModel.changeFullScreenState() },
                colorFilterList = viewModel.uiListColorFiltered,
                filteredUiList = viewModel.filteredUiList,
                onAccountBtnClick = { viewModel.onAccountBtnClick() },
                isAccountDialogOpen = viewModel.isAccountDialogOpen,
                imgAlbumUrl = viewModel.imgUrls,
                colorFilterLazyRowState = viewModel.colorFilterLazyRowState.value
            )
        }
    }
}