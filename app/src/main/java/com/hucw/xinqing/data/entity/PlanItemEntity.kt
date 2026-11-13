package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "plan_items",
    indices = [Index("date")]
)
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val title: String,
    val done: Boolean,
    val startMinute: Int? = null,
    val endMinute: Int? = null,
    val reminderMinutes: Int? = null
)
