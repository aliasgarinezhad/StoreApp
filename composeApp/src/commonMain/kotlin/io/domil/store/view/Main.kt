package io.domil.store.view

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
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
import io.domil.store.getPlatform
import io.domil.store.theme.ErrorSnackBar
import io.domil.store.theme.FilterDropDownList
import io.domil.store.theme.FullScreenImage
import io.domil.store.theme.Jeanswest
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.MyTypography
import io.domil.store.theme.Shapes
import io.domil.store.theme.iconColor
import kotlinx.serialization.Serializable
import networking.Product
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.barcode_scan_icon
import storeapp.composeapp.generated.resources.ic_barcode_scan
import storeapp.composeapp.generated.resources.ic_big_barcode_scan
import storeapp.composeapp.generated.resources.store

@Serializable
object MainScreen

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

@Composable
fun BarcodeScanButton(onBottomBarButtonClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
        Button(
            modifier = Modifier.align(BottomStart).padding(start = 16.dp),
            onClick = onBottomBarButtonClick
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_barcode_scan),
                contentDescription = "",
                modifier = Modifier.size(36.dp).padding(end = 8.dp)
            )
            Text(
                text = "اسکن کالای جدید", style = MaterialTheme.typography.h1
            )
        }
    }
}

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
            Row(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colors.primary)
                Text(
                    text = "در حال بارگذاری",
                    modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically)
                )
            }
        } else {
            if (isAccountDialogOpen) {
                AlertDialog(onDismissRequest = {
                    onAccountBtnClick()
                }, buttons = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = 24.dp, bottom = 16.dp, start = 24.dp, end = 24.dp),
                    ) {

                        Text(
                            "تنظیمات حساب کاربری",
                            modifier = Modifier.fillMaxWidth().align(CenterHorizontally),
                            style = MaterialTheme.typography.h1
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
                                        tint = iconColor,
                                        modifier = Modifier.size(28.dp)
                                            .align(Alignment.CenterVertically).padding(start = 6.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = storesFilterValue,
                                        style = MaterialTheme.typography.body2,
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
                        color = MaterialTheme.colors.onPrimary, shape = RectangleShape
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
                            style = MaterialTheme.typography.h1,
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
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "سطح",
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Text(
                                    text = "دپو",
                                    style = MaterialTheme.typography.body2,
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
                                                width = 0.5.dp, shape = RoundedCornerShape(0.dp), color = Jeanswest
                                            ).shadow(
                                                elevation = 1.dp, shape = RoundedCornerShape(0.dp), spotColor = Jeanswest, ambientColor = Jeanswest
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
                                            style = MyTypography().body1,
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

    OutlinedTextField(textStyle = MaterialTheme.typography.body2,

        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search, contentDescription = ""
            )
        },
        value = textFieldValue,
        onValueChange = onTextValueChange,
        modifier = modifier.testTag("SearchProductCodeTextField").background(
            color = MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.small
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
                Icon(painter = painterResource(Res.drawable.ic_big_barcode_scan),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.align(Center).clickable {
                        onScanButtonClick()
                    })
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
            style = MyTypography().h1,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = list[i].StoreMojodi.toString(),
            style = MyTypography().h3,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = list[i].DepoMojodi.toString(),
            style = MyTypography().h3,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}