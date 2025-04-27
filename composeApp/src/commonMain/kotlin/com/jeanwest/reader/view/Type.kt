package com.jeanwest.reader.view

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.sans_bold
import storeapp.composeapp.generated.resources.sans_regular

@Composable
fun bodyFontFamily() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.sans_regular))
@Composable
fun displayFontFamily() = FontFamily(org.jetbrains.compose.resources.Font(Res.font.sans_bold))
// Default material3 3 typography values
val baseline = Typography()

@Composable
fun MyTypography() = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily()),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = displayFontFamily(),
        color = primaryLight
    ),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily()),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily()),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily()),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily()),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily()),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily()),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily()),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily()),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily() /*color = primaryLight*/),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily()),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily(), color = onPrimaryLight),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily(), color = primaryLight),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily()),
)

