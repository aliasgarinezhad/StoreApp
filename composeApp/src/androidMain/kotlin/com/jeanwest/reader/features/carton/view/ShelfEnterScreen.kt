package com.jeanwest.reader.features.carton.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfBarcodeAddress
import com.jeanwest.reader.models.StockDraftRequestItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShelfEnterScreen(
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    uiListProduct: StockDraftRequestItem,
    state: SnackbarHostState,
    onBottomBarClick: () -> Unit,
    popupState: NotificationPopupHost,
    bottomBarText: String,
    scannedNumber: Int,
    shelfNumber: String,
    suggestedShelfList: List<ShelfBarcodeAddress>,
    inputNumberChange: (String) -> Unit,
    enableInputNumber: Boolean,
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
                            scannedNumber = scannedNumber,
                            shelfNumber = shelfNumber,
                            suggestedShelfList = suggestedShelfList,
                            inputNumberChange = inputNumberChange,
                            enableInputNumber = enableInputNumber
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = {
                    // Only show the bottom bar if something scanned
                    if (scannedNumber != 0) {
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
    loading: Boolean,
    uiListProduct: StockDraftRequestItem,
    popupState: NotificationPopupHost,
    scannedNumber: Int,
    shelfNumber: String,
    suggestedShelfList: List<ShelfBarcodeAddress>,
    inputNumberChange: (String) -> Unit,
    enableInputNumber: Boolean,
) {
    Column {
        if (loading) {
            LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
        } else {
            NotificationPopUp(popupState)
            if (shelfNumber.isEmpty()) {

                Text(
                    text = "لطفا قفسه مورد نظر را اسکن کنید.",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                        .align(Alignment.Start),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Text(
                    text = "قفسه های پیشنهادی:",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 0.dp, bottom = 8.dp)
                        .align(Alignment.Start),
                    color = Color.Black
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(suggestedShelfList) { index, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = if (index == suggestedShelfList.size - 1) 200.dp else 8.dp
                                )
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (item.shelfID.length > 1) {
                                        item.shelfID.let {
                                            // Splits the ID into spaced groups of 2 characters
                                            it.substring(0, 2) + ' ' +
                                                    (if (it.length > 3) it.substring(
                                                        2,
                                                        4
                                                    ) + ' ' else "") +
                                                    (if (it.length > 5) it.substring(
                                                        4,
                                                        6
                                                    ) + ' ' else "") +
                                                    (if (it.length > 7) it.substring(
                                                        6,
                                                        8
                                                    ) + ' ' else "") +
                                                    (if (it.length > 8) it.substring(8) else "")
                                        }
                                    } else "",
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(end = 16.dp)
                                )

                                Text(
                                    text = "تعداد: " + item.qty.toString(),
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "لطفا کالا را اسکن کنید.",
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                        .align(Alignment.Start),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                ColumnItem(
                    item = uiListProduct,
                    uiItem4Shelf = shelfNumber,
                    scannedNumber = scannedNumber,
                    inputNumberChange = inputNumberChange,
                    enableInputNumber = enableInputNumber
                )
            }
        }
    }
}

@Composable
fun ColumnItem(
    item: StockDraftRequestItem,
    uiItem4Shelf: String,
    scannedNumber: Int,
    inputNumberChange: (String) -> Unit,
    enableInputNumber: Boolean,
) {
    val modifier = Modifier
        .padding(top = 2.dp, bottom = 2.dp)
        .wrapContentWidth()

    val localInputValue = remember {
        mutableStateOf(
            if (item.product.scannedBarcodeNumber == 0) ""
            else item.product.scannedBarcodeNumber.toString()
        )
    }

    val focusManager = LocalFocusManager.current

    Column {
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)
        ) {
            Box(modifier = Modifier.size(width = 180.dp, height = 200.dp)) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .height(200.dp)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }

            Column {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = item.product.KBarCode,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "رنگ: " + item.product.color,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "سایز: " + item.product.size,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "تعداد درخواستی: " + item.notFoundNumber,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "قفسه: $uiItem4Shelf",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "موجودی انبار: ${item.product.wareHouseNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                Text(
                    text = "اسکن شده: $scannedNumber",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = modifier
                )
                if (enableInputNumber) {
                    OutlinedTextField(
                        value = localInputValue.value,
                        onValueChange = { newValue ->
                            // Allow only numeric input
                            val onlyDigits = newValue.filter { it.isDigit() }
                            localInputValue.value = onlyDigits
                        },
                        label = {
                            Text(
                                text = "تعداد",
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier
                            .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
                            .wrapContentWidth()
                            .padding(end = 40.dp, bottom = 10.dp, top = 10.dp)
                            .align(Alignment.Start),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (localInputValue.value.isNotEmpty()) {
                                    inputNumberChange(localInputValue.value)
                                }
                                focusManager.clearFocus()
                            }
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewShelfEnterScreen() {
    ShelfEnterScreen(
        topBarTitle = "ورود به قفسه",
        topBarOnClick = {},
        loading = false,
        uiListProduct = StockDraftRequestItem(
            Product(
                KBarCode = "98374j-3273-321",
                scannedBarcodeNumber = 0
            )
        ),
        state = SnackbarHostState(),
        onBottomBarClick = {},
        popupState = NotificationPopupHost(),
        bottomBarText = "ورود به قفسه",
        scannedNumber = 1,
        shelfNumber = "SHRA1232",
        suggestedShelfList = listOf(
            ShelfBarcodeAddress(shelfID = "SHRA1232"),
            ShelfBarcodeAddress(shelfID = "SHRA14t62"),
            ShelfBarcodeAddress(shelfID = "SHRE35435"),
            ShelfBarcodeAddress(shelfID = "SHRA56e")
        ),
        inputNumberChange = {},
        true
    )
}
