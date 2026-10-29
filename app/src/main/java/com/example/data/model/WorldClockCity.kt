package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Entity(tableName = "world_cities")
data class WorldClockCity(
    @PrimaryKey
    val id: String, // e.g. "Asia/Tokyo"
    val cityName: String,
    val country: String,
    val timeZoneId: String,
    val displayOrder: Int = 0
) {
    fun getZonedDateTime(): ZonedDateTime {
        return try {
            ZonedDateTime.now(ZoneId.of(timeZoneId))
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
    }

    fun getFormattedTime(is24Hour: Boolean = false): String {
        val dt = getZonedDateTime()
        val pattern = if (is24Hour) "HH:mm" else "hh:mm"
        return dt.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun getAmPm(is24Hour: Boolean = false): String {
        if (is24Hour) return ""
        val dt = getZonedDateTime()
        return dt.format(DateTimeFormatter.ofPattern("a", Locale.getDefault()))
    }

    fun getOffsetString(): String {
        return try {
            val zone = ZoneId.of(timeZoneId)
            val offset = zone.rules.getOffset(Instant.now())
            val totalSeconds = offset.totalSeconds
            val hours = totalSeconds / 3600
            val minutes = Math.abs((totalSeconds % 3600) / 60)
            if (hours >= 0) {
                if (minutes > 0) String.format("UTC+%d:%02d", hours, minutes)
                else String.format("UTC+%d", hours)
            } else {
                if (minutes > 0) String.format("UTC%d:%02d", hours, minutes)
                else String.format("UTC%d", hours)
            }
        } catch (e: Exception) {
            "UTC"
        }
    }

    fun getDayDifference(): String {
        val here = ZonedDateTime.now()
        val there = getZonedDateTime()
        val hereDay = here.dayOfYear
        val thereDay = there.dayOfYear
        val hereYear = here.year
        val thereYear = there.year

        return when {
            thereYear > hereYear || (thereYear == hereYear && thereDay > hereDay) -> "Tomorrow"
            thereYear < hereYear || (thereYear == hereYear && thereDay < hereDay) -> "Yesterday"
            else -> "Today"
        }
    }
}
