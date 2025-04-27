package com.jeanwest.reader.shop.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.jeanwest.reader.view.FilterDropDownList
import com.jeanwest.reader.view.FullScreenImage
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.MyTypography
import com.jeanwest.reader.view.Shapes
import kotlinx.serialization.Serializable
import com.jeanwest.reader.shop.data.Product
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingIndicator
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.barcode_scan_icon
import storeapp.composeapp.generated.resources.ic_barcode_scan
import storeapp.composeapp.generated.resources.ic_big_barcode_scan
import storeapp.composeapp.generated.resources.store

@Serializable
object MainScreen

/**
 * Composable function for the main page of the application.  It displays the search content,
 * handles UI state, and manages user interactions.
 *
 * @param state The state of the SnackbarHost for displaying error messages.
 * @param isCameraOn Boolean flag indicating if the camera is active for scanning.
 * @param colorFilterValue The currently selected color filter value.
 * @param sizeFilterValue The currently selected size filter value.
 * @param onBottomBarButtonClick Callback for when the scan button on the bottom bar is clicked.
 * @param loading Boolean flag indicating if data is being loaded.
 * @param uiList The list of products to display before filtering.
 * @param onScanButtonClick Callback for when the scan button is clicked.  (Duplicate with onBottomBarButtonClick, needs clarification)
 * @param onColorFilterValueChange Callback for when the color filter value changes.
 * @param onSizeFilterValueChange Callback for when the size filter value changes.
 * @param textFieldValue The current text value in the search text field.
 * @param onTextValueChange Callback for when the search text field value changes.
 * @param onImeAction Callback for when the user performs an IME action (e.g., search).
 * @param onScanSuccess Callback for successful barcode scan, providing the scanned barcode string.
 * @param barcodeScanner Composable function that renders the barcode scanner UI. It accepts a callback
 *   `onScanSuccess` that is invoked when a barcode is successfully scanned, receiving the barcode string as a parameter.
 * @param onLogoutClick Callback for when the user clicks the logout button.
 * @param storesFilterValue The currently selected store filter value.
 * @param storesFilterValues List of available store filter values.
 * @param onStoreFilterValueChange Callback for when the store filter value changes.
 * @param isFullScreenImage Boolean flag indicating if the image is displayed in full screen.
 * @param changeImageFullScreen Callback to toggle full-screen */
