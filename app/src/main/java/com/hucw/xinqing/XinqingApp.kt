package com.hucw.xinqing

import android.app.Application
import com.hucw.xinqing.di.AppContainer
import com.hucw.xinqing.reminder.Notifications
import kotlinx.coroutines.launch

class XinqingApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.ensureChannels(this)
        container.appScope.launch { container.alarmScheduler.rescheduleAll() }
    }
}
