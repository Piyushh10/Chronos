package com.example.chronos.data

data class Reminder(
    val id: String = "",
    val title: String = "",
    val dateTime: Long = 0L,
    val notes: String? = null,
    val imageUrl: String? = null
) 