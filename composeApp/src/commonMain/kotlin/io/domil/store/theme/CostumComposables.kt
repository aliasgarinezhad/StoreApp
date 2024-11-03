package io.domil.store.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toUri
import networking.Product
import org.jetbrains.compose.resources.painterResource
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.ic_baseline_arrow_drop_down_24
import storeapp.composeapp.generated.resources.ic_baseline_arrow_drop_up_24

@Composable
fun ErrorSnackBar(state: SnackbarHostState) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {

        SnackbarHost(hostState = state, snackbar = {
            Snackbar(
                shape = MaterialTheme.shapes.large,
                action = {
                    Text(
                        text = "باشه",
                        color = Done,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                state.currentSnackbarData?.dismiss()
                            }
                    )
                }
            ) {
                Text(
                    text = state.currentSnackbarData?.message ?: "",
                    color = Error,
                    style = MaterialTheme.typography.body1,
                )
            }
        })
    }
}

@Composable
fun Item(
    i: Int,
    uiList: List<Product>,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {

    val topPadding = if (i == 0) 16.dp else 12.dp
    val bottomPadding = if (i == uiList.size - 1) 94.dp else 0.dp

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = topPadding)
            .shadow(elevation = 5.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .fillMaxWidth()
            .height(100.dp)
            .testTag("items")
            .clickable(enabled = clickable) {
                onClick()
            },
    ) {

        Box {

            AsyncImage(
                model = uiList[i].ImgUrl,
                contentDescription = "image",
                modifier = Modifier
                    .padding(end = 4.dp, top = 12.dp, bottom = 12.dp, start = 12.dp)
                    .shadow(0.dp, shape = Shapes.large)
                    .background(
                        color = MaterialTheme.colors.onPrimary,
                        shape = Shapes.large
                    )
                    .border(
                        BorderStroke(2.dp, color = BorderLight),
                        shape = Shapes.large
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
                    text = uiList[i].KBarCode ?: "not found",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Right,
                )
                Text(
                    text = uiList[i].productName ?: "not found",
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
                    text = "فروشگاه: " + uiList[i].dbCountStore,
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
                    text = "انبار: " + uiList[i].dbCountDepo.toString(),
                    style = MaterialTheme.typography.h3,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FilterDropDownList(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    values: List<String>,
    onClick: (item: String) -> Unit
) {

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colors.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .border(
                BorderStroke(1.dp, if (expanded) Jeanswest else borderColor),
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
                    if (expanded) {
                        Res.drawable.ic_baseline_arrow_drop_up_24
                    } else {
                        Res.drawable.ic_baseline_arrow_drop_down_24
                    }
                ),
                "",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 4.dp, end = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
                .background(color = BottomBar, shape = Shapes.small)
        ) {
            values.forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onClick(it)
                }) {
                    Text(text = it)
                }
            }
        }
    }
}
/*
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun BarcodeScannerWithCamera(
    enable: Boolean,
    context: ComponentActivity,
    onClick: (barcodes: MutableList<Barcode>) -> Unit
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
                                onClick(barcodes)
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
}*/
