package com.hucw.xinqing.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hucw.xinqing.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY weekday, startMinute")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY weekday, startMinute")
    suspend fun getAllOnce(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: Long): CourseEntity?

    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
