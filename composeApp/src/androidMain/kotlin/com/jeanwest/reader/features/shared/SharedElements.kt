@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeanwest.reader.features.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.jeanwest.reader.R
import com.jeanwest.reader.data.local.FeatureLocation
import com.jeanwest.reader.data.local.banimodeFeatures
import com.jeanwest.reader.data.local.cartonsFeatures
import com.jeanwest.reader.data.local.driverFeatures
import com.jeanwest.reader.data.local.mojoodiReviewFeatures
import com.jeanwest.reader.data.local.requestFeatures
import com.jeanwest.reader.data.local.shelfFeatures
import com.jeanwest.reader.data.local.stockDraftsFeatures
import com.jeanwest.reader.data.local.transferFeatures
import com.jeanwest.reader.features.main.view.MainViewModel
import com.jeanwest.reader.models.Feature
import com.jeanwest.reader.models.Product
import com.jeanwest.reader.models.ShelfItem
import com.jeanwest.reader.models.StockDraftRequestItem
import java.util.Locale


@Composable
fun InventoryReportItem(
    modifier: Modifier,
    title: String,
    description: String,
    itemPercentage: Float,
    buttonTitle: String,
    buttonOnClick: () -> Unit,
) {
    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = Shapes.large
        )
    ) {

        Box(
            modifier = modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(
                        Alignment.Center
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = Shapes.medium
                    ),
            ) {

                CircularProgressIndicator(
                    progress = { itemPercentage },
                    modifier = Modifier
                        .size(150.dp)
                        .align(
                            Alignment.Center
                        ),
                    strokeWidth = 8.dp,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = String.format(
                        locale = Locale.US,
                        "%.2f",
                        itemPercentage * 100
                    ) + "%\n" + description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(
                            Alignment.Center
                        ),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Text(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
        BigButton(
            text = buttonTitle, modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(36.dp)
        ) {
            buttonOnClick()
        }
    }
}

@Composable
fun EmptyBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("emptyBox")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(256.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = Shapes.medium
                    )
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
                text,
                style = Typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Composable
fun EmptyBarcode(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("emptyBox")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(256.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = Shapes.medium
                    )
                    .size(256.dp)
                    .clickable(onClick = onClick)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_big_barcode_scan),
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Text(
                text,
                style = Typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun EmptyShelf(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("emptyBox")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(256.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = Shapes.medium
                    )
                    .size(256.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shelf),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }
            Text(
                text,
                style = Typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ShowProductDetails(
    modifier: Modifier = Modifier,
    product: Product,
    specialParameter: String = "",
) {

    val textModifier = Modifier
        .padding(top = 2.dp, bottom = 2.dp)
        .wrapContentWidth()

    Row(
        modifier = modifier
            .shadow(2.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp),
    ) {

        Image(
            painter = rememberAsyncImagePainter(model = product.imageUrl),
            contentDescription = "",
            modifier = Modifier
                .height(200.dp)
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )

        Column {
            Text(
                text = product.name,
                style = Typography.headlineMedium,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = product.KBarCode,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = "قیمت: " + product.originalPrice,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = "قیمت با تخفیف: " + product.salePrice,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = "موجودی فروشگاه: " + product.storeNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = "موجودی انبار: " + product.wareHouseNumber.toString(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
            Text(
                text = specialParameter,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = textModifier,
            )
        }
    }
}

@Composable
fun OpenActivityButton(text: Int, iconId: Int, onClick: () -> Unit) {

    val iconSize = 48.dp
    val textSize = 64.dp

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onClick()
            }
    ) {

        Icon(
            painter = painterResource(iconId),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "",
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.large
                )
                .padding(4.dp)
        )
        Text(
            stringResource(id = text),
            modifier = Modifier
                .width(textSize)
                .padding(top = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun BigButton(
    modifier: Modifier = Modifier,
    text: String, onClick: () -> Unit,
) {
    Button(
        modifier = modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = Typography.labelLarge
        )
    }
}

@Composable
fun BottomBarButton(text: String, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            //.shadow(30.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .height(64.dp)
    ) {

        Button(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .align(Alignment.Center)
                .fillMaxSize()
                .testTag("bottomBarButton"),
            onClick = onClick
        ) {
            Text(
                text = text,
                style = Typography.bodyMedium
            )
        }
    }
}

@Composable
fun ExpandableCard(key: String, features: MutableList<Feature>, viewModel: MainViewModel) {
    var title = ""

    var featureList = mutableListOf<Feature>()

    when (key) {

        "cartonsFeatures" -> {
            title = "کارتن"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in cartonsFeatures
            }.toMutableList()
        }

        "stockDraftsFeatures" -> {
            title = "حواله"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in stockDraftsFeatures
            }.toMutableList()
        }

        "shelfFeatures" -> {
            title = "قفسه"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in shelfFeatures
            }.toMutableList()
        }

        "driverFeatures" -> {
            title = "راننده"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in driverFeatures
            }.toMutableList()
        }

        "transferFeatures" -> {
            title = "انتقال"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in transferFeatures
            }.toMutableList()
        }

        "mojoodiReviewFeatures" -> {
            title = "کنترل موجودی"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in mojoodiReviewFeatures
            }.toMutableList()
        }

        "requestFeatures" -> {
            title = "درخواست ها"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in requestFeatures
            }.toMutableList()
        }

        "banimodeFeatures" -> {
            title = "بانی مد"
            featureList = features.filter {
                FeatureLocation.CENTRAL_WAREHOUSE in it.featureLocationsArray &&
                        it.accessKey in banimodeFeatures
            }.toMutableList()
        }

        else -> {

        }
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
            }

    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(
                        id = if (expanded) {
                            R.drawable.ic_baseline_arrow_drop_up_24
                        } else {
                            R.drawable.ic_baseline_arrow_drop_down_24
                        }
                    ),
                    "",
                    modifier = Modifier
                        .padding(start = 0.dp, end = 4.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(8.dp),

                    )
            }


            if (expanded) {
                val numberOfRowsBeforeLastRow = (featureList.size / 4)
                val numberOfFeaturesInLastRow = (featureList.size % 4)

                for (rowIndex in 0 until numberOfRowsBeforeLastRow) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {

                        for (i in 0..3) {
                            val it = featureList[rowIndex * 4 + i]
                            OpenActivityButton(
                                it.featureTitleResourceAddress,
                                it.featureIconResourceAddress
                            ) {
                                var data: String? = null
                                if (it.featureTitleResourceAddress == R.string.NewShelfInRequest) {
                                    data = "RFID"
                                }
                                viewModel.onFeatureButtonClick(it.featureClass, data)
                            }
                        }
                    }
                }

                if (numberOfFeaturesInLastRow != 0) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {

                        for (i in 0 until numberOfFeaturesInLastRow) {
                            val it = featureList[numberOfRowsBeforeLastRow * 4 + i]
                            OpenActivityButton(
                                it.featureTitleResourceAddress,
                                it.featureIconResourceAddress
                            ) {
                                var data: String? = null
                                if (it.featureTitleResourceAddress == R.string.NewShelfInRequest) {
                                    data = "RFID"
                                }
                                viewModel.onFeatureButtonClick(it.featureClass, data)
                            }
                        }

                        for (i in 0 until (4 - numberOfFeaturesInLastRow)) {
                            Box(modifier = Modifier.size(80.dp))
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemShelfItems(
    i: Int,
    uiList: List<ShelfItem> = mutableListOf(),
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    enableRequestNumber: Boolean = true,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    signedKBarCode: MutableList<ShelfItem>,
) {

    val isColorFull by remember {
        derivedStateOf { uiList[i] in signedKBarCode }
    }

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = if (isColorFull) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].product.imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .shadow(0.dp, shape = Shapes.large)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = Shapes.large
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if ((enableRequestNumber && uiList[i].product.requestedNumber > 0) || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else uiList[i].product.requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].product.KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].product.name else text2,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}


@Composable
fun LoginContent() {
    Column(
        modifier = Modifier
            .background(Color.White)
    ) {
        SimpleTextField(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                .fillMaxWidth(),
            hint = "نام کاربری خود را وارد کنید",
            onValueChange = { },
            value = "",
        )

        SimpleTextField(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
                .fillMaxWidth(),
            hint = "رمز عبور خود را وارد کنید",
            onValueChange = {},
            value = "",
            visualTransformation = PasswordVisualTransformation()
        )

        BigButton(text = "ورود") {

        }
        Row(
            modifier = Modifier
                .align(Alignment.Start)
        ) {
            var isChecked by remember { mutableStateOf(false) }

            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = onPrimaryDark,
                    uncheckedThumbColor = errorContainerLight,
                    checkedTrackColor = primaryDark,
                    uncheckedTrackColor = primaryDark
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )

            // Display the switch state
            Text(
                text = "ورود بدون اینترنت",
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 24.dp, end = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemWithInputText(
    i: Int = 0,
    uiList: MutableList<Product> = mutableListOf(),
    enableSign: Boolean = false,
    signNumber: Int = 0,
    text1: String = "",
    text2: String = "",
    onConfirm: (Product) -> Unit = {},
    onTextChange: (String) -> Unit = {},
    text: String = "1",
) {

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = topPadding)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = Shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (uiList[i].requestedNumber > 0 || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else uiList[i].requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(16.dp)
                    .wrapContentWidth()
                    .background(
                        color = Color.Transparent,
                        shape = Shapes.small
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = text,
                    onValueChange = { newValue ->
                        onTextChange(newValue)
                    },
                    label = {
                        Text(
                            text = "تعداد",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally), textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val onlyNumbers = text.filter { it.isDigit() }
                            if (onlyNumbers.isNotEmpty()) {
                                uiList[i].scannedBarcodeNumber =
                                    Integer.valueOf(onlyNumbers)
                                onConfirm(uiList[i])
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            } else {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        }
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(
    i: Int,
    uiList: List<Product> = mutableListOf(),
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    enableRequestNumber: Boolean = true,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 8.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            //.shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (uiList[i].scannedNumber > uiList[i].wareHouseNumber && enableWarehouseNumberCheck) {
                    MaterialTheme.colorScheme.error
                } else if (colorFull) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if ((enableRequestNumber && uiList[i].requestedNumber > 0) || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else uiList[i].requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemNewRefill(
    i: Int,
    uiList: List<Product> = mutableListOf(),
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 8.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (uiList[i].scannedNumber > uiList[i].wareHouseNumber && enableWarehouseNumberCheck) {
                    MaterialTheme.colorScheme.error
                } else if (colorFull) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (enableSign && signNumber > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = signNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StockDraftRequestItem(
    item: StockDraftRequestItem,
    clickable: Boolean = false,
    text3: String,
    text4: String,
    text1: String = "",
    text2: String = "",
    enableTopSpace: Boolean,
    enableBottomSpace: Boolean,
    onClick: () -> Unit = {},
) {

    val topPadding = if (enableTopSpace) 16.dp else 8.dp
    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .clickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(item.product.imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") item.product.KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") item.product.name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = primaryContainerLight,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemReverseRefill(
    i: Int,
    uiList: MutableList<Product> = mutableListOf(),
    clickable: Boolean = false,
    text3: String,
    text4: String,
    enableSign: Boolean = false,
    signNumber: Int = 0,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 8.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (uiList[i].scannedNumber > uiList[i].storeNumber && enableWarehouseNumberCheck) {
                    errorLight
                } else if (colorFull) {
                    secondaryLight
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onLongClick = { onLongClick() },
                onClick = { onClick() })
    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (uiList[i].requestedNumber > 0 || (enableSign && signNumber > 0)) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                        .testTag("sign")
                ) {
                    Text(
                        text = if (enableSign) signNumber.toString() else uiList[i].requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = primaryContainerLight,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = text3,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp),
                    thickness = 1.dp,
                    color = primaryLight
                )
                Text(
                    text = text4,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item2(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    text1: String,
    customColor: Boolean = false,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    text7: String = "",
    colorFull: Boolean = false,
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (colorFull) doneColor else MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(104.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {


        Row(
            modifier = Modifier.height(76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Row(
                modifier = Modifier
                    .weight(3F)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.5F)
                        .fillMaxHeight()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text1,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text3,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text5,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text2,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                    )

                    Text(
                        text = text4,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .background(
                                color = if (colorFull) doneColor else if (customColor) doneColorLighterShade else Color.White,
                                shape = RectangleShape
                            ),
                    )

                    Text(
                        text = text6,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .height(28.dp),
        ) {
            Text(
                text = text7,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurfaceWith2Columns5Rows(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    text1: String,
    customColor: Boolean = false,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    text7: String = "",
    text8: String,
    colorFull: Boolean = false,
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (colorFull) doneColor else MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(130.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {


        Row(
            modifier = Modifier.height(76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Row(
                modifier = Modifier
                    .weight(3F)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.5F)
                        .fillMaxHeight()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text1,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text3,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text5,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text2,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                    )

                    Text(
                        text = text4,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .background(
                                color = if (colorFull) doneColor else if (customColor) doneColorLighterShade else Color.White,
                                shape = RectangleShape
                            ),
                    )

                    Text(
                        text = text6,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )
                }
            }
        }
        Row {
            Text(
                text = text7,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .height(28.dp),
        ) {
            Text(
                text = text8,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item3(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    text1: String,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(90.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onClick = { onClick() }),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        Row(
            modifier = Modifier
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1.5F)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                )

                Text(
                    text = text3,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                )

                Text(
                    text = text5,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .wrapContentWidth(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                )

                Text(
                    text = text4,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1F),
                )

                Text(
                    text = text6,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1F),
                )
            }
        }
    }
}

@Composable
fun Item4(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    colorFull: Boolean = false,
    text1: String,
    text2: String,
    text3: String,
    text4: String,
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (colorFull) doneColor else MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(80.dp)
            .testTag("items")
            .clickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {

        Row(
            modifier = Modifier
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1.5F)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp),
                )

                Text(
                    text = text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(start = 16.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = text3,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )

                Text(
                    text = text4,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item5(
    i: Int,
    uiList: MutableList<Product> = mutableListOf(),
    text3: String,
    text4: String,
    colorFull: Boolean = false,
    enableWarehouseNumberCheck: Boolean = false,
    text1: String = "",
    text2: String = "",
    onClick: () -> Unit = {},
) {

    val topPadding = if (i == 0) 16.dp else 8.dp
    val bottomPadding = if (i == uiList.size - 1) 128.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = bottomPadding,
                top = topPadding
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = if (uiList[i].scannedNumber > uiList[i].wareHouseNumber && enableWarehouseNumberCheck) {
                    errorLight
                } else if (colorFull) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")

    ) {

        Box {

            Image(
                painter = rememberAsyncImagePainter(uiList[i].imageUrl),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large
                    )
                    .fillMaxHeight()
                    .width(70.dp)
            )

            if (uiList[i].requestedNumber > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 6.dp)
                        .background(
                            shape = RoundedCornerShape(24.dp),
                            color = warningColor
                        )
                        .size(24.dp)
                ) {
                    Text(
                        text = uiList[i].requestedNumber.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = if (text1 == "") uiList[i].KBarCode else text1,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = if (text2 == "") uiList[i].name else text2,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Right,
                )
            }

            OutlinedButton(
                enabled = !uiList[i].isPrinted,
                onClick = onClick,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)

            ) {
                Text(
                    text = text3 + "\n" + text4,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item6(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    text1: String,
    customColor: Boolean = false,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    text7: String = "",
    text8: String = "",
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 12.dp)
            .shadow(elevation = 3.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(130.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {


        Row(
            modifier = Modifier.height(76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Row(
                modifier = Modifier
                    .weight(3F)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.5F)
                        .fillMaxHeight()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text1,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text3,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text5,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text2,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                    )

                    Text(
                        text = text4,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .background(
                                color = if (customColor) doneColorLighterShade else Color.White,
                                shape = RectangleShape
                            ),
                    )

                    Text(
                        text = text6,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .height(28.dp),
        ) {
            Text(
                text = text7,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .height(28.dp),
        ) {
            Text(
                text = text8,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item7(
    clickable: Boolean = false,
    enableBottomSpace: Boolean = false,
    text1: String,
    text2: String,
    text3: String,
    text4: String,
    text5: String,
    text6: String,
    text7: String = "",
    onClick: () -> Unit = {},
) {

    val bottomPadding = if (enableBottomSpace) 128.dp else 0.dp

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(104.dp)
            .testTag("items")
            .combinedClickable(
                enabled = clickable,
                onClick = { onClick() })
    ) {


        Row(
            modifier = Modifier.height(76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Row(
                modifier = Modifier
                    .weight(3F)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text1,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text3,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )

                    Text(
                        text = text5,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight()
                        .padding(top = 8.dp)
                        .wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(
                        text = text2,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxWidth(),
                    )

                    Text(
                        text = text4,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )

                    Text(
                        text = text6,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .weight(1F),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .height(28.dp),
        ) {
            Text(
                text = text7,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
fun FilterDropDownList(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    text: @Composable () -> Unit,
    values: List<String>,
    onClick: (item: String) -> Unit,
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            //.shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.small
            )
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .testTag("FilterDropDownList")
                .fillMaxHeight(),
        ) {

            icon()
            text()
            Icon(
                painter = painterResource(
                    id = if (expanded) {
                        R.drawable.ic_baseline_arrow_drop_up_24
                    } else {
                        R.drawable.ic_baseline_arrow_drop_down_24
                    }
                ),
                "",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 0.dp, end = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                //.background(color = Color.White, shape = Shapes.small)
                .align(Alignment.Center)
        ) {
            values.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(it)
                }, text = { Text(text = it, color = MaterialTheme.colorScheme.onPrimaryContainer) })
            }
        }
    }
}


@Composable
fun FilterDropDownListWithSearch(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    text: @Composable () -> Unit,
    values: List<String>,
    onClick: (item: String) -> Unit,
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    var filterValue by rememberSaveable {
        mutableStateOf("")
    }

    var filteredValues by remember {
        mutableStateOf(values)
    }

    Box(
        modifier = modifier
            //.shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.small
            )
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    filteredValues = values
                    expanded = true
                }
                .testTag("FilterDropDownList")
                .fillMaxHeight(),
        ) {

            icon()
            text()
            Icon(
                painter = painterResource(
                    id = if (expanded) {
                        R.drawable.ic_baseline_arrow_drop_up_24
                    } else {
                        R.drawable.ic_baseline_arrow_drop_down_24
                    }
                ),
                "",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 0.dp, end = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                filterValue = ""
            },
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                //.background(color = Color.White, shape = Shapes.small)
                .align(Alignment.Center)
        ) {
            SimpleTextField(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                hint = "جستجو",
                onValueChange = { filter ->
                    filterValue = filter
                    filteredValues = if (filter == "") {
                        values
                    } else {
                        values.filter { value ->
                            value.contains(filter)
                        }
                    }
                },
                value = filterValue
            )

            filteredValues.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    filterValue = ""
                    onClick(it)
                }, text = { Text(text = it, color = MaterialTheme.colorScheme.onPrimaryContainer) })
            }
        }
    }
}

@Composable
fun ScanFilterDropDownList(
    modifier: Modifier,
    filterValue: String,
    onFilterValueChange: (filterValue: String) -> Unit,
) {

    val scanValues = listOf("اضافی", "کسری")

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .testTag("checkInFilterDropDownList")
                .clickable { expanded = true }) {
            Text(text = filterValue)
            Icon(imageVector = Icons.Filled.ArrowDropDown, "")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentWidth()
        ) {

            scanValues.forEach {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onFilterValueChange(it)
                    },
                    text = { Text(text = it, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                )
            }
        }
    }
}

@Composable
fun AppBarWithBack(
    onBackPressed: () -> Unit = {},
    title: String,
) {
    AppBarWithNavigationButton(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        title = title,
        onNavigationButtonPressed = { onBackPressed() },
        testTag = "back"
    )
}

@Composable
fun AppBarWithNavigationButton(
    imageVector: ImageVector,
    title: String,
    onNavigationButtonPressed: () -> Unit = {},
    testTag: String = "",
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("navigation"),
                onClick = { onNavigationButtonPressed() }) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "",
                    Modifier.testTag(
                        if (testTag != "") testTag else ""
                    )
                )
            }
        },

        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(end = 50.dp)
                    .fillMaxSize()
                    .wrapContentSize(),
                textAlign = TextAlign.Right,
            )
        }
    )
}

@Composable
fun AppBarWithDeleteButton(
    onBackPressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    title: String,
) {

    TopAppBar(

        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    Modifier.testTag("backButton")
                )
            }
        },

        actions = {

            IconButton(
                modifier = Modifier.testTag("deleteButton"),
                onClick = {
                    onDeletePressed()
                }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
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
        }
    )
}


@Composable
fun AppBarWithFileButton(
    onBackPressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    title: String,
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

        actions = {

            IconButton(
                modifier = Modifier.testTag("deleteButton"),
                onClick = {
                    onDeletePressed()
                }) {
                Icon(
                    imageVector = Icons.Filled.Share,
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
        }
    )
}

@Composable
fun SimpleTextField(
    modifier: Modifier,
    hint: String,
    onValueChange: (it: String) -> Unit,
    value: String,
    isError: Boolean = false,
    onDone: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        //textStyle = MaterialTheme.typography.bodyMedium,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        modifier = modifier
            .testTag("TextField")
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            onDone()
        }),
        label = { Text(text = hint) },
        isError = isError,
        visualTransformation = visualTransformation,
        singleLine = true,

        )
}

@Composable
fun LoadingCircularProgressIndicator(
    isScanning: Boolean = false,
    isDataLoading: Boolean = false,
) {

    if (isScanning || isDataLoading) {
        Row(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

            if (isScanning) {
                Text(
                    text = "در حال اسکن",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Text(
                    text = "در حال بارگذاری",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun NotFound(text: String) {
    Box(
        modifier = Modifier
            .padding(top = 100.dp)

    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color.White, shape = Shapes.medium)
                    .size(256.dp)
                    .align(Alignment.CenterHorizontally)

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_not_found),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                )
            }

            Text(
                text,
                style = Typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 4.dp, end = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PowerSlider(enable: Boolean, rfPower: Int, onClick: (it: Int) -> Unit) {

    var slideValue by rememberSaveable { mutableFloatStateOf(rfPower.toFloat()) }

    if (enable) {
        Row {

            Text(
                text = "قدرت آنتن (" + slideValue.toInt() + ")  ",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.Center
            )

            Slider(
                value = slideValue,
                onValueChange = {
                    slideValue = it
                    onClick(it.toInt())
                },
                enabled = true,
                valueRange = 5f..30f,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }
}

@Composable
fun ScanTypeDropDownList(
    modifier: Modifier,
    scanTypeValue: String,
    onClick: (it: String) -> Unit,
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    val scanTypeValues = mutableListOf("RFID", "بارکد")

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .testTag("scanTypeDropDownList")) {
            Text(text = scanTypeValue)
            Icon(
                painter = if (!expanded) {
                    painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24)
                } else {
                    painterResource(id = R.drawable.ic_baseline_arrow_drop_up_24)
                }, ""
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.wrapContentWidth()
        ) {

            scanTypeValues.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(it)
                }, text = { Text(text = it) })
            }
        }
    }
}

@Composable
fun ScanBox(item: String, modifier: Modifier) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(color = Color.Transparent, shape = Shapes.medium)
                    .fillMaxWidth()
                    .weight(1f)
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
                item,
                style = Typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 4.dp,
                        end = 4.dp
                    )
                    .weight(0.5f)
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun ScanOrTypeNumberPage(
    loading: Boolean,
    onClick: () -> Unit,
    value: String,
    onValueChange: (it: String) -> Unit,
    item: String,
    popupHost: NotificationPopupHost = NotificationPopupHost(),
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
                            style = Typography.headlineSmall,
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

@Composable
fun AlertDialogWith2Button(
    title: String,
    btnConfirm: String,
    btnNotConfirm: String,
    btnNotConfirmOnClick: () -> Unit,
    btnConfirmOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                Row(horizontalArrangement = Arrangement.SpaceAround) {

                    OutlinedButton(
                        onClick = {
                            btnNotConfirmOnClick()
                        }, modifier = Modifier
                            .padding(top = 12.dp, end = 16.dp)
                            .testTag("notConfirm")
                    )
                    {
                        Text(
                            text = btnNotConfirm,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Button(
                        onClick = {
                            btnConfirmOnClick()
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag("confirm")
                    ) {
                        Text(
                            text = btnConfirm,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AlertDialogWith2ButtonAndAppVersion(
    title: String,
    appVersion: String,
    btnConfirm: String,
    btnNotConfirm: String,
    btnNotConfirmOnClick: () -> Unit,
    btnConfirmOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),

        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(
                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = "ورژن برنامه: $appVersion",
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontSize = 18.sp
                )

                Row(horizontalArrangement = Arrangement.SpaceAround) {

                    OutlinedButton(
                        onClick = {
                            btnNotConfirmOnClick()
                        }, modifier = Modifier
                            .padding(top = 12.dp, end = 16.dp)
                            .testTag("notConfirm")
                    )
                    {
                        Text(
                            text = btnNotConfirm,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = {
                            btnConfirmOnClick()
                        },
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .testTag("confirm")
                    ) {
                        Text(
                            text = btnConfirm,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButton(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {

            Column(

                verticalArrangement = Arrangement.SpaceAround
            ) {

                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 10.dp),
                    fontSize = 18.sp
                )

                Button(
                    onClick = { btnOnClick() },
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertBtn")
                ) {
                    Text(
                        text = btnTxt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButton1InputText(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    defaultText: String,
    onDismiss: () -> Unit,
    onValueChange: (it: String) -> Unit,
) {
    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        content = {
            Column {
                Text(
                    text = title, modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                )
                OutlinedTextField(
                    value = defaultText, onValueChange = {
                        onValueChange(it)
                    },
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertDialogInput")
                )

                Button(
                    modifier = Modifier
                        .padding(
                            bottom = 12.dp,
                            top = 12.dp,
                            start = 12.dp,
                            end = 12.dp
                        )
                        .align(Alignment.CenterHorizontally)
                        .testTag("alertBtn"),
                    onClick = {
                        btnOnClick()
                    }) {
                    Text(
                        text = btnTxt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        onDismissRequest = {
            onDismiss()
        }
    )
}

@Composable
fun AlertDialogWithHeadlineMediumButtonDropDownList(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    dropDownText: String,
    onDismiss: () -> Unit,
    dropDownRes: MutableList<String>,
    onSelectItem: (item: String) -> Unit,
) {

    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column {

                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row {
                    var text = dropDownText
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text = it
                            onSelectItem(it)
                        },
                        values = dropDownRes
                    )

                    Button(
                        onClick = {
                            btnOnClick()
                        }, modifier = Modifier
                            .padding(top = 24.dp)
                            .align(Alignment.CenterVertically)
                            .testTag("alertBtn")
                    ) {
                        Text(
                            text = btnTxt,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun AlertDialogWith2ButtonDropDownList(
    title: String,
    btnTxt: String,
    btnOnClick: () -> Unit,
    dropDownText: String,
    onDismiss: () -> Unit,
    dropDownRes: List<String>,
    onSelectItem: (item: String) -> Unit,
    dropDown2Text: String,
    dropDown2Res: List<String>,
    onSelectItem2: (item: String) -> Unit,
) {

    BasicAlertDialog(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onDismissRequest = {
            onDismiss()
        },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row {
                    var text = dropDownText
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text = it
                            onSelectItem(it)
                        },
                        values = dropDownRes
                    )

                    var text2 = dropDown2Text
                    FilterDropDownList(
                        modifier = Modifier
                            .padding(top = 24.dp, end = 16.dp),
                        icon = {},
                        text = {
                            Text(
                                text = text2,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        },
                        onClick = {
                            text2 = it
                            onSelectItem2(it)
                        },
                        values = dropDown2Res
                    )
                }
                Row {
                    BigButton(
                        onClick = {
                            btnOnClick()
                        },
                        modifier = Modifier.padding(top = 32.dp),
                        text = btnTxt
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun Preview() {
    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialogWith2ButtonDropDownList(
                title = "لطفا پرینتر و نوع بسته را انتخاب کنید",
                btnTxt = "تایید",
                dropDownText = "carton2",
                dropDownRes = listOf("carton2", "stock2", "example"),
                dropDown2Res = listOf("carton", "stock", "examplef"),
                dropDown2Text = "stock",
                btnOnClick = {},
                onDismiss = {},
                onSelectItem = {},
                onSelectItem2 = {}

            )
        }
    }
}