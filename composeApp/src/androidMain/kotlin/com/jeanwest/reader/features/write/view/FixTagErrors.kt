package com.jeanwest.reader.features.write.view


import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.features.shared.AppBarWithBack
import com.jeanwest.reader.features.shared.BottomBarButton
import com.jeanwest.reader.features.shared.EmptyBarcode
import com.jeanwest.reader.features.shared.ErrorSnackBar
import com.jeanwest.reader.features.shared.LoadingCircularProgressIndicator
import com.jeanwest.reader.features.shared.MyApplicationTheme
import com.jeanwest.reader.features.shared.NotificationPopUp
import com.jeanwest.reader.features.shared.NotificationPopupHost
import com.jeanwest.reader.features.shared.doneColor
import com.jeanwest.reader.features.shared.errorLight
import com.jeanwest.reader.features.shared.secondaryLight
import com.jeanwest.reader.features.write.viewmodel.FixTagErrorsViewModel
import com.jeanwest.reader.models.EPC
import com.jeanwest.reader.models.Tag
import com.jeanwest.reader.models.TagStatus
import com.jeanwest.reader.useCases.EncodingType
import com.jeanwest.reader.useCases.ExceptionHandler
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.AndroidEntryPoint

/*
* this feature is used for fixing RFID tag errors.
* will be deprecated or rarely used after all items tagged by RFID labels.
*  */

@AndroidEntryPoint
class FixTagErrors : ComponentActivity() {

    val viewModel: FixTagErrorsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposableHost(viewModel, onBackPressed = { finish() })
        }
        viewModel.init()
        exceptionHandler()

        this.onBackPressedDispatcher.addCallback {
            finish()
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

        if (keyCode == 280 || keyCode == 139 || keyCode == 293) {
            if (event.repeatCount == 0) {
                viewModel.scanTrigger()
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
private fun ComposableHost(viewModel: FixTagErrorsViewModel, onBackPressed: () -> Unit) {
    Page(
        state = viewModel.state,
        popupState = viewModel.popupState,
        product = viewModel.product.KBarCode,
        tags = viewModel.tags,
        loading = viewModel.loading,
        scanning = viewModel.rf.scanning,
        onBackPressed = onBackPressed,
        onBottomBarButtonClick = { viewModel.onBottomBarButtonClick() },
        onBarcodeIconClick = { viewModel.startBarcodeScanByButton() },
        onEditButtonClick = { viewModel.write(it) }
    )
}

@Composable
private fun Page(
    state: SnackbarHostState,
    popupState: NotificationPopupHost,
    product: String,
    tags: List<Tag>,
    loading: Boolean,
    onBackPressed: () -> Unit,
    onBottomBarButtonClick: () -> Unit,
    scanning: Boolean,
    onBarcodeIconClick: () -> Unit,
    onEditButtonClick: (tid: String) -> Unit,
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        onBackPressed = onBackPressed,
                        title = "اصلاح تگ های معیوب"
                    )
                },
                content = {
                    Box(modifier = Modifier.padding(it)) {
                        Content(
                            product = product,
                            loading = loading,
                            tags = tags,
                            onBarcodeIconClick = onBarcodeIconClick,
                            popupState = popupState,
                            onEditButtonClick = onEditButtonClick,
                        )
                    }
                },

                bottomBar = {
                    if (product.length > 2 && !loading) {
                        BottomBarButton(text = if (!scanning) "شروع اسکن" else "پایان اسکن") {
                            onBottomBarButtonClick()
                        }
                    }
                },

                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun Content(
    product: String,
    loading: Boolean,
    tags: List<Tag>,
    onBarcodeIconClick: () -> Unit,
    popupState: NotificationPopupHost,
    onEditButtonClick: (tid: String) -> Unit,
) {

    Column {

        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = true)
        } else {

            NotificationPopUp(state = popupState)

            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxSize()
            ) {

                if (product.length > 2) {
                    Text("کالا: $product", modifier = Modifier.padding(start = 16.dp))
                    Text(
                        "تعداد تگ های بررسی شده: ${tags.size}",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 56.dp)
                            .fillMaxSize()
                    ) {
                        item {

                            tags.forEach {

                                Column(
                                    modifier = Modifier
                                        .padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            bottom = 4.dp,
                                            top = 4.dp
                                        )
                                        .border(
                                            1.dp,
                                            color = secondaryLight,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.onPrimary,
                                            shape = MaterialTheme.shapes.small
                                        )
                                ) {

                                    Row(
                                        modifier = Modifier.padding(
                                            top = 16.dp,
                                            bottom = if (it.status == TagStatus.CORRECT) 16.dp else 8.dp,
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                    ) {

                                        Row(
                                            modifier = Modifier
                                                .weight(1F)
                                                .align(Alignment.CenterVertically)
                                        ) {
                                            Icon(
                                                imageVector = if (it.status == TagStatus.CORRECT) {
                                                    Icons.Filled.CheckCircle
                                                } else {
                                                    Icons.Filled.Info
                                                },
                                                contentDescription = "",
                                                tint = if (it.status == TagStatus.CORRECT) {
                                                    doneColor
                                                } else {
                                                    errorLight
                                                }
                                            )

                                            Text(
                                                text = it.status.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier
                                                    .padding(start = 4.dp, top = 0.dp)
                                                    .align(Alignment.CenterVertically)
                                            )
                                        }


                                        Column(modifier = Modifier.weight(2F)) {

                                            Text(
                                                text = "TID: ${it.tagInfo.tid}",
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 8.dp)
                                            )
                                            Text(
                                                text = "EPC: ${it.tagInfo.epc}",
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    if (it.status != TagStatus.CORRECT) {
                                        OutlinedButton(
                                            onClick = { onEditButtonClick(it.tagInfo.tid) },
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(bottom = 16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = ""
                                            )
                                            Text(
                                                text = "اصلاح",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    EmptyBarcode(
                        text = "لطفا بارکد کالا را اسکن کنید.",
                        onClick = onBarcodeIconClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {

    val uhfTagInfo = UHFTAGInfo()
    uhfTagInfo.epc = "30001940010ab7c000000016"
    uhfTagInfo.tid = "32401940010ab7c000545768"

    val uhfTagInfo2 = UHFTAGInfo()
    uhfTagInfo2.epc = "00001940010ab7c000000016"
    uhfTagInfo2.tid = "89701940010ab7c000545768"


    val tags = listOf(
        Tag(
            tagInfo = uhfTagInfo,
            epcDetails = EPC(encodingType = EncodingType.AVAKATAN),
            status = TagStatus.CORRECT
        ),
        Tag(
            tagInfo = uhfTagInfo2,
            epcDetails = EPC(encodingType = EncodingType.JOOTIJEANS),
            status = TagStatus.DUPLICATE
        )
    )


    Page(
        state = SnackbarHostState(),
        popupState = NotificationPopupHost(),
        product = "11531052J-2010-L",
        tags = tags,
        loading = false,
        onBackPressed = {},
        onBottomBarButtonClick = {},
        scanning = false,
        onBarcodeIconClick = {},
        onEditButtonClick = {}
    )
}
