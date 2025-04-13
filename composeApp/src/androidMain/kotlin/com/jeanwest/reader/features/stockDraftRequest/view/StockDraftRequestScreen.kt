package com.jeanwest.reader.features.stockDraftRequest.view

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.models.StockDraftRequest

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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
                    Content(
                        loading = loading,
                        uiListProduct = stockDraftRequestsList,
                        popupState = popupState,
                        uiListOnClick = {
                            uiListOnClick(it)
                        }
                    )
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