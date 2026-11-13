package com.hucw.xinqing.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoodTest {
    @Test
    fun `scores are unique and ordered`() {
        val scores = Mood.entries.map { it.score }
        assertEquals(listOf(1, 2, 3, 4, 5), scores)
        assertEquals(scores.distinct().size, scores.size)
    }

    @Test
    fun `every mood has a label and an emoji`() {
        assertTrue(Mood.entries.all { it.label.isNotBlank() && it.emoji.isNotBlank() })
    }

    @Test
    fun `fromScore falls back to the default mood`() {
        assertEquals(Mood.CALM, Mood.fromScore(3))
        assertEquals(Mood.CALM, Mood.fromScore(0))
        assertEquals(Mood.CALM, Mood.fromScore(9))
        assertEquals(Mood.GREAT, Mood.fromScore(5))
    }
}
