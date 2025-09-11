package com.example.madassignment.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // List<String> converters
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}