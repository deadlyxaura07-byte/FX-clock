package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FocusMode(val title: String, val defaultMinutes: Int) {
    WORK("Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 25,
    val mode: String = FocusMode.WORK.name,
    val isCompleted: Boolean = true,
    val taskTag: String = "General"
)
