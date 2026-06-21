package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationHelper {
    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Reminder 1: 12:00 PM
        val intent1 = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Daily Budget Check")
            putExtra("message", "Time to log your morning expenses and stay on track!")
        }
        val pendingIntent1 = PendingIntent.getBroadcast(
            context,
            1001,
            intent1,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar1 = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar1.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent1
        )

        // Reminder 2: 8:00 PM
        val intent2 = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "Evening Budget Check")
            putExtra("message", "Don't forget to log your dinner and evening expenses!")
        }
        val pendingIntent2 = PendingIntent.getBroadcast(
            context,
            1002,
            intent2,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar2.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent2
        )
    }

    fun triggerLiveNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        context.sendBroadcast(intent)
    }
}
