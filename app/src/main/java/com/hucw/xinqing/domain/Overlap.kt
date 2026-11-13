package com.hucw.xinqing.domain

object Overlap {
    fun findConflict(candidate: Course, existing: List<Course>): Course? =
        existing.firstOrNull { other ->
            other.id != candidate.id &&
                other.weekdays.any { it in candidate.weekdays } &&
                candidate.startMinute < other.endMinute &&
                other.startMinute < candidate.endMinute &&
                weeksConflict(candidate, other)
        }

    private fun weeksConflict(a: Course, b: Course): Boolean {
        val start = maxOf(a.startWeek, b.startWeek)
        val end = minOf(a.endWeek, b.endWeek)
        if (start > end) return false
        return (start..end).any { week ->
            Recurrence.parityMatches(a.weekParity, week) &&
                Recurrence.parityMatches(b.weekParity, week)
        }
    }
}
