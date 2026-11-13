package com.hucw.xinqing.domain

import java.time.LocalDate

object Recurrence {
    fun weekNumber(date: LocalDate, semesterMonday: LocalDate): Int =
        WeekDates.weekNumber(date, semesterMonday)

    fun parityMatches(parity: WeekParity, week: Int): Boolean = when (parity) {
        WeekParity.ALL -> true
        WeekParity.ODD -> week % 2 == 1
        WeekParity.EVEN -> week % 2 == 0
    }

    fun occursOn(course: Course, date: LocalDate, semesterMonday: LocalDate): Boolean {
        if (date.dayOfWeek.value !in course.weekdays) return false
        val week = weekNumber(date, semesterMonday)
        return week in course.startWeek..course.endWeek &&
            parityMatches(course.weekParity, week)
    }

    fun occurrencesOn(
        course: Course,
        date: LocalDate,
        semesterMonday: LocalDate,
        exceptions: List<CourseException> = emptyList()
    ): List<CourseOccurrence> {
        val mine = exceptions.filter { it.courseId == course.id }
        val cancelled = mine.any {
            it.type == ExceptionType.CANCELLED && it.originalDate == date
        }
        val result = mutableListOf<CourseOccurrence>()
        if (!cancelled && occursOn(course, date, semesterMonday)) {
            result += occurrence(course, date, course.startMinute, course.endMinute)
        }
        mine.filter { it.type == ExceptionType.MOVED && it.newDate == date }
            .forEach { moved ->
                result += occurrence(
                    course,
                    date,
                    moved.newStartMinute ?: course.startMinute,
                    moved.newEndMinute ?: course.endMinute
                )
            }
        return result
    }

    fun occurrencesBetween(
        course: Course,
        from: LocalDate,
        to: LocalDate,
        semesterMonday: LocalDate,
        exceptions: List<CourseException> = emptyList()
    ): List<CourseOccurrence> {
        if (to.isBefore(from)) return emptyList()
        val result = mutableListOf<CourseOccurrence>()
        var date = from
        while (!date.isAfter(to)) {
            result += occurrencesOn(course, date, semesterMonday, exceptions)
            date = date.plusDays(1)
        }
        return result
    }

    private fun occurrence(
        course: Course,
        date: LocalDate,
        startMinute: Int,
        endMinute: Int
    ): CourseOccurrence = CourseOccurrence(
        courseId = course.id,
        name = course.name,
        date = date,
        startMinute = startMinute,
        endMinute = endMinute,
        location = course.location,
        color = course.color
    )
}
