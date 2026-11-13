package com.hucw.xinqing.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hucw.xinqing.R
import com.hucw.xinqing.diagnostics.Diagnostics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmScheduler {
    const val TEST_NOTIFICATION_ID = 9001

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager?.canScheduleExactAlarms() == true
    }

    fun scheduleTestReminder(context: Context, delayMillis: Long): Boolean {
        return schedule(
            context = context,
            requestCode = TEST_NOTIFICATION_ID,
            triggerAtMillis = System.currentTimeMillis() + delayMillis,
            notificationId = TEST_NOTIFICATION_ID,
            title = context.getString(R.string.test_reminder_title),
            text = context.getString(R.string.test_reminder_text)
        )
    }

    fun schedule(
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
        val pendingIntent = PendingIntent.getBroadcast(
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
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
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
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun formatTrigger(millis: Long): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
}
