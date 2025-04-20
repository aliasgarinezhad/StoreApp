@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.logistic.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.jeanwest.reader.R
import com.jeanwest.reader.features.logistic.viewmodel.ConfirmStockDraftLogisticInStoreViewModel
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.Item2
import com.jeanwest.reader.features.shared.Item4
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.Shapes
import com.jeanwest.reader.features.shared.SimpleTextField
import com.jeanwest.reader.features.shared.Typography
import com.jeanwest.reader.features.shared.onPrimaryLight
import com.jeanwest.reader.models.Logistic
import com.jeanwest.reader.models.StockDraft
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors


/**
 * this screen responsible for
 * confirming existed (not confirmed)
 * stock draft logistics
 * (logistics that contains stock drafts not cartons)
 * in stores. it usually used by drivers in
 * tablet mode.
 */

@AndroidEntryPoint
@OptIn(ExperimentalFoundationApi::class)
class ConfirmStockDraftLogisticInStore : ComponentActivity() {

    val viewModel by viewModels<ConfirmStockDraftLogisticInStoreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Page(
                popupState = viewModel.popupState,
                uiList2 = viewModel.logistics.values.toList(),
                onContent2ItemClick = { viewModel.onLogisticClick(it) },
                onTextFieldImeAction = { viewModel.onTextFieldImeAction() },
                uiList = viewModel.uiList,
                textFieldValue = viewModel.stockDraftNumber,
                onTextFieldValueChange = { viewModel.onTextFieldValueChange(it) },
                title = stringResource(id = R.string.stockDraftTransferToStoreByDriverConfirmation),
                loading = viewModel.loading,
                scanningMode = viewModel.scanningMode,
                state = viewModel.state,
                onBackPressed = { back() },
                onBottomBarButtonPressed = { viewModel.confirmCheckIns() },
                isCameraOn = viewModel.isCameraOn,
                onBarcodeScannerResult = { viewModel.onBarcodeScanResult(it) },
                context = this,
                onScanButtonClick = { viewModel.onScanButtonClick() }
            )
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

    private fun back() {
        if (viewModel.scanningMode) {
            viewModel.back()
        } else {
            finish()
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun Page(
    title: String,
    state: SnackbarHostState,
    textFieldValue: String,
    onTextFieldValueChange: (newValue: String) -> Unit,
    onTextFieldImeAction: () -> Unit,
    scanningMode: Boolean,
    uiList: List<StockDraft>,
    loading: Boolean,
    onBottomBarButtonPressed: () -> Unit,
    onBackPressed: () -> Unit,
    onContent2ItemClick: (logistic: Logistic) -> Unit,
    uiList2: List<Logistic>,
    popupState: NotificationPopupHost,
    isCameraOn: Boolean,
    onBarcodeScannerResult: (scannedBarcode: String) -> Unit,
    context: ComponentActivity,
    onScanButtonClick: () -> Unit,

    ) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBar(
                        title = title,
                        onScanButtonClick = onScanButtonClick,
                        onBackPressed = onBackPressed
                    )
                },
                content = {

                    Box(modifier = Modifier.padding(it)) {
                        if (scanningMode) Content(
                            onTextFieldImeAction = { onTextFieldImeAction() },
                            loading = loading,
                            uiList = uiList,
                            textFieldValue = textFieldValue,
                            onTextFieldValueChange = onTextFieldValueChange,
                            isCameraOn = isCameraOn,
                            onBarcodeScannerResult = onBarcodeScannerResult,
                            context = context
                        ) else Content2(
                            popupState = popupState,
                            loading = loading,
                            uiList = uiList2,
                            onItemClick = { onContent2ItemClick(it) }
                        )
                    }
                },
                bottomBar = {
                    BottomBar(loading = loading, scanningMode = scanningMode) {
                        onBottomBarButtonPressed()
                    }
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun BottomBar(
    scanningMode: Boolean,
    loading: Boolean,
    onBottomBarButtonPressed: () -> Unit,
) {

    if (!loading && scanningMode) {

        BottomBarButton(text = "دریافت حواله ها") {
            onBottomBarButtonPressed()
        }
    }
}

@Composable
private fun AppBar(
    title: String,
    onBackPressed: () -> Unit,
    onScanButtonClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = ""
                )
            }
        },

        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Right,
            )
        },
        actions = {
            IconButton(onClick = { onScanButtonClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.barcode_scan_icon),
                    modifier = Modifier.size(36.dp),
                    contentDescription = ""

                )
            }
        })
}

