package com.hucw.xinqing.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormat {
    fun formatMinute(minute: Int): String =
        String.format(Locale.getDefault(), "%02d:%02d", minute / 60, minute % 60)

    fun parseMinute(text: String): Int? {
        val parts = text.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINA))

    fun shortDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA))
}
