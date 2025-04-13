package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.useCases.RFID
import com.jeanwest.reader.features.shared.ItemShelfItems


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShelfItemsScreen(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    uiListProduct: List<ShelfItem>,
    state: SnackbarHostState,
    onBottomBarClick: () -> Unit,
    popupState: NotificationPopupHost,
    bottomBarText: String,
    isSecondPage: Boolean,
    itemOnClick: (shelfItem: ShelfItem) -> Unit,
    signedKBarCode: MutableList<ShelfItem>,
    completeRfScan: Boolean,
    rfid: RFID,
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
                    ContentShelfScreen(
                        loading = loading,
                        uiListProduct = uiListProduct,
                        popupState = popupState,
                        isSecondPage = isSecondPage,
                        itemOnClick = itemOnClick,
                        signedKBarCode = signedKBarCode,
                        rfid = rfid
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = {
                    if (isSecondPage) {
                        if (completeRfScan) {
                            BottomBarButton(text = bottomBarText, onClick = onBottomBarClick)
                        }
                    } else {
                        if (signedKBarCode.isNotEmpty()) {
                            BottomBarButton(text = bottomBarText, onClick = onBottomBarClick)
                        }
                    }
                }
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun ContentShelfScreen(
    loading: Boolean,
    uiListProduct: List<ShelfItem>,
    popupState: NotificationPopupHost,
    isSecondPage: Boolean,
    itemOnClick: (shelfItem: ShelfItem) -> Unit,
    signedKBarCode: MutableList<ShelfItem>,
    rfid: RFID,
) {
    Column {
        if (loading || rfid.scanning) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)
            if (isSecondPage) {
                Text(
                    text = "لطفا کالا های مورد نظر برای خروج از قفسه را اسکن کنید",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                Text(
                    text = "مجموع اسکن: ${
                        uiListProduct.filter { it in signedKBarCode }
                            .sumOf { it.product.scannedBarcodeNumber }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }
            if (uiListProduct.isEmpty()) {
                EmptyBox("هنوز کالایی اسکن نکرده اید")
            } else {
                LazyColumn() {
                    items(uiListProduct.size) { i ->
                        LazyColumnItemProductShelf(
                            i,
                            uiListProduct,
                            isSecondPage,
                            itemOnClick,
                            signedKBarCode
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItemProductShelf(
    i: Int,
    uiListProduct: List<ShelfItem>,
    isSecondPage: Boolean,
    itemOnClick: (shelfItem: ShelfItem) -> Unit,
    signedKBarCode: MutableList<ShelfItem>,
) {
    ItemShelfItems(
        i = i, uiList = uiListProduct,
        text3 = "تعداد: ${uiListProduct[i].qtyInShelf}",
        text4 = if (isSecondPage) "اسکن: ${uiListProduct[i].product.scannedNumber}" else uiListProduct[i].product.size,
        enableSign = false,
        enableRequestNumber = false,
        clickable = true,
        onClick = { itemOnClick(uiListProduct[i]) },
        signedKBarCode = signedKBarCode
    )
}

@Preview
@Composable
private fun PreviewShelfItemsScreen() {
//    ShelfItemsScreen(
//        "اسکن قفسه دوم",
//        {},
//        false,
//        listOf(
//            ShelfItem(
//                Product(
//                    name = "اسلش  8170 XXXL",
//                    KBarCode = "43751458J-8170-XXXL",
//                    imageUrl = "https://www.banimode.com/jeanswest/image.php?token=tmv43w4as&code=43751458J-8170-XXXL",
//                    size = "XXXL",
//                    color = "8170"
//                ), "SHBR1212", listOf("kjsdhkuh"), 21
//            )
//        ),
//        SnackbarHostState(),
//        {},
//        NotificationPopupHost(),
//
//        "اسکن قفسه دوم",
//        false,
//        {},
//
//        mutableListOf("43751458J-8170-XXXL"),
//        true,
//        rfid = RFID()
//    )
}