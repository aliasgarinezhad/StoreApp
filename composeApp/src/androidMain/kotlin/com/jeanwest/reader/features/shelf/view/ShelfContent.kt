package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.EmptyBox
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shelf.viewmodel.ShelfContentViewModel
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ShelfContent : ComponentActivity() {

    val viewModel: ShelfContentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                back()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseActivity()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResumeActivity()
    }

    private fun exceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            ExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!
            )
        )
    }

    private fun back() {
        if (viewModel.pageState.intValue != 0) viewModel.back() else finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            if (keyCode == 4) {
                back()
            }
        }
        return true
    }
}

@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun Page(viewModel: ShelfContentViewModel, back: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        { back() },
                        stringResource(id = R.string.shelf_content)
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.pageState.intValue) {
                            0 -> ScanPage(viewModel)
                            1 -> ProductContent(viewModel)
                            2 -> CartonContent(viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = { if (viewModel.pageState.intValue == 1) BottomBar(viewModel) }
            )
        }
    }
}


@ExperimentalFoundationApi
@Composable
fun ProductContent(viewModel: ShelfContentViewModel) {
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
        }
        NotificationPopUp(viewModel.popupState)
        if (viewModel.uiListProduct.isEmpty()) {
            EmptyBox(text = "کالایی در این قفسه وجود ندارد")
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 16.dp, start = 16.dp),
                horizontalArrangement = if (viewModel.editShelfMode) Arrangement.SpaceEvenly else Arrangement.Start,
            ) {
                Text(
                    text = "مجموع: " + (viewModel.uiListProduct.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )

                if (viewModel.editShelfMode) {
                    Text(
                        text = "انتخاب شده: ${viewModel.editShelfCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    )
                }
            }
            LazyColumn {
                items(viewModel.uiListProduct.size) { i ->
                    AdditionalItems(i, viewModel)
                }
            }
        }
    }
}

@Composable
fun AdditionalItems(i: Int, viewModel: ShelfContentViewModel) {

    val topPaddingClearButton = if (i == 0) 8.dp else 4.dp

    Box {
        Item(
            i, viewModel.uiListProduct, false,
            text3 = "موجودی: " + viewModel.uiListProduct[i].shelfCount.toString(),
            text4 = "سایز: " + viewModel.uiListProduct[i].size,
            enableSign = true,
            signNumber = viewModel.uiListProduct[i].scannedNumber
        )
        if (viewModel.editShelfMode) {
            Box(
                modifier = Modifier
                    .padding(top = topPaddingClearButton, end = 8.dp)
                    .background(
                        shape = RoundedCornerShape(36.dp),
                        color = errorContainerLight
                    )
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .testTag("remove")
                    .clickable {
                        viewModel.editShelf(viewModel.uiListProduct[i])
                        viewModel.uiListProduct[i].scannedBarcodeNumber++
                        val temp = mutableListOf<Product>()
                        temp.addAll(viewModel.uiListProduct)
                        viewModel.uiListProduct.clear()
                        viewModel.uiListProduct.addAll(temp)
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remove),
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

@Composable
fun CartonContent(viewModel: ShelfContentViewModel) {
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
        }
        NotificationPopUp(viewModel.popupState)
        if (viewModel.uiListCarton.isEmpty()) {
            EmptyBox(text = "کارتنی در این قفسه وجود ندارد")
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مجموع: " + (viewModel.uiListCarton.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
                )
                Text(
                    text = "شماره قفسه: " + (viewModel.cageNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(bottom = 0.dp, top = 16.dp, end = 16.dp)
                )
            }
            LazyColumn {
                items(viewModel.uiListCarton.size) { i ->
                    AdditionalItemsCarton(i, viewModel)
                }
            }
        }
    }
}

@Composable
fun AdditionalItemsCarton(i: Int, viewModel: ShelfContentViewModel) {
    Box {
        Item4(
            enableBottomSpace = i == viewModel.uiListCarton.size - 1,
            text1 = viewModel.uiListCarton[i].number,
            text2 = "انبار جاری: " + viewModel.memory.erpData.warehousesIDsToTitles[viewModel.uiListCarton[i].cartonSource],
            text3 = "",
            text4 = "جمع اجناس: " + viewModel.uiListCarton[i].numberOfItems,
        )
    }
}

@Composable
fun ScanPage(viewModel: ShelfContentViewModel) {
    ScanOrTypeNumberPage(
        loading = viewModel.loading,
        onClick = {
            viewModel.getShelfProducts()
        },
        value = viewModel.cageNumber,
        onValueChange = { viewModel.cageNumber = it },
        item = "شماره قفسه"
    )
}

@Composable
fun BottomBar(viewModel: ShelfContentViewModel) {

    if (!viewModel.loading) {
        if (!viewModel.clearShelfDialog && !viewModel.editShelfMode) {
            BottomAppBar(
                modifier = Modifier.wrapContentHeight()
            ) {
                Column {

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            viewModel.popupState.showPopupWith2Button(
                                "تمام محتوای قفسه خالی شود؟",
                                {
                                    viewModel.clearShelf()
                                    viewModel.clearShelfDialog = false
                                },
                                { viewModel.clearShelfDialog = false },
                                "بله",
                                "خیر، منصرف شدم",
                                { viewModel.clearShelfDialog = false })

                        }) {
                            Text(text = "صفر کردن قفسه")
                        }

                        Button(onClick = {
                            viewModel.editShelfMode = true
                        }) {
                            Text(text = "ویرایش قفسه")
                        }
                    }
                }
            }
        } else if (viewModel.editShelfMode) {
            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp), onClick = {
                viewModel.popupState.showPopupWith2Button("تغییرات انجام شده نهایی شوند؟", {
                    viewModel.editShelfConfirm()
                }, {
                    viewModel.clear()
                    viewModel.editShelfMode = false
                    viewModel.pageState.intValue = 0
                }, "بله", "خیر، منصرف شدم")
            }) {
                Text(text = "تایید نهایی")
            }
        }
    }
}