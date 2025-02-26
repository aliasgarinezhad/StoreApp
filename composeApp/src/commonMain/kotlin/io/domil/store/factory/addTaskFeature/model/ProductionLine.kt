package io.domil.store.factory.addTaskFeature.model

import androidx.compose.ui.graphics.Color

data class ProductionLine(
    val name: String = "تی شرت",
    val style: String = "41531052",
    val color: String = "2010",
    val sizes: List<String> = listOf(),
    val uiColor: Color = Color.Blue,
    val tasks: List<String> = listOf("یقه", "آستین", "دکمه"),
    val running: Boolean = true,
    val line: String = "2",
)
