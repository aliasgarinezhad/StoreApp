package com.jeanwest.reader.features.stockDraftRequest.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item4
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.models.StockDraftRequest

/**
 * A composable function that displays a screen listing stock draft requests.
 *
 * @param topBarTitle The title to display in the top app bar.
 * @param topBarOnClick The callback function to be executed when the back button in the top app bar is clicked.
 * @param loading A boolean value indicating whether the screen is in a loading state.  If true, a loading indicator should be displayed within the Content composable.
 * @param stockDraftRequestsList A list of [StockDraftRequest] objects representing the stock draft requests to be displayed.
 * @param uiListOnClick A callback function that is called when a stock draft request in the list is clicked.  It receives the clicked [StockDraftRequest] as a parameter.
 * @param state A [SnackbarHostState] object used to manage the display of Snackbars for error messages or other notifications.
 * @param popupState A [NotificationPopupHost] object used to manage the display of popup notifications (if any) within the content area.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StockDraftRequestScreen(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    stockDraftRequestsList: List<StockDraftRequest>,
    uiListOnClick: (stockDraftRequest: StockDraftRequest) -> Unit,
    state: SnackbarHostState,
    popupState: NotificationPopupHost,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        title = topBarTitle,
                        onBackPressed = topBarOnClick
                    )
                },
                content = {
                    Box(Modifier.padding(it)) {
                        Content(
                            loading = loading,
                            uiListProduct = stockDraftRequestsList,
                            popupState = popupState,
                            uiListOnClick = {
                                uiListOnClick(it)
                            }
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    loading: Boolean,
    uiListProduct: List<StockDraftRequest>,
    uiListOnClick: (stockDraftRequest: StockDraftRequest) -> Unit,
    popupState: NotificationPopupHost,
) {
    Column {
        if (loading) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)
            if (uiListProduct.isEmpty()) {
                EmptyBox("درخواستی برای ورود کالا به قفسه برای شما وجود ندارد")
            } else {
                LazyColumn {
                    items(uiListProduct.size) { i ->
                        Item4(
                            clickable = true,
                            enableBottomSpace = i == uiListProduct.size - 1,
                            text1 = "درخواست: " + uiListProduct[i].number,
                            text2 = "تاریخ: " + uiListProduct[i].date,
                            text3 = "وارد شده: " + uiListProduct[i].sumOfFoundItems,
                            text4 = "تعداد: " + uiListProduct[i].sumOfRequestedItems,
                        ) {
                            uiListOnClick(uiListProduct[i])
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewShelfEnterScreen() {
    StockDraftRequestScreen(
        "درخواست های اخیر", {}, false, listOf(
            StockDraftRequest(number = 219838L)

        ), {}, SnackbarHostState(), NotificationPopupHost()
    )
}