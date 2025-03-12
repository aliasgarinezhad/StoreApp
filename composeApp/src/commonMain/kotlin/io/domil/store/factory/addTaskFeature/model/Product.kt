package io.domil.store.factory.addTaskFeature.model

import androidx.compose.ui.graphics.Color

data class Product(
    val lineID: Long = 0L,
    val name: String = "تی شرت",
    val style: String = "41531052",
    val color: String = "2010",
    val sizes: Map<String, Int> = mapOf("Small" to 0, "Medium" to 1, "Large" to 2, "XLarger" to 3),
    val colorHex: String = "",
    val tasks: Map<String, Long> = mapOf("یقه" to 0L, "آستین" to 1L, "دکمه" to 2L),
    val part: String = "2",
) {
    val uiColor: Color
        get() {
            if (colorHex.length == 7) {
                val redCode = colorHex.substring(1, 3).toInt(16)
                val greenCode = colorHex.substring(3, 5).toInt(16)
                val blueCode = colorHex.substring(5, 7).toInt(16)
                return Color(redCode, greenCode, blueCode, )
            } else {
                return Color.White
            }
        }
}
