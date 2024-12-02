package io.domil.store

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.domil.store.view.LoginPage
import io.domil.store.view.LoginScreen
import io.domil.store.view.MainPage
import io.domil.store.view.MainScreen
import io.domil.store.viewModel.AppViewModel
import networking.User
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    viewModel: AppViewModel,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
) {

    val navHostController = rememberNavController()
    ComposableHost(
        viewModel,
        navHostController = navHostController,
        barcodeScanner = barcodeScanner,
    )
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
