package com.hucw.xinqing.reminder

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.hucw.xinqing.domain.ReminderStyle

object Vibration {
    private val pattern = longArrayOf(0, 800, 300, 800, 300, 1200)

    fun vibrate(context: Context, style: ReminderStyle) {
        if (style == ReminderStyle.SILENT) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        } ?: return
        if (!vibrator.hasVibrator()) return
        try {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (e: SecurityException) {
            // 缺少 VIBRATE 权限时会走到这里
        }
    }
}
