package io.domil.store

import android.util.Size
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerWithCamera(
    enable: Boolean,
    context: ComponentActivity,
    onScanSuccess: (barcodes: String) -> Unit
) {

    if (enable) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val previewView = PreviewView(context)
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128
            )
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

            if (enable) {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {

                    val image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    val scanner = BarcodeScanning.getClient(options)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                onScanSuccess(barcodes[0].displayValue.toString())
                            } else {
                                imageProxy.close()
                            }
                        }
                        .addOnFailureListener {
                            imageProxy.close()
                        }
                }
            } else {
                imageProxy.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            context,
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
            imageAnalysis,
            preview
        )

        Box(
            modifier = Modifier
                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
                .fillMaxSize()
        ) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        }
    }
}