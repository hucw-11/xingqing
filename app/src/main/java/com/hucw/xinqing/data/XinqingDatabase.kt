package com.hucw.xinqing.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hucw.xinqing.data.dao.AlarmDao
import com.hucw.xinqing.data.dao.CourseDao
import com.hucw.xinqing.data.dao.CourseExceptionDao
import com.hucw.xinqing.data.entity.CourseEntity
import com.hucw.xinqing.data.entity.CourseExceptionEntity
import com.hucw.xinqing.data.entity.DiaryEntryEntity
import com.hucw.xinqing.data.entity.MealEntryEntity
import com.hucw.xinqing.data.entity.MoodEntryEntity
import com.hucw.xinqing.data.entity.PlanItemEntity
import com.hucw.xinqing.data.entity.ScheduledAlarmEntity

@Database(
    entities = [
        CourseEntity::class,
        CourseExceptionEntity::class,
        ScheduledAlarmEntity::class,
        DiaryEntryEntity::class,
        MoodEntryEntity::class,
        PlanItemEntity::class,
        MealEntryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class XinqingDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun courseExceptionDao(): CourseExceptionDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN weekdays TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE courses SET weekdays = CAST(weekday AS TEXT)")
            }
        }

        fun build(context: Context): XinqingDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                XinqingDatabase::class.java,
                "xinqing.db"
            ).addMigrations(MIGRATION_1_2).build()
    }
}
