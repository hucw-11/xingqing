package com.hucw.xinqing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.hucw.xinqing.domain.WeekDates
import com.hucw.xinqing.reminder.AlarmScheduler
import com.hucw.xinqing.reminder.Notifications
import com.hucw.xinqing.ui.common.SystemIntents
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as XinqingApp).container }
    val semester by container.settings.semester.collectAsState()
    var semesterText by remember { mutableStateOf(semester?.toString().orEmpty()) }
    var message by remember { mutableStateOf("") }
    var notificationsOk by remember { mutableStateOf(Notifications.canPost(context)) }
    var exactOk by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsOk = Notifications.canPost(context)
                exactOk = AlarmScheduler.canScheduleExact(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "设置 · 心晴 1.2",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("学期", style = MaterialTheme.typography.titleMedium)
                Text(
                    "填写学期第一个周一；单双周会以它为第 1 周计算。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = semesterText,
                    onValueChange = { semesterText = it },
                    label = { Text("学期第一个周一（yyyy-MM-dd）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val parsed = runCatching {
                            LocalDate.parse(semesterText.trim())
                        }.getOrNull()
                        if (parsed == null) {
                            message = "日期格式应为 yyyy-MM-dd，例如 2026-02-23"
                        } else {
                            val monday = WeekDates.mondayOf(parsed)
                            container.settings.setSemesterStart(monday)
                            semesterText = monday.toString()
                            message = "已保存：第 1 周从 $monday 开始"
                            container.appScope.launch {
                                container.alarmScheduler.rescheduleAll()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存学期")
                }
                if (message.isNotEmpty()) {
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("提醒权限", style = MaterialTheme.typography.titleMedium)
                Text("通知权限：${if (notificationsOk) "正常" else "未开启"}")
                Text("精确闹钟：${if (exactOk) "正常" else "未开启（提醒可能延迟）"}")
                OutlinedButton(
                    onClick = { SystemIntents.openNotificationSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("通知设置")
                }
                OutlinedButton(
                    onClick = { SystemIntents.openExactAlarmSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("精确闹钟设置")
                }
                OutlinedButton(
                    onClick = { SystemIntents.openBatteryOptimizationSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("电池优化设置")
                }
                OutlinedButton(
                    onClick = { SystemIntents.openAppSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用设置（自启动/后台权限）")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("关于", style = MaterialTheme.typography.titleMedium)
                Text(
                    "心晴 1.0 · 数据保存在本机，不上传",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
