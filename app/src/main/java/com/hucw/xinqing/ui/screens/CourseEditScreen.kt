package com.hucw.xinqing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.hucw.xinqing.domain.CourseValidation
import com.hucw.xinqing.domain.Overlap
import com.hucw.xinqing.domain.ReminderStyle
import com.hucw.xinqing.domain.WeekDates
import com.hucw.xinqing.domain.WeekParity
import com.hucw.xinqing.ui.components.NumberWheel
import com.hucw.xinqing.ui.theme.courseColorOptions
import com.hucw.xinqing.util.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CourseEditScreen(courseId: Long, onDone: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as XinqingApp).container }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var weekday by remember { mutableStateOf(LocalDate.now().dayOfWeek.value) }
    var startHour by remember { mutableStateOf(8) }
    var startMinuteOfHour by remember { mutableStateOf(0) }
    var durationText by remember { mutableStateOf("90") }
    var startWeekText by remember { mutableStateOf("1") }
    var weekCountText by remember { mutableStateOf("16") }
    var location by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var parity by remember { mutableStateOf(WeekParity.ALL) }
    var reminderMinutes by remember { mutableStateOf(15) }
    var reminderStyle by remember { mutableStateOf(ReminderStyle.SOUND) }
    var color by remember { mutableStateOf("sage") }
    var error by remember { mutableStateOf("") }
    var conflictName by remember { mutableStateOf("") }
    var showConflictDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        if (courseId == 0L) return@LaunchedEffect
        val course = container.courseRepository.findById(courseId) ?: return@LaunchedEffect
        name = course.name
        weekday = course.weekday
        startHour = course.startMinute / 60
        startMinuteOfHour = course.startMinute % 60
        durationText = (course.endMinute - course.startMinute).toString()
        startWeekText = course.startWeek.toString()
        weekCountText = course.weekCount.toString()
        location = course.location
        teacher = course.teacher
        parity = course.weekParity
        reminderMinutes = course.reminderMinutes
        reminderStyle = course.reminderStyle
        color = course.color
    }

    fun save(ignoreConflict: Boolean) {
        val duration = durationText.toIntOrNull()
        if (duration == null || duration <= 0) {
            error = "持续时间请填写大于 0 的分钟数"
            return
        }
        val startWeek = startWeekText.toIntOrNull()
        val weekCount = weekCountText.toIntOrNull()
        if (startWeek == null || weekCount == null) {
            error = "周次请填写数字"
            return
        }
        val startMinute = startHour * 60 + startMinuteOfHour
        val endMinute = startMinute + duration
        if (endMinute > 24 * 60) {
            error = "结束时间超出当天范围，请缩短持续时间"
            return
        }
        val course = Course(
            id = courseId,
            name = name,
            weekday = weekday,
            startMinute = startMinute,
            endMinute = endMinute,
            startWeek = startWeek,
            weekCount = weekCount,
            weekParity = parity,
            location = location,
            teacher = teacher,
            color = color,
            reminderMinutes = reminderMinutes,
            reminderStyle = reminderStyle
        )
        val validation = CourseValidation.validate(course)
        if (validation != null) {
            error = validation
            return
        }
        error = ""
        scope.launch {
            if (!ignoreConflict) {
                val conflict = Overlap.findConflict(
                    course,
                    container.courseRepository.getAll()
                )
                if (conflict != null) {
                    conflictName = conflict.name
                    showConflictDialog = true
                    return@launch
                }
            }
            container.courseRepository.save(course)
            container.appScope.launch { container.alarmScheduler.rescheduleAll() }
            onDone()
        }
    }

    val durationValue = durationText.toIntOrNull()
    val startMinuteValue = startHour * 60 + startMinuteOfHour
    val endPreview = durationValue
        ?.takeIf { it > 0 }
        ?.let { startMinuteValue + it }
        ?.takeIf { it <= 24 * 60 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (courseId == 0L) "添加课程" else "编辑课程",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("课程名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "星期",
            options = (1..7).map { it to WeekDates.dayLabel(it) },
            selected = weekday,
            onSelect = { weekday = it }
        )

        Text("开始时间", style = MaterialTheme.typography.bodyMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberWheel(
                value = startHour,
                range = 0..23,
                onValueChange = { startHour = it },
                modifier = Modifier.weight(1f).height(120.dp)
            )
            Text("时")
            NumberWheel(
                value = startMinuteOfHour,
                range = 0..59,
                onValueChange = { startMinuteOfHour = it },
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = startWeekText,
                onValueChange = { startWeekText = it },
                label = { Text("开始周") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weekCountText,
                onValueChange = { weekCountText = it },
                label = { Text("持续周数") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        DropdownSelector(
            label = "单双周",
            options = listOf(
                WeekParity.ALL to "每周",
                WeekParity.ODD to "单周",
                WeekParity.EVEN to "双周"
            ),
            selected = parity,
            onSelect = { parity = it }
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("地点") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = teacher,
            onValueChange = { teacher = it },
            label = { Text("老师") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownSelector(
            label = "颜色",
            options = courseColorOptions,
            selected = color,
            onSelect = { color = it }
        )

        DropdownSelector(
            label = "提醒",
            options = CourseValidation.reminderOptions.map {
                it to if (it == 0) "上课时" else "$it 分钟前"
            },
            selected = reminderMinutes,
            onSelect = { reminderMinutes = it }
        )

        DropdownSelector(
            label = "提醒方式",
            options = listOf(
                ReminderStyle.SILENT to "静音",
                ReminderStyle.VIBRATE to "仅振动",
                ReminderStyle.SOUND to "声音+振动"
            ),
            selected = reminderStyle,
            onSelect = { reminderStyle = it }
        )

        if (error.isNotEmpty()) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { save(ignoreConflict = false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }

        if (courseId != 0L) {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("删除课程")
            }
        }
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("时间冲突") },
            text = { Text("与「$conflictName」在同一时间且周次重叠，仍要保存吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConflictDialog = false
                        save(ignoreConflict = true)
                    }
                ) {
                    Text("仍然保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除课程") },
            text = { Text("删除后，这门课的提醒也会取消。确定删除吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            container.courseRepository.delete(courseId)
                            container.appScope.launch {
                                container.alarmScheduler.rescheduleAll()
                            }
                            onDone()
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun <T> DropdownSelector(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selected }?.second ?: ""
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("$label：$selectedText")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
