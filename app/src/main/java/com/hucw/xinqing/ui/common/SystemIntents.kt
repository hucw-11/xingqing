package com.hucw.xinqing.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object SystemIntents {
    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { openAppSettings(context) }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        runCatching { context.startActivity(intent) }
            .onFailure { openAppSettings(context) }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        runCatching { context.startActivity(intent) }
            .onFailure { openAppSettings(context) }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        runCatching { context.startActivity(intent) }
    }
}