@ExperimentalFoundationApi
@Composable
private fun Content(
    loading: Boolean,
    textFieldValue: String,
    onTextFieldValueChange: (newValue: String) -> Unit,
    onTextFieldImeAction: () -> Unit,
    uiList: List<StockDraft>,
    isCameraOn: Boolean,
    onBarcodeScannerResult: (scannedBarcode: String) -> Unit,
    context: ComponentActivity,
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
        } else if (isCameraOn) {
            BarcodeScannerWithCamera(context = context) {
                Log.e("cameraScanner", it[0].displayValue.toString())
                onBarcodeScannerResult(it[0].displayValue.toString())
            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 16.dp, start = 16.dp)
            ) {
                Text(
                    text = "تعداد حواله ها: " + (uiList.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = "اسکن شده: " + (uiList.filter { it.isScanned }.size).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
            }

            Row {
                SimpleTextField(
                    value = textFieldValue,
                    hint = "شماره حواله",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                            top = 12.dp
                        )
                        .fillMaxWidth(),
                    onValueChange = {
                        onTextFieldValueChange(it)
                    },
                    onDone = {
                        onTextFieldImeAction()
                    })
            }

            if (uiList.isEmpty()) {
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
                            "هنوز حواله ای برای ارسال اسکن نکرده اید",
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

                LazyColumn(modifier = Modifier.padding(bottom = 56.dp)) {

                    items(uiList.size) { i ->
                        Item2(
                            clickable = false,
                            enableBottomSpace = i == uiList.size - 1,
                            text1 = "شناسه عملیاتی : " + uiList[i].driverOperationId,
                            text2 = "تاریخ: " + uiList[i].date,
                            text3 = "از: " + uiList[i].sourceTitle,
                            text4 = "تعداد کالاها: " + uiList[i].numberOfItems,
                            text5 = "به: " + uiList[i].destinationTitle,
                            text6 = "تگ RFID: " + if (uiList[i].epcsToPrimaryKeysMap.isNotEmpty()) "دارد" else "ندارد",
                            text7 = "شرح: " + if (uiList[i].specification == "null") "بدون شرح" else uiList[i].specification,
                            colorFull = uiList[i].isScanned,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Content2(
    loading: Boolean,
    uiList: List<Logistic>,
    popupState: NotificationPopupHost,
    onItemClick: (logistic: Logistic) -> Unit,
) {

    val listState = rememberLazyListState(0)

    if (loading) {
        LoadingCircularProgressIndicator(isDataLoading = true)
    } else {

        NotificationPopUp(popupState)

        Column(modifier = Modifier.fillMaxSize()) {

            LazyColumn(modifier = Modifier.padding(top = 4.dp), state = listState) {

                items(uiList.size) { i ->
                    Item4(
                        clickable = true,
                        enableBottomSpace = i == uiList.size - 1,
                        text1 = "نام راننده: " + uiList[i].name,
                        text3 = "تعداد حواله ها: " + uiList[i].numberOfItems,
                        text4 = "",
                        text2 = "مقصد: " + uiList[i].destination,
                    ) {
                        onItemClick(uiList[i])
                    }
                }
            }
        }
    }
}


@SuppressLint("UnsafeOptInUsageError")
@Composable
fun BarcodeScannerWithCamera(
    context: ComponentActivity,
    onClick: (barcodes: List<Barcode>) -> Unit,
) {

    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
    val previewView = PreviewView(context)
    val preview = Preview.Builder().build()
    preview.surfaceProvider = previewView.surfaceProvider
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(1280, 720))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

        val mediaImage = imageProxy.image
        if (mediaImage != null) {

            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->

                    Log.e("scanned", barcodes.toList().toString())
                    if (barcodes.isNotEmpty()) {
                        onClick(barcodes)
                    } else {
                        imageProxy.close()
                    }
                }
                .addOnFailureListener {
                    imageProxy.close()
                }

        }
    }

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        context,
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
        imageAnalysis,
        preview
    )

    Box(
        modifier = Modifier
            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
    }
}
