package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class FakeexxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Alarm channel
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "FAKEEXX Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for active FAKEEXX clock alarms"
                enableVibration(true)
                enableLights(true)
            }

            // Timer & Focus channel
            val timerChannel = NotificationChannel(
                CHANNEL_TIMER_ID,
                "FAKEEXX Timer & Focus",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for completed timers and focus sessions"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(timerChannel)
        }
    }

    companion object {
        lateinit var instance: FakeexxApplication
            private set

        const val CHANNEL_ALARM_ID = "fakeexx_alarm_channel"
        const val CHANNEL_TIMER_ID = "fakeexx_timer_channel"
    }
}
