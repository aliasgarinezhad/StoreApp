package io.domil.store.factory.model

import androidx.compose.ui.graphics.painter.Painter

data class Feature(
    val accessKey: String,
    val title: String,
    val routeScreen: Any,
    val icon: Painter,
)