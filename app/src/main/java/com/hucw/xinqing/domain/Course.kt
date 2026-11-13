package com.hucw.xinqing.domain

import java.time.LocalDate

enum class WeekParity { ALL, ODD, EVEN }

enum class ReminderStyle { SILENT, VIBRATE, SOUND }

data class Course(
    val id: Long,
    val name: String,
    val weekday: Int,
    val startMinute: Int,
    val endMinute: Int,
    val startWeek: Int,
    val weekCount: Int,
    val weekParity: WeekParity,
    val location: String,
    val teacher: String,
    val color: String,
    val reminderMinutes: Int,
    val reminderStyle: ReminderStyle
) {
    val endWeek: Int get() = startWeek + weekCount - 1
}

enum class ExceptionType { CANCELLED, MOVED }

data class CourseException(
    val courseId: Long,
    val originalDate: LocalDate,
    val type: ExceptionType,
    val newDate: LocalDate? = null,
    val newStartMinute: Int? = null,
    val newEndMinute: Int? = null
)

data class CourseOccurrence(
    val courseId: Long,
    val name: String,
    val date: LocalDate,
    val startMinute: Int,
    val endMinute: Int,
    val location: String,
    val color: String
)
