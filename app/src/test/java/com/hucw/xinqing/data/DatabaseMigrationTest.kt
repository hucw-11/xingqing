package com.hucw.xinqing.data

import android.content.Context
import com.hucw.xinqing.data.mapper.toModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class DatabaseMigrationTest {
    @Test
    fun `v1 course and exception survive upgrade and reopen`() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("xinqing.db")
        // Other tables are unchanged between v1 and v2. Recreate the exact v1 course table.
        val seed = XinqingDatabase.build(context)
        val sql = seed.openHelper.writableDatabase
        sql.execSQL("DROP TABLE courses")
        sql.execSQL("""CREATE TABLE courses (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            name TEXT NOT NULL, weekday INTEGER NOT NULL,
            startMinute INTEGER NOT NULL, endMinute INTEGER NOT NULL,
            startWeek INTEGER NOT NULL, weekCount INTEGER NOT NULL,
            weekParity TEXT NOT NULL, location TEXT NOT NULL, teacher TEXT NOT NULL,
            color TEXT NOT NULL, reminderMinutes INTEGER NOT NULL,
            reminderStyle TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL
        )""")
        sql.execSQL("CREATE INDEX index_courses_weekday ON courses(weekday)")
        sql.execSQL("INSERT INTO courses VALUES (7, '旧课程', 3, 480, 570, 1, 16, 'ALL', 'A101', '老师', 'sage', 15, 'VIBRATE', 100, 200)")
        sql.execSQL("INSERT INTO course_exceptions (courseId, originalDate, type) VALUES (7, '2026-11-11', 'CANCELLED')")
        sql.execSQL("PRAGMA user_version = 1")
        seed.close()

        val upgraded = XinqingDatabase.build(context)
        try {
            val old = upgraded.courseDao().getById(7)!!
            assertEquals("旧课程", old.name)
            assertEquals("3", old.weekdays)
            assertEquals(listOf(3), old.toModel().weekdays)
            assertEquals(100L, old.createdAt)
            assertEquals(1, upgraded.courseExceptionDao().getAllOnce().size)
            upgraded.courseDao().update(old.copy(weekdays = "1,3", weekday = 1))
        } finally { upgraded.close() }
        val reopened = XinqingDatabase.build(context)
        try {
            assertEquals(listOf(1, 3), reopened.courseDao().getById(7)!!.toModel().weekdays)
        } finally {
            reopened.close()
            context.deleteDatabase("xinqing.db")
        }
    }
}
