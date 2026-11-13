package com.hucw.xinqing.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun stringListToString(value: List<String>?): String =
        value?.joinToString(SEPARATOR).orEmpty()

    @TypeConverter
    fun stringToStringList(value: String?): List<String> =
        value?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()

    private companion object {
        const val SEPARATOR = "\u0001"
    }
}
