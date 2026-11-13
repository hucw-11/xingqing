package com.hucw.xinqing

import android.app.Application
import com.hucw.xinqing.reminder.Notifications

class XinqingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannel(this)
    }
}
