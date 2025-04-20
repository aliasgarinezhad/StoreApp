package com.jeanwest.reader.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    /*primary = Jeanswest,
    primaryVariant = JeanswestStatusBar,
    background = JeanswestBackground,
    surface = JeanswestBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    secondaryVariant = Jeanswest,*/
)

private val LightColorPalette = lightColors(

    primary = Jeanswest,
    background = Background,
    surface = Background,
    onPrimary = Color.White,
    secondaryVariant = Jeanswest,
    onBackground = Color.Black,
    onSurface = Color.Black,
    secondary = BorderLight,
    error = Error,
)

/**
 *  Applies the application's custom theme, including color palette, typography, and shapes,
 *  to the composable content.  This allows consistent styling across the entire application.
 *
 *  @param darkTheme Boolean indicating whether to use the dark theme color palette.  Defaults to false (light theme).
 *                  Note:  The original code commented out `isSystemInDarkTheme()`, forcing a light theme.  This
 *                         has been preserved, but can be changed back to follow system settings if desired.
 *  @param content The composable content to which the theme will be applied.
 *
 *  Example Usage:
 *  ```kotlin
 *  MyApplicationTheme {
 *      Scaffold(topBar = { TopAppBar(title = { Text("My App") }) }) {
 *          // Main content of the application
 *          Text("Hello, world!")
 *      }
 *  }
 *  ```
 */
@Composable
fun MyApplicationTheme(
    //darkTheme: Boolean = isSystemInDarkTheme(),
    darkTheme: Boolean = false,
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = MyTypography(),
        shapes = Shapes,
        content = content
    )
}