package com.example.medjadya.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medjadya.MainActivity
import com.example.medjadya.model.TimeSlot
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("MED_NAME") ?: "ยาของคุณ"
        val timeStr = intent.getStringExtra("TIME") ?: ""
        val isHourly = intent.getBooleanExtra("IS_HOURLY", false)
        val interval = intent.getIntExtra("INTERVAL", 0)

        Log.i("AlarmReceiver", "!!! ALARM TRIGGERED !!! Med: $medName, isHourly: $isHourly, interval: $interval")

        showNotification(context, medName, timeStr, isHourly)

        // If it is an hourly medication, schedule the next one aligned to 8:00 AM
        if (isHourly && interval > 0) {
            rescheduleNextHourlyAlarm(context, medName, interval)
        }
    }

    private fun showNotification(context: Context, medName: String, timeStr: String, isHourly: Boolean) {
        val channelId = "med_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "แจ้งเตือนทานยา",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Determine the TimeSlot for navigation
        val slot = if (isHourly) {
            TimeSlot.HOURLY.name
        } else if (timeStr.isNotEmpty()) {
            val hour = try { timeStr.substringBefore(":").toInt() } catch (e: Exception) { -1 }
            when (hour) {
                in 5..10 -> TimeSlot.MORNING.name
                in 11..14 -> TimeSlot.LUNCH.name
                in 15..19 -> TimeSlot.EVENING.name
                in 20..23, in 0..4 -> TimeSlot.BEFORE_BED.name
                else -> TimeSlot.MORNING.name
            }
        } else {
            TimeSlot.MORNING.name
        }

        // Deep link with medName parameter
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "medication_list/$slot?medName=$medName")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medName.hashCode() + slot.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ได้เวลาทานยาแล้ว!")
            .setContentText("อย่าลืมทานยา: $medName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(medName.hashCode(), builder.build())
    }

    private fun rescheduleNextHourlyAlarm(context: Context, medName: String, interval: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Always calculate from 8:00 AM anchor to prevent drift
        val nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val currentTime = System.currentTimeMillis()
        
        // Find the next aligned slot after current time (plus 1 second buffer to avoid immediate re-trigger)
        while (nextTime.timeInMillis <= currentTime + 1000) {
            nextTime.add(Calendar.HOUR_OF_DAY, interval)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MED_NAME", medName)
            putExtra("TIME", String.format("%02d:00", nextTime.get(Calendar.HOUR_OF_DAY)))
            putExtra("IS_HOURLY", true)
            putExtra("INTERVAL", interval)
        }

        val requestCode = (medName + "hourly").hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.i("AlarmReceiver", ">>> Rescheduling next hourly alarm for $medName at ${nextTime.time}")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to reschedule hourly alarm: ${e.message}")
        }
    }
}
