package com.hucw.xinqing.di

import android.content.Context
import com.hucw.xinqing.data.CourseRepository
import com.hucw.xinqing.data.SettingsStore
import com.hucw.xinqing.data.XinqingDatabase
import com.hucw.xinqing.reminder.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    val database: XinqingDatabase = XinqingDatabase.build(context)
    val settings: SettingsStore = SettingsStore(context)
    val courseRepository: CourseRepository =
        CourseRepository(database.courseDao(), database.courseExceptionDao())
    val alarmScheduler: AlarmScheduler =
        AlarmScheduler(context.applicationContext, database, settings, courseRepository)
    val appScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
