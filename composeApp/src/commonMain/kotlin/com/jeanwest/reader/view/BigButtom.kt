package com.jeanwest.reader.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            style = MaterialTheme.typography.labelLarge
        )
    }
}