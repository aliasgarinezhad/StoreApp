package io.domil.store.factory.main.model

import org.jetbrains.compose.resources.DrawableResource

data class Feature(
    val accessKey: String,
    val title: String,
    val routeScreen: Any,
    val iconRes: DrawableResource,
)