package com.hucw.xinqing.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hucw.xinqing.reminder.AlarmScheduler
import com.hucw.xinqing.reminder.Notifications

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var notificationsOk by remember { mutableStateOf(Notifications.canPost(context)) }
    var exactAlarmOk by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }
    var message by remember { mutableStateOf("") }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsOk = Notifications.canPost(context)
        message = if (granted) "通知权限已开启，可以发送测试提醒了" else "通知权限被拒绝，提醒将无法显示"
        if (!granted) openNotificationSettings(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsOk = Notifications.canPost(context)
                exactAlarmOk = AlarmScheduler.canScheduleExact(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "心晴",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "记录心情 · 写日记 · 课表提醒 · 每日计划",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("提醒链路自检", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "通知权限：${if (notificationsOk) "已开启" else "未开启"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "精确闹钟：${if (exactAlarmOk) "已允许" else "未允许（提醒可能延迟）"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsOk) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    AlarmScheduler.scheduleTestReminder(context, delayMinutes = 5)
                    message = "已安排 5 分钟后的测试提醒，可以锁屏等待"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("发送 5 分钟后的测试提醒")
        }

        if (!notificationsOk) {
            OutlinedButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openNotificationSettings(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开启通知权限")
            }
        }

        if (!exactAlarmOk) {
            OutlinedButton(
                onClick = { openExactAlarmSettings(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("允许精确闹钟")
            }
        }

        OutlinedButton(
            onClick = { openBatteryOptimizationSettings(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("关闭电池优化（国产 ROM 建议）")
        }

        OutlinedButton(
            onClick = { openAppSettings(context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("应用设置（自启动/后台权限）")
        }

        Text(
            text = "提示：部分国产手机会在锁屏后限制后台，建议在应用设置里允许自启动，并关闭电池优化。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    runCatching { context.startActivity(intent) }
        .onFailure { openAppSettings(context) }
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    runCatching { context.startActivity(intent) }
        .onFailure { openAppSettings(context) }
}

private fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    runCatching { context.startActivity(intent) }
        .onFailure { openAppSettings(context) }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:${context.packageName}")
    )
    runCatching { context.startActivity(intent) }
}
