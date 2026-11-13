package com.hucw.xinqing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hucw.xinqing.XinqingApp
import com.hucw.xinqing.domain.Course
import com.hucw.xinqing.domain.CourseException
import com.hucw.xinqing.domain.ExceptionType
import com.hucw.xinqing.ui.components.NumberWheel
import com.hucw.xinqing.util.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AdjustCourseScreen(
    courseId: Long,
    originalDateText: String,
    onDone: () -> Unit,
    onEditCourse: () -> Unit
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as XinqingApp).container }
    val scope = rememberCoroutineScope()

    var course by remember { mutableStateOf<Course?>(null) }
    var targetDateText by remember { mutableStateOf(originalDateText) }
    var targetHour by remember { mutableStateOf(8) }
    var targetMinute by remember { mutableStateOf(0) }
    var durationText by remember { mutableStateOf("90") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(courseId) {
        val loaded = container.courseRepository.findById(courseId)
        course = loaded
        if (loaded != null) {
            targetHour = loaded.startMinute / 60
            targetMinute = loaded.startMinute % 60
            durationText = (loaded.endMinute - loaded.startMinute).toString()
        }
    }

    val originalDate = runCatching { LocalDate.parse(originalDateText) }.getOrNull()
    val current = course

    if (current == null || originalDate == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("无法调课", style = MaterialTheme.typography.headlineSmall)
            Text("课程不存在，或日期格式不正确。", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    fun applyExceptions(exceptions: List<CourseException>) {
        scope.launch {
            container.courseRepository.replaceExceptions(courseId, originalDate, exceptions)
            container.appScope.launch { container.alarmScheduler.rescheduleAll() }
            onDone()
        }
    }

    fun moveOccurrence() {
        val targetDate = runCatching { LocalDate.parse(targetDateText.trim()) }.getOrNull()
        if (targetDate == null) {
            error = "日期格式应为 yyyy-MM-dd"
            return
        }
        val duration = durationText.toIntOrNull()
        if (duration == null || duration <= 0) {
            error = "持续时间请填写大于 0 的分钟数"
            return
        }
        val start = targetHour * 60 + targetMinute
        val end = start + duration
        if (end > 24 * 60) {
            error = "结束时间超出当天范围"
            return
        }
        error = ""
        applyExceptions(
            listOf(
                CourseException(courseId, originalDate, ExceptionType.CANCELLED),
                CourseException(
                    courseId,
                    originalDate,
                    ExceptionType.MOVED,
                    newDate = targetDate,
                    newStartMinute = start,
                    newEndMinute = end
                )
            )
        )
    }

    val startValue = targetHour * 60 + targetMinute
    val endPreview = durationText.toIntOrNull()
        ?.takeIf { it > 0 }
        ?.let { startValue + it }
        ?.takeIf { it <= 24 * 60 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "调课 / 停课",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(current.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "原时间：${TimeFormat.formatDate(originalDate)} " +
                        "${TimeFormat.formatMinute(current.startMinute)}–" +
                        TimeFormat.formatMinute(current.endMinute),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = {
                applyExceptions(
                    listOf(
                        CourseException(courseId, originalDate, ExceptionType.CANCELLED)
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("停课（取消这一次）")
        }

        HorizontalDivider()

        Text("调整到", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = targetDateText,
            onValueChange = { targetDateText = it },
            label = { Text("日期（yyyy-MM-dd）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberWheel(
                value = targetHour,
                range = 0..23,
                onValueChange = { targetHour = it },
                modifier = Modifier.weight(1f).height(120.dp)
            )
            Text("时")
            NumberWheel(
                value = targetMinute,
                range = 0..59,
                onValueChange = { targetMinute = it },
                modifier = Modifier.weight(1f).height(120.dp)
            )
            Text("分")
        }
        OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it },
            label = { Text("持续分钟数") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "结束时间：" + (endPreview?.let { TimeFormat.formatMinute(it) } ?: "—"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (error.isNotEmpty()) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = { moveOccurrence() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存调课")
        }
        OutlinedButton(
            onClick = onEditCourse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("编辑课程本身")
        }
    }
}
