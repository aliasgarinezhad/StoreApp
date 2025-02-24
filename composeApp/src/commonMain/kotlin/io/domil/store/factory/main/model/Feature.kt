package io.domil.store.factory.main.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Feature(
    val accessKey: String,
    val title: String,
    val routeScreen: Any,
    val icon: ImageVector,
)