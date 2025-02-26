package io.domil.store.factory.addTaskFeature.view

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
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
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
import io.domil.store.factory.addTaskFeature.model.ProductionLine
import io.domil.store.theme.BorderLight
import io.domil.store.theme.Jeanswest
import io.domil.store.theme.MyApplicationTheme
import io.domil.store.theme.Shapes
import io.domil.store.theme.innerBackground
import io.domil.store.view.ErrorSnackBar
import io.domil.store.view.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
object ShowProductionLinesList

@Composable
fun ShowProductionLinesList(
    loading: Boolean,
    productionLines: List<ProductionLine>,
    onClick: (productionLine: ProductionLine) -> Unit,
    state: SnackbarHostState
) {

    MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                content = {
                    Content(
                        loading = loading,
                        productionLines = productionLines,
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
    productionLines: List<ProductionLine>,
    onClick: (productionLine: ProductionLine) -> Unit
) {

    if (loading) {
        LoadingIndicator()
    } else {
        LazyColumn {
            item(productionLines.size) {
                productionLines.forEachIndexed { index, productLine ->
                    ProductLineItem(
                        productionLine = productLine,
                        enableBottomPadding = index == productionLines.size - 1,
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
    productionLine: ProductionLine,
    enableBottomPadding: Boolean = false,
    enableTopPadding: Boolean = false,
    onClick: (productionLine: ProductionLine) -> Unit = {},
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
                color = MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .clickable {
                onClick(productionLine)
            }
    ) {

        Box(
            modifier = Modifier.padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                .background(
                    color = productionLine.uiColor,
                    shape = Shapes.large
                )
                .border(
                    BorderStroke(2.dp, color = BorderLight),
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
                    .weight(1.2F)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    text = productionLine.style,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                    fontSize = 12.sp
                )
                Text(
                    text = productionLine.name,
                    style = MaterialTheme.typography.h4,
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
                        color = innerBackground,
                        shape = Shapes.large
                    ),

                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = productionLine.color,
                    style = MaterialTheme.typography.h3,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
                Divider(
                    color = Jeanswest,
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .width(66.dp)
                )
                Text(
                    text = productionLine.line,
                    style = MaterialTheme.typography.h3,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}