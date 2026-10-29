package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: String = "1,2,3,4,5", // 1=Sun, 2=Mon... Calendar constants or day indices (1..7)
    val snoozeDurationMinutes: Int = 5,
    val soundUri: String = "default",
    val isVibrateEnabled: Boolean = true,
    val isOneTime: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getRepeatDaySet(): Set<Int> {
        if (repeatDays.isBlank()) return emptySet()
        return repeatDays.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    fun formattedTime(is24Hour: Boolean = false): String {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            String.format("%02d:%02d", h, minute)
        }
    }

    fun amPm(): String {
        return if (hour < 12) "AM" else "PM"
    }

    fun getNextRingTimeMillis(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysSet = getRepeatDaySet()
        if (daysSet.isEmpty() || isOneTime) {
            // One-time or no repeat
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // Repeating on specific days
        for (i in 0..7) {
            val checkCal = (target.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK)
            if (daysSet.contains(dayOfWeek)) {
                if (checkCal.timeInMillis > now.timeInMillis) {
                    return checkCal.timeInMillis
                }
            }
        }

        target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }

    fun getRemainingTimeDescription(): String {
        if (!isEnabled) return "Off"
        val nextRing = getNextRingTimeMillis()
        val diff = nextRing - System.currentTimeMillis()
        if (diff <= 0) return "Due now"
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        return when {
            hours > 0 && minutes > 0 -> "In ${hours}h ${minutes}m"
            hours > 0 -> "In ${hours}h"
            minutes > 0 -> "In ${minutes}m"
            else -> "In less than 1m"
        }
    }
}
