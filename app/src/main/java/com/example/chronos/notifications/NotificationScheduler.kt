package com.example.chronos.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleReminder(context: Context, title: String, message: String, triggerAtMillis: Long) {
        val delay = triggerAtMillis - System.currentTimeMillis()
        if (delay <= 0) return
        val data = Data.Builder()
            .putString(ReminderNotificationWorker.KEY_TITLE, title)
            .putString(ReminderNotificationWorker.KEY_MESSAGE, message)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
} 