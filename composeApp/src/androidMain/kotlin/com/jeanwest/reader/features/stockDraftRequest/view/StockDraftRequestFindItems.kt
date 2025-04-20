package com.jeanwest.reader.features.stockDraftRequest.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownListWithSearch
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.StockDraftRequestItem
import com.jeanwest.reader.features.shared.SurfaceWith2Columns5Rows
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.features.stockDraftRequest.viewmodel.StockDraftRequestFindViewModel
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.models.StockDraftRequestItem
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for finding items to add to a stock draft request.
 *
 * This activity handles user interaction for searching and selecting items
 * to be included in a stock draft request.  It uses a ViewModel to manage
 * the UI state and data operations.
 *
 * Key Features:
 * - Displays a search interface for finding items. (Implementation in the `Page` composable)
 * - Allows users to select items for the draft request. (Implementation likely within ViewModel and `Page`)
 * - Supports barcode scanning for item identification (via `scanTrigger()`).
 * - Handles back navigation and activity lifecycle events.
 * - Includes error handling using a custom `ExceptionHandler`.
 *
 * The UI is built using Jetpack Compose, specifically the `Page` composable.
 * Data and logic are managed by the `StockDraftRequestFindViewModel`.
 *
 *  **Note:** Replace placeholder imports (e.g., `com.example.yourproject...`) with the actual paths in your project.
 */
@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class StockDraftRequestFindItems : ComponentActivity() {

    val viewModel: StockDraftRequestFindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exceptionHandler()
        setContent {
            Page(viewModel) {
                if (viewModel.step != 1) {
                    viewModel.back()
                } else {
                    finish()
                }
            }
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
                viewModel.scanTrigger()
            } else if (keyCode == 4) {

                if (viewModel.step != 1) {
                    viewModel.back()
                } else {
                    finish()
                }
            }
        }
        return true
    }
}


@OptIn(ExperimentalCoilApi::class)
@ExperimentalFoundationApi
@Composable
fun Page(viewModel: StockDraftRequestFindViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { onBackPressed() },
                        stringResource(id = R.string.stockDraftRequestFind)
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.step) {
                            1 -> Content1(viewModel)
                            2 -> Content2(viewModel)
                            3 -> Content3(viewModel)
                            4 -> Content4(viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    if (viewModel.step == 4) BottomBar4(viewModel) else if (viewModel.step == 2) BottomBar2(
                        viewModel
                    )
                },
            )
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BottomBar4(viewModel: StockDraftRequestFindViewModel) {

    if (!viewModel.loading) {

        if (viewModel.uiItem4.value.product.scannedNumber > 0 && !viewModel.loading) {

            BottomBarButton("تایید اسکن شده ها") {
                viewModel.bottomBar4OnClick()
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun BottomBar2(viewModel: StockDraftRequestFindViewModel) {

    if (!viewModel.loading) {

        if (!viewModel.loading) {

            BottomBarButton("پایان جمع آوری درخواست") {
                viewModel.bottomBar2OnClick()
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content4(viewModel: StockDraftRequestFindViewModel) {

    Column {
        if (viewModel.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(false, viewModel.loading)
            }
        } else {
            Column(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, primaryLight),
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ),
            ) {
                ColumnItem(
                    viewModel.uiItem4.value,
                    viewModel.uiItem4Shelf.value,
                    onRemoveClick = { viewModel.onRemoveButtonClick() },
                    onAddClick = { viewModel.onAddButtonClick() })
            }
        }
    }
}

@Composable
fun ColumnItem(
    item: StockDraftRequestItem,
    uiItem4Shelf: ShelfBarcodeAddress,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {

    val modifier = Modifier
        .padding(top = 2.dp, bottom = 2.dp)
        .wrapContentWidth()

    Column {

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp),
        ) {

            Box(modifier = Modifier.size(width = 180.dp, height = 200.dp)) {
                AsyncImage(
                    model = item.product.imageUrl, contentDescription = null, modifier = Modifier
                        .height(200.dp)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }

            Column {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = item.product.KBarCode,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "رنگ: " + item.product.color,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "سایز: " + item.product.size,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "تعداد درخواستی: " + item.notFoundNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )

                Text(
                    text = "قفسه: " + uiItem4Shelf.shelfID,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )

                Text(
                    text = "موجودی قفسه: " + uiItem4Shelf.qty,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
                Text(
                    text = "اسکن شده: ${item.product.scannedNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Right,
                    modifier = modifier,
                )
            }
        }

        if (item.product.scannedNumber > 0) {

            Row(
                modifier = modifier
                    .padding(top = 32.dp, bottom = 32.dp)
                    .align(Alignment.CenterHorizontally)
            ) {

                OutlinedIconButton(onClick = onAddClick) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "")
                }

                Text(
                    text = item.product.scannedNumber.toString(),
                    Modifier
                        .padding(start = 24.dp)
                        .align(Alignment.CenterVertically)
                )

                OutlinedIconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "")
                }
            }
        }
    }
}

@Preview
@Composable
fun Preview1() {

    val product = Product(
        name = "ساپورت",
        KBarCode = "64822109J-8010-F",
        imageUrl = "https://www.banimode.com/primaryLight/image.php?token=tmv43w4as&code=64822109J-8010-F",
        storeNumber = 1,
        wareHouseNumber = 0,
        productCode = "64822109",
        size = "F",
        color = "8010",
        originalPrice = "1490000",
        salePrice = "1490000",
        primaryKey = 9514289L,
        rfidKey = 130290L,
        scannedBarcodeNumber = 5
    )

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { },
                        stringResource(id = R.string.stockDraftRequestFind)
                    )
                },
                content = { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        ColumnItem(
                            item = StockDraftRequestItem(
                                product = product,
                                KBarcode = "64822109J-8010-F",
                                requestNumber = 15,
                                shelfID = "SH010203"
                            ),
                            uiItem4Shelf = ShelfBarcodeAddress(
                                shelfID = "SH010203"
                            ),
                            onAddClick = {},
                            onRemoveClick = {}
                        )
                    }
                },
            )
        }
    }

}

