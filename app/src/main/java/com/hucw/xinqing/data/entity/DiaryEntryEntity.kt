package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "diary_entries",
    indices = [Index("entryDate")]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryDate: LocalDate,
    val entryTime: LocalTime,
    val title: String,
    val content: String,
    val mood: Int,
    val tags: List<String>,
    val favorite: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
