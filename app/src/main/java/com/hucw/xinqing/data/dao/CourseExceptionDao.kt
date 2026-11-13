package com.hucw.xinqing.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hucw.xinqing.data.entity.CourseExceptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseExceptionDao {
    @Query("SELECT * FROM course_exceptions")
    suspend fun getAllOnce(): List<CourseExceptionEntity>

    @Query("SELECT * FROM course_exceptions")
    fun observeAll(): Flow<List<CourseExceptionEntity>>

    @Query("DELETE FROM course_exceptions WHERE courseId = :courseId AND originalDate = :originalDate")
    suspend fun deleteForOccurrence(courseId: Long, originalDate: String)

    @Query("SELECT * FROM course_exceptions WHERE courseId = :courseId")
    suspend fun getForCourse(courseId: Long): List<CourseExceptionEntity>

    @Insert
    suspend fun insert(exception: CourseExceptionEntity): Long

    @Update
    suspend fun update(exception: CourseExceptionEntity)

    @Query("DELETE FROM course_exceptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM course_exceptions WHERE courseId = :courseId")
    suspend fun deleteForCourse(courseId: Long)
}
