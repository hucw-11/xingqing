package com.hucw.xinqing.domain

/** Overlapping lessons get separate lanes, so every lesson remains tappable. */
object TimetableLayout {
    fun lanes(occurrences: List<CourseOccurrence>): List<List<CourseOccurrence>> {
        val lanes = mutableListOf<MutableList<CourseOccurrence>>()
        occurrences.sortedBy { it.startMinute }.forEach { occurrence ->
            val lane = lanes.firstOrNull { it.last().endMinute <= occurrence.startMinute }
                ?: mutableListOf<CourseOccurrence>().also { lanes.add(it) }
            lane.add(occurrence)
        }
        return lanes.ifEmpty { listOf(emptyList()) }
    }
}
