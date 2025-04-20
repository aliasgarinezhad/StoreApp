package com.jeanwest.reader.features.shelf.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.jeanwest.reader.features.shelf.model.RequestType
import com.jeanwest.reader.features.shelf.viewmodel.CreateEnterShelfRequestViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateShelfInRequest : ComponentActivity() {

    val viewModel by viewModels<CreateEnterShelfRequestViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getStringExtra("data")
        viewModel.newFeature = !data.isNullOrEmpty() && data == "RFID"
        setContent {
            BackHandler {
                viewModel.rf.stopScanning()
                finish()
            }
            ScanCartonAndBarcodeScreen(
                topBarTitle = "ایجاد درخواست انتقال به قفسه",
                topBarOnClick = {
                    viewModel.rf.stopScanning()
                    finish()
                },
                loading = viewModel.loading,
                uiListProduct = viewModel.productsUiList,
                uiListCarton = viewModel.cartonsUiList,
                state = viewModel.state,
                popupState = viewModel.popupHost,
                inputValue = viewModel.textFieldValue,
                onValueChange = { viewModel.onTextFieldValueChange(it) },
                inputHint = "لطفا شماره کارتن را وارد یا اسکن کنید",
                clearItem = { viewModel.deleteProduct(it) },
                bottomBarText = "ایجاد درخواست انتقال به قفسه",
                onBottomBarClick = { viewModel.onCreateShelfInRequestButtonClick() },
                syncScanItem = { viewModel.onImeAction() },
                typeFilter = viewModel.requestType.toString(),
                onTypeFilterChange = { viewModel.onRequestTypeChange(it) },
                typeFilterList = viewModel.requestTypes,
                hasTypeFilter = viewModel.newFeature,
                isRFScanning = viewModel.rf.scanning,
                showInputProductTextField = viewModel.newFeature && viewModel.requestType == RequestType.Carton
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 280 || keyCode == 293) {
                viewModel.scanTrigger()
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

