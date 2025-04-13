package com.jeanwest.reader.features.logistic.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.logistic.viewmodel.SackCreateViewModel
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.AppBarWithDeleteButton
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item3
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import dagger.hilt.android.AndroidEntryPoint

/**
 * this screen responsible for
 * create a sack from multiple
 * stock drafts. the reason for
 * creating sack is to pack multiple
 * stock drafts in one piece and
 * transfer them with lower price.
 */

@OptIn(ExperimentalFoundationApi::class)
@AndroidEntryPoint
class SackCreate : ComponentActivity() {

    val viewModel by viewModels<SackCreateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Page(viewModel = viewModel, back = { back() })
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    fun back() {
        finish()
    }
}

@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
private fun Page(viewModel: SackCreateViewModel, back: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBar(viewModel, back) },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(viewModel)
                    }
                },
                bottomBar = { BottomBar(viewModel) },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
            )
        }
    }
}

@Composable
private fun AppBar(viewModel: SackCreateViewModel, back: () -> Unit) {
    if (viewModel.stockDraftUiList.isNotEmpty()) {
        AppBarWithDeleteButton(
            title = stringResource(id = R.string.sackCreate),
            onDeletePressed = { viewModel.showClearPopup() },
            onBackPressed = back
        )
    } else {
        AppBarWithBack(
            title = stringResource(id = R.string.sackCreate),
            onBackPressed = back
        )
    }
}

@Composable
private fun BottomBar(viewModel: SackCreateViewModel) {
    if (!viewModel.loading && viewModel.stockDraftUiList.isNotEmpty()) {
        BottomBarButton(text = if (viewModel.creatingStockDraft) "در حال ثبت ..." else " تعداد ${viewModel.stockDraftUiList.size} حواله ایجاد گونی شوند") {
            if (!viewModel.creatingStockDraft) {
                viewModel.showPrintPopUp()
            }
        }
    }
}

@Composable
private fun Content(viewModel: SackCreateViewModel) {

    Column {

        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {

            NotificationPopUp(state = viewModel.popupState)

            Column(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .shadow(6.dp, Shapes.medium)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        )
                        .fillMaxWidth(),
                ) {

                    Row {
                        SimpleTextField(
                            value = viewModel.stockDraftNumber,
                            hint = "شماره حواله",
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp,
                                    top = 12.dp
                                )
                                .fillMaxWidth(),
                            onValueChange = {
                                viewModel.onTextFieldValueChanged(it)
                            },
                            onDone = {
                                viewModel.getStockDraftsDetails(viewModel.stockDraftNumber)
                            })
                    }
                }

                if (viewModel.stockDraftUiList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 56.dp)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(256.dp)
                        ) {
                            Box(

                                modifier = Modifier
                                    .background(color = Color.White, shape = Shapes.medium)
                                    .size(256.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_empty_box),
                                    contentDescription = "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Text(
                                "هنوز حواله ای برای ایجاد گونی اسکن نکرده اید",
                                style = Typography.headlineMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(
                                    top = 16.dp,
                                    start = 4.dp,
                                    end = 4.dp
                                ),
                            )
                        }
                    }
                } else {

                    LazyColumn(
                        modifier = Modifier.padding(top = 4.dp),
                        state = viewModel.listState
                    ) {

                        items(viewModel.stockDraftUiList.size) { i ->

                            Box {

                                Item3(
                                    clickable = false,
                                    enableBottomSpace = i == viewModel.stockDraftUiList.size - 1,
                                    text1 = "حواله: " + viewModel.stockDraftUiList[i].number,
                                    text4 = "تعداد کالاها: " + viewModel.stockDraftUiList[i].numberOfItems,
                                    text2 = "تاریخ: " + viewModel.stockDraftUiList[i].date,
                                    text3 = "از: " + viewModel.memory.erpData.warehousesIDsToTitles[viewModel.stockDraftUiList[i].source.toString()],
                                    text6 = "شرح: " + viewModel.stockDraftUiList[i].specification,
                                    text5 = "به: " + viewModel.memory.erpData.warehousesIDsToTitles[viewModel.stockDraftUiList[i].destination.toString()]
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp, end = 8.dp)
                                        .background(
                                            shape = RoundedCornerShape(36.dp),
                                            color = errorContainerLight
                                        )
                                        .size(30.dp)
                                        .align(Alignment.TopEnd)
                                        .testTag("clear")
                                        .clickable {
                                            viewModel.clear(viewModel.stockDraftUiList[i].number.toString())
                                        }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                                        contentDescription = "",
                                        tint = errorLight,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}