package com.jeanwest.reader.features.carton.view

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeanwest.reader.R
import com.jeanwest.reader.features.carton.viewmodel.CartonModificationViewModel
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartonModification : ComponentActivity() {

    val viewModel: CartonModificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            AppNavigation(viewModel) { finish() }
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 280 || keyCode == 293) {
                if (viewModel.currentScreen.value != "ScanCarton") {
                    viewModel.scanTrigger()
                }
            } else if (keyCode == 4) {
                finish()
            }
        }
        return true
    }

}

@Composable
fun AppNavigation(viewModel: CartonModificationViewModel, onBack: () -> Unit) {
    val navController = rememberNavController()
    viewModel.navController = navController
    NavHost(
        navController = navController,
        startDestination = "ScanCarton"
    ) {
        composable("ScanCarton") {
            ScanCartonScreen(
                loading = viewModel.loading,
                value = viewModel.cartonNumber.value,
                onClick = {
                    viewModel.getCartonDetails(viewModel.navController)
                },
                onValueChange = {
                    Log.e("change", it)
                    viewModel.cartonNumber.value = it
                },
                state = viewModel.state,
                topBarTitle = stringResource(R.string.CartonModification),
                topBarOnClick = {
                    onBack()
                },
                navController = navController
            )
        }
        composable("CartonCreate") {
            CartonCreateScreen(
                context = viewModel.context,
                topBarTitle = stringResource(R.string.CartonModification),
                topBarOnClick = { onBack() },
                loading = viewModel.loading,
                uiList = viewModel.uiList,
                state = viewModel.state,
                onBottomBarClick = { viewModel.onBottomBarClick() },
                openPrintDialog = viewModel.openPrintDialog,
                printer = viewModel.printer,
                popupState = viewModel.popupState,
                scannedNumber = viewModel.scannedNumber,
                scanTypeValue = viewModel.scanTypeValue,
                onPrinterSelected = { viewModel.onPrinterSelected(it) },
                onPrintConfirm = { viewModel.onPrintConfirm() },
                onPopupDismiss = { viewModel.onPopupDismiss() },
                onScanTypeChanged = { viewModel.onScanTypeChanged(it) },
                clearItem = { viewModel.clear(it) },
                onOpenSearchActivity = { product, context ->
                    viewModel.openSearchActivity(product, context)
                },
                rf = viewModel.rf,
                printerList = viewModel.printerList,
                rfScanning = viewModel.rf.scanning
            )
        }
    }
}