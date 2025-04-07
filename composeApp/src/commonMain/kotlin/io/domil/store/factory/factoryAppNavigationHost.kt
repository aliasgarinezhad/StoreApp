package io.domil.store.factory

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.domil.store.factory.addTaskFeature.data.RemoteConnection
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

@Composable
fun FactoryApp(
    factoryMainViewModel: FactoryMainViewModel,
    factoryAddTaskViewModel: FactoryAddTaskViewModel,
    factoryStopActivityViewModel: StopActivityViewModel,
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = LoginScreen) {
        composable<LoginScreen> {

            LoginPage(
                username = factoryMainViewModel.username,
                password = factoryMainViewModel.password,
                onSignInButtonClick = { factoryMainViewModel.signIn() },
                onPasswordValueChanged = { factoryMainViewModel.onPasswordValueChanges(it) },
                onUsernameValueChanged = { factoryMainViewModel.onUsernameValueChanges(it) },
                state = factoryMainViewModel.state,
                loading = factoryMainViewModel.loading,
            )
        }

        composable<FeatureListScreen> {

            FeatureListScreen(
                onFeatureIconClick = { factoryMainViewModel.onFeatureIconClick(it) },
                state = factoryMainViewModel.state,
                loading = factoryMainViewModel.loading,
                featuresList = factoryMainViewModel.featureList,
                factoryUser = RemoteConnection.factoryUser,
                textFieldValue = factoryMainViewModel.machineCodeTextFieldValue,
                onTextFieldChanged = { factoryMainViewModel.changeMachineCode(it) }
            )
        }

        composable<ShowProductionLinesScreen> {
            ShowProductionLinesScreen(
                loading = factoryAddTaskViewModel.loading,
                products = factoryAddTaskViewModel.products,
                onClick = { factoryAddTaskViewModel.onProductLineClick(it) },
                state = factoryAddTaskViewModel.state,
                pageTitle = "انتخاب کالا",
                onBack = { factoryAddTaskViewModel.changeScreen(FeatureListScreen) }
            )
        }

        composable<SelectTaskScreen> {
            SelectTaskScreen(
                loading = factoryAddTaskViewModel.loading,
                product = factoryAddTaskViewModel.userTask.product,
                onClick = { factoryAddTaskViewModel.onTaskClick(it) },
                state = factoryAddTaskViewModel.state,
                pageTitle = "انتخاب نوع فعالیت",
                onBack = { factoryAddTaskViewModel.changeScreen(ShowProductionLinesScreen) }
            )
        }

        composable<EnterDateAndNumberScreen> {
            EnterDateAndNumberScreen(
                popupHost = factoryAddTaskViewModel.popupHost,
                loading = factoryAddTaskViewModel.loading,
                product = factoryAddTaskViewModel.userTask.product,
                onClick = { factoryAddTaskViewModel.onAddTaskButtonClick() },
                state = factoryAddTaskViewModel.state,
                onSizeSelected = {
                    factoryAddTaskViewModel.onSizeChanged(it)
                },
                onTextFieldChanged = {
                    factoryAddTaskViewModel.onNumberChanged(it)
                },
                onStartHourChanged = {
                    factoryAddTaskViewModel.onStartHourChanged(it.toInt())
                },
                onStartMinuteChanged = {
                    factoryAddTaskViewModel.onStartMinuteChanged(it.toInt())
                },
                onEndHourChanged = {
                    factoryAddTaskViewModel.onEndHourChanged(it.toInt())
                },
                onEndMinuteChanged = {
                    factoryAddTaskViewModel.onEndMinuteChanged(it.toInt())
                },
                userTask = factoryAddTaskViewModel.userTask,
                pageTitle = "انتخاب سایز و تعداد",
                onBack = { factoryAddTaskViewModel.changeScreen(SelectTaskScreen) },
                textFieldValue = factoryAddTaskViewModel.textFieldValue
            )
        }
        composable<StopActivityScreen> {
            StopActivityScreen(
                loading = factoryStopActivityViewModel.loading,
                state = factoryStopActivityViewModel.state,
                pageTitle = "دلیل توقف",
                onBack = { factoryStopActivityViewModel.changeScreen(FeatureListScreen) },
                reasons = factoryStopActivityViewModel.reasons,
                onConfirm = { factoryStopActivityViewModel.confirmStopActivity(it) }
            )
        }
    }
}