package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [Index("weekday")]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val weekday: Int,
    val startMinute: Int,
    val endMinute: Int,
    val startWeek: Int,
    val weekCount: Int,
    val weekParity: String,
    val location: String,
    val teacher: String,
    val color: String,
    val reminderMinutes: Int,
    val reminderStyle: String,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "''") val weekdays: String = ""
)
