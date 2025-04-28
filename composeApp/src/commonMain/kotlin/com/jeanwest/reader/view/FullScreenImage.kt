package com.jeanwest.reader.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlin.math.max
import kotlin.math.min

@Composable
fun FullScreenImage(
    url: String,
    onImageClick: () -> Unit,
    imageLists: List<String>,
    onListImageClick: (image: String) -> Unit
) {

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationState by remember { mutableFloatStateOf(0f) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                containerSize = layoutCoordinates.size // Get the size of the container
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    val newScale = scale * zoom
                    scale = max(1f, min(newScale, 4f))
                    rotationState += rotation

                    // Calculate the maximum offset for each direction
                    val maxX = (imageSize.width * scale - containerSize.width) / 2f
                    val maxY = (imageSize.height * scale - containerSize.height) / 2f

                    // Apply panning with constraints
                    offsetX = max(-maxX, min(maxX, offsetX + pan.x))
                    offsetY = max(-maxY, min(maxY, offsetY + pan.y))
                }
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = url),
            contentDescription = "",
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    imageSize = layoutCoordinates.size // Get the size of the image
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                    rotationZ = rotationState
                )
                .fillMaxSize()
                .clickable {
                    onImageClick()
                },  // Initial size of the image
            contentScale = ContentScale.Crop
        )
        ZoomableImageGrid(images = imageLists, onImageClick = onListImageClick)
    }
}

@Composable
fun ZoomableImageGrid(images: List<String>, onImageClick: (image: String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxHeight()
            .width(65.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images.size) { index ->
            Image(
                painter = rememberAsyncImagePainter(images[index]),
                contentDescription = images[index],
                modifier = Modifier
                    .size(60.dp)
                    .clickable {
                        onImageClick(images[index])
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

