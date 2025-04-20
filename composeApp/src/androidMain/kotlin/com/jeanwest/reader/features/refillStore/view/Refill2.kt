package com.jeanwest.reader.features.refillStore.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.refillStore.viewmodel.RefillViewModel2
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.ItemNewRefill
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 *  Refill2 Activity.
 *
 *  This activity manages the refill process, including loading and saving data to memory,
 *  handling user interaction with the UI (provided by the `Page` composable), and managing activity lifecycle events.
 *  It uses a [RefillViewModel2] to handle the underlying logic and state.  It also includes an exception handler for unhandled exceptions.
 */
@AndroidEntryPoint
class Refill2 : ComponentActivity() {

    val viewModel: RefillViewModel2 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        viewModel.loadMemory()
        setContent {
            Page(viewModel, onBackPressed = {
                viewModel.saveToMemory()
                finish()
            })
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.saveToMemory()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 4) {
                finish()
            }
        }
        return true
    }

}

@Composable
fun Page(viewModel: RefillViewModel2, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        title = stringResource(id = R.string.refill2),
                        onBackPressed = { onBackPressed() })
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                bottomBar = { BottomBar(viewModel) },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
            )
        }
    }
}

@Composable
fun BottomBar(viewModel: RefillViewModel2) {
    if (!viewModel.loading) {
        if (viewModel.uiList.filter {
                it.scannedNumber > 0
            }.toList().isNotEmpty() && !viewModel.loading) {
            BottomBarButton("ارسال به فروشگاه") {
                viewModel.createStockDraft()
            }
        }
    }
}

@Composable
fun Content(viewModel: RefillViewModel2) {

    Column {

        if (viewModel.loading || viewModel.localStoreDatabase.loading) {

            LoadingCircularProgressIndicator(
                false,
                viewModel.loading || viewModel.localStoreDatabase.loading
            )

        } else {

            NotificationPopUp(viewModel.popupState)

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1.3F)
                ) {
                    FilterDropDownList(
                        icon = { }, text = {
                        Text(
                            text = viewModel.selectedDepartmentFilter,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp)
                        )
                    }, values = viewModel.departmentFilterList
                    ) {
                        viewModel.filterUiByDepName(it)
                    }
                }

                Text(
                    text = "خطی: ${
                        viewModel.uiList.size - viewModel.uiList.filter {
                            it.scannedNumber > 0
                        }.size
                    }",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1F),
                )
                Text(
                    text = "کل اسکن: ${viewModel.barcode.scannedBarcodes.size}",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1F),
                )
            }
            if (viewModel.uiList.isEmpty()) {
                EmptyBox(text = "کالایی در خطی وجود ندارد.")
            }
            LazyColumn(
                modifier = Modifier.testTag("RefillActivityLazyColumn"),
                state = viewModel.listState
            ) {
                items(viewModel.uiList.size) { i ->
                    LazyColumnItem(i, viewModel)
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(i: Int, viewModel: RefillViewModel2) {

    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        ItemNewRefill(
            i, viewModel.uiList, true,
            text3 = "درخواست: " + viewModel.uiList[i].requestedNumber.toString(),
            text4 = "انبار: " + viewModel.uiList[i].wareHouseNumber.toString(),
            enableSign = true,
            signNumber = viewModel.uiList[i].scannedNumber,
            enableWarehouseNumberCheck = true,
        ) {
            viewModel.openSearchActivity(viewModel.uiList[i])
        }

        if (viewModel.uiList[i].scannedNumber > 0) {
            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp),
                        color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .testTag("clear")
                    .clickable {
                        viewModel.clear(viewModel.uiList[i])
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                    contentDescription = "",
                    tint = errorLight,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
fun Content2(viewModel: RefillViewModel2) {
    Column {

        if (viewModel.loading || viewModel.localStoreDatabase.loading) {
            LoadingCircularProgressIndicator(
                false,
                viewModel.loading || viewModel.localStoreDatabase.loading
            )
        } else {

            NotificationPopUp(viewModel.popupState)
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {

                items(viewModel.uiList.filter {
                    it.scannedBarcodeNumber > 0
                }.size) { i ->
                    Item(
                        i,
                        viewModel.uiList.filter {
                            it.scannedBarcodeNumber > 0
                        }.toMutableList(),
                        text3 = "اسکن: " + viewModel.uiList.filter {
                            it.scannedBarcodeNumber > 0
                        }.toMutableList()[i].scannedNumber,
                        text4 = "انبار: " + viewModel.uiList.filter {
                            it.scannedBarcodeNumber > 0
                        }.toMutableList()[i].wareHouseNumber.toString(),
                        enableWarehouseNumberCheck = true,
                    )
                }
            }
        }
    }
}