package com.jeanwest.reader.features.print.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.features.print.viewmodel.PrintPricePerProductViewModel
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item5
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for printing price labels per product within a stock draft.
 *
 * This activity displays a UI for printing price labels associated with individual products
 * within a specified stock draft.  It interacts with a [PrintPricePerProductViewModel] to
 * manage data and printing logic.
 *
 * Key features include:
 *  - Loading and displaying products from a stock draft based on the provided [StockDraftId].
 *  - Handling user interactions related to printing.
 *  - Gracefully handling lifecycle events (resume, pause, destroy).
 *  - Implementing a custom exception handler.
 *  - Overriding back button behavior for navigation.
 */
@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class PrintPricePerProduct : ComponentActivity() {

    val viewModel: PrintPricePerProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()

        val stockDraftId = intent.getStringExtra("StockDraftId").orEmpty()
        viewModel.initialize(stockDraftId)

        setContent { Page(viewModel, ::finish) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.handleResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.handlePause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handleDestroy()
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()!!)
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}


@OptIn(ExperimentalCoilApi::class)
@ExperimentalFoundationApi
@Composable
fun Page(
    viewModel: PrintPricePerProductViewModel,
    back: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { back() },
                        title = viewModel.title
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel = viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.snackBarHostState) },
                bottomBar = {},
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun Content(viewModel: PrintPricePerProductViewModel) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(
                isDataLoading = viewModel.loading
            )
        } else {

            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .border(
                        BorderStroke(1.dp, primaryLight),
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "شماره حواله: ${viewModel.stockDraftId}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = viewModel.stockDraftSpecification,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                    )
                }
            }

            LazyColumn(state = viewModel.listState) {
                items(viewModel.uiList.size) { i ->
                    Item5(
                        text1 = viewModel.uiList[i].productCode,
                        i = i,
                        uiList = viewModel.uiList,
                        text3 = "تعداد ${viewModel.uiList[i].scannedNumber} لیبل ",
                        text4 = "پرینت شود",

                        ) {
                        viewModel.onProductClick(viewModel.uiList[i], i)
                    }
                }
            }
        }
    }
}