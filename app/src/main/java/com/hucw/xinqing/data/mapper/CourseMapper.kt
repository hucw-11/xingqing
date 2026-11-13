package com.hucw.xinqing.data.mapper

import com.hucw.xinqing.data.entity.CourseEntity
import com.hucw.xinqing.data.entity.CourseExceptionEntity
import com.hucw.xinqing.domain.Course
import com.hucw.xinqing.domain.CourseException
import com.hucw.xinqing.domain.ExceptionType
import com.hucw.xinqing.domain.ReminderStyle
import com.hucw.xinqing.domain.WeekParity
import java.time.LocalDate

fun CourseEntity.toModel(): Course = Course(
    id = id,
    name = name,
    weekday = weekday,
    startMinute = startMinute,
    endMinute = endMinute,
    startWeek = startWeek,
    weekCount = weekCount,
    weekParity = runCatching { WeekParity.valueOf(weekParity) }
        .getOrDefault(WeekParity.ALL),
    location = location,
    teacher = teacher,
    color = color,
    reminderMinutes = reminderMinutes,
    reminderStyle = runCatching { ReminderStyle.valueOf(reminderStyle) }
        .getOrDefault(ReminderStyle.SOUND)
)

fun Course.toEntity(createdAt: Long, updatedAt: Long): CourseEntity = CourseEntity(
    id = id,
    name = name.trim(),
    weekday = weekday,
    startMinute = startMinute,
    endMinute = endMinute,
    startWeek = startWeek,
    weekCount = weekCount,
    weekParity = weekParity.name,
    location = location.trim(),
    teacher = teacher.trim(),
    color = color,
    reminderMinutes = reminderMinutes,
    reminderStyle = reminderStyle.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CourseExceptionEntity.toModel(): CourseException = CourseException(
    courseId = courseId,
    originalDate = LocalDate.parse(originalDate),
    type = runCatching { ExceptionType.valueOf(type) }
        .getOrDefault(ExceptionType.CANCELLED),
    newDate = newDate?.let(LocalDate::parse),
    newStartMinute = newStartMinute,
    newEndMinute = newEndMinute
)

fun CourseException.toEntity(): CourseExceptionEntity = CourseExceptionEntity(
    courseId = courseId,
    originalDate = originalDate.toString(),
    type = type.name,
    newDate = newDate?.toString(),
    newStartMinute = newStartMinute,
    newEndMinute = newEndMinute
)
