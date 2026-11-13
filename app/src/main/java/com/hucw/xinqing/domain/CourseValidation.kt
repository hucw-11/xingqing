package com.hucw.xinqing.domain

object CourseValidation {
    val reminderOptions = listOf(0, 5, 15, 30, 60)

    fun validate(course: Course): String? {
        if (course.name.isBlank()) return "课程名称不能为空"
        if (course.name.length > 100) return "课程名称不能超过 100 字"
        if (course.weekdays.isEmpty()) return "请至少选择一个上课日"
        if (course.weekdays.any { it !in 1..7 }) return "请选择正确的星期"
        if (course.startMinute < 0 || course.startMinute >= 24 * 60) return "开始时间不合法"
        if (course.endMinute <= course.startMinute) return "结束时间必须晚于开始时间"
        if (course.endMinute > 24 * 60) return "结束时间不合法"
        if (course.startWeek < 1) return "开始周不能小于 1"
        if (course.weekCount < 1) return "持续周数至少为 1"
        if (course.endWeek > 60) return "学期周数超出范围"
        if (course.reminderMinutes !in reminderOptions) return "提醒时间不合法"
        if (course.location.length > 200) return "地点不能超过 200 字"
        if (course.teacher.length > 200) return "老师不能超过 200 字"
        return null
    }
}
