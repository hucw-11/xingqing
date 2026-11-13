package com.hucw.xinqing.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hucw.xinqing.XinqingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.applicationContext as XinqingApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.alarmScheduler.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
