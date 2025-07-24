package com.example.chronos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chronos.data.Reminder
import com.example.chronos.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReminderListViewModel(
    internal val repository: ReminderRepository = ReminderRepository()
) : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders

    fun loadReminders() {
        viewModelScope.launch {
            _reminders.value = repository.getReminders()
        }
    }
} 