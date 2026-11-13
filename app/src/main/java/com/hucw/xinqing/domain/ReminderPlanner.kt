package com.hucw.xinqing.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PlannedReminder(
    val courseId: Long,
    val occurrenceDate: LocalDate,
    val startMinute: Int,
    val triggerAt: Instant
)

object ReminderPlanner {
    fun plan(
        courses: List<Course>,
        exceptions: List<CourseException>,
        semesterMonday: LocalDate,
        from: Instant,
        windowDays: Int = 21,
        maxCount: Int = 200,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<PlannedReminder> {
        val today = from.atZone(zone).toLocalDate()
        val until = today.plusDays(windowDays.toLong())
        val result = mutableListOf<PlannedReminder>()
        for (course in courses) {
            val occurrences = Recurrence.occurrencesBetween(
                course, today, until, semesterMonday, exceptions
            )
            for (occurrence in occurrences) {
                val trigger = occurrence.date
                    .atTime(occurrence.startMinute / 60, occurrence.startMinute % 60)
                    .minusMinutes(course.reminderMinutes.toLong())
                    .atZone(zone)
                    .toInstant()
                if (trigger.isAfter(from)) {
                    result += PlannedReminder(
                        courseId = course.id,
                        occurrenceDate = occurrence.date,
                        startMinute = occurrence.startMinute,
                        triggerAt = trigger
                    )
                }
            }
        }
        return result.sortedBy { it.triggerAt }.take(maxCount)
    }
}
