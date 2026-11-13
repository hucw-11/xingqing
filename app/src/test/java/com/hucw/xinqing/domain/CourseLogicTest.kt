package com.hucw.xinqing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CourseLogicTest {
    private val semester = LocalDate.of(2026, 2, 23) // Monday

    private fun course(
        id: Long = 1,
        weekday: Int = 1,
        startMinute: Int = 8 * 60,
        endMinute: Int = 9 * 60 + 30,
        startWeek: Int = 1,
        weekCount: Int = 4,
        parity: WeekParity = WeekParity.ALL,
        reminderMinutes: Int = 15
    ) = Course(
        id = id,
        name = "测试课",
        weekday = weekday,
        startMinute = startMinute,
        endMinute = endMinute,
        startWeek = startWeek,
        weekCount = weekCount,
        weekParity = parity,
        location = "教室",
        teacher = "老师",
        color = "sage",
        reminderMinutes = reminderMinutes,
        reminderStyle = ReminderStyle.SOUND
    )

    @Test
    fun `semester monday is week one`() {
        assertEquals(1, Recurrence.weekNumber(semester, semester))
        assertEquals(2, Recurrence.weekNumber(semester.plusWeeks(1), semester))
    }

    @Test
    fun `multiple weekdays expand and respect parity and end week`() {
        val target = course(parity = WeekParity.ODD).copy(weekdays = listOf(1, 3))
        val dates = Recurrence.occurrencesBetween(target, semester, semester.plusWeeks(5), semester)
            .map { it.date }
        assertEquals(listOf(semester, semester.plusDays(2), semester.plusDays(14), semester.plusDays(16)), dates)
    }

    @Test
    fun `shared secondary weekday conflicts but disjoint days do not`() {
        val target = course().copy(weekdays = listOf(1, 3))
        assertEquals(target, Overlap.findConflict(course(id = 2, weekday = 3), listOf(target)))
        assertNull(Overlap.findConflict(course(id = 2, weekday = 2), listOf(target)))
    }

    @Test
    fun `empty or invalid weekday selection is rejected`() {
        assertTrue(CourseValidation.validate(course().copy(weekdays = emptyList())) != null)
        assertTrue(CourseValidation.validate(course().copy(weekdays = listOf(1, 8))) != null)
    }

    @Test
    fun `moving one day preserves other weekday and replans both reminders`() {
        val target = course().copy(weekdays = listOf(1, 3))
        val exceptions = listOf(
            CourseException(target.id, semester, ExceptionType.CANCELLED),
            CourseException(target.id, semester, ExceptionType.MOVED, semester.plusDays(1), 600, 660)
        )
        val planned = ReminderPlanner.plan(listOf(target), exceptions, semester,
            semester.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant(), 6, 20, ZoneId.of("Asia/Shanghai"))
        assertEquals(listOf(semester.plusDays(1), semester.plusDays(2)), planned.map { it.occurrenceDate })
        assertEquals(Instant.parse("2026-02-24T01:45:00Z"), planned.first().triggerAt)
    }

    @Test
    fun `overlapping lessons remain in separate lanes while adjacent lessons reuse lane`() {
        fun occurrence(start: Int, end: Int) = CourseOccurrence(1, "测试", semester, start, end, "", "sage")
        val a = occurrence(480, 570)
        val b = occurrence(500, 540)
        val c = occurrence(570, 600)
        assertEquals(listOf(listOf(a, c), listOf(b)), TimetableLayout.lanes(listOf(c, b, a)))
        assertEquals(listOf(emptyList<CourseOccurrence>()), TimetableLayout.lanes(emptyList()))
    }

    @Test
    fun `odd parity only matches odd weeks`() {
        val odd = course(parity = WeekParity.ODD)
        assertTrue(Recurrence.occursOn(odd, semester, semester))
        assertFalse(Recurrence.occursOn(odd, semester.plusWeeks(1), semester))
        assertTrue(Recurrence.occursOn(odd, semester.plusWeeks(2), semester))
        assertFalse(Recurrence.occursOn(odd, semester.plusWeeks(3), semester))
    }

    @Test
    fun `even parity only matches even weeks`() {
        val even = course(parity = WeekParity.EVEN)
        assertFalse(Recurrence.occursOn(even, semester, semester))
        assertTrue(Recurrence.occursOn(even, semester.plusWeeks(1), semester))
    }

    @Test
    fun `course starts at configured week`() {
        val late = course(startWeek = 3, weekCount = 2)
        assertFalse(Recurrence.occursOn(late, semester, semester))
        assertTrue(Recurrence.occursOn(late, semester.plusWeeks(2), semester))
        assertTrue(Recurrence.occursOn(late, semester.plusWeeks(3), semester))
        assertFalse(Recurrence.occursOn(late, semester.plusWeeks(4), semester))
    }

    @Test
    fun `cancelled occurrence is removed and moved occurrence appears`() {
        val target = course()
        val secondWeek = semester.plusWeeks(1)
        val exceptions = listOf(
            CourseException(target.id, semester, ExceptionType.CANCELLED),
            CourseException(target.id, secondWeek, ExceptionType.CANCELLED),
            CourseException(
                target.id,
                secondWeek,
                ExceptionType.MOVED,
                newDate = secondWeek.plusDays(1)
            )
        )
        assertTrue(Recurrence.occurrencesOn(target, semester, semester, exceptions).isEmpty())
        assertTrue(Recurrence.occurrencesOn(target, secondWeek, semester, exceptions).isEmpty())
        val moved = Recurrence.occurrencesOn(
            target,
            secondWeek.plusDays(1),
            semester,
            exceptions
        )
        assertEquals(1, moved.size)
    }

    @Test
    fun `adjacent courses do not conflict`() {
        val first = course(id = 1, endMinute = 9 * 60)
        val second = course(id = 2, startMinute = 9 * 60, endMinute = 10 * 60)
        assertNull(Overlap.findConflict(second, listOf(first)))
    }

    @Test
    fun `overlapping courses conflict`() {
        val first = course(id = 1)
        val second = course(id = 2, startMinute = 9 * 60, endMinute = 10 * 60)
        assertEquals(first, Overlap.findConflict(second, listOf(first)))
    }

    @Test
    fun `complementary parity does not conflict`() {
        val odd = course(id = 1, parity = WeekParity.ODD)
        val even = course(id = 2, parity = WeekParity.EVEN)
        assertNull(Overlap.findConflict(even, listOf(odd)))
    }

    @Test
    fun `validation rejects bad times and weeks`() {
        assertNull(CourseValidation.validate(course()))
        assertTrue(CourseValidation.validate(course(endMinute = 7 * 60)) != null)
        assertTrue(CourseValidation.validate(course(weekCount = 0)) != null)
        assertTrue(CourseValidation.validate(course(startMinute = -1)) != null)
    }

    @Test
    fun `planner produces reminder before class start`() {
        val zone = ZoneId.of("Asia/Shanghai")
        val from = LocalDateTime.of(2026, 2, 23, 0, 0).atZone(zone).toInstant()
        val planned = ReminderPlanner.plan(
            courses = listOf(course()),
            exceptions = emptyList(),
            semesterMonday = semester,
            from = from,
            windowDays = 6,
            maxCount = 10,
            zone = zone
        )
        assertEquals(1, planned.size)
        assertEquals(Instant.parse("2026-02-22T23:45:00Z"), planned.first().triggerAt)
    }
}
