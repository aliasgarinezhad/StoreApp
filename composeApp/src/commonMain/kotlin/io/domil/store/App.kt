package io.domil.store

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.domil.store.factory.view.FeatureListScreen
import io.domil.store.view.LoginPage
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainPage
import io.domil.store.view.MainScreen
import io.domil.store.viewModel.AppViewModel
import io.domil.store.factory.viewModel.FactoryViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    viewModel: AppViewModel,
    factoryViewModel: FactoryViewModel = FactoryViewModel(),
    isFactoryAppRequested: Boolean = false,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
) {

    val navHostController = rememberNavController()
    if (isFactoryAppRequested) {
        FactoryApp(
            viewModel = factoryViewModel,
            navHostController = navHostController
        )
    } else {
        ComposableHost(
            viewModel,
            navHostController = navHostController,
            barcodeScanner = barcodeScanner,
        )
    }

    if (factoryViewModel.screenChangePending) {

        if (factoryViewModel.destinationScreen == FeatureListScreen && factoryViewModel.currentScreen == LoginScreen) {
            navHostController.navigate(FeatureListScreen)
            factoryViewModel.onScreenChanged()
        } else if (factoryViewModel.destinationScreen == LoginScreen && factoryViewModel.currentScreen == FeatureListScreen) {
            navHostController.popBackStack()
            factoryViewModel.onScreenChanged()
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

@Composable
fun FactoryApp(
    viewModel: FactoryViewModel,
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = LoginScreen) {
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

        composable<FeatureListScreen> {

            FeatureListScreen(
                changeScreen = { viewModel.changeScreen(it) },
                state = viewModel.state,
                loading = viewModel.loading,
                featuresList = viewModel.featureList
            )
        }
    }
}
