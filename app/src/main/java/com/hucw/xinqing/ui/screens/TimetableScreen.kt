package com.hucw.xinqing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
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
    var todayRequest by remember { mutableStateOf(0) }
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
                TextButton(onClick = { weekOffset = 0; todayRequest++ }) { Text("今天") }
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
                Text("默认显示整周；点课程查看详情，放大后可滑动查看",
                    style = MaterialTheme.typography.bodySmall)
                WeekTimeline(days, todayRequest, onOccurrence)
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
internal fun WeekTimeline(
    days: List<Pair<LocalDate, List<CourseOccurrence>>>,
    todayRequest: Int,
    onClickOccurrence: (Long, LocalDate) -> Unit
) {
    var compact by rememberSaveable { mutableStateOf(true) }
    var selected by remember { mutableStateOf<CourseOccurrence?>(null) }
    val all = days.flatMap { it.second }
    val startHour = minOf(7, (all.minOfOrNull { it.startMinute } ?: 420) / 60)
    val endHour = maxOf(22, ((all.maxOfOrNull { it.endMinute } ?: 1320) + 59) / 60)
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val density = LocalDensity.current
    Column {
        TextButton(onClick = { compact = !compact }) {
            Text(if (compact) "放大查看" else "整周总览")
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val timeWidth = 40.dp
            val dayWidth = if (compact) (maxWidth - timeWidth) / 7 else 112.dp
            val viewportHeight = 380.dp
            val minuteHeight = if (compact) 356f / ((endHour - startHour) * 60) else 1f
            val gridHeight = ((endHour - startHour) * 60 * minuteHeight + 24).dp
            val xOffset = with(density) { horizontal.value.toDp() }
            val yOffset = with(density) { vertical.value.toDp() }
            LaunchedEffect(todayRequest, compact, days.first().first) {
                if (compact) {
                    horizontal.scrollTo(0)
                    vertical.scrollTo(0)
                } else if (todayRequest > 0) {
                    val index = days.indexOfFirst { it.first == LocalDate.now() }
                    if (index >= 0) {
                        horizontal.animateScrollTo(with(density) { (dayWidth * index).roundToPx() })
                        val now = java.time.LocalTime.now()
                        val minute = (now.hour * 60 + now.minute - startHour * 60 - 60).coerceAtLeast(0)
                        vertical.animateScrollTo(with(density) { (minute * minuteHeight).dp.roundToPx() })
                    }
                }
            }
            Column {
                Row {
                    Box(Modifier.width(timeWidth).height(44.dp), contentAlignment = Alignment.Center) {
                        Text("时间", fontSize = 10.sp)
                    }
                    Box(Modifier.weight(1f).height(44.dp).clipToBounds()) {
                        Row(Modifier.offset(x = -xOffset).wrapContentWidth(Alignment.Start, unbounded = true).width(dayWidth * 7)) {
                            days.forEach { (date, _) ->
                                val today = date == LocalDate.now()
                                Column(Modifier.width(dayWidth).height(44.dp)
                                    .background(if (today) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(WeekDates.dayLabel(date.dayOfWeek.value), fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("${date.monthValue}/${date.dayOfMonth}", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                Row(Modifier.height(viewportHeight)) {
                    // Only the course area scrolls horizontally; the time ruler stays at the left.
                    Box(Modifier.width(timeWidth).height(viewportHeight).clipToBounds()) {
                        (startHour..endHour).forEach { hour ->
                            Text("%02d:00".format(hour),
                                Modifier.offset(y = ((hour - startHour) * 60 * minuteHeight).dp - yOffset),
                                fontSize = 9.sp, maxLines = 1)
                        }
                    }
                    Box(Modifier.weight(1f).height(viewportHeight).testTag("week-grid")
                        .horizontalScroll(horizontal).verticalScroll(vertical)) {
                        Row(Modifier.width(dayWidth * 7).height(gridHeight)) {
                            days.forEach { (date, occurrences) ->
                                val lanes = TimetableLayout.lanes(occurrences)
                                val laneWidth = dayWidth / lanes.size
                                Box(Modifier.width(dayWidth).height(gridHeight)
                                    .background(if (date == LocalDate.now())
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surface)) {
                                    (startHour..endHour).forEach { hour ->
                                        HorizontalDivider(Modifier.offset(y = ((hour - startHour) * 60 * minuteHeight).dp))
                                    }
                                    lanes.forEachIndexed { lane, lessons ->
                                        lessons.forEach { lesson ->
                                            Box(Modifier.offset(x = laneWidth * lane + 1.dp,
                                                y = ((lesson.startMinute - startHour * 60) * minuteHeight).dp)
                                                .width((laneWidth - 2.dp).coerceAtLeast(1.dp))
                                                .height(((lesson.endMinute - lesson.startMinute) * minuteHeight).dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { selected = lesson }) {
                                                Box(Modifier.width(3.dp).fillMaxHeight().background(courseColor(lesson.color)))
                                                Column(Modifier.padding(start = 4.dp, top = 1.dp, end = 1.dp)) {
                                                    Text(lesson.name, fontSize = if (compact) 10.sp else 13.sp,
                                                        lineHeight = if (compact) 11.sp else 16.sp,
                                                        maxLines = if (compact) 3 else 2,
                                                        overflow = TextOverflow.Ellipsis)
                                                    if (!compact) {
                                                        Text("${TimeFormat.formatMinute(lesson.startMinute)}–${TimeFormat.formatMinute(lesson.endMinute)}",
                                                            fontSize = 10.sp, maxLines = 1)
                                                        Text(lesson.location, fontSize = 11.sp, maxLines = 1)
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
            }
        }
    }
    selected?.let { lesson ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(lesson.name) },
            text = { Text("${lesson.date}  ${WeekDates.dayLabel(lesson.date.dayOfWeek.value)}\n" +
                "${TimeFormat.formatMinute(lesson.startMinute)}–${TimeFormat.formatMinute(lesson.endMinute)}\n${lesson.location}") },
            confirmButton = { TextButton(onClick = {
                selected = null
                onClickOccurrence(lesson.courseId, lesson.date)
            }) { Text("调课 / 停课") } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("关闭") } }
        )
    }
}
private fun parityLabel(parity: WeekParity): String = when (parity) {
    WeekParity.ALL -> "每周"
    WeekParity.ODD -> "单周"
    WeekParity.EVEN -> "双周"
}
