package io.domil.store

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import qrscanner.QrScanner

@Composable
fun QrScanner(onScanSuccess: (barcode: String) -> Unit) {
    QrScanner(
        modifier = Modifier.fillMaxSize(),
        flashlightOn = false,
        openImagePicker = false,
        onCompletion = {
            onScanSuccess(it)
        },
        onFailure = {},
        imagePickerHandler = {}
    )
}