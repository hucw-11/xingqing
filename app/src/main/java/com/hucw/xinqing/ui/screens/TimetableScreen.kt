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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.hucw.xinqing.domain.TimetableLayout
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
    onOccurrence: (Long, LocalDate) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as XinqingApp).container }
    val courseFlow = remember { container.courseRepository.observeCourses() }
    val courses by courseFlow.collectAsState(initial = emptyList())
    val exceptionFlow = remember { container.courseRepository.observeExceptions() }
    val exceptions by exceptionFlow.collectAsState(initial = emptyList())
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
                val days = (0..6).map { day ->
                    val date = monday.plusDays(day.toLong())
                    date to courses.flatMap {
                        Recurrence.occurrencesOn(it, date, semesterValue, exceptions)
                    }
                }
                Text("左右滑动查看整周 · 上下滑动查看时间 · 点击课程调课/停课",
                    style = MaterialTheme.typography.bodySmall)
                WeekTimeline(days, onOccurrence)
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
                                "${course.weekdays.joinToString("、") { WeekDates.dayLabel(it) }} " +
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
private fun WeekTimeline(
    days: List<Pair<LocalDate, List<CourseOccurrence>>>,
    onClickOccurrence: (Long, LocalDate) -> Unit
) {
    val all = days.flatMap { it.second }
    val startHour = minOf(7, (all.minOfOrNull { it.startMinute } ?: 420) / 60)
    val endHour = maxOf(22, ((all.maxOfOrNull { it.endMinute } ?: 1320) + 59) / 60)
    val minuteHeight = 1.5f
    val gridHeight = ((endHour - startHour) * 60 * minuteHeight).dp
    val dayLanes = days.map { TimetableLayout.lanes(it.second) }
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row {
            Box(Modifier.width(52.dp).height(52.dp), contentAlignment = Alignment.Center) {
                Text("时间", style = MaterialTheme.typography.labelMedium)
            }
            days.forEachIndexed { index, (date, _) ->
                val isToday = date == LocalDate.now()
                Box(
                    Modifier.width((124 * dayLanes[index].size).dp).height(52.dp)
                        .background(if (isToday) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${WeekDates.dayLabel(date.dayOfWeek.value)} ${date.monthValue}/${date.dayOfMonth}" +
                        if (isToday) " · 今天" else "", fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.height(420.dp).verticalScroll(rememberScrollState())) {
            Box(Modifier.width(52.dp).height(gridHeight + 24.dp)) {
                (startHour..endHour).forEach { hour ->
                    Text("%02d:00".format(hour),
                        modifier = Modifier.offset(y = ((hour - startHour) * 90).dp),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            days.forEachIndexed { index, (date, _) ->
                Box(Modifier.width((124 * dayLanes[index].size).dp).height(gridHeight + 24.dp)
                    .background(if (date == LocalDate.now()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface)) {
                    (startHour..endHour).forEach { hour ->
                        HorizontalDivider(Modifier.offset(y = ((hour - startHour) * 90).dp))
                    }
                    dayLanes[index].forEachIndexed { lane, occurrences ->
                        occurrences.forEach { occurrence ->
                            val tint = courseColor(occurrence.color)
                            Row(
                                Modifier.offset(x = (lane * 124 + 2).dp,
                                    y = ((occurrence.startMinute - startHour * 60) * minuteHeight).dp)
                                    .width(120.dp)
                                    .height(((occurrence.endMinute - occurrence.startMinute) * minuteHeight).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onClickOccurrence(occurrence.courseId, occurrence.date) }
                            ) {
                                Box(Modifier.width(4.dp).fillMaxHeight().background(tint))
                                Column(Modifier.padding(5.dp)) {
                                    Text(occurrence.name, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text("${TimeFormat.formatMinute(occurrence.startMinute)}–${TimeFormat.formatMinute(occurrence.endMinute)}",
                                        fontSize = 11.sp, maxLines = 1)
                                    if (occurrence.location.isNotBlank()) {
                                        Text(occurrence.location, fontSize = 12.sp, maxLines = 1,
                                            overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
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
