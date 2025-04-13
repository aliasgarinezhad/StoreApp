package com.jeanwest.reader.features.carton.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.R
import com.jeanwest.reader.features.carton.viewmodel.CartonDetailsViewModel
import com.jeanwest.reader.features.shared.AlertDialogWithHeadlineMediumButtonDropDownList
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.primaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * this file responsible for showing
 * details of a given carton number.
 */

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class CartonDetails : ComponentActivity() {

    val viewModel: CartonDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                if (!viewModel.showDetailMode) {
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
            if (keyCode == 4) {
                if (!viewModel.showDetailMode) {
                    finish()
                } else {
                    viewModel.back()
                }
            }
        }
        return true

    }

    @SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalCoilApi::class)
    @ExperimentalFoundationApi
    @Composable
    fun Page(viewModel: CartonDetailsViewModel, onBackPressed: () -> Unit) {
        MyApplicationTheme {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = {
                        AppBarWithBack(
                            { onBackPressed() },
                            stringResource(id = R.string.showCarton)
                        )
                    },
                    content = {
                        Box(modifier = Modifier.padding(it)) {
                            if (viewModel.showDetailMode) Content() else Content2()
                        }
                    },
                    snackbarHost = { ErrorSnackBar(viewModel.state) },
                    bottomBar = { BottomBar(viewModel) })
            }
        }
    }

    @Composable
    fun BottomBar(viewModel: CartonDetailsViewModel) {

        if (!viewModel.loading) {
            if (viewModel.showDetailMode) {
                BottomBarButton(text = "پرینت لیبل") {
                    viewModel.openPrintDialog = true
                }
            }
        }
    }

    @ExperimentalCoilApi
    @ExperimentalFoundationApi
    @Composable
    fun Content() {

        Column {

            if (viewModel.loading) {
                LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
            } else {

                if (viewModel.openPrintDialog) {
                    AlertDialogWithHeadlineMediumButtonDropDownList(
                        title = "لطفا پرینتر مورد نظر خود را مشخص کنید ",
                        btnTxt = "پرینت",
                        btnOnClick = {
                            viewModel.openPrintDialog = false
                            viewModel.printCarton()
                        },
                        dropDownText = viewModel.printer,
                        onDismiss = { viewModel.openPrintDialog = false },
                        dropDownRes = viewModel.printers.keys.toMutableList(),
                        onSelectItem = { viewModel.printer = it }
                    )
                }

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
                    Column(modifier = Modifier.wrapContentHeight()) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {

                            Text(
                                text = "مجموع: ${viewModel.cartonProperties.numberOfItems}",
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(1F),
                            )
                            Text(
                                text = "انبار جاری: ${viewModel.memory.erpData.warehousesIDsToTitles[viewModel.cartonProperties.cartonSource]}",
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(2.5F),
                            )
                        }
                        Text(
                            text = "آدرس قفسه: ${viewModel.cartonProperties.shelfAddress}",
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 8.dp)
                                .align(Alignment.Start),
                        )
                    }
                }

                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                    items(viewModel.uiList.size) { i ->
                        Item(
                            i,
                            viewModel.uiList,
                            true,
                            text3 = "موجودی: " + viewModel.uiList[i].draftNumber,
                            text4 = "سایز: " + viewModel.uiList[i].size,
                        ) {
                            viewModel.openSearchActivity(viewModel.uiList[i])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Content2() {
        ScanOrTypeNumberPage(
            loading = viewModel.loading,
            onClick = { viewModel.getCartonDetail(viewModel.cartonNumber) },
            value = viewModel.cartonNumber,
            onValueChange = { viewModel.cartonNumber = it },
            item = "شماره کارتن"
        )
    }
}