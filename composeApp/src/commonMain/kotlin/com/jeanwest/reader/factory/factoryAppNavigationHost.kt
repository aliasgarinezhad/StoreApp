package com.jeanwest.reader.factory

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jeanwest.reader.factory.addTaskFeature.data.RemoteConnection
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

/**
 * The main Composable function for the Factory application.
 *
 * This function sets up the navigation graph for the application, managing the different screens
 * and their transitions. It uses a [NavHostController] to handle navigation between composables
 * representing different features or pages within the app.  Each screen is associated with a
 * specific route (defined as constants like `LoginScreen`, `FeatureListScreen`, etc.) and has
 * its own composable function to render the UI.
 *
 *  The composables receive ViewModel instances as parameters and interact with them to manage state
 * and handle user interactions. The ViewModels are responsible for business logic and data
 * manipulation.
 *
 *  The navigation graph includes screens for:
 *  - Login: Authenticating the user.
 *  - Feature List: Displaying a list of available features.
 *  - Production Lines: Selecting a product to work on.
 *  - Task Selection: Choosing the type of activity for a product.
 *  - Date and Number Entry: Specifying details for a task (size, quantity, time).
 *  - Stop Activity: Recording a reason for halting an ongoing activity.
 *
 * @param factoryMainViewModel ViewModel for the main application logic.  Handles sign-in, feature selection, and other general tasks.
 * @param factoryAddTaskViewModel ViewModel for adding new tasks.  Manages product selection, task details, and data submission.
 * @param factoryStopActivityViewModel ViewModel for stopping ongoing activities. Handles reason selection and confirmation.
 * @param navHostController The NavHostController instance used to manage navigation within the app.
 */
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
                onTextFieldChanged = { factoryMainViewModel.changeMachineCode(it) },
                onTextFieldFocused = { factoryMainViewModel.onTextFieldFocused() }
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
                textFieldValue = factoryAddTaskViewModel.textFieldValue,
                onTextFieldFocused = { factoryAddTaskViewModel.onNumberFieldFocused() },

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