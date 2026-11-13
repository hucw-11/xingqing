package com.hucw.xinqing.ui.screens

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hucw.xinqing.XinqingApp
import com.hucw.xinqing.diagnostics.Diagnostics
import com.hucw.xinqing.domain.Recurrence
import com.hucw.xinqing.reminder.AlarmScheduler
import com.hucw.xinqing.reminder.Notifications
import com.hucw.xinqing.util.TimeFormat
import java.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var notificationsOk by remember { mutableStateOf(Notifications.canPost(context)) }
    var channelOk by remember { mutableStateOf(isChannelEnabled(context)) }
    var exactAlarmOk by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }
    var batteryOk by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }
    var scheduledAt by remember { mutableStateOf(Diagnostics.scheduledAt(context)) }
    var triggerCount by remember { mutableStateOf(Diagnostics.triggerCount(context)) }
    var events by remember { mutableStateOf(Diagnostics.events(context)) }
    var message by remember { mutableStateOf("") }

    val app = context.applicationContext as XinqingApp
    val courseFlow = remember { app.container.courseRepository.observeCourses() }
    val courses by courseFlow.collectAsState(initial = emptyList())
    val semester by app.container.settings.semester.collectAsState()
    val today = LocalDate.now()
    val todayOccurrences = remember(courses, semester, today) {
        semester?.let { monday ->
            courses.flatMap { course -> Recurrence.occurrencesOn(course, today, monday) }
                .sortedBy { it.startMinute }
        }.orEmpty()
    }

    fun refresh() {
        notificationsOk = Notifications.canPost(context)
        channelOk = isChannelEnabled(context)
        exactAlarmOk = AlarmScheduler.canScheduleExact(context)
        batteryOk = isIgnoringBatteryOptimizations(context)
        scheduledAt = Diagnostics.scheduledAt(context)
        triggerCount = Diagnostics.triggerCount(context)
        events = Diagnostics.events(context)
    }

    fun scheduleTest(delayMillis: Long, label: String) {
        val ok = AlarmScheduler.scheduleTestReminder(context, delayMillis)
        message = if (ok) "已安排 $label 的提醒，可以锁屏等待" else "安排失败：请确认精确闹钟权限"
        refresh()
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        refresh()
        if (granted) {
            scheduleTest(60_000L, "1 分钟后")
            message = "通知权限已开启，已自动安排 1 分钟后的测试提醒"
        } else {
            message = "通知权限被拒绝，提醒将无法显示"
            openNotificationSettings(context)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refresh()
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "今天 · ${TimeFormat.formatDate(today)}",
                    style = MaterialTheme.typography.titleMedium
                )
                when {
                    semester == null -> Text(
                        "先在「设置」里填写学期起始日期",
                        style = MaterialTheme.typography.bodySmall
                    )
                    todayOccurrences.isEmpty() -> Text(
                        "今天没有课",
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> todayOccurrences.forEach { occurrence ->
                        Text(
                            "${TimeFormat.formatMinute(occurrence.startMinute)}–" +
                                "${TimeFormat.formatMinute(occurrence.endMinute)}  " +
                                occurrence.name +
                                if (occurrence.location.isNotBlank()) {
                                    " · ${occurrence.location}"
                                } else {
                                    ""
                                },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("提醒状态", style = MaterialTheme.typography.titleMedium)
                StatusLine("通知权限", notificationsOk)
                StatusLine("通知渠道", channelOk)
                StatusLine("精确闹钟", exactAlarmOk)
                StatusLine("忽略电池优化", batteryOk)
                Text(
                    text = "已触发次数：$triggerCount",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = scheduledText(scheduledAt),
                    style = MaterialTheme.typography.bodySmall
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
                    scheduleTest(60_000L, "1 分钟后")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("1 分钟后提醒我")
        }

        OutlinedButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsOk) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    scheduleTest(10_000L, "10 秒后")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("10 秒后快速测试")
        }

        if (!notificationsOk || !channelOk) {
            OutlinedButton(
                onClick = { openNotificationSettings(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("打开通知设置")
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

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("提醒诊断记录", style = MaterialTheme.typography.titleMedium)
                if (events.isEmpty()) {
                    Text("暂无记录", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        text = events.joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(
                    onClick = {
                        Diagnostics.clear(context)
                        refresh()
                    }
                ) {
                    Text("清空记录")
                }
            }
        }

        Text(
            text = "提示：部分国产手机会在锁屏后限制后台，建议在应用设置里允许自启动，并关闭电池优化。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusLine(label: String, ok: Boolean) {
    Text(
        text = "$label：${if (ok) "正常" else "未开启"}",
        style = MaterialTheme.typography.bodyMedium,
        color = if (ok) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
    )
}

private fun scheduledText(scheduledAt: Long): String {
    if (scheduledAt <= 0L) return "尚未安排测试提醒"
    val now = System.currentTimeMillis()
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(scheduledAt))
    return if (scheduledAt > now) {
        val seconds = ((scheduledAt - now) / 1000).coerceAtLeast(0)
        "已安排 $time（约 $seconds 秒后）"
    } else {
        "最近安排时间 $time"
    }
}

private fun isChannelEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
    val manager = context.getSystemService(NotificationManager::class.java) ?: return false
    val channel = manager.getNotificationChannel(Notifications.CHANNEL_ID) ?: return false
    return channel.importance != NotificationManager.IMPORTANCE_NONE
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java) ?: return false
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
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
