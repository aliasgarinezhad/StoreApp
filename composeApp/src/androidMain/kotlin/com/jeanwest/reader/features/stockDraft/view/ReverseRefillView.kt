package com.jeanwest.reader.features.stockDraft.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.view.AlertDialogWith2Button
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.AppBarWithDeleteButton
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.ItemReverseRefill
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.Barcode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReverseRefillViewModel @Inject constructor(
    @set:Inject
    var state: SnackbarHostState,
) : ComponentActivity() {
    var back: () -> Unit = {}
    var deleteAll: () -> Unit = {}
    var title: String = "ارسال کالا به دپو"
    var loading by mutableStateOf(false)
    var localServerLoading by mutableStateOf(false)
    var uiList = mutableStateListOf<Product>()
    var uiListOnClick: (product: Product) -> Unit = { _ -> }
    var deleteItem: (product: Product) -> Unit = { _ -> }
    var bottomBarOnClick: () -> Unit = { }
    var clear: () -> Unit = { }
    var disableBarcode: () -> Unit = { }
    var scanningMode by mutableStateOf(true)
    var popupState = NotificationPopupHost()
    var openClearDialog by mutableStateOf(false)
    lateinit var barcode: Barcode

}

/**
 * Composable function that represents the main page of the Reverse Refill application.
 * It manages the overall layout, including the top app bar, content area, snackbar for error messages,
 * and the bottom navigation bar.  It also handles RTL layout direction and applies the app's theme.
 *
 * The content of the page switches between two different composables, `Content` and `Content2`,
 * based on the `scanningMode` state within the `ReverseRefillViewModel`.
 *
 * @param viewModel The [ReverseRefillViewModel] instance that holds the application's state
 * and logic.  This ViewModel is the single source of truth for data and interactions within
 * the page.
 */

@ExperimentalFoundationApi
@Composable
fun Page(viewModel: ReverseRefillViewModel) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBar(viewModel) },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (viewModel.scanningMode) Content(viewModel) else Content2(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = { BottomBar(viewModel) },
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(viewModel: ReverseRefillViewModel) {
    Column {
        if (viewModel.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(
                    false,
                    viewModel.loading || viewModel.localServerLoading
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
            ) {
                Text(
                    text = "مجموع: " + (viewModel.barcode.scannedBarcodes.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
            }

            if (viewModel.uiList.isEmpty()) {
                EmptyBox("هنوز کالایی برای ارسال اسکن نکرده اید")
            } else {
                if (viewModel.openClearDialog) {
                    AlertDialogWith2Button(
                        "کالاهای اسکن شده پاک شوند؟",
                        "بله",
                        "خیر",
                        btnConfirmOnClick = {
                            viewModel.clear()
                            viewModel.openClearDialog = false
                        },
                        btnNotConfirmOnClick = { viewModel.openClearDialog = false },
                        onDismiss = { viewModel.openClearDialog = false })
                }
                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                    items(viewModel.uiList.size) { i ->
                        LazyColumnItem(viewModel, i)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(viewModel: ReverseRefillViewModel, i: Int) {

    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {

        ItemReverseRefill(
            i, viewModel.uiList, true,
            text3 = "اسکن: " + viewModel.uiList[i].scannedNumber,
            text4 = "فروشگاه: " + viewModel.uiList[i].storeNumber,
            colorFull = viewModel.uiList[i].scannedNumber >= viewModel.uiList[i].storeNumber,
            enableWarehouseNumberCheck = true,
        ) {
            viewModel.uiListOnClick(viewModel.uiList[i])
        }

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
                    viewModel.deleteItem(viewModel.uiList[i])
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

@Composable
fun Content2(viewModel: ReverseRefillViewModel) {

    Column {

        if (viewModel.loading) {

            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(
                    false,
                    viewModel.loading || viewModel.localServerLoading
                )
            }
        } else {

            NotificationPopUp(viewModel.popupState)

            if (viewModel.uiList.filter {
                    it.scannedBarcodeNumber > 0
                }.toMutableList().isEmpty()) {
                EmptyBox(text = "هنوز کالایی برای ارسال به انبار اسکن نکرده اید")
            } else {

                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {

                    items(viewModel.uiList.filter {
                        it.scannedBarcodeNumber > 0
                    }.size) { i ->
                        ItemReverseRefill(
                            i,
                            viewModel.uiList.filter {
                                it.scannedBarcodeNumber > 0
                            }.toMutableList(),
                            text3 = "اسکن: " + viewModel.uiList.filter {
                                it.scannedBarcodeNumber > 0
                            }.toMutableList()[i].scannedNumber,
                            text4 = "فروشگاه: " + viewModel.uiList.filter {
                                it.scannedBarcodeNumber > 0
                            }.toMutableList()[i].storeNumber.toString(),
                            enableWarehouseNumberCheck = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(viewModel: ReverseRefillViewModel) {

    if (viewModel.scanningMode && viewModel.uiList.isNotEmpty()) {
        AppBarWithDeleteButton(
            title = stringResource(id = R.string.reverseRefill),
            onBackPressed = viewModel.back,
            onDeletePressed = viewModel.deleteAll
        )
    } else {
        AppBarWithBack(
            title = stringResource(id = R.string.reverseRefill),
            onBackPressed = viewModel.back
        )
    }
}

@Composable
fun BottomBar(viewModel: ReverseRefillViewModel) {

    if (!viewModel.loading) {
        if (viewModel.uiList.filter { it1 ->
                it1.scannedNumber > 0
            }.toList().isNotEmpty() && !viewModel.loading) {
            if (viewModel.scanningMode) {

                BottomBarButton(text = "ارسال به انبار") {
                    viewModel.scanningMode = false
                    viewModel.disableBarcode
                }
            } else {
                BottomBarButton("ارسال به انبار") {
                    viewModel.bottomBarOnClick()
                }
            }
        }
    }
}