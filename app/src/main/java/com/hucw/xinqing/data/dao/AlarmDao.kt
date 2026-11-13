package com.hucw.xinqing.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hucw.xinqing.data.entity.ScheduledAlarmEntity

@Dao
interface AlarmDao {
    @Query("SELECT * FROM scheduled_alarms")
    suspend fun getAll(): List<ScheduledAlarmEntity>

    @Insert
    suspend fun insert(alarm: ScheduledAlarmEntity): Long

    @Query("DELETE FROM scheduled_alarms")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM scheduled_alarms")
    suspend fun count(): Int
}
