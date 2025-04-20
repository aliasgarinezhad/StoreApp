package com.jeanwest.reader.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays a loading indicator with a circular progress bar and a "Loading..." text.
 *
 * The indicator is centered horizontally within its parent and uses the primary color from the app's MaterialTheme.
 */
@Composable
fun LoadingIndicator() {
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
}