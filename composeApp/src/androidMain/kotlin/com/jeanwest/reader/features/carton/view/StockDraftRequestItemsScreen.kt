package com.jeanwest.reader.features.carton.view


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.StockDraftRequestItem
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.StockDraftRequestItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StockDraftRequestItemScreen(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    uiListProduct: List<StockDraftRequestItem>,
    state: SnackbarHostState,
    onBottomBarClick: () -> Unit,
    popupState: NotificationPopupHost,
    bottomBarText: String,
    onProductClick: (stockDraftRequestItem: StockDraftRequestItem) -> Unit,
    stockDraftRequestNumber: Long,
    shortageNumber: Int,
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
                            uiListProduct = uiListProduct,
                            popupState = popupState,
                            onProductClick = onProductClick,
                            stockDraftRequestNumber = stockDraftRequestNumber,
                            shortageNumber = shortageNumber
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = {
                    BottomBarButton(text = bottomBarText, onClick = onBottomBarClick)
                }
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    loading: Boolean,
    uiListProduct: List<StockDraftRequestItem>,
    popupState: NotificationPopupHost,
    onProductClick: (stockDraftRequestItem: StockDraftRequestItem) -> Unit,
    stockDraftRequestNumber: Long,
    shortageNumber: Int,
) {

    Column {
        if (loading) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)
            Column {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "درخواست: $stockDraftRequestNumber",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .wrapContentWidth()
                    )
                    Text(
                        text = "وارد نشده: $shortageNumber",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(end = 32.dp)
                    )
                }
            }
            if (uiListProduct.isNotEmpty()) {
                LazyColumn {

                    items(uiListProduct.size) { i ->
                        StockDraftRequestItem(
                            item = uiListProduct[i],
                            clickable = true,
                            text3 = "سایز: " + uiListProduct[i].product.size,
                            text4 = "وارد نشده: " + uiListProduct[i].notFoundNumber,
                            enableBottomSpace = i == uiListProduct.size - 1,
                            enableTopSpace = i == 0
                        ) {
                            onProductClick(uiListProduct[i])
                        }
                    }
                }
            } else {
                EmptyBox("هنوز کالایی اسکن نکرده اید")
            }
        }
    }
}

@Preview
@Composable
fun PreviewStockDraftRequestItemScreen() {
    StockDraftRequestItemScreen(
        "انتخاب کالا",
        {},
        false,
        listOf(
            StockDraftRequestItem(
                Product(
                    name = "اسلش  8170 XXXL",
                    KBarCode = "43751458J-8170-XXXL",
                    imageUrl = "https://www.banimode.com/jeanswest/image.php?token=tmv43w4as&code=43751458J-8170-XXXL",
                    size = "XXXL",
                    color = "8170"
                ), requestNumber = 10, foundNumber = 0

            ),
            StockDraftRequestItem(
                Product(
                    name = "اسلش  8170 XXXL",
                    KBarCode = "43751458J-8170-XXXL",
                    imageUrl = "https://www.banimode.com/jeanswest/image.php?token=tmv43w4as&code=43751458J-8170-XXXL",
                    size = "XXXL",
                    color = "8170"
                ), requestNumber = 10, foundNumber = 0

            ),
        ),
        SnackbarHostState(),
        {},
        NotificationPopupHost(),
        "پایان درخواست",
        {},
        31000254,
        10
    )
}