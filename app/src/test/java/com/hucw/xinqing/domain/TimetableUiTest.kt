package com.hucw.xinqing.domain

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hucw.xinqing.ui.screens.WeekTimeline
import com.hucw.xinqing.ui.theme.XinqingTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class TimetableUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `overview shows all seven days and zoom keeps time ruler pinned`() {
        val monday = LocalDate.of(2026, 11, 9)
        val days = (0..6).map { monday.plusDays(it.toLong()) to emptyList<CourseOccurrence>() }
        compose.setContent { XinqingTheme { WeekTimeline(days, 0) { _, _ -> } } }
        compose.onNodeWithText("周一").assertIsDisplayed()
        compose.onNodeWithText("周日").assertIsDisplayed()
        compose.onNodeWithText("22:00").assertIsDisplayed()
        compose.onNodeWithText("放大查看").performClick()
        val before = compose.onNodeWithText("07:00").fetchSemanticsNode().boundsInRoot.left
        compose.onNodeWithTag("week-grid").performTouchInput { swipeLeft() }
        compose.onNodeWithText("07:00").assertIsDisplayed()
        assertEquals(before, compose.onNodeWithText("07:00").fetchSemanticsNode().boundsInRoot.left)
        compose.onNodeWithText("整周总览").performClick()
        compose.onNodeWithText("周日").assertIsDisplayed()
    }
}
