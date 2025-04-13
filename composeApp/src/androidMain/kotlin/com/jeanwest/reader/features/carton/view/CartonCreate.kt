package com.jeanwest.reader.features.carton.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import com.jeanwest.reader.R
import com.jeanwest.reader.features.carton.viewmodel.CartonCreateViewModel
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartonCreate : ComponentActivity() {

    val viewModel: CartonCreateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            CartonCreateScreen(
                context = viewModel.context,
                topBarTitle = stringResource(R.string.createCarton),
                topBarOnClick = { finish() },
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 280 || keyCode == 293) {
                viewModel.scanTrigger()
            } else if (keyCode == 4) {
                finish()
            }
        }
        return true
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

}