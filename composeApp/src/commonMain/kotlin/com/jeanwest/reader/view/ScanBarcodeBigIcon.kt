package com.jeanwest.reader.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.ic_big_barcode_scan


@Composable
fun EmptyBarcode(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
                        color = Color.White,
                        shape = Shapes.medium
                    )
                    .size(256.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_big_barcode_scan),
                    tint = Color.Unspecified,
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.Center).clickable(onClick = onClick)
                )
            }

            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}