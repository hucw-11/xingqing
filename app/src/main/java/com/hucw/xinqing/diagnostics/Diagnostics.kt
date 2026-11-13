package com.hucw.xinqing.diagnostics

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Diagnostics {
    private const val PREFS = "diagnostics"
    private const val KEY_EVENTS = "events"
    private const val KEY_SCHEDULED_AT = "scheduled_at"
    private const val KEY_TRIGGER_COUNT = "trigger_count"
    private const val MAX_EVENTS = 8

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun record(context: Context, event: String) {
        val p = prefs(context)
        val line = "${timeFormat().format(Date())}  $event"
        val old = p.getString(KEY_EVENTS, "").orEmpty().lines().filter { it.isNotBlank() }
        val merged = (old + line).takeLast(MAX_EVENTS)
        p.edit().putString(KEY_EVENTS, merged.joinToString("\n")).apply()
    }

    fun events(context: Context): List<String> =
        prefs(context).getString(KEY_EVENTS, "").orEmpty().lines().filter { it.isNotBlank() }

    fun setScheduledAt(context: Context, millis: Long) {
        prefs(context).edit().putLong(KEY_SCHEDULED_AT, millis).apply()
    }

    fun scheduledAt(context: Context): Long =
        prefs(context).getLong(KEY_SCHEDULED_AT, 0L)

    fun markTriggered(context: Context) {
        val p = prefs(context)
        p.edit().putInt(KEY_TRIGGER_COUNT, triggerCount(context) + 1).apply()
    }

    fun triggerCount(context: Context): Int =
        prefs(context).getInt(KEY_TRIGGER_COUNT, 0)

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun timeFormat(): SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
}
