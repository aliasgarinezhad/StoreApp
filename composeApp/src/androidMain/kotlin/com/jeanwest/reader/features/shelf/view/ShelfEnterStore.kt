package com.jeanwest.reader.features.shelf.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyShelf
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.errorContainerLight
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.features.shelf.viewmodel.ShelfEnterStoreViewModel
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShelfEnterStore : ComponentActivity() {

    val viewModel: ShelfEnterStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                if (viewModel.uiState == 1) {
                    viewModel.uiListProduct.clear()
                    viewModel.skuList.clear()
                    viewModel.barcodes.clear()
                    viewModel.uiState = 0
                } else {
                    finish()
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

}

@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@Composable
fun Page(viewModel: ShelfEnterStoreViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(onBackPressed, stringResource(R.string.EnterShelfStore))
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        when (viewModel.uiState) {
                            0 -> Content(viewModel)
                            1 -> Content2(viewModel)
                        }
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
                bottomBar = {
                    if (viewModel.uiListProduct.isNotEmpty()) {
                        BottomBarButton("ورود به قفسه") {
                            viewModel.addProductToShelf()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Content(viewModel: ShelfEnterStoreViewModel) {
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
            SimpleTextField(
                value = viewModel.shelfCode,
                hint = "شماره قفسه",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .fillMaxWidth(),
                onValueChange = {
                    viewModel.shelfCode = it
                },
                onDone = {
                    if (viewModel.shelfCode.isNotBlank()) {
                        viewModel.getShelProducts(viewModel.shelfCode)
                    }
                })
            EmptyShelf("بارکد قفسه را اسکن یا وارد کنید")
        }
    }
}

@Composable
fun Content2(viewModel: ShelfEnterStoreViewModel) {
    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(isDataLoading = viewModel.loading)
        } else {
            SimpleTextField(
                value = viewModel.productBarcode,
                hint = "بارکد کالا",
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                        top = 12.dp
                    )
                    .fillMaxWidth(),
                onValueChange = {
                    viewModel.productBarcode = it
                },
                onDone = {
                    if (viewModel.shelfCode.isNotBlank()) {
                        viewModel.getProductDetails(viewModel.productBarcode)
                    }
                })
            if (viewModel.uiListProduct.isEmpty()) {
                EmptyShelf("بارکد کالا را اسکن یا وارد کنید")
            } else {
                LazyColumn {
                    items(viewModel.uiListProduct.size) { i ->
                        val topPaddingClearButton = if (i == 0) 8.dp else 4.dp
                        Box {
                            Item(
                                i,
                                viewModel.uiListProduct,
                                text3 = "رنگ: " + viewModel.uiListProduct[i].color,
                                text4 = "سایز: " + viewModel.uiListProduct[i].size
                            )
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
                                        viewModel.clear(viewModel.uiListProduct[i])
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