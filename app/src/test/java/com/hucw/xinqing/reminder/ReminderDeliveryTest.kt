package com.hucw.xinqing.reminder

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationAttributes
import com.hucw.xinqing.domain.ReminderStyle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class ReminderDeliveryTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `legacy API uses notification audio usage`() {
        val vibrator = context.getSystemService(Vibrator::class.java)
        Vibration.vibrate(context, ReminderStyle.VIBRATE)
        assertEquals(AudioAttributes.USAGE_NOTIFICATION,
            shadowOf(vibrator).audioAttributesFromLastVibration!!.usage)
    }

    @Test
    @Config(sdk = [33])
    fun `modern API uses notification vibration usage`() {
        val vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator
        Vibration.vibrate(context, ReminderStyle.SOUND)
        val attributes = shadowOf(vibrator).vibrationAttributesFromLastVibration as VibrationAttributes
        assertEquals(VibrationAttributes.USAGE_NOTIFICATION, attributes.usage)
    }

    @Test
    fun `silent reminder does not call vibrator and course notification opens timetable`() {
        assertTrue(Notifications.show(context, 71, "测试", "课程", ReminderStyle.SILENT, "timetable"))
        assertFalse(shadowOf(context.getSystemService(Vibrator::class.java)).isVibrating)
        val notification = shadowOf(context.getSystemService(NotificationManager::class.java)).getNotification(71)
        val intent = shadowOf(notification.contentIntent).savedIntent
        assertEquals("timetable", intent.getStringExtra("destination"))
    }
}
