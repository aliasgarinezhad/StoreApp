package com.jeanwest.reader.factory.main.model

import org.jetbrains.compose.resources.DrawableResource

/**
 * Represents a feature within the application.
 *
 * @property accessKey A unique key used to identify and access the feature.
 * @property title The user-friendly title of the feature displayed in the UI.
 * @property routeScreen An identifier for the screen associated with the feature's route (implementation detail, can be a route string, a screen object, etc.)  Use `Any` as the type for flexibility, but consider a more specific type like `String` or a custom `Screen` sealed class in a real application for better type safety and clarity.
 * @property iconRes A drawable resource representing the icon for the feature, to be displayed in the UI.
 */
data class Feature(
    val accessKey: String,
    val title: String,
    val routeScreen: Any,
    val iconRes: DrawableResource,
)