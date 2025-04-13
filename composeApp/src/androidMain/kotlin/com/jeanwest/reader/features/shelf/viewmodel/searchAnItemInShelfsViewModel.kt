package com.jeanwest.reader.features.shelf.viewmodel

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.models.ShelfBarcodeAddress


@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun Page(
    showContent1: Boolean,
    back: () -> Unit = {},
    title: String,
    state: SnackbarHostState,
    loading: Boolean,
    uiList: MutableList<ShelfBarcodeAddress>,
    textFieldValue: String,
    onTextValueChange: (it: String) -> Unit = {},
    onTextFieldConfirm: () -> Unit = {},
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBarWithBack(onBackPressed = back, title) },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (showContent1) Content1(loading, uiList, textFieldValue) else Content2(
                            loading,
                            textFieldValue,
                            { onTextValueChange(it) },
                            { onTextFieldConfirm() }
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
fun Content1(
    loading: Boolean,
    uiList: MutableList<ShelfBarcodeAddress>,
    itemBarcode: String,
) {
    Column {
        if (loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
            }
        } else {
            if (uiList.isEmpty()) {
                EmptyBox("این کالا در هیچ قفسه ای وجود ندارد")
            } else {

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "کالا: $itemBarcode",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                }

                LazyColumn {
                    items(uiList.size) { i ->
                        LazyColumnItem(uiList, i)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(uiList: MutableList<ShelfBarcodeAddress>, i: Int) {
    Box {
        Item4(
            enableBottomSpace = i == uiList.size - 1,
            text1 = "انبار: " + uiList[i].wareHouseTitle,
            text2 = "قفسه: " +
                    if (uiList[i].shelfID.length > 1) {
                        uiList[i].shelfID.let {
                            it.substring(0, 2) + ' ' +
                                    (if (it.length > 3) it.substring(2, 4) + ' ' else "") +
                                    (if (it.length > 5) it.substring(4, 6) + ' ' else "") +
                                    (if (it.length > 7) it.substring(6, 8) + ' ' else "") +
                                    (if (it.length > 8) it.substring(8, it.length) else "")
                        }
                    } else "",
            text3 = "تعداد: " + uiList[i].qty,
            text4 = "سایز: " + uiList[i].size
        )
    }
}

@Composable
fun Content2(
    loading: Boolean,
    textFieldValue: String,
    onTextValueChange: (it: String) -> Unit,
    onTextFieldConfirm: () -> Unit,
) {
    ScanOrTypeNumberPage(
        loading = loading,
        onClick = { onTextFieldConfirm() },
        value = textFieldValue,
        onValueChange = { onTextValueChange(it) },
        item = "بارکد کالا"
    )
}