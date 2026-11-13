package com.hucw.xinqing.data

import com.hucw.xinqing.data.dao.CourseDao
import com.hucw.xinqing.data.dao.CourseExceptionDao
import com.hucw.xinqing.data.mapper.toEntity
import com.hucw.xinqing.data.mapper.toModel
import com.hucw.xinqing.domain.Course
import com.hucw.xinqing.domain.CourseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class CourseRepository(
    private val courseDao: CourseDao,
    private val exceptionDao: CourseExceptionDao
) {
    fun observeCourses(): Flow<List<Course>> =
        courseDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun getAll(): List<Course> =
        courseDao.getAllOnce().map { it.toModel() }

    suspend fun findById(id: Long): Course? =
        courseDao.getById(id)?.toModel()

    suspend fun save(course: Course) {
        val now = System.currentTimeMillis()
        if (course.id == 0L) {
            courseDao.insert(course.toEntity(createdAt = now, updatedAt = now))
        } else {
            val existing = courseDao.getById(course.id) ?: return
            courseDao.update(
                course.toEntity(createdAt = existing.createdAt, updatedAt = now)
            )
        }
    }

    suspend fun delete(id: Long) {
        courseDao.deleteById(id)
        exceptionDao.deleteForCourse(id)
    }

    suspend fun exceptions(): List<CourseException> =
        exceptionDao.getAllOnce().map { it.toModel() }

    fun observeExceptions(): Flow<List<CourseException>> =
        exceptionDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun replaceExceptions(
        courseId: Long,
        originalDate: LocalDate,
        replacements: List<CourseException>
    ) {
        exceptionDao.deleteForOccurrence(courseId, originalDate.toString())
        replacements.forEach { exceptionDao.insert(it.toEntity()) }
    }
}
