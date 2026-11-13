package com.hucw.xinqing.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.hucw.xinqing.R
import com.hucw.xinqing.data.CourseRepository
import com.hucw.xinqing.data.SettingsStore
import com.hucw.xinqing.data.XinqingDatabase
import com.hucw.xinqing.data.entity.ScheduledAlarmEntity
import com.hucw.xinqing.diagnostics.Diagnostics
import com.hucw.xinqing.domain.ReminderPlanner
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class AlarmScheduler(
    private val appContext: Context,
    private val database: XinqingDatabase,
    private val settings: SettingsStore,
    private val repository: CourseRepository
) {
    private val mutex = Mutex()

    suspend fun rescheduleAll() {
        mutex.withLock {
            val alarmManager =
                appContext.getSystemService(AlarmManager::class.java) ?: return@withLock
            val alarmDao = database.alarmDao()

            for (job in alarmDao.getAll()) {
                val pending = pendingIntent(job.id, courseIntent(job.id))
                alarmManager.cancel(pending)
                pending.cancel()
            }
            alarmDao.deleteAll()

            val semester = settings.semester.value ?: return@withLock
            val courses = repository.getAll()
            if (courses.isEmpty()) return@withLock
            val planned = ReminderPlanner.plan(
                courses = courses,
                exceptions = repository.exceptions(),
                semesterMonday = semester,
                from = Instant.now(),
                windowDays = 21,
                maxCount = 200,
                zone = ZoneId.systemDefault()
            )
            for (reminder in planned) {
                val id = alarmDao.insert(
                    ScheduledAlarmEntity(
                        courseId = reminder.courseId,
                        occurrenceDate = reminder.occurrenceDate.toString(),
                        triggerAtMillis = reminder.triggerAt.toEpochMilli()
                    )
                )
                val intent = courseIntent(id).apply {
                    putExtra(CourseReminderReceiver.EXTRA_COURSE_ID, reminder.courseId)
                    putExtra(CourseReminderReceiver.EXTRA_DATE, reminder.occurrenceDate.toString())
                    putExtra(CourseReminderReceiver.EXTRA_START_MINUTE, reminder.startMinute)
                }
                scheduleAlarm(
                    alarmManager,
                    reminder.triggerAt.toEpochMilli(),
                    pendingIntent(id, intent)
                )
            }
        }
    }

    private fun courseIntent(id: Long): Intent =
        Intent(appContext, CourseReminderReceiver::class.java).apply {
            data = Uri.parse("xinqing://course-reminder/$id")
        }

    private fun pendingIntent(requestCode: Long, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            requestCode.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pending: PendingIntent
    ) {
        if (canScheduleExact(appContext)) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pending
                )
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pending
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pending
            )
        }
    }

    companion object {
        const val TEST_NOTIFICATION_ID = 9001

        fun canScheduleExact(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            return alarmManager?.canScheduleExactAlarms() == true
        }

        fun scheduleTestReminder(context: Context, delayMillis: Long): Boolean {
            return scheduleTest(
                context = context,
                requestCode = TEST_NOTIFICATION_ID,
                triggerAtMillis = System.currentTimeMillis() + delayMillis,
                notificationId = TEST_NOTIFICATION_ID,
                title = context.getString(R.string.test_reminder_title),
                text = context.getString(R.string.test_reminder_text)
            )
        }

        private fun scheduleTest(
            context: Context,
            requestCode: Int,
            triggerAtMillis: Long,
            notificationId: Int,
            title: String,
            text: String
        ): Boolean {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return false
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_ID, notificationId)
                putExtra(ReminderReceiver.EXTRA_TITLE, title)
                putExtra(ReminderReceiver.EXTRA_TEXT, text)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val exact = canScheduleExact(context)
            try {
                if (exact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pending
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pending
                    )
                }
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pending
                )
                Diagnostics.record(context, "精确闹钟被系统拒绝，已降级为非精确")
            }
            Diagnostics.setScheduledAt(context, triggerAtMillis)
            Diagnostics.record(
                context,
                "已安排 ${formatTrigger(triggerAtMillis)}（${if (exact) "精确" else "非精确"}）"
            )
            return true
        }

        fun cancel(context: Context, requestCode: Int) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            context.getSystemService(AlarmManager::class.java)?.cancel(pending)
            pending.cancel()
        }

        private fun formatTrigger(millis: Long): String =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
    }
}
