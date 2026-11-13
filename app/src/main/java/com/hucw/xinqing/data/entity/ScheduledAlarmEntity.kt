package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_alarms",
    indices = [Index("triggerAtMillis")]
)
data class ScheduledAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val occurrenceDate: String,
    val triggerAtMillis: Long
)
