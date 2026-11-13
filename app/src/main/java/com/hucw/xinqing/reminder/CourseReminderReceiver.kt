package com.hucw.xinqing.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hucw.xinqing.XinqingApp
import com.hucw.xinqing.domain.Recurrence
import com.hucw.xinqing.util.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, 0L)
        val dateText = intent.getStringExtra(EXTRA_DATE) ?: return
        val startMinute = intent.getIntExtra(EXTRA_START_MINUTE, -1)
        if (courseId == 0L || startMinute < 0) return

        val pending = goAsync()
        val app = context.applicationContext as XinqingApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val course = app.container.courseRepository.findById(courseId) ?: return@launch
                val semester = app.container.settings.semester.value ?: return@launch
                val date = runCatching { LocalDate.parse(dateText) }.getOrNull() ?: return@launch
                val occurrences = Recurrence.occurrencesOn(
                    course,
                    date,
                    semester,
                    app.container.courseRepository.exceptions()
                )
                val occurrence = occurrences.firstOrNull { it.startMinute == startMinute }
                    ?: return@launch
                val title = "该准备上课啦 · ${course.name}"
                val text = buildString {
                    append(TimeFormat.formatMinute(occurrence.startMinute))
                    append("–")
                    append(TimeFormat.formatMinute(occurrence.endMinute))
                    if (occurrence.location.isNotBlank()) {
                        append(" · ").append(occurrence.location)
                    }
                    append(" · 提前 ").append(course.reminderMinutes).append(" 分钟")
                }
                Notifications.show(
                    context,
                    courseId.toInt(),
                    title,
                    text,
                    course.reminderStyle,
                    destination = "timetable"
                )
                app.container.alarmScheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_COURSE_ID = "course_id"
        const val EXTRA_DATE = "occurrence_date"
        const val EXTRA_START_MINUTE = "start_minute"
    }
}
