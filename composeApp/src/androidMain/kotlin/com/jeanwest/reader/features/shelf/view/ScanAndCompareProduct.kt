package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.models.Product

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScanAndCompareProduct(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    uiListProduct: List<Product>,
    state: SnackbarHostState,
    onBottomBarClick: () -> Unit,
    popupState: NotificationPopupHost,
    bottomBarText: String,
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
                        uiListProduct = uiListProduct,
                        popupState = popupState,
                    )
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
    uiListProduct: List<Product>,
    popupState: NotificationPopupHost,
) {

    Column {
        if (loading) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)

            Text(
                text = "لطفا کالا های دارای موجودی فیزیکی را اسکن کنید.",
                style = MaterialTheme.typography.h1,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            Text(
                text = "مجموع اسکن: ${uiListProduct.sumOf { it.scannedBarcodeNumber }}",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            if (uiListProduct.isEmpty()) {
                EmptyBox("هنوز کالایی اسکن نکرده اید")
            } else {
                LazyColumn() {
                    items(uiListProduct.size) { i ->
                        LazyColumnItemProduct(i, uiListProduct)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItemProduct(
    i: Int,
    uiListProduct: List<Product>,
) {
    Item(
        i = i, uiList = uiListProduct,
        text3 = "تعداد: ${uiListProduct[i].requestedNumber}",
        text4 = "اسکن: ${uiListProduct[i].scannedNumber}",
        enableSign = false,
        enableRequestNumber = false
    )
}

@Preview
@Composable
fun Preview() {
    ScanAndCompareProduct(
        "برگشت کالا",
        {},
        false,
        listOf(
            Product(
                name = "اسلش  8170 XXXL",
                KBarCode = "43751458J-8170-XXXL",
                imageUrl = "https://www.banimode.com/jeanswest/image.php?token=tmv43w4as&code=43751458J-8170-XXXL",
                size = "XXXL",
                color = "8170"
            )
        ),
        SnackbarHostState(),
        {},
        NotificationPopupHost(),

        "برگشت کالاها"
    )
}