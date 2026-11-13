package com.hucw.xinqing.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object WeekDates {
    fun mondayOf(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value - 1).toLong())

    fun weekNumber(date: LocalDate, semesterMonday: LocalDate): Int =
        (ChronoUnit.DAYS.between(semesterMonday, date) / 7).toInt() + 1

    fun dayLabel(weekday: Int): String = when (weekday) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }
}
