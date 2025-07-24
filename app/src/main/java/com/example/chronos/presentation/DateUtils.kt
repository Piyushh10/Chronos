package com.example.chronos.presentation

fun formatDateTime(millis: Long): String {
    return if (millis > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(millis)) else ""
} 