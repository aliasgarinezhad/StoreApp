package com.jeanwest.reader.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

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
                    Box(Modifier.padding(it)) {
                        ContentScanOrTypeNumberScreen(
                            loading,
                            onClick,
                            value,
                            onValueChange,
                            item,
                            popupHost
                        )
                    }
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

                        ScanBox(
                            item = "$item را اسکن یا در کادر جستجو وارد کنید",
                            modifier = Modifier
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