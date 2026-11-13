package com.hucw.xinqing.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hucw.xinqing.diagnostics.Diagnostics

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Diagnostics.markTriggered(context)
        Diagnostics.record(context, "闹钟触发")
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE)
        if (title == null) {
            Diagnostics.record(context, "通知内容缺失，未发出")
            return
        }
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        val posted = Notifications.show(context, id, title, text)
        Diagnostics.record(
            context,
            if (posted) "通知已发出" else "通知未发出（权限或渠道被关闭）"
        )
    }

    companion object {
        const val EXTRA_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
    }
}
