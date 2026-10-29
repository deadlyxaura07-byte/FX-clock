package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.FakeexxApplication
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import com.example.service.AlarmScheduler
import com.example.service.SoundVibrateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveAlarmState(
    val alarmId: Long,
    val label: String,
    val formattedTime: String,
    val isRinging: Boolean = true
)

object AlarmStateManager {
    private val _ringingAlarm = MutableStateFlow<ActiveAlarmState?>(null)
    val ringingAlarm: StateFlow<ActiveAlarmState?> = _ringingAlarm.asStateFlow()

    fun triggerRinging(state: ActiveAlarmState) {
        _ringingAlarm.value = state
    }

    fun dismissRinging() {
        _ringingAlarm.value = null
        SoundVibrateHelper.stopAlarmSound()
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)

        if (action == AlarmScheduler.ACTION_STOP_ALARM) {
            AlarmStateManager.dismissRinging()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarmId.toInt())
            return
        }

        if (action == AlarmScheduler.ACTION_SNOOZE_ALARM) {
            AlarmStateManager.dismissRinging()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarmId.toInt())

            // Reschedule in snooze duration
            val snoozeMinutes = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE, 5)
            val scheduler = AlarmScheduler(context)
            val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Snoozed Alarm"
            val nextRingMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

            val snoozeAlarmItem = com.example.data.model.AlarmItem(
                id = if (alarmId != 0L) alarmId else System.currentTimeMillis(),
                label = "$label (Snoozed)",
                hour = java.util.Calendar.getInstance().apply { timeInMillis = nextRingMillis }.get(java.util.Calendar.HOUR_OF_DAY),
                minute = java.util.Calendar.getInstance().apply { timeInMillis = nextRingMillis }.get(java.util.Calendar.MINUTE),
                isEnabled = true,
                isOneTime = true
            )
            scheduler.schedule(snoozeAlarmItem)
            return
        }

        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "FAKEEXX Alarm"
        val hour = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_HOUR, 7)
        val minute = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_MINUTE, 0)
        val snoozeMins = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE, 5)
        val shouldVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_ALARM_VIBRATE, true)
        val isOneTime = intent.getBooleanExtra(AlarmScheduler.EXTRA_ALARM_ONE_TIME, false)

        val timeFormatted = String.format("%02d:%02d", hour, minute)

        // 1. Play sound & vibrate
        SoundVibrateHelper.playAlarmSound(context)
        if (shouldVibrate) {
            SoundVibrateHelper.vibrateDevice(context, 2000L)
        }

        // 2. Update state manager for in-app UI
        AlarmStateManager.triggerRinging(
            ActiveAlarmState(
                alarmId = alarmId,
                label = label,
                formattedTime = timeFormatted,
                isRinging = true
            )
        )

        // 3. Post notification with Stop & Snooze intents
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_TAB", "alarms")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = AlarmScheduler.ACTION_STOP_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = AlarmScheduler.ACTION_SNOOZE_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_SNOOZE, snoozeMins)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, FakeexxApplication.CHANNEL_ALARM_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FAKEEXX • $label")
            .setContentText("Alarm ringing for $timeFormatted")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze ($snoozeMins m)", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId.toInt(), notification)

        // 4. If one-time alarm, disable in database
        if (isOneTime && alarmId != 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val existing = db.alarmDao().getAlarmById(alarmId)
                if (existing != null) {
                    db.alarmDao().updateAlarm(existing.copy(isEnabled = false))
                }
            }
        }
    }
}
