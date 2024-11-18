package io.domil.store.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import storeapp.composeapp.generated.resources.Res
import storeapp.composeapp.generated.resources.sans_regular

// Set of Material typography styles to start with

@Composable
fun MyTypography() = Typography(
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Color(0xFF272727),
    ),
    h1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    h2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Jeanswest,
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Color(0xFF707070),
    ),

    /* Other default text styles to override*/
    button = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    defaultFontFamily = FontFamily(Font(Res.font.sans_regular)),

    )