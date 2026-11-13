package com.hucw.xinqing.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hucw.xinqing.R
import com.hucw.xinqing.domain.ReminderStyle

object Notifications {
    const val CHANNEL_SOUND = "reminders_sound"
    const val CHANNEL_VIBRATE = "reminders_vibrate"
    const val CHANNEL_SILENT = "reminders_silent"
    const val CHANNEL_ID = CHANNEL_SOUND

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SOUND,
                context.getString(R.string.channel_sound),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_sound_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_VIBRATE,
                context.getString(R.string.channel_vibrate),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_vibrate_desc)
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 1200)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SILENT,
                context.getString(R.string.channel_silent),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_silent_desc)
                setSound(null, null)
                enableVibration(false)
            }
        )
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun show(
        context: Context,
        id: Int,
        title: String,
        text: String,
        style: ReminderStyle = ReminderStyle.SOUND
    ): Boolean {
        if (!canPost(context)) return false
        ensureChannels(context)
        val channelId = when (style) {
            ReminderStyle.SILENT -> CHANNEL_SILENT
            ReminderStyle.VIBRATE -> CHANNEL_VIBRATE
            ReminderStyle.SOUND -> CHANNEL_SOUND
        }
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(
                if (style == ReminderStyle.SILENT) NotificationCompat.PRIORITY_LOW
                else NotificationCompat.PRIORITY_HIGH
            )
            .setAutoCancel(true)
        if (style == ReminderStyle.SOUND) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 800))
        }
        if (style == ReminderStyle.VIBRATE) {
            builder.setVibrate(longArrayOf(0, 800, 300, 800, 300, 1200))
        }
        return try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
            Vibration.vibrate(context, style)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
