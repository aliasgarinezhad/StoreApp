package com.jeanwest.reader.features.carton.view

import android.content.Context
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.view.AlertDialogWithHeadlineMediumButtonDropDownList
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.BottomBarButton
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.FilterDropDownList
import com.jeanwest.reader.view.Item
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.NotificationPopUp
import com.jeanwest.reader.view.NotificationPopupHost
import com.jeanwest.reader.view.PowerSlider
import com.jeanwest.reader.view.errorContainerLight
import com.jeanwest.reader.view.errorLight
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.RFID

/**
 * Composable function for the Carton Creation screen.
 *
 * This screen allows users to scan products, manage a list of scanned items,
 * and print carton labels.
 *
 * @param context The Android application context.
 * @param topBarTitle The title to display in the top app bar.
 * @param topBarOnClick Callback function for handling back button clicks in the top app bar.
 * @param loading Boolean flag indicating whether a loading state is active.  Disables button clicks and shows loading indicator on bottom bar.
 * @param rfScanning Boolean flag indicating whether RF scanning is active.  Disables button clicks and shows scanning indicator on bottom bar.
 * @param uiList The list of currently scanned products.
 * @param state The SnackbarHostState for displaying snackbar messages (e.g., errors).
 * @param onBottomBarClick Callback function for handling clicks on the bottom bar button (e.g., to confirm carton creation).
 * @param openPrintDialog Boolean flag indicating whether the print dialog should be displayed.
 * @param printer The currently selected printer.
 * @param popupState A [NotificationPopupHost] to manage the display of popup notifications.
 * @param scannedNumber The number of items successfully scanned during RF scanning (if applicable).
 * @param scanTypeValue The type of scanning being performed (e.g., "Barcode", "RFID").
 * @param onPrinterSelected Callback function */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartonCreateScreen(
    context: Context,
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    loading: Boolean,
    rfScanning: Boolean,
    uiList: List<Product>,
    state: SnackbarHostState,
    onBottomBarClick: () -> Unit,
    openPrintDialog: Boolean,
    printer: String,
    popupState: NotificationPopupHost,
    scannedNumber: Int,
    scanTypeValue: String,
    onPrinterSelected: (String) -> Unit,
    onPrintConfirm: () -> Unit,
    onPopupDismiss: () -> Unit,
    onScanTypeChanged: (String) -> Unit,
    clearItem: (Product) -> Unit,
    onOpenSearchActivity: (Product, Context) -> Unit,
    rf: RFID,
    printerList: List<String>,
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
                    Box(modifier = Modifier.padding(it)) {
                        Content(
                            context = context,
                            loading = loading,
                            uiList = uiList,
                            openPrintDialog = openPrintDialog,
                            printer = printer,
                            popupState = popupState,
                            scannedNumber = scannedNumber,
                            scanTypeValue = scanTypeValue,
                            onPrinterSelected = onPrinterSelected,
                            onPrintConfirm = onPrintConfirm,
                            onPopupDismiss = onPopupDismiss,
                            onScanTypeChanged = onScanTypeChanged,
                            clearItem = clearItem,
                            onOpenSearchActivity = onOpenSearchActivity,
                            rf = rf,
                            printerList = printerList
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
                bottomBar = { BottomBar(loading, rfScanning, uiList, onBottomBarClick) }
            )
        }
    }
}

@Composable
fun BottomBar(
    loading: Boolean,
    rfScanning: Boolean,
    uiList: List<Product>,
    onBottomBarClick: () -> Unit,
) {
    if (uiList.isNotEmpty() && !loading && !rfScanning) {
        BottomBarButton(text = "ایجاد کارتن", onClick = onBottomBarClick)
    }
}

@ExperimentalFoundationApi
@Composable
fun Content(
    context: Context,
    loading: Boolean,
    rf: RFID,
    uiList: List<Product>,
    openPrintDialog: Boolean,
    printer: String,
    popupState: NotificationPopupHost,
    scannedNumber: Int,
    scanTypeValue: String,
    printerList: List<String>,
    onPrinterSelected: (String) -> Unit,
    onPrintConfirm: () -> Unit,
    onPopupDismiss: () -> Unit,
    onScanTypeChanged: (String) -> Unit,
    clearItem: (Product) -> Unit,
    onOpenSearchActivity: (Product, Context) -> Unit,
) {
    Column {
        if (loading || rf.scanning) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(rf.scanning, loading)
            }
        } else {
            if (openPrintDialog) {
                AlertDialogWithHeadlineMediumButtonDropDownList(
                    title = "لطفا پرینتر مورد نظر خود را مشخص کنید",
                    btnTxt = "پرینت",
                    btnOnClick = onPrintConfirm,
                    dropDownText = printer,
                    onDismiss = onPopupDismiss,
                    dropDownRes = printerList.toMutableList(),
                    onSelectItem = onPrinterSelected
                )
            }

            NotificationPopUp(popupState)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "مجموع: $scannedNumber",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                FilterDropDownList(
                    modifier = Modifier.padding(start = 16.dp),
                    icon = {},
                    text = {
                        Text(
                            text = scanTypeValue,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp)
                        )
                    },
                    onClick = onScanTypeChanged,
                    values = listOf("RFID", "بارکد")
                )
            }

            PowerSlider(scanTypeValue == "RFID", rf.scanningPower) {
                rf.scanningPower = it
            }

            if (uiList.isEmpty()) {
                EmptyBox("هنوز کالایی برای ایجاد کارتن اسکن نکرده اید")
            } else {
                LazyColumn(modifier = Modifier.padding(top = 0.dp)) {
                    items(uiList.size) { i ->
                        LazyColumnItem(i, context, uiList, clearItem, onOpenSearchActivity)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(
    i: Int,
    context: Context,
    uiList: List<Product>,
    clear: (Product) -> Unit,
    openSearchActivity: (Product, Context) -> Unit,
) {
    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        Item(
            i, uiList, true,
            text3 = "اسکن: ${uiList[i].scannedNumber}",
            text4 = "سایز: ${uiList[i].size}",
        ) {
            openSearchActivity(uiList[i], context)
        }

        Box(
            modifier = Modifier
                .padding(top = topPaddingClearButton, end = 8.dp)
                .background(
                    shape = RoundedCornerShape(36.dp),
                    color = errorContainerLight
                )
                .size(30.dp)
                .align(Alignment.TopEnd)
                .testTag("clear")
                .clickable {
                    clear(uiList[i])
                }
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
}