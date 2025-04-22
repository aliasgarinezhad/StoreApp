@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.stockDraft.view

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.features.shared.AlertDialogWith2Button
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButtonDropDownList
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.StockDraft
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateStockDraftFromCartonView @Inject constructor(
    @set:Inject
    var state: SnackbarHostState,
    @set:Inject
    var memory: SharedPreference,
) : ComponentActivity() {
    var back: () -> Unit = {}
    var clear: () -> Unit = {}
    var clearItem: (cartonId: String) -> Unit = {}
    var title: String = "ایجاد حواله از کارتن"
    var loading by mutableStateOf(false)
    var enterBarcodeOnClick: (barcode: String) -> Unit = {}
    var createSack: () -> Unit = {}
    var scanningMode by mutableStateOf(true)
    var openClearDialog by mutableStateOf(false)
    var cartonUiList = mutableStateListOf<Carton>()
    var creatingStockDraft by mutableStateOf(false)
    var openSendDialog by mutableStateOf(false)
    var stockDraftIDs = mutableListOf<String>()
    var stockDraftNumber by mutableStateOf("")
    val listState = LazyListState(0)
    var draftsMap = mutableStateMapOf<String, StockDraft>()
    var openPrintDialog by mutableStateOf(false)
    var printers = mutableStateMapOf<String, Int>()
    var printer by mutableStateOf("")
    var popupState = NotificationPopupHost()
}

/**
 * Composable function that represents the main page of the "Create Stock Draft From Carton" screen.
 * It provides the overall layout structure including top app bar, content area, bottom bar, and snackbar.
 *
 * The function utilizes `Scaffold` from Material3 for layout management.
 *  - `topBar`:  Displays an [AppBar] composable, providing the top navigation bar.  The AppBar's content is determined by the provided [CreateStockDraftFromCartonView].
 *  - `content`:  Renders the main content area using the [Content] composable.  It is enclosed in a `Box` with padding to account for the top bar and bottom bar. The content is also determined by the provided [CreateStockDraftFromCartonView].
 *  - `bottomBar`: Shows the [BottomBar] composable, typically containing actions or navigation elements. Its content is determined by the provided [CreateStockDraftFromCartonView].
 *  - `snackbarHost`:  Handles displaying snackbars for error messages using the [ErrorSnackBar] composable.  The snackbar's visibility and content are controlled by the `state` within the provided [CreateStockDraftFromCartonView].
 *
 * The layout direction is set to right-to-left (RTL) using `CompositionLocalProvider` and `LocalLayoutDirection`, catering to languages that read from right to left.
 *
 * @param viewModel The [CreateStockDraftFromCartonView] instance, which acts as the ViewModel for this screen and provides data and interactions for the UI elements (AppBar, Content, BottomBar, and snackbar).
 */
@ExperimentalFoundationApi
@Composable
fun Page(viewModel: CreateStockDraftFromCartonView) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBar(viewModel) },
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
fun AppBar(viewModel: CreateStockDraftFromCartonView) {
    if (viewModel.cartonUiList.isNotEmpty()) {
        AppBarWithDeleteButton(title = stringResource(id = R.string.createStockDraftFromCarton),
            onDeletePressed = { viewModel.openClearDialog = true },
            onBackPressed = { viewModel.back() }
        )
    } else {
        AppBarWithBack(
            title = stringResource(id = R.string.createStockDraftFromCarton),
            onBackPressed = { viewModel.back() })
    }
}

