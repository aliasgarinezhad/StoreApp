package com.jeanwest.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.factory.addTaskFeature.data.RemoteConnection
import com.jeanwest.reader.factory.addTaskFeature.view.EnterDateAndNumberScreen
import com.jeanwest.reader.factory.addTaskFeature.view.SelectTaskScreen
import com.jeanwest.reader.factory.addTaskFeature.view.ShowProductionLinesScreen
import com.jeanwest.reader.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import com.jeanwest.reader.factory.main.view.FeatureListScreen
import com.jeanwest.reader.factory.main.viewModel.FactoryMainViewModel
import com.jeanwest.reader.factory.stopActivityFeature.view.StopActivityScreen
import com.jeanwest.reader.factory.stopActivityFeature.viewModel.StopActivityViewModel
import com.jeanwest.reader.login.view.NavigationEvents
import com.jeanwest.reader.login.viewModel.LoginViewModel
import com.jeanwest.reader.login.view.LoginPage
import com.jeanwest.reader.login.view.LoginScreen
import com.jeanwest.reader.shop.view.KioskMainPage
import com.jeanwest.reader.shop.view.KioskScreen
import com.jeanwest.reader.shop.viewModel.KioskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    mainViewModel: FactoryMainViewModel,
    addTaskViewModel: FactoryAddTaskViewModel,
    stopActivityViewModel: StopActivityViewModel,
) {

    val navHostController = rememberNavController()
    println("nav host run")
    CommonMainNavHost(navController = navHostController)

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

@Composable
fun CommonMainNavHost(navController: NavHostController) {

    NavHost(navController = navController, startDestination = LoginScreen) {

        composable<LoginScreen> {

            val loginViewModel = remember { LoginViewModel() }

            LoginPage(
                username = loginViewModel.username,
                password = loginViewModel.password,
                onSignInButtonClick = { loginViewModel.login() },
                onPasswordValueChanged = { loginViewModel.onPasswordValueChanges(it) },
                onUsernameValueChanged = { loginViewModel.onUsernameValueChanges(it) },
                state = loginViewModel.state,
                loading = loginViewModel.loading,
                isFactoryModeRequested = loginViewModel.isFactoryMode,
                onIsFactoryModeChanged = { loginViewModel.onIsFactoryModeChanged(it) }
            )

            CoroutineScope(Dispatchers.Main).launch {
                loginViewModel.navigationEvents.collect {
                    if (it is NavigationEvents.OpenStoreModuleEvent) {
                        navController.navigate(KioskScreen)
                    } else if (it is NavigationEvents.OpenFactoryModuleEvent) {
                        navController.navigate(FeatureListScreen)
                    }
                }
            }
        }

        composable<KioskScreen> {

            val kioskViewModel = remember {
                KioskViewModel(
                    savedStoreUser = getERPUserData(),
                    webPageRequestBarcode = ""
                )
            }

            KioskMainPage(
                state = kioskViewModel.state,
                isCameraOn = kioskViewModel.isCameraOn,
                colorFilterValue = kioskViewModel.colorFilterValue,
                onBottomBarButtonClick = { kioskViewModel.openCamera() },
                loading = kioskViewModel.loading,
                uiList = kioskViewModel.filteredUiList,
                onColorFilterValueChange = { kioskViewModel.onColorFilterValueChange(it) },
                textFieldValue = kioskViewModel.productCode,
                onTextValueChange = { kioskViewModel.onTextValueChange(it) },
                onImeAction = { kioskViewModel.onImeAction() },
                onScanSuccess = {
                    kioskViewModel.barcodeScanner(it)
                },
                barcodeScanner = { barcodeScanner() },
                isFullScreenImage = kioskViewModel.isFullScreenImage,
                changeImageFullScreen = { kioskViewModel.changeFullScreenState() },
                colorFilterList = kioskViewModel.uiListColorFiltered,
                filteredUiList = kioskViewModel.filteredUiList,
                onAccountBtnClick = { kioskViewModel.onAccountBtnClick(navHostController = navController) },
                imgAlbumUrl = kioskViewModel.imgUrls,
                colorFilterLazyRowState = kioskViewModel.colorFilterLazyRowState.value,
                popupState = kioskViewModel.popupHost
            )
        }

        composable<FeatureListScreen> {

            val factoryMainViewModel =
                remember { FactoryMainViewModel(factoryUser = getFactoryUserData()) }

            FeatureListScreen(
                onFeatureIconClick = { factoryMainViewModel.onFeatureIconClick(it) },
                state = factoryMainViewModel.state,
                loading = factoryMainViewModel.loading,
                featuresList = factoryMainViewModel.featureList,
                factoryUser = factoryMainViewModel.userFullName,
                textFieldValue = factoryMainViewModel.machineCodeTextFieldValue,
                onTextFieldChanged = { factoryMainViewModel.changeMachineCode(it) },
                onTextFieldFocused = { factoryMainViewModel.onTextFieldFocused() }
            )
        }

//        composable<ShowProductionLinesScreen> {
//            ShowProductionLinesScreen(
//                loading = factoryAddTaskViewModel.loading,
//                products = factoryAddTaskViewModel.products,
//                onClick = { factoryAddTaskViewModel.onProductLineClick(it) },
//                state = factoryAddTaskViewModel.state,
//                pageTitle = "انتخاب کالا",
//                onBack = { factoryAddTaskViewModel.changeScreen(FeatureListScreen) }
//            )
//        }
//
//        composable<SelectTaskScreen> {
//            SelectTaskScreen(
//                loading = factoryAddTaskViewModel.loading,
//                product = factoryAddTaskViewModel.userTask.product,
//                onClick = { factoryAddTaskViewModel.onTaskClick(it) },
//                state = factoryAddTaskViewModel.state,
//                pageTitle = "انتخاب نوع فعالیت",
//                onBack = { factoryAddTaskViewModel.changeScreen(ShowProductionLinesScreen) }
//            )
//        }
//
//        composable<EnterDateAndNumberScreen> {
//            EnterDateAndNumberScreen(
//                popupHost = factoryAddTaskViewModel.popupHost,
//                loading = factoryAddTaskViewModel.loading,
//                product = factoryAddTaskViewModel.userTask.product,
//                onClick = { factoryAddTaskViewModel.onAddTaskButtonClick() },
//                state = factoryAddTaskViewModel.state,
//                onSizeSelected = {
//                    factoryAddTaskViewModel.onSizeChanged(it)
//                },
//                onTextFieldChanged = {
//                    factoryAddTaskViewModel.onNumberChanged(it)
//                },
//                onStartHourChanged = {
//                    factoryAddTaskViewModel.onStartHourChanged(it.toInt())
//                },
//                onStartMinuteChanged = {
//                    factoryAddTaskViewModel.onStartMinuteChanged(it.toInt())
//                },
//                onEndHourChanged = {
//                    factoryAddTaskViewModel.onEndHourChanged(it.toInt())
//                },
//                onEndMinuteChanged = {
//                    factoryAddTaskViewModel.onEndMinuteChanged(it.toInt())
//                },
//                userTask = factoryAddTaskViewModel.userTask,
//                pageTitle = "انتخاب سایز و تعداد",
//                onBack = { factoryAddTaskViewModel.changeScreen(SelectTaskScreen) },
//                textFieldValue = factoryAddTaskViewModel.textFieldValue,
//                onTextFieldFocused = { factoryAddTaskViewModel.onNumberFieldFocused() },
//
//                )
//        }
//        composable<StopActivityScreen> {
//            StopActivityScreen(
//                loading = factoryStopActivityViewModel.loading,
//                state = factoryStopActivityViewModel.state,
//                pageTitle = "دلیل توقف",
//                onBack = { factoryStopActivityViewModel.changeScreen(FeatureListScreen) },
//                reasons = factoryStopActivityViewModel.reasons,
//                onConfirm = { factoryStopActivityViewModel.confirmStopActivity(it) }
//            )
//        }
    }
}
