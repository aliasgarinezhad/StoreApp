package com.jeanwest.reader.features.cardex.viewmodel

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.jeanwest.reader.data.local.SharedPreference
import com.jeanwest.reader.data.remote.API
import com.jeanwest.reader.features.cardex.view.CardexViewModel
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.ScanOrTypeNumberPage
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.useCases.Barcode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * this file holds Cardex feature screens.
 * the feature is responsible for showing
 * transfer history for a given
 * product in current user warehouse.
 */
@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class Cardex : ComponentActivity() {

    lateinit var barcode: Barcode

    @Inject
    lateinit var memory: SharedPreference

    @Inject
    lateinit var api: API

    val viewModel by viewModels<CardexViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Page(viewModel, onBackPressed = { back() })
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

    fun back() {
        finish()
    }
}


@OptIn(ExperimentalCoilApi::class)
@SuppressLint("Unusedmaterial3ScaffoldPaddingParameter")
@ExperimentalFoundationApi
@Composable
fun Page(viewModel: CardexViewModel, onBackPressed: () -> Unit) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = { onBackPressed() },
                        viewModel.title
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        if (viewModel.scanningMode) Content2(viewModel) else Content(viewModel)
                    }
                },
                snackbarHost = { ErrorSnackBar(viewModel.state) }
            )
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun Content(viewModel: CardexViewModel) {
    Column {
        if (viewModel.loading) {
            LoadingCircularProgressIndicator(
                isDataLoading = viewModel.loading
            )
        } else {
            Row(
                modifier = Modifier
                    .shadow(6.dp, Shapes.medium)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .height(90.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            viewModel.productDetails.name,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                        Text(
                            viewModel.productDetails.KBarCode,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight()
                            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "موجودی فعلی: " + viewModel.uiListCardex[viewModel.uiListCardex.size - 1].qtyAfterDelivery,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )

                        Text(
                            "رنگ: ${viewModel.productDetails.color}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Right,
                        )
                    }
                }
            }

            LazyColumn {
                items(viewModel.uiListCardex.size) { i ->
                    Item2(
                        clickable = true,
                        enableBottomSpace = i == viewModel.uiListCardex.size - 1,
                        text1 = "نوع: " + viewModel.uiListCardex[i].deliveryTypeDetails,
                        customColor = viewModel.uiListCardex[i].deliveryHasColor,
                        text2 = "قبل سند: " + viewModel.uiListCardex[i].qtyBeforeDelivery,
                        text3 = viewModel.uiListCardex[i].deliveryDetails,
                        text4 = "در سند: " + viewModel.uiListCardex[i].deliveryQty,
                        text5 = "شماره: " + viewModel.uiListCardex[i].deliveryID,
                        text6 = "بعد سند: " + viewModel.uiListCardex[i].qtyAfterDelivery,
                        text7 = "تاریخ: " + viewModel.uiListCardex[i].deliveryDateShamsi,
                        onClick = {
                            viewModel.onItemClick(viewModel.uiListCardex[i])
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun Content2(viewModel: CardexViewModel) {
    ScanOrTypeNumberPage(
        loading = viewModel.loading,
        onClick = {
            viewModel.getProductDetails(viewModel.scannedBarcode)
        },
        value = viewModel.scannedBarcode,
        onValueChange = { viewModel.scannedBarcode = it },
        item = "بارکد کالا"
    )
}
