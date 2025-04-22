package com.jeanwest.reader.features.shelf.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.jeanwest.reader.features.shared.ItemShelfItems
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.useCases.RFID


/**
 * Composable function for displaying a screen showing items on a shelf.
 *
 * This screen includes a top app bar with a back button, a content area displaying the shelf items,
 * a snackbar for error messages, and a bottom bar with a button for actions like submitting or completing the process.
 *
 * @param topBarTitle The title to be displayed in the top app bar.
 * @param topBarOnClick Callback function to be executed when the back button in the top app bar is clicked.
 * @param loading Boolean flag indicating whether the screen is in a loading state.  A loading indicator should be displayed if true.
 * @param uiListProduct List of [ShelfItem] objects representing the items on the shelf to be displayed.
 * @param state [SnackbarHostState] for managing and displaying snackbar messages.
 * @param onBottomBarClick Callback function to be executed when the button in the bottom bar is clicked.
 * @param popupState [NotificationPopupHost] for managing and displaying popup notifications.
 * @param bottomBarText The text to be displayed on the button in the bottom bar.
 * @param isSecondPage Boolean flag indicating if this is the second page in a multi-step process. This affects the bottom bar button's visibility.
 * @param itemOnClick Callback function to be executed when a shelf item is clicked. It receives the clicked [ShelfItem] as a parameter.
 * @param signedKBarCode Mutable list of [ShelfItem] objects representing items signed with a specific barcode (likely a KBar */
@OptIn(ExperimentalFoundationApi::class)
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
    text1: String,
    text2: String,
    specText: String = "",
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
                        ContentShelfScreen(
                            loading = loading,
                            uiListProduct = uiListProduct,
                            popupState = popupState,
                            isSecondPage = isSecondPage,
                            itemOnClick = itemOnClick,
                            signedKBarCode = signedKBarCode,
                            rfid = rfid,
                            text1 = text1,
                            text2 = text2,
                            specText = specText
                        )
                    }
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
    text1: String,
    text2: String,
    specText: String = "",
) {
    Column {
        if (loading || rfid.scanning) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)
            if (isSecondPage) {

                if (specText != "") {
                    Text(
                        text = specText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = text1,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                    Text(
                        text = text2,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )
                }
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
//        rfid = RFID(),
//        text1 = "",
//        text2 = ""
//    )
}