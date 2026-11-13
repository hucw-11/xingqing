package com.hucw.xinqing.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class SettingsStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _semester = MutableStateFlow(loadSemester())
    val semester: StateFlow<LocalDate?> = _semester.asStateFlow()

    fun setSemesterStart(date: LocalDate) {
        prefs.edit().putString(KEY_SEMESTER, date.toString()).apply()
        _semester.value = date
    }

    private fun loadSemester(): LocalDate? =
        prefs.getString(KEY_SEMESTER, null)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

    private companion object {
        const val KEY_SEMESTER = "semester_start"
    }
}
