package com.jeanwest.reader.features.shelf.newShelfIn.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.FilterDropDownList
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shelf.newShelfIn.model.RequestType
import com.jeanwest.reader.models.Carton
import com.jeanwest.reader.models.Product

/**
 * A composable function that displays a screen for scanning cartons and barcodes.  It provides UI elements
 * for user input, displays scanned items, and allows for filtering and submission of the scanned data.
 *
 * @param topBarTitle The title displayed in the top app bar.
 * @param topBarOnClick Callback function invoked when the back button in the top bar is clicked.
 * @param loading Boolean flag indicating whether a loading indicator should be displayed.
 * @param uiListProduct A list of [Product] objects representing scanned products.
 * @param state A [SnackbarHostState] to manage and display snackbar messages.
 * @param inputValue The current value of the input text field for scanning.
 * @param inputHint The hint text displayed in the input text field.
 * @param onValueChange Callback function invoked when the input text field value changes.  It receives the new input string.
 * @param syncScanItem Callback function invoked to process the scanned item (typically triggered by a button click or enter key press).
 * @param onBottomBarClick Callback function invoked when the bottom bar button is clicked (if displayed).
 * @param popupState  A [NotificationPopupHost] to manage and display popup notifications (errors, etc.).
 * @param clearItem Callback function invoked to remove an item from the scanned list. It receives the index of the item to remove.
 * @param bottomBarText The text displayed on the bottom bar button.
 * @param uiListCarton A list of [Carton] objects representing scanned cartons.
 * @param hasTypeFilter Boolean flag indicating whether a type filter is enabled.
 * @param typeFilter The currently selected type filter value.
 * @param onTypeFilterChange Callback function invoked when the type filter selection changes.  It receives the new filter string */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScanCartonAndBarcodeScreen(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    uiListProduct: List<Product>,
    state: SnackbarHostState,
    inputValue: String,
    inputHint: String,
    onValueChange: (String) -> Unit,
    syncScanItem: () -> Unit,
    onBottomBarClick: () -> Unit,
    popupState: NotificationPopupHost,
    clearItem: (Int) -> Unit,
    bottomBarText: String,
    uiListCarton: List<Carton>,
    hasTypeFilter: Boolean = false,
    typeFilter: String = "",
    onTypeFilterChange: (String) -> Unit,
    typeFilterList: List<String>,
    isRFScanning: Boolean,
    showInputProductTextField: Boolean,
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
                            inputValue = inputValue,
                            inputHint = inputHint,
                            onValueChange = onValueChange,
                            syncScanItem = syncScanItem,
                            loading = loading,
                            uiListProduct = uiListProduct,
                            popupState = popupState,
                            clearItem = clearItem,
                            uiListCarton = uiListCarton,
                            hasTypeFilter = hasTypeFilter,
                            typeFilter = typeFilter,
                            onTypeFilterChange = onTypeFilterChange,
                            typeFilterList = typeFilterList,
                            isRFScanning = isRFScanning,
                            showInputProductTextField = showInputProductTextField
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = {
                    val shouldShowBottomBar =
                        (uiListCarton.isNotEmpty() || uiListProduct.isNotEmpty()) && !loading && !isRFScanning
                    if (shouldShowBottomBar) {
                        BottomBarButton(text = bottomBarText, onClick = onBottomBarClick)
                    }
                }
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    inputValue: String,
    inputHint: String,
    onValueChange: (String) -> Unit,
    syncScanItem: () -> Unit,
    loading: Boolean,
    uiListProduct: List<Product>,
    popupState: NotificationPopupHost,
    clearItem: (Int) -> Unit,
    uiListCarton: List<Carton>,
    hasTypeFilter: Boolean = false,
    typeFilter: String = "",
    onTypeFilterChange: (String) -> Unit,
    typeFilterList: List<String>,
    isRFScanning: Boolean,
    showInputProductTextField: Boolean,
) {

    Column {
        if (loading || isRFScanning) {
            LoadingCircularProgressIndicator(isScanning = isRFScanning, isDataLoading = loading)
        } else {
            NotificationPopUp(popupState)
            if (showInputProductTextField) {
                Row {
                    SimpleTextField(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .fillMaxWidth()
                            .testTag("CustomTextField"),
                        hint = inputHint,
                        onValueChange = {
                            onValueChange(it)
                        },
                        value = inputValue,
                        onDone = {
                            syncScanItem()
                        }
                    )
                }
            }

            Row {

                if (hasTypeFilter) {
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 16.dp)
                            .align(Alignment.CenterVertically),
                        text = {
                            Text(
                                typeFilter,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically),
                                textAlign = TextAlign.Center
                            )
                        },
                        values = typeFilterList,
                        onClick = onTypeFilterChange
                    )
                }

                Text(
                    text = "مجموع: ${uiListProduct.sumOf { it.scannedNumber } + uiListCarton.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            if (uiListProduct.isEmpty() && uiListCarton.isEmpty()) {
                EmptyBox("هنوز کالایی اسکن نکرده اید")
            } else {
                LazyColumn {
                    if (uiListProduct.isNotEmpty()) {
                        items(uiListProduct.size) { i ->
                            LazyColumnItemProduct(i, uiListProduct, clearItem)
                        }
                    } else {
                        items(uiListCarton.size) { i ->
                            LazyColumnItemCarton(i, uiListCarton, clearItem)
                        }
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
    clear: (Int) -> Unit,
) {
    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        Item(
            i, uiListProduct, true,
            text3 = "تعداد: ${uiListProduct[i].scannedNumber}",
            text4 = "موجودی: ${uiListProduct[i].wareHouseNumber}",
            enableWarehouseNumberCheck = true
        )
        ClearButton(topPaddingClearButton, Modifier.align(Alignment.TopEnd)) { clear(i) }
    }
}

@Composable
fun LazyColumnItemCarton(
    i: Int,
    uiListCarton: List<Carton>,
    clear: (Int) -> Unit,
) {
    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp
    Box {
        Item4(
            enableBottomSpace = i == uiListCarton.size - 1,
            text1 = uiListCarton[i].number,
            text2 = "انبار جاری: " + uiListCarton[i].cartonSource,
            text3 = "تنوع جنس: " + uiListCarton[i].barcodeTable.distinct().size,
            text4 = "جمع اجناس: " + uiListCarton[i].numberOfItems,

            )
        ClearButton(topPaddingClearButton, Modifier.align(Alignment.TopEnd)) { clear(i) }
    }
}

@Composable
fun ClearButton(topPadding: Dp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(top = topPadding, end = 8.dp)
            .background(
                shape = RoundedCornerShape(36.dp),
                color = errorContainerLight
            )
            .size(30.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_clear_24),
            contentDescription = null,
            tint = errorLight,
            modifier = Modifier
                .align(Alignment.Center)
                .size(20.dp)
        )
    }
}


@Preview
@Composable
private fun Preview1() {
    ScanCartonAndBarcodeScreen(
        "ورود به قفسه",
        {},
        false,
//        listOf(
//            Product(
//                name = "اسلش  8170 XXXL",
//                KBarCode = "43751458J-8170-XXXL",
//                imageUrl = "https://www.banimode.com/jeanswest/image.php?token=tmv43w4as&code=43751458J-8170-XXXL",
//                size = "XXXL",
//                color = "8170"
//            )
//        ),
        emptyList(),
        SnackbarHostState(),
        "1234567",
        "کارتن یا کالا را اسکن کنید",
        {},
        {},
        {},
        NotificationPopupHost(),
        {},
        "انتقال به قفسه",
//        emptyList()
        uiListCarton = listOf(
            Carton(
                number = "Cn31267238",
                numberOfItems = 28,
                cartonSource = "1918",
            )
        ),
        hasTypeFilter = false,
        typeFilter = RequestType.Product.toString(),
        onTypeFilterChange = {},
        typeFilterList = RequestType.entries.toList().map { it.toString() },
        isRFScanning = false,
        showInputProductTextField = true
    )
}