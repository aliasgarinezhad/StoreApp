package com.jeanwest.reader.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OpenActivityButton(title: String, icon: Painter, onClick: () -> Unit) {

    val iconSize = 88.dp
    val textSize = 96.dp

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onClick()
            }
    ) {

        Icon(
            painter = icon, contentDescription = "", modifier = Modifier
                .size(iconSize)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.large
                )
                .padding(4.dp), tint = MaterialTheme.colors.onPrimary
        )
        Text(
            title,
            modifier = Modifier
                .width(textSize)
                .padding(top = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
    }
}