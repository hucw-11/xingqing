package com.hucw.xinqing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hucw.xinqing.XinqingApp
import com.hucw.xinqing.domain.CourseOccurrence
import com.hucw.xinqing.domain.Recurrence
import com.hucw.xinqing.domain.WeekDates
import com.hucw.xinqing.domain.WeekParity
import com.hucw.xinqing.ui.theme.courseColor
import com.hucw.xinqing.util.TimeFormat
import java.time.LocalDate

@Composable
fun TimetableScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as XinqingApp).container }
    val courseFlow = remember { container.courseRepository.observeCourses() }
    val courses by courseFlow.collectAsState(initial = emptyList())
    val semester by container.settings.semester.collectAsState()
    var weekOffset by rememberSaveable { mutableStateOf(0) }
    val today = LocalDate.now()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "课表",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { weekOffset -= 1 }) { Text("上周") }
                TextButton(onClick = { weekOffset = 0 }) { Text("本周") }
                TextButton(onClick = { weekOffset += 1 }) { Text("下周") }
            }

            val semesterValue = semester
            if (semesterValue == null) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("还没有设置学期", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "填写学期第一个周一后，单双周和课表才能正确展开。",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = onOpenSettings) { Text("去设置") }
                    }
                }
            } else {
                val monday = WeekDates.mondayOf(today).plusWeeks(weekOffset.toLong())
                Text(
                    "第 ${WeekDates.weekNumber(monday, semesterValue)} 周 · ${TimeFormat.formatDate(monday)} 起",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (day in 0..6) {
                        val date = monday.plusDays(day.toLong())
                        val occurrences = courses
                            .flatMap { Recurrence.occurrencesOn(it, date, semesterValue) }
                            .sortedBy { it.startMinute }
                        DayColumn(date, day + 1, occurrences, onEdit)
                    }
                }
                HorizontalDivider()
                Text("全部课程", style = MaterialTheme.typography.titleMedium)
                if (courses.isEmpty()) {
                    Text(
                        "还没有课程，点右下角 + 添加",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                courses.forEach { course ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEdit(course.id) }
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(course.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${WeekDates.dayLabel(course.weekday)} " +
                                    "${TimeFormat.formatMinute(course.startMinute)}–" +
                                    "${TimeFormat.formatMinute(course.endMinute)} · " +
                                    "第 ${course.startWeek}~${course.endWeek} 周 · " +
                                    parityLabel(course.weekParity),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(Modifier.height(72.dp))
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Text("+")
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    weekday: Int,
    occurrences: List<CourseOccurrence>,
    onEdit: (Long) -> Unit
) {
    val isToday = date == LocalDate.now()
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "${WeekDates.dayLabel(weekday)} ${date.dayOfMonth} 日",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (occurrences.isEmpty()) {
            Text(
                "—",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            occurrences.forEach { occurrence ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(courseColor(occurrence.color).copy(alpha = 0.22f))
                        .clickable { onEdit(occurrence.courseId) }
                        .padding(6.dp)
                ) {
                    Text(
                        "${TimeFormat.formatMinute(occurrence.startMinute)}–" +
                            TimeFormat.formatMinute(occurrence.endMinute),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(occurrence.name, style = MaterialTheme.typography.bodySmall)
                    if (occurrence.location.isNotBlank()) {
                        Text(
                            occurrence.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun parityLabel(parity: WeekParity): String = when (parity) {
    WeekParity.ALL -> "每周"
    WeekParity.ODD -> "单周"
    WeekParity.EVEN -> "双周"
}
