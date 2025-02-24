package io.domil.store.factory.main.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.domil.store.factory.addTaskFeature.view.ShowProductionLinesList
import io.domil.store.factory.addTaskFeature.view.SelectTaskScreen
import io.domil.store.factory.addTaskFeature.viewModel.FactoryAddTaskViewModel
import io.domil.store.factory.main.viewModel.FactoryMainViewModel
import io.domil.store.view.LoginPage
import io.domil.store.view.LoginScreen


@Composable
fun FactoryApp(
    factoryMainViewModel: FactoryMainViewModel,
    factoryAddTaskViewModel: FactoryAddTaskViewModel,
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
                featuresList = factoryMainViewModel.featureList
            )
        }

        composable<ShowProductionLinesList> {
            ShowProductionLinesList(
                loading = factoryAddTaskViewModel.loading,
                productionLines = factoryAddTaskViewModel.productionLines,
                onClick = { factoryAddTaskViewModel.onProductLineClick(it) },
                state = factoryAddTaskViewModel.state
            )
        }

        composable<SelectTaskScreen> {
            SelectTaskScreen(
                loading = factoryAddTaskViewModel.loading,
                productionLine = factoryAddTaskViewModel.productionLine,
                onClick = { factoryAddTaskViewModel.onTaskClick(it) },
                state = factoryAddTaskViewModel.state
            )
        }
    }
}