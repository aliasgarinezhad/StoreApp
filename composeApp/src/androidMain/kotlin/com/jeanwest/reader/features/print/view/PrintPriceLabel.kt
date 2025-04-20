package com.jeanwest.reader.features.print.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.print.viewmodel.PrintPriceLabelViewModel
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * This activity handles the printing of price labels for products.
 *
 * It allows users to scan or enter a product barcode, retrieves associated stock information,
 * and presents options for printing price labels.
 *
 * Key features include:
 *  - Scanning or manual entry of product barcodes.
 *  - Displaying a list of stock drafts associated with the product.
 *  - Navigation through stock drafts and selection for printing.
 *  - Error handling and display of error messages.
 *  - Custom back button handling.
 *  - UI implemented using Jetpack Compose.
 */
@AndroidEntryPoint
class PrintPriceLabel : ComponentActivity() {

    val viewModel: PrintPriceLabelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupExceptionHandler()
        setupContent()
        handleBackPress()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    /**
     * Sets up the default uncaught exception handler.
     */
    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()!!)
        )
    }

    /**
     * Sets up the content view with the Compose UI.
     */
    private fun setupContent() {
        setContent {
            Page(viewModel) {
                handleBackAction()
            }
        }
    }

    /**
     * Handles the back button press and updates the scanning mode.
     */
    private fun handleBackAction() {
        if (viewModel.scanningMode) {
            finish()
        } else {
            viewModel.scanningMode = true
            viewModel.productBarcode = ""
        }
    }

    /**
     * Sets up the back press dispatcher to handle custom back action.
     */
    private fun handleBackPress() {
        this.onBackPressedDispatcher.addCallback(this) {
            handleBackAction()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0 && keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackAction()
        }
        return true
    }

    /**
     * Main page composable that includes the scaffold and content.
     */
    @Composable
    fun Page(viewModel: PrintPriceLabelViewModel, onBackPressed: () -> Unit) {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = {
                        AppBarWithBack(
                            onBackPressed = { onBackPressed() },
                            title = stringResource(id = R.string.PrintPriceLabel)
                        )
                    },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            Content(
                                viewModel
                            )
                        }
                    },
                    snackbarHost = { ErrorSnackBar(viewModel.state) }
                )
            }
        }
    }

    /**
     * Content composable for rendering the main UI.
     */
    @Composable
    fun Content(viewModel: PrintPriceLabelViewModel) {
        Column {
            if (viewModel.loading) {
                LoadingCircularProgressIndicator(isDataLoading = true)
            } else {
                SimpleTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    hint = stringResource(id = R.string.scan_or_enter_barcode),
                    onValueChange = { viewModel.onTextValueChange(it) },
                    value = viewModel.productBarcode,
                    onDone = { viewModel.onTextFieldConfirm() }
                )
                LazyColumn {
                    items(viewModel.uiList.size) { index ->
                        val stockDraft = viewModel.uiList[index]
                        Item2(
                            clickable = true,
                            enableBottomSpace = index == viewModel.uiList.size - 1,
                            text1 = "حواله: ${stockDraft.number}",
                            text2 = "تاریخ: ${stockDraft.date}",
                            text3 = "از: ${viewModel.convertWarehouseCodeToText(stockDraft.source.toString())}",
                            text4 = "تعداد کالاها: ${stockDraft.numberOfItems}",
                            text5 = "به: ${viewModel.convertWarehouseCodeToText(stockDraft.destination.toString())}",
                            text6 = "تگ RFID: ${if (stockDraft.epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد"}",
                            text7 = "شرح: ${stockDraft.specification}",
                            onClick = {
                                viewModel.uiListOnClick(stockDraft.number.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}