@Composable
fun BottomBar(viewModel: CreateStockDraftFromCartonView) {
    if (!viewModel.loading && viewModel.cartonUiList.isNotEmpty()) {
        BottomBarButton(text = if (viewModel.creatingStockDraft) "در حال ثبت ..." else " تعداد ${viewModel.cartonUiList.size} کارتن حواله شود") {
            if (!viewModel.creatingStockDraft) {
                viewModel.openPrintDialog = true
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(viewModel: CreateStockDraftFromCartonView) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {

            if (viewModel.openPrintDialog) {
                val filteredPrinterList = viewModel.printers.keys.toMutableList()
                AlertDialogWithHeadlineMediumButtonDropDownList(
                    title = "لطفا پرینتر مورد نظر خود را مشخص کنید",
                    btnTxt = "پرینت",
                    btnOnClick = {
                        viewModel.openPrintDialog = false
                        viewModel.createSack()
                    },
                    dropDownText = viewModel.printer,
                    onDismiss = { viewModel.openPrintDialog = false },
                    dropDownRes = filteredPrinterList,
                    onSelectItem = { viewModel.printer = it }
                )
            }

            NotificationPopUp(state = viewModel.popupState)

            if (viewModel.openClearDialog) {
                AlertDialogWith2Button(
                    "کارتن های اسکن شده پاک شوند؟",
                    "بله",
                    "خیر",
                    btnConfirmOnClick = {
                        viewModel.clear()
                        viewModel.openClearDialog = false
                    },
                    btnNotConfirmOnClick = { viewModel.openClearDialog = false },
                    onDismiss = { viewModel.openClearDialog = false })
            }

            if (viewModel.openSendDialog) {
                BasicAlertDialog(
                    onDismissRequest = {
                        viewModel.loading = false
                        viewModel.creatingStockDraft = false
                        viewModel.clear()
                        viewModel.openSendDialog = false
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 120.dp)
                                .wrapContentHeight()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {

                            Text(
                                text = " تعداد ${viewModel.cartonUiList.size} حواله با موفقیت ایجاد شد.",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Button(
                                onClick = {
                                    viewModel.openSendDialog = false
                                    viewModel.loading = false
                                    viewModel.creatingStockDraft = false
                                    viewModel.clear()
                                },
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("alertBtn")
                            ) {
                                Text(text = "باشه", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .shadow(6.dp, Shapes.medium)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        )
                        .fillMaxWidth(),
                ) {

                    Row {
                        SimpleTextField(
                            value = viewModel.stockDraftNumber,
                            hint = "شماره کارتن",
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp,
                                    top = 12.dp
                                )
                                .fillMaxWidth(),
                            onValueChange = {
                                viewModel.stockDraftNumber = it
                            },
                            onDone = {
                                viewModel.enterBarcodeOnClick(viewModel.stockDraftNumber)
                            })
                    }
                }

                if (viewModel.cartonUiList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 56.dp)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(256.dp)
                        ) {
                            Box(

                                modifier = Modifier
                                    .background(color = Color.White, shape = Shapes.medium)
                                    .size(256.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_empty_box),
                                    contentDescription = "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Text(
                                "هنوز حواله ای اسکن نکرده اید",
                                style = Typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(
                                        top = 16.dp,
                                        start = 4.dp,
                                        end = 4.dp
                                    )
                                    .align(Alignment.CenterHorizontally),
                            )
                        }
                    }
                } else {

                    LazyColumn(
                        modifier = Modifier.padding(top = 4.dp),
                        state = viewModel.listState
                    ) {

                        items(viewModel.cartonUiList.size) { i ->

                            Box {

                                Item2(
                                    clickable = false,
                                    enableBottomSpace = i == viewModel.cartonUiList.size - 1,
                                    text1 = viewModel.cartonUiList[i].number,
                                    text2 = "تنوع جنس: " + viewModel.cartonUiList[i].barcodeTable.distinct().size,
                                    text3 = "مبدا: " + viewModel.memory.erpData.warehousesIDsToTitles[viewModel.cartonUiList[i].operationSource],
                                    text4 = "جمع اجناس: " + viewModel.cartonUiList[i].numberOfItems,
                                    text5 = "مقصد: " + if (viewModel.memory.erpData.warehousesIDsToTitles[viewModel.cartonUiList[i].operationDes] == null) "-" else viewModel.memory.erpData.warehousesIDsToTitles[viewModel.cartonUiList[i].operationDes],
                                    text6 = "تاریخ: " + viewModel.cartonUiList[i].date,
                                    text7 = "شرح: " + if (viewModel.cartonUiList[i].specification == "null") "بدون شرح" else viewModel.cartonUiList[i].specification,
                                    colorFull = viewModel.cartonUiList[i].isConfirmed,
                                )
                                /*
                                                                Item4(
                                                                    clickable = false,
                                                                    enableBottomSpace = i == viewModel.cartonUiList.size - 1,
                                                                    text1 = viewModel.cartonUiList[i].number,
                                                                    text4 = viewModel.memory.erpData.warehousesIDsToTitles[viewModel.cartonUiList[i].source].toString(),
                                                                    text2 = "تاریخ: " + viewModel.cartonUiList[i].date,
                                                                    text3 = "تعداد کالاها: " + viewModel.cartonUiList[i].numberOfItems
                                                                )

                                 */
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp, end = 8.dp)
                                        .background(
                                            shape = RoundedCornerShape(36.dp),
                                            color = errorContainerLight
                                        )
                                        .size(30.dp)
                                        .align(Alignment.TopEnd)
                                        .testTag("clear")
                                        .clickable {
                                            viewModel.clearItem(viewModel.cartonUiList[i].number)
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
                }
            }
        }
    }
}