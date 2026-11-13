package com.hucw.xinqing.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "mood_entries",
    indices = [Index("timestamp")]
)
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant,
    val score: Int,
    val tags: List<String>,
    val note: String
)
