package com.example.chronos.notifications

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Reminder"
        val message = inputData.getString(KEY_MESSAGE) ?: ""
        Log.d("ReminderWorker", "Showing notification: $title - $message")
        try {
            Toast.makeText(applicationContext, "Reminder: $title", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {}
        NotificationHelper.showNotification(applicationContext, title, message)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
    }
} 