package com.example.chronos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronos.data.Reminder
import com.example.chronos.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ReminderViewModel(
    private val repository: ReminderRepository = ReminderRepository()
) : ViewModel() {
    private val _reminderAdded = MutableStateFlow(false)
    val reminderAdded: StateFlow<Boolean> = _reminderAdded

    fun addReminder(title: String, dateTime: Long, notes: String?, imageUrl: String?) {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            dateTime = dateTime,
            notes = notes,
            imageUrl = imageUrl
        )
        viewModelScope.launch {
            repository.addReminder(reminder)
            _reminderAdded.value = true
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            _reminderAdded.value = true
        }
    }

    fun resetReminderAdded() {
        _reminderAdded.value = false
    }
} 