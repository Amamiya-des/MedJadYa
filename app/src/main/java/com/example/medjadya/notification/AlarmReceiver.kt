package com.example.medjadya.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.medjadya.MainActivity
import com.example.medjadya.model.TimeSlot
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("MED_NAME") ?: "ยาของคุณ"
        val timeStr = intent.getStringExtra("TIME") ?: ""
        val isHourly = intent.getBooleanExtra("IS_HOURLY", false)
        val interval = intent.getIntExtra("INTERVAL", 0)
        val type = intent.getStringExtra("NOTIFICATION_TYPE")

        Log.i("AlarmReceiver", "onReceive: type=$type, medName=$medName")

        if (type == "REFILL") {
            showRefillNotification(context, medName)
            return
        }

        showMedicationNotification(context, medName, timeStr, isHourly)

        if (isHourly && interval > 0) {
            rescheduleNextHourlyAlarm(context, medName, interval)
        }
    }

    private fun showMedicationNotification(context: Context, medName: String, timeStr: String, isHourly: Boolean) {
        val channelId = "med_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "แจ้งเตือนทานยา", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "แจ้งเตือนเมื่อถึงเวลาทานยา"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

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

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NAVIGATE_TO", "medication_list/$slot?medName=$medName")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, Math.abs(medName.hashCode() + slot.hashCode()), activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ได้เวลาทานยาแล้ว!")
            .setContentText("อย่าลืมทานยา: $medName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(Math.abs(medName.hashCode()), builder.build())
    }

    private fun rescheduleNextHourlyAlarm(context: Context, medName: String, interval: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentTime = System.currentTimeMillis()
        while (nextTime.timeInMillis <= currentTime + 1000) {
            nextTime.add(Calendar.HOUR_OF_DAY, interval)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MED_NAME", medName)
            putExtra("TIME", String.format("%02d:00", nextTime.get(Calendar.HOUR_OF_DAY)))
            putExtra("IS_HOURLY", true)
            putExtra("INTERVAL", interval)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, Math.abs((medName + "hourly").hashCode()), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Reschedule failed: ${e.message}")
        }
    }

    companion object {
        fun showRefillNotification(context: Context, medName: String) {
            Log.i("AlarmReceiver", "Attempting to show Refill Notification for $medName")
            
            // 1. Check if notifications are enabled for the app
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                Log.e("AlarmReceiver", "Notifications are DISABLED for this app in system settings")
                return
            }

            // 2. Check POST_NOTIFICATIONS permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("AlarmReceiver", "POST_NOTIFICATIONS permission NOT granted")
                    return
                }
            }

            val channelId = "refill_alert_channel_v3" // Versioned to ensure high importance
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "แจ้งเตือนเติมยา", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "แจ้งเตือนเมื่อยาของคุณเหลือน้อย"
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NAVIGATE_TO", "refill_alerts_screen")
            }

            val pendingIntent = PendingIntent.getActivity(
                context, Math.abs(medName.hashCode() + 1234), activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("⚠️ ยาใกล้หมดแล้ว!")
                .setContentText("ยา $medName ของคุณเหลือน้อยแล้ว กรุณาเติมยาให้เรียบร้อย")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, false) // Increase visibility

            val notificationId = Math.abs(medName.hashCode() + 777)
            notificationManager.notify(notificationId, builder.build())
            Log.i("AlarmReceiver", "Notification posted: ID=$notificationId, Med=$medName")
        }

        fun sendRefillNotification(context: Context, medName: String) {
            showRefillNotification(context, medName)
        }
    }
}
