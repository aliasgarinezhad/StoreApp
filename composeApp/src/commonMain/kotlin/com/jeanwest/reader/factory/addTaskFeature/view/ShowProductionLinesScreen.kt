package com.jeanwest.reader.factory.addTaskFeature.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanwest.reader.factory.addTaskFeature.model.Product
import com.jeanwest.reader.view.MyApplicationTheme
import com.jeanwest.reader.view.Shapes
import com.jeanwest.reader.view.ErrorSnackBar
import com.jeanwest.reader.view.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
object ShowProductionLinesScreen

/**
 * Displays a screen showing a list of production lines (products).
 *
 * @param loading Indicates whether the data is currently being loaded. If true, a loading indicator is displayed.
 * @param products The list of [Product] objects representing the production lines.
 * @param onClick Callback function triggered when a product item in the list is clicked.  It receives the clicked [Product] as a parameter.
 * @param state The [SnackbarHostState] used to manage and display snackbar messages.
 * @param pageTitle The title to be displayed in the top app bar.
 * @param onBack Callback function triggered when the back button in the app bar is pressed.
 */
@Composable
fun ShowProductionLinesScreen(
    loading: Boolean,
    products: List<Product>,
    onClick: (product: Product) -> Unit,
    state: SnackbarHostState,
    pageTitle: String,
    onBack: () -> Unit
) {

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(

                topBar = {
                    AppBarWithBack(title = pageTitle, onBackPressed = onBack)
                },
                content = {
                    Content(
                        loading = loading,
                        products = products,
                        onClick = onClick
                    )
                },
                snackbarHost = { ErrorSnackBar(state) },
            )
        }
    }
}

@Composable
private fun Content(
    loading: Boolean,
    products: List<Product>,
    onClick: (product: Product) -> Unit
) {

    if (loading) {
        LoadingIndicator()
    } else {
        LazyColumn {
            item(products.size) {
                products.forEachIndexed { index, productLine ->
                    ProductLineItem(
                        product = productLine,
                        enableBottomPadding = index == products.size - 1,
                        onClick = onClick,
                        enableTopPadding = index == 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductLineItem(
    product: Product,
    enableBottomPadding: Boolean = false,
    enableTopPadding: Boolean = false,
    onClick: (product: Product) -> Unit = {},
) {

    val topPadding = if (enableTopPadding) 16.dp else 12.dp
    val bottomPadding = if (enableBottomPadding) 128.dp else 0.dp

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
                color = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .clickable {
                onClick(product)
            }
    ) {

        Box(
            modifier = Modifier.padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                .background(
                    color = product.uiColor,
                    shape = Shapes.large
                )
                .border(
                    BorderStroke(2.dp, color = MaterialTheme.colorScheme.onPrimary),
                    shape = Shapes.large
                )
                .fillMaxHeight()
                .width(70.dp)
        )

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .weight(1.5F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Row {

                    Text(
                        text = product.style.substring(product.style.length - 3, product.style.length) + "-",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Right,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = product.style.substring(0 , product.style.length - 3),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Right,
                    )
                }

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    maxLines = 1
                )
            }

            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "رنگ: " + product.color,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp)
                )
                Text(
                    text = "پارت: " + product.part,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}