@Composable
fun MainPage(
    state: SnackbarHostState,
    isCameraOn: Boolean,
    colorFilterValue: String,
    sizeFilterValue: String,
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
    changeImageFullScreen: () -> Unit,
    colorFilterList: Map<String, String>,
    filteredUiList: List<Product>,
    onAccountBtnClick: () -> Unit,
    isAccountDialogOpen: Boolean,
    imgAlbumUrl: List<String>,
    colorFilterLazyRowState: LazyListState
) {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(content = {
                SearchContent(
                    colorFilterValue = colorFilterValue,
                    sizeFilterValue = sizeFilterValue,
                    storesFilterValues = storesFilterValues,
                    storesFilterValue = storesFilterValue,
                    onStoreFilterValueChange = onStoreFilterValueChange,
                    isCameraOn = isCameraOn,
                    loading = loading,
                    uiList = uiList,
                    onColorFilterValueChange = onColorFilterValueChange,
                    onSizeFilterValueChange = onSizeFilterValueChange,
                    onScanButtonClick = onBottomBarButtonClick,
                    textFieldValue = textFieldValue,
                    onTextValueChange = onTextValueChange,
                    onImeAction = onImeAction,
                    onLogoutClick = onLogoutClick,
                    onScanSuccess = onScanSuccess,
                    barcodeScanner = barcodeScanner,
                    isFullScreenImage = isFullScreenImage,
                    changeImageFullScreen = changeImageFullScreen,
                    colorFilterList = colorFilterList,
                    filteredUiList = filteredUiList,
                    onAccountBtnClick = onAccountBtnClick,
                    isAccountDialogOpen = isAccountDialogOpen,
                    imgAlbumUrl = imgAlbumUrl,
                    colorFilterLazyRowState = colorFilterLazyRowState
                )
            }, snackbarHost = { ErrorSnackBar(state) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    colorFilterValue: String,
    sizeFilterValue: String,
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
    changeImageFullScreen: () -> Unit,
    colorFilterList: Map<String, String>,
    filteredUiList: List<Product>,
    onAccountBtnClick: () -> Unit,
    isAccountDialogOpen: Boolean,
    imgAlbumUrl: List<String>,
    colorFilterLazyRowState: LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            LoadingIndicator()
        } else {
            if (isAccountDialogOpen) {
                BasicAlertDialog(onDismissRequest = {
                    onAccountBtnClick()
                }, content = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
                    ) {

                        Text(
                            "تنظیمات حساب کاربری",
                            modifier = Modifier.fillMaxWidth().align(CenterHorizontally),
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Row(horizontalArrangement = Arrangement.Center) {

//                                Text(
//                                    "انتخاب فروشگاه",
//                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp).align(
//                                        CenterVertically
//                                    )
//                                )

                            FilterDropDownList(
                                modifier = Modifier.padding(bottom = 24.dp, top = 24.dp),
                                icon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.store),
                                        contentDescription = "",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                            .align(Alignment.CenterVertically).padding(start = 6.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = storesFilterValue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                            .padding(start = 6.dp)
                                    )
                                },
                                onClick = onStoreFilterValueChange,
                                values = storesFilterValues
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {

                            OutlinedButton(
                                onClick = {
                                    onLogoutClick()
                                },
                                modifier = Modifier.testTag("alertBtn"),
                            ) {
                                Text(text = "خروج از حساب")
                            }

                            Button(
                                onClick = {
                                    onAccountBtnClick()
                                }, modifier = Modifier.testTag("alertBtn")
                            ) {
                                Text(text = "ذخیره")
                            }
                        }
                    }
                })
            }
            Column(
                modifier = Modifier.padding(bottom = 0.dp)
                    .shadow(elevation = 1.dp, shape = RectangleShape).background(
                        color = MaterialTheme.colorScheme.onPrimary, shape = RectangleShape
                    ).fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProductCodeTextField(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 14.dp
                        ).weight(1F).fillMaxWidth(),
                        onImeAction = onImeAction,
                        onTextValueChange = onTextValueChange,
                        textFieldValue = textFieldValue
                    )
                    IconButton(
                        onClick = {
                            onScanButtonClick()
                        }, modifier = Modifier
                            //.padding(start = 4.dp)
                            .size(32.dp).align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.barcode_scan_icon),
                            contentDescription = "",
                        )
                    }

                    IconButton(
                        onClick = {
                            onAccountBtnClick()
                        },
                        modifier = Modifier.padding(start = 32.dp, end = 16.dp).size(32.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "",
                        )
                    }
                }
            }

            if (isCameraOn) {
                barcodeScanner {
                    onScanSuccess(it)
                }
            } else {
                if (uiList.isEmpty()) {
                    EmptyList(onScanButtonClick = onScanButtonClick)
                } else {
                    if (isFullScreenImage) {
                        var currentImage = uiList[0].ImgUrl
                        FullScreenImage(currentImage, changeImageFullScreen, imgAlbumUrl) {
                            currentImage = it
                        }
                    } else {

                        println(uiList[0].SalePrice)

                        Text(
                            text = uiList[0].SalePrice.toString().dropLast(4).reversed().chunked(3)
                                .joinToString(",").reversed() + "T",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )

                        AsyncImage(
                            modifier = Modifier.weight(2.5f).fillMaxWidth()
                                /*.clickable {
                                changeImageFullScreen()
                            }*/.padding(top = 8.dp, end = 16.dp, start = 16.dp, bottom = 16.dp),
                            model = filteredUiList[0].ImgUrl,
                            contentDescription = "",
                            contentScale = ContentScale.FillHeight
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(color = Color.White, RoundedCornerShape(0.dp))
                                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(end = 16.dp)) {
                                Text(
                                    text = "سایز",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "سطح",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "دپو",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            Column {
                                LazyRow {
                                    items(filteredUiList.size) { i ->
                                        sizeAndCountItem(i, filteredUiList)
                                    }
                                }
                            }
                        }

                        //BottomBar choice color
                        Row(
                            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().width(65.dp),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                state = colorFilterLazyRowState
                            ) {
                                items(colorFilterList.keys.size) { index ->
                                    Column(
                                        modifier = if (colorFilterValue == colorFilterList.keys.toList()[index]) {
                                            Modifier.clickable {
                                                onColorFilterValueChange(colorFilterList.keys.toList()[index])
                                            }.border(
                                                width = 0.5.dp,
                                                shape = RoundedCornerShape(0.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ).shadow(
                                                elevation = 1.dp,
                                                shape = RoundedCornerShape(0.dp),
                                                spotColor = MaterialTheme.colorScheme.primary,
                                                ambientColor = MaterialTheme.colorScheme.primary
                                            ).background(
                                                color = Color.White, RoundedCornerShape(0.dp)
                                            )
                                        } else {
                                            Modifier.clickable {
                                                onColorFilterValueChange(colorFilterList.keys.toList()[index])
                                            }.shadow(
                                                elevation = 1.dp, shape = RoundedCornerShape(0.dp)
                                            ).background(
                                                color = Color.White, RoundedCornerShape(0.dp)
                                            )
                                        }
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(colorFilterList.values.toList()[index]),
                                            contentDescription = "",
                                            modifier = Modifier.size(60.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            text = colorFilterList.keys.toList()[index],
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                        )
                                    }
                                }
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
        textStyle = MaterialTheme.typography.bodyMedium,

        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search, contentDescription = ""
            )
        },
        value = textFieldValue,
        onValueChange = onTextValueChange,
        modifier = modifier.testTag("SearchProductCodeTextField").background(
            color = MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small
        ),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            onImeAction()
        }),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        placeholder = { Text(text = "کد محصول") })
}

@Composable
fun EmptyList(
    onScanButtonClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(bottom = 56.dp).fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Center).width(256.dp)
        ) {
            Box(
                modifier = Modifier.background(color = Color.White, shape = Shapes.medium)
                    .size(256.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_big_barcode_scan),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.align(Center).clickable {
                        onScanButtonClick()
                    })
            }

            Text(
                "بارکد را اسکن یا کد محصول را در کادر جستجو وارد کنید",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

@Composable
fun sizeAndCountItem(
    i: Int, list: List<Product>
) {

    val sizesMap = mapOf("XXL" to "2XL", "XXXL" to "3XL")

    Column(
        modifier = if (i != list.size - 1) {
            Modifier.drawBehind {
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }.padding(end = 16.dp, start = 16.dp)
        } else {
            Modifier.padding(end = 16.dp, start = 16.dp)
        },
    ) {
        Text(
            text = list[i].Size.let {
                if (it in sizesMap)
                    sizesMap[it]!!
                else it
            },
            color = Color.Black,
            style = MyTypography().headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = list[i].StoreMojodi.toString(),
            style = MyTypography().labelMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = list[i].DepoMojodi.toString(),
            style = MyTypography().labelMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}