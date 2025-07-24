package com.example.chronos.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReminderRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val reminders = db.collection("reminders")

    suspend fun addReminder(reminder: Reminder) {
        reminders.document(reminder.id).set(reminder).await()
    }

    suspend fun getReminders(): List<Reminder> {
        return reminders.get().await().toObjects(Reminder::class.java)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminders.document(reminder.id).set(reminder).await()
    }

    suspend fun deleteReminder(id: String) {
        reminders.document(id).delete().await()
    }
} 