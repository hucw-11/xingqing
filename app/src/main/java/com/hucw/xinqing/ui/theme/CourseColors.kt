package com.hucw.xinqing.ui.theme

import androidx.compose.ui.graphics.Color

fun courseColor(name: String): Color = when (name) {
    "lilac" -> Color(0xFFB58AA5)
    "peach" -> Color(0xFFE8A87C)
    "blue" -> Color(0xFF7D9DB8)
    "rose" -> Color(0xFFC98C8C)
    else -> Color(0xFF5F8A72)
}

val courseColorOptions = listOf(
    "sage" to "鼠尾草绿",
    "lilac" to "淡淡丁香",
    "peach" to "暖暖杏桃",
    "blue" to "晴空蓝",
    "rose" to "浅玫瑰"
)
