package com.jeanwest.reader.features.carton.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.features.carton.viewmodel.SearchAnItemInCartonsViewModel
import com.jeanwest.reader.view.AppBarWithBack
import com.jeanwest.reader.view.EmptyBox
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.Item3
import com.jeanwest.reader.view.LoadingCircularProgressIndicator
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.ScanOrTypeNumberPage
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.models.CartonItem
import com.jeanwest.reader.view.onPrimaryLight
import com.jeanwest.reader.useCases.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

/**
 * this file responsible for search
 * a barcode in user warehouse cartons
 * and show the result.
 */

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class SearchAnItemInCartons : ComponentActivity() {

    val viewModel: SearchAnItemInCartonsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler()
        setContent {
            Page(viewModel) {
                back()
            }
        }

        this.onBackPressedDispatcher.addCallback(this) {
            back()
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
        if (viewModel.scanningMode) viewModel.back() else finish()
    }
}

@ExperimentalFoundationApi
@Composable
fun Page(
    viewModel: SearchAnItemInCartonsViewModel,
    back: () -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = { AppBarWithBack(onBackPressed = { back() }, viewModel.title) },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (viewModel.scanningMode) Content1(viewModel) else Content2(
                            viewModel
                        )
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) },
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Content1(
    view: SearchAnItemInCartonsViewModel,
) {
    Column {
        if (view.loading) {
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .background(onPrimaryLight, Shapes.small)
                    .fillMaxWidth()
            ) {
                LoadingCircularProgressIndicator(isScanning = false, isDataLoading = true)
            }
        } else {
            if (view.uiList.isEmpty()) {
                EmptyBox("این کالا در هیچ کارتنی وجود ندارد")
            } else {
                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {
                    items(view.uiList.size) { i ->
                        LazyColumnItem(view.uiList, i)
                    }
                }
            }
        }
    }
}

@Composable
fun LazyColumnItem(uiList: SnapshotStateList<CartonItem>, i: Int) {
    Box {
        Item3(
            enableBottomSpace = i == uiList.size - 1,
            text1 = uiList[i].product.KBarCode,
            text2 = "تعداد: " + uiList[i].product.draftNumber,
            text3 = "کارتن: " + uiList[i].cartonNumber,
            text4 = "انبار: " + uiList[i].product.warehouseCode,
            text5 = "قفسه: " + uiList[i].product.shelfAddress,
            text6 = "سایز: " + uiList[i].product.size
        )
    }
}

@Composable
fun Content2(
    view: SearchAnItemInCartonsViewModel,
) {
    ScanOrTypeNumberPage(
        loading = view.loading,
        onClick = { view.searchInCartons(view.skuNumber) },
        value = view.skuNumber,
        onValueChange = { view.skuNumber = it },
        item = "SKU کالا"
    )
}