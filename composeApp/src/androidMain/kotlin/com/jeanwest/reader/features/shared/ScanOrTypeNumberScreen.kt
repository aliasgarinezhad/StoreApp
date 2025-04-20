package com.jeanwest.reader.features.shared

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jeanwest.reader.R


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScanOrTypeNumberScreen(
    loading: Boolean,
    onClick: () -> Unit,
    value: String,
    onValueChange: (it: String) -> Unit,
    item: String,
    popupHost: NotificationPopupHost = NotificationPopupHost(),
    topBarTitle: String,
    topBarOnClick: () -> Unit,
    state: SnackbarHostState,
) {

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                topBar = {
                    AppBarWithBack(
                        title = topBarTitle,
                        onBackPressed = topBarOnClick
                    )
                },
                content = {
                    ContentScanOrTypeNumberScreen(
                        loading,
                        onClick,
                        value,
                        onValueChange,
                        item,
                        popupHost
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
fun ContentScanOrTypeNumberScreen(
    loading: Boolean,
    onClick: () -> Unit,
    value: String,
    onValueChange: (it: String) -> Unit,
    item: String,
    popupHost: NotificationPopupHost,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        if (loading) {
            LoadingCircularProgressIndicator(isDataLoading = true)
        } else {
            Column {

                NotificationPopUp(popupHost)

                SimpleTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                        .testTag("CustomTextField"),
                    hint = item,
                    onValueChange = {
                        onValueChange(it)
                    },
                    value = value,
                    onDone = {
                        onClick()
                    }
                )

                Box(
                    modifier = Modifier.fillMaxSize()
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
                                painter = painterResource(id = R.drawable.ic_big_barcode_scan),
                                contentDescription = "",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }

                        Text(
                            "$item را اسکن یا در کادر جستجو وارد کنید",
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
            }
        }
    }
}

@Preview
@Composable
fun PreviewScanOrTypeNumberScreen() {
    ScanOrTypeNumberScreen(
        loading = false,
        onClick = {},
        value = "SHSR0921",
        onValueChange = {},

        item = "قفسه",
        popupHost = NotificationPopupHost(),
        "انتقال قفسه",
        {},
        state = SnackbarHostState()
    )
}