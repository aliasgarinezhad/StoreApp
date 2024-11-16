package io.domil.store.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import io.domil.store.getPlatform
import io.domil.store.theme.ErrorSnackBar
import io.domil.store.theme.FilterDropDownList
import io.domil.store.theme.FullScreenImage
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.Shapes
import io.domil.store.theme.iconColor
import kotlinx.serialization.Serializable
import networking.Product
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.barcode_scan_icon
import storeapp.composeapp.generated.resources.ic_barcode_scan
import storeapp.composeapp.generated.resources.ic_big_barcode_scan
import storeapp.composeapp.generated.resources.ic_person
import storeapp.composeapp.generated.resources.store

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
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
    onLogoutClick: () -> Unit,
    storesFilterValue: String,
    storesFilterValues: List<String>,
    onStoreFilterValueChange: (value: String) -> Unit,
    isFullScreenImage: Boolean,
    changeImageFullScreen: () -> Unit
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
                        barcodeScanner = barcodeScanner,
                        onLogoutClick = onLogoutClick,
                        storesFilterValues = storesFilterValues,
                        storesFilterValue = storesFilterValue,
                        onStoreFilterValueChange = onStoreFilterValueChange,
                        isFullScreenImage = isFullScreenImage,
                        changeImageFullScreen = changeImageFullScreen
                    )
                },
                snackbarHost = { ErrorSnackBar(state) }
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
    storesFilterValues: List<String>,
    storesFilterValue: String,
    onStoreFilterValueChange: (value: String) -> Unit,
    isCameraOn: Boolean,
    loading: Boolean,
    uiList: List<Product>,
    onColorFilterValueChange: (value: String) -> Unit,
    onSizeFilterValueChange: (value: String) -> Unit,
    onScanButtonClick: () -> Unit,
    textFieldValue: String,
    onTextValueChange: (value: String) -> Unit,
    onImeAction: () -> Unit,
    onLogoutClick: () -> Unit,
    onScanSuccess: (barcodes: String) -> Unit,
    barcodeScanner: @Composable (onScanSuccess: (barcode: String) -> Unit) -> Unit,
    isFullScreenImage: Boolean,
    changeImageFullScreen: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
        Column(
            modifier = Modifier
                .padding(bottom = 0.dp)
                .shadow(elevation = 6.dp, shape = MaterialTheme.shapes.large)
                .background(
                    color = MaterialTheme.colors.onPrimary,
                    shape = MaterialTheme.shapes.large
                )
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
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
                Icon(
                    painter = painterResource(Res.drawable.barcode_scan_icon),
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier
                        .clickable {
                            onScanButtonClick()
                        }
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp, end = 10.dp)
                )

                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier
                        .clickable {
                            onLogoutClick()
                        }
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp, end = 10.dp)
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
                            painter = painterResource(Res.drawable.store),
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
                            text = storesFilterValue,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 6.dp)
                        )
                    },
                    onClick = onStoreFilterValueChange,
                    values = storesFilterValues
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
                if (isFullScreenImage) {
                    val imageList = uiList.map { it.ImgUrl.toString() }
                    var currentImage = uiList[0].ImgUrl.toString()
                    FullScreenImage(currentImage, changeImageFullScreen, imageList) {
                        currentImage = it
                    }
                } else {
                    AsyncImage(
                        modifier = Modifier.weight(2f).fillMaxWidth().clickable {
                            changeImageFullScreen()
                        },
                        model = uiList[0].ImgUrl,
                        contentDescription = "",
                    )
                    Row(
                        modifier = Modifier.weight(.3f)
                            .padding(start = 30.dp, end = 30.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .width(65.dp),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val images = uiList.map { it.ImgUrl.toString() }
                            items(images.size) { index ->
                                Image(
                                    painter = rememberAsyncImagePainter(images[index]),
                                    contentDescription = images[index],
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable {

                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
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