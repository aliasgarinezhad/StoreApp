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
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
    saveUserData: (user: User) -> Unit,
    loadUserData: (onDataReceived: (user: User) -> Unit) -> Unit,
) {
    val viewModel = AppViewModel(saveUserData = saveUserData, loadUserData = loadUserData)
    val navHostController = rememberNavController()
    viewModel.checkUserAuth(navHostController)
    ComposableHost(
        viewModel,
        navHostController = navHostController,
        barcodeScanner = barcodeScanner
    )
}

@Composable
fun ComposableHost(
    viewModel: AppViewModel,
    navHostController: NavHostController,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit
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
                loading = viewModel.loading
            )
        }

        composable<MainScreen> {
            MainPage(
                state = viewModel.state,
                sizeFilterValue = viewModel.sizeFilterValue,
                sizeFilterValues = viewModel.sizeFilterValues,
                colorFilterValue = viewModel.colorFilterValue,
                colorFilterValues = viewModel.colorFilterValues,
                storesFilterValue = viewModel.storeFilterValue,
                storesFilterValues = viewModel.storeFilterValues.keys.toMutableList(),
                onStoreFilterValueChange = { viewModel.onStoreFilterValueChange(it) },
                onImeAction = { viewModel.onImeAction() },
                onScanButtonClick = { viewModel.openCamera() },
                onTextValueChange = { viewModel.onTextValueChange(it) },
                onSizeFilterValueChange = { viewModel.onSizeFilterValueChange(it) },
                onColorFilterValueChange = { viewModel.onColorFilterValueChange(it) },
                onBottomBarButtonClick = { viewModel.openCamera() },
                loading = viewModel.loading,
                isCameraOn = viewModel.isCameraOn,
                textFieldValue = viewModel.productCode,
                uiList = viewModel.filteredUiList,
                onScanSuccess = {
                    viewModel.barcodeScanner(it)
                },
                barcodeScanner = barcodeScanner,
                onLogoutClick = { viewModel.onLogoutClick(navHostController) }
            )
        }
    }
}