@ExperimentalFoundationApi
@Composable
fun Content3(viewModel: StockDraftRequestFindViewModel) {
    Column {
        if (viewModel.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(false, viewModel.loading)
            }
        } else {
            if (viewModel.uiList3.isEmpty()) {
                EmptyBox("این کالا در هیچ قفسه ای وجود ندارد")
            } else {
                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {
                    items(viewModel.uiList3.size) { i ->
                        Box {
                            Item4(
                                clickable = true,
                                enableBottomSpace = i == viewModel.uiList3.size - 1,
                                text1 = "انبار: " + viewModel.uiList3[i].wareHouseTitle,
                                text2 = "قفسه: " + viewModel.uiList3[i].shelfID,
                                text3 = "تعداد: " + viewModel.uiList3[i].qty,
                                text4 = "سایز: " + viewModel.uiList3[i].size,
                                onClick = {
                                    Log.e("JamAvari", viewModel.uiList3[i].toString())
                                    viewModel.uiList3OnClick(viewModel.uiList3[i])
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun Content2(viewModel: StockDraftRequestFindViewModel) {
    LaunchedEffect(viewModel.isRefresh) {
        if (viewModel.isRefresh) {
            viewModel.getStockDraftRequestDetails(viewModel.stockDraftRequestNumber)
            viewModel.isRefresh = false
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(viewModel.isRefresh),
        onRefresh = {
            viewModel.isRefresh = true
        }
    ) {
        Column {

            if (viewModel.scanning || viewModel.loading) {
                LoadingCircularProgressIndicator(
                    isScanning = viewModel.scanning,
                    isDataLoading = viewModel.loading
                )
            } else {

                NotificationPopUp(state = viewModel.popupState)

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
                            text = "تعداد کل: ${viewModel.requestedNumber}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                                .weight(1F),
                        )
                        Text(
                            text = "جور شده: ${viewModel.foundNumber}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )
                        Text(
                            text = "جور نشده: ${viewModel.notFoundNumber}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1F),
                        )
                    }
                }

                LazyColumn {

                    items(viewModel.uiList2.size) { i ->
                        StockDraftRequestItem(
                            item = viewModel.uiList2[i],
                            clickable = true,
                            text3 = viewModel.uiList2[i].shelfCode,
                            text4 = "درخواستی: " + viewModel.uiList2[i].notFoundNumber,
                            enableBottomSpace = i == viewModel.uiList2.size - 1,
                            enableTopSpace = i == 0,
                        ) {
                            viewModel.uiList2OnClick(viewModel.uiList2[i])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Content1(viewModel: StockDraftRequestFindViewModel) {

    if (viewModel.loading) {
        LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
    } else {

        Column(modifier = Modifier.fillMaxSize()) {
            if (viewModel.openUpdateUserDialog) {
                AlertDialogWith2Button(
                    title = "درخواست به شما اختصاص داده شود؟",
                    btnNotConfirm = "خیر",
                    btnConfirm = "بله",
                    btnConfirmOnClick = {
                        viewModel.updateUserDialogOnConfirm()
                    },
                    btnNotConfirmOnClick = { viewModel.updateUserDialogOnNotConfirmOrDismiss() },
                    onDismiss = { viewModel.updateUserDialogOnNotConfirmOrDismiss() }
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 0.dp)
                    .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SimpleTextField(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .weight(1F)
                            .fillMaxWidth(),
                        hint = "جستجو مقصد یا کد درخواست",
                        onValueChange = { viewModel.onSearchTextFieldChange(it) },
                        value = viewModel.stockDraftRequestSearchKey
                    )
                }

                Row {
                    FilterDropDownListWithSearch(
                        modifier = Modifier
                            .padding(bottom = 12.dp, start = 16.dp),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_location_city_24),
                                contentDescription = "",
                                tint = primaryLight,
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        text = {
                            Text(
                                text = viewModel.stockDraftRequestLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 6.dp)
                            )
                        },
                        onClick = {
                            viewModel.stockDraftRequestDropDownFilterOnClick(it)
                        },
                        values = viewModel.stockDraftRequestLocationsList
                    )
                }
            }

            val filteredUiList = if (viewModel.stockDraftRequestLocation == "انتخاب مقصد") {
                viewModel.uiList1
            } else {
                viewModel.uiList1.filter {
                    it.destination.startsWith(viewModel.stockDraftRequestLocation)
                }
            }

            val filteredUiList2 = if (viewModel.stockDraftRequestSearchKey != "") {
                if (viewModel.stockDraftRequestSearchKey.toLongOrNull() == null) {
                    filteredUiList.filter {
                        it.destination.contains(viewModel.stockDraftRequestSearchKey)
                    }
                } else {
                    filteredUiList.filter {
                        it.number.toString().contains(viewModel.stockDraftRequestSearchKey)
                    }
                }
            } else {
                filteredUiList
            }

            if (filteredUiList2.isEmpty()) {
                EmptyBox(text = "هیچ درخواستی برای فیلتر انتخاب شده وجود ندارد.")
            } else {
                LazyColumn {

                    items(filteredUiList2.size) { i ->
                        SurfaceWith2Columns5Rows(
                            clickable = true,
                            enableBottomSpace = i == filteredUiList2.size - 1,
                            text1 = "درخواست: " + filteredUiList2[i].number,
                            text2 = "درخواستی: " + filteredUiList2[i].sumOfRequestedItems,
                            text3 = "مقصد: " + filteredUiList2[i].destination,
                            text4 = "جور شده: " + filteredUiList2[i].sumOfFoundItems,
                            text8 = "جمع کننده: " + if (filteredUiList2[i].collectorName == "null") "-" else filteredUiList2[i].collectorName,
                            text5 = "تاریخ: " + filteredUiList2[i].createDateJalali,
                            text7 = "شرح: " + filteredUiList2[i].specification,
                            text6 = "",
                        ) {
                            viewModel.uiList1OnClick(filteredUiList2[i])
                        }
                    }
                }
            }
        }
    }
}