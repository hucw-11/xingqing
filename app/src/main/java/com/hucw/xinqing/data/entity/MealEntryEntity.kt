package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "meal_entries",
    indices = [Index("date")]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mealType: String,
    val time: LocalTime,
    val note: String,
    val photoPath: String? = null,
    val ateWell: Boolean,
    val createdAt: Instant
)
