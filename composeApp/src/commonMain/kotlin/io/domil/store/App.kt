package io.domil.store

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.domil.store.factory.addTaskFeature.view.EnterDateAndNumberScreen
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesList
import io.domil.store.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import io.domil.store.factory.main.view.FactoryApp
import io.domil.store.factory.main.view.FeatureListScreen
import io.domil.store.factory.main.viewModel.FactoryMainViewModel
import io.domil.store.view.LoginPage
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainPage
import io.domil.store.view.MainScreen
import io.domil.store.viewModel.AppViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(
    viewModel: AppViewModel,
    factoryMainViewModel: FactoryMainViewModel,
    factoryAddTaskViewModel: FactoryAddTaskViewModel,
    isFactoryAppRequested: Boolean = false,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
) {

    val navHostController = rememberNavController()
    if (isFactoryAppRequested) {
        FactoryApp(
            factoryMainViewModel = factoryMainViewModel,
            factoryAddTaskViewModel = factoryAddTaskViewModel,
            navHostController = navHostController
        )
    } else {
        ComposableHost(
            viewModel,
            navHostController = navHostController,
            barcodeScanner = barcodeScanner,
        )
    }

    if (factoryMainViewModel.screenChangePending || factoryAddTaskViewModel.screenChangePending) {

        println("screenChangePending")
        if (factoryMainViewModel.destinationScreen == FeatureListScreen && factoryMainViewModel.currentScreen == LoginScreen) {
            navHostController.navigate(FeatureListScreen)
            factoryMainViewModel.onScreenChanged()
        } else if (factoryMainViewModel.destinationScreen == LoginScreen && factoryMainViewModel.currentScreen == FeatureListScreen) {
            navHostController.popBackStack()
            factoryMainViewModel.onScreenChanged()
        } else if (factoryMainViewModel.destinationScreen == ShowProductionLinesList && factoryMainViewModel.currentScreen == FeatureListScreen) {
            navHostController.navigate(ShowProductionLinesList)
            factoryMainViewModel.onScreenChanged()
        } else if (factoryAddTaskViewModel.destinationScreen == FeatureListScreen && factoryAddTaskViewModel.currentScreen == ShowProductionLinesList) {
            navHostController.popBackStack()
            factoryMainViewModel.onScreenChanged()
        } else if (factoryAddTaskViewModel.destinationScreen == SelectTaskScreen && factoryAddTaskViewModel.currentScreen == ShowProductionLinesList) {
            println("SelectTaskScreen")
            navHostController.navigate(SelectTaskScreen)
            factoryAddTaskViewModel.onScreenChanged()
        } else if (factoryAddTaskViewModel.destinationScreen == ShowProductionLinesList && factoryAddTaskViewModel.currentScreen == SelectTaskScreen) {
            navHostController.popBackStack()
            factoryAddTaskViewModel.onScreenChanged()
        } else if (factoryAddTaskViewModel.destinationScreen == EnterDateAndNumberScreen && factoryAddTaskViewModel.currentScreen == SelectTaskScreen) {
            println("EnterDateAndNumberScreen")
            navHostController.navigate(EnterDateAndNumberScreen)
            factoryAddTaskViewModel.onScreenChanged()
        } else if (factoryAddTaskViewModel.destinationScreen == SelectTaskScreen && factoryAddTaskViewModel.currentScreen == EnterDateAndNumberScreen) {
            navHostController.popBackStack()
            factoryAddTaskViewModel.onScreenChanged()
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

