package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE isCompleted = 1")
    fun getCompletedSessionCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearHistory()
}
