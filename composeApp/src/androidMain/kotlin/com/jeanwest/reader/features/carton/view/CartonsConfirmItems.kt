@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.carton.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.carton.viewmodel.CartonsConfirmItemsViewModel
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.ScanFilterDropDownList
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * this file responsible for confirming
 * all items in a carton by RFID. it shows all
 * of carton logistics and user should
 * select a carton to confirm its items.
 * */

@AndroidEntryPoint
class CartonsConfirmItems : ComponentActivity() {

    val viewModel: CartonsConfirmItemsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exceptionHandler()
        setContent {
            Page(viewModel) {
                if (viewModel.uiState == 0) {
                    finish()
                } else {
                    viewModel.back()
                }
            }
        }
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        if (event.repeatCount == 0) {

            if (keyCode == 280 || keyCode == 293) {
                viewModel.scanTrigger()
            } else if (keyCode == 4) {
                if (viewModel.uiState == 1) {
                    finish()
                } else {
                    viewModel.back()
                }
            }
        }
        return true
    }
}

@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@Composable
fun Page(viewModel: CartonsConfirmItemsViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBar(viewModel) {
                        onBackPressed()
                    }
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.uiState) {
                            0 -> Content(viewModel)
                            1 -> Content2(viewModel)
                            2 -> Content3(viewModel)
                        }
                    }
                },
                bottomBar = { BottomBar(viewModel) },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
            )
        }
    }
}

@Composable
private fun AppBar(viewModel: CartonsConfirmItemsViewModel, onBackPressed: () -> Unit) {

    TopAppBar(

        navigationIcon = {
            IconButton(onClick = {
                onBackPressed()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = ""
                )
            }
        },

        title = {
            Text(
                text = stringResource(id = R.string.cartonsDetailConfirm),
                modifier = Modifier
                    .padding(end = 50.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Right,
            )
        }

    )
}

@Composable
fun BottomBar(viewModel: CartonsConfirmItemsViewModel) {

    if (!viewModel.loading && viewModel.uiState == 2) {

        BottomAppBar(
            modifier = Modifier.wrapContentHeight()
        ) {

            Column {

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ScanFilterDropDownList(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        filterValue = viewModel.scanFilterValue
                    ) { value ->
                        viewModel.onFilterValueChange(value)
                    }
                    Button(onClick = {
                        viewModel.onConfirmCartonButtonClick()
                    }) {
                        Text(text = "تایید نهایی")
                    }
                }
            }
        }

    }
}

@Composable
fun Content(viewModel: CartonsConfirmItemsViewModel) {

    Column {

        if (viewModel.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(false, viewModel.loading)
            }
        } else {
            if (viewModel.uiList0.isEmpty()) {
                EmptyBox("حواله ای برای نمایش وجود ندارد")
            } else {
                LazyColumn {
                    items(viewModel.uiList0.size) { i ->
                        Item4(
                            clickable = true,
                            enableBottomSpace = i == viewModel.uiList0.size - 1,
                            text1 = "نام راننده: " + viewModel.uiList0.keys.toList()[i],
                            text2 = ("تعداد کارتن ها: " + viewModel.uiList0[viewModel.uiList0.keys.toList()[i]]?.size),
                            text3 = "",
                            text4 = ""
                        ) {
                            viewModel.onSelectLogistic(viewModel.uiList0.keys.toList()[i])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Content2(viewModel: CartonsConfirmItemsViewModel) {
    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {

            NotificationPopUp(viewModel.popupState)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 16.dp, start = 16.dp),
                Arrangement.Absolute.SpaceEvenly
            ) {
                Text(
                    text = "تعداد کارتن ها: " + (viewModel.uiList1.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = "تایید شده: " + (viewModel.uiList1.filter { it.isConfirmedByRFID }.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
            }

            Row {
                SimpleTextField(
                    value = viewModel.selectedCartonID,
                    hint = "شماره کارتن",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                            top = 12.dp
                        )
                        .fillMaxWidth(),
                    onValueChange = {
                        viewModel.selectedCartonID = it
                    },
                    onDone = {
                        if (viewModel.selectedCartonID.isNotBlank()) {
                            viewModel.onSelectCarton(viewModel.selectedCartonID)
                        }
                    })
            }

            LazyColumn {

                items(viewModel.uiList1.size) { i ->

                    Item2(
                        clickable = true,
                        enableBottomSpace = i == viewModel.uiList1.size - 1,
                        text1 = viewModel.uiList1[i].number,
                        text2 = "تاریخ: " + viewModel.uiList1[i].date,
                        text3 = "از: " + viewModel.memory.erpData.warehousesIDsToTitles[viewModel.uiList1[i].operationSource],
                        text4 = "جمع اجناس: " + viewModel.uiList1[i].numberOfItems,
                        text5 = "به: " + if (viewModel.memory.erpData.warehousesIDsToTitles[viewModel.uiList1[i].operationDes] == null) "-" else viewModel.memory.erpData.warehousesIDsToTitles[viewModel.uiList1[i].operationDes],
                        text6 = "",
                        text7 = "اسکن شده با RFID: " + if (viewModel.uiList1[i].everyProductHaveEpc) "بله" else "خیر",
                        colorFull = viewModel.uiList1[i].isConfirmedByRFID,
                        onClick = {
                            viewModel.onSelectCarton(viewModel.uiList1[i].number)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun Content3(viewModel: CartonsConfirmItemsViewModel) {


    Column {

        NotificationPopUp(viewModel.popupState)

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(
                isDataLoading = viewModel.loading,
                isScanning = viewModel.isScanning
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .border(
                        BorderStroke(1.dp, primaryLight),
                        shape = MaterialTheme.shapes.small
                    )
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "اسکن: ${viewModel.scannedNumber}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.CenterVertically)
                            .weight(1F),
                    )
                    Text(
                        text = "کسری: ${viewModel.shortagesNumber}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                    )
                    Text(
                        text = "اضافی: ${viewModel.additionalNumber}",
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.testTag("RefillActivityLazyColumn")
            ) {
                items(viewModel.uiList2.size) { i ->
                    LazyColumnItem(i, viewModel)
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(i: Int, viewModel: CartonsConfirmItemsViewModel) {

    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        Item(
            i, viewModel.uiList2, true,
            text3 = "موجودی: " + viewModel.uiList2[i].draftNumber,
            text4 = viewModel.uiList2[i].conflictType + ": " + viewModel.uiList2[i].conflictNumber,
        ) {
        }

//        if (viewModel.uiList2[i].scannedNumber > 0) {
//            Box(
//                modifier = Modifier
//                    .padding(top = topPaddingClearButton, end = 8.dp)
//                    .background(
//                        shape = RoundedCornerShape(36.dp),
//                        color = errorContainerLight
//                    )
//                    .size(30.dp)
//                    .align(Alignment.TopEnd)
//                    .testTag("clear")
//                    .clickable {
//                        viewModel.uiList2[i].scannedEPCs.clear()
//                    }
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_baseline_clear_24),
//                    contentDescription = "",
//                    tint = errorLight,
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .size(20.dp)
//                )
//            }
//        }
    }
}
