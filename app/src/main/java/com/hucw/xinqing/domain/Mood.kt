package com.hucw.xinqing.domain

enum class Mood(
    val score: Int,
    val emoji: String,
    val label: String,
    val shortLabel: String
) {
    LOW(1, "🌧️", "有点低落", "低落"),
    TIRED(2, "☁️", "有些疲惫", "疲惫"),
    CALM(3, "🌤️", "平平淡淡", "平静"),
    GOOD(4, "☀️", "心情不错", "不错"),
    GREAT(5, "🌈", "超级开心", "开心");

    companion object {
        val default: Mood = CALM

        fun fromScore(score: Int): Mood =
            Mood.entries.firstOrNull { it.score == score } ?: default
    }
}
