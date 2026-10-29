package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AlarmItem
import com.example.data.model.FocusSession
import com.example.data.model.WorldClockCity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AlarmItem::class, FocusSession::class, WorldClockCity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun focusDao(): FocusDao
    abstract fun worldClockDao(): WorldClockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fakeexx_clock_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).worldClockDao()
                            val defaultCities = listOf(
                                WorldClockCity("Asia/Tokyo", "Tokyo", "Japan", "Asia/Tokyo", 0),
                                WorldClockCity("Europe/London", "London", "United Kingdom", "Europe/London", 1),
                                WorldClockCity("America/New_York", "New York", "United States", "America/New_York", 2),
                                WorldClockCity("Europe/Paris", "Paris", "France", "Europe/Paris", 3),
                                WorldClockCity("Asia/Dubai", "Dubai", "UAE", "Asia/Dubai", 4),
                                WorldClockCity("Australia/Sydney", "Sydney", "Australia", "Australia/Sydney", 5),
                                WorldClockCity("America/Los_Angeles", "San Francisco", "United States", "America/Los_Angeles", 6),
                                WorldClockCity("Asia/Singapore", "Singapore", "Singapore", "Asia/Singapore", 7),
                                WorldClockCity("Europe/Zurich", "Zurich", "Switzerland", "Europe/Zurich", 8)
                            )
                            dao.insertCities(defaultCities)

                            // Default sample alarm
                            val alarmDao = getDatabase(context).alarmDao()
                            alarmDao.insertAlarm(
                                AlarmItem(
                                    hour = 7,
                                    minute = 0,
                                    label = "Morning Rise",
                                    isEnabled = true,
                                    repeatDays = "2,3,4,5,6"
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
