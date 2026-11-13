package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "course_exceptions",
    indices = [Index("courseId")]
)
data class CourseExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val originalDate: String,
    val type: String,
    val newDate: String? = null,
    val newStartMinute: Int? = null,
    val newEndMinute: Int? = null
)
