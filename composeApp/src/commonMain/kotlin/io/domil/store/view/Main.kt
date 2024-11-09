package io.domil.store.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.domil.store.getPlatform
import io.domil.store.theme.ErrorSnackBar
import io.domil.store.theme.FilterDropDownList
import io.domil.store.theme.Item
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.Shapes
import io.domil.store.theme.iconColor
import kotlinx.serialization.Serializable
import networking.Product
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.ic_barcode_scan
import storeapp.composeapp.generated.resources.ic_baseline_color_lens_24
import storeapp.composeapp.generated.resources.ic_big_barcode_scan
import storeapp.composeapp.generated.resources.size

@Serializable
object MainScreen

@Composable
fun MainPage(
    state: SnackbarHostState,
    isCameraOn: Boolean,
    colorFilterValue: String,
    colorFilterValues: List<String>,
    sizeFilterValue: String,
    sizeFilterValues: List<String>,
    onBottomBarButtonClick: () -> Unit,
    loading: Boolean,
    uiList: List<Product>,
    onScanButtonClick: () -> Unit,
    onColorFilterValueChange: (value: String) -> Unit,
    onSizeFilterValueChange: (value: String) -> Unit,
    textFieldValue: String,
    onTextValueChange: (value: String) -> Unit,
    onImeAction: () -> Unit,
    onScanSuccess: (barcodes: String) -> Unit,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    SearchContent(
                        colorFilterValue = colorFilterValue,
                        colorFilterValues = colorFilterValues,
                        sizeFilterValue = sizeFilterValue,
                        sizeFilterValues = sizeFilterValues,
                        isCameraOn = isCameraOn,
                        loading = loading,
                        uiList = uiList,
                        onColorFilterValueChange = onColorFilterValueChange,
                        onSizeFilterValueChange = onSizeFilterValueChange,
                        onScanButtonClick = onBottomBarButtonClick,
                        onTextValueChange = onTextValueChange,
                        onImeAction = onImeAction,
                        textFieldValue = textFieldValue,
                        onScanSuccess = onScanSuccess,
                        barcodeScanner = barcodeScanner
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
                floatingActionButton = { BarcodeScanButton(onBottomBarButtonClick = onBottomBarButtonClick) },
                floatingActionButtonPosition = FabPosition.End,
            )
        }
    }
}

@Composable
fun BarcodeScanButton(onBottomBarButtonClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
        Button(
            modifier = Modifier
                .align(BottomStart)
                .padding(start = 16.dp),
            onClick = onBottomBarButtonClick
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_barcode_scan),
                contentDescription = "",
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "اسکن کالای جدید",
                style = MaterialTheme.typography.h1
            )
        }
    }
}

@Composable
fun SearchContent(
    colorFilterValue: String,
    colorFilterValues: List<String>,
    sizeFilterValue: String,
    sizeFilterValues: List<String>,
    isCameraOn: Boolean,
    loading: Boolean,
    uiList: List<Product>,
    onColorFilterValueChange: (value: String) -> Unit,
    onSizeFilterValueChange: (value: String) -> Unit,
    onScanButtonClick: () -> Unit,
    textFieldValue: String,
    onTextValueChange: (value: String) -> Unit,
    onImeAction: () -> Unit,
    onScanSuccess: (barcodes: String) -> Unit,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .padding(bottom = 0.dp)
                .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                .background(
                    color = MaterialTheme.colors.onPrimary,
                    shape = MaterialTheme.shapes.large
                )
                .fillMaxWidth(),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductCodeTextField(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 14.dp)
                        .weight(1F)
                        .fillMaxWidth(),
                    onImeAction = onImeAction,
                    onTextValueChange = onTextValueChange,
                    textFieldValue = textFieldValue
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {

                FilterDropDownList(
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp),
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_baseline_color_lens_24),
                            contentDescription = "",
                            tint = iconColor,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 4.dp)
                        )
                    },
                    text = {
                        Text(
                            style = MaterialTheme.typography.body2,
                            text = colorFilterValue,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 4.dp)
                        )
                    },
                    onClick = onColorFilterValueChange,
                    values = colorFilterValues
                )

                FilterDropDownList(
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 16.dp),
                    icon = {
                        Icon(
                            painter = painterResource(Res.drawable.size),
                            contentDescription = "",
                            tint = iconColor,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.CenterVertically)
                                .padding(start = 6.dp)
                        )
                    },
                    text = {
                        Text(
                            text = sizeFilterValue,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 6.dp)
                        )
                    },
                    onClick = onSizeFilterValueChange,
                    values = sizeFilterValues
                )
            }
        }

        if (loading) {
            Row(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
                Text(
                    text = "در حال بارگذاری",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        if (isCameraOn && getPlatform().name.contains("Android")) {
            barcodeScanner {
                onScanSuccess(it)
            }
        } else if (isCameraOn && getPlatform().name.contains("IOS")) {
            barcodeScanner {
                onScanSuccess(it)
            }
        } else {
            if (uiList.isEmpty()) {
                EmptyList(onScanButtonClick = onScanButtonClick)
            } else {
                LazyColumn {
                    items(uiList.size) { i ->
                        Item(i, uiList = uiList, false)
                    }
                }
            }
        }
    }
}


@Composable
fun ProductCodeTextField(
    modifier: Modifier,
    textFieldValue: String,
    onTextValueChange: (value: String) -> Unit,
    onImeAction: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        textStyle = MaterialTheme.typography.body2,

        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = ""
            )
        },
        value = textFieldValue,
        onValueChange = onTextValueChange,
        modifier = modifier
            .testTag("SearchProductCodeTextField")
            .background(
                color = MaterialTheme.colors.secondary,
                shape = MaterialTheme.shapes.small
            ),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onImeAction()
        }),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = MaterialTheme.colors.secondary
        ),
        placeholder = { Text(text = "کد محصول") }
    )
}

@Composable
fun EmptyList(
    onScanButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(bottom = 56.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Center)
                .width(256.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color.White, shape = Shapes.medium)
                    .size(256.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_big_barcode_scan),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Center)
                        .clickable {
                            onScanButtonClick()
                        }
                )
            }

            Text(
                "بارکد را اسکن یا کد محصول را در کادر جستجو وارد کنید",
                style = MaterialTheme.typography.h1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}