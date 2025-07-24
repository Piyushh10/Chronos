package com.example.chronos.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.chronos.data.Reminder
import androidx.compose.ui.res.painterResource
import com.example.chronos.R
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.draw.clip

@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onReminderClick: (Reminder) -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadReminders() }
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        items(reminders) { reminder ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onReminderClick(reminder) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (!reminder.imageUrl.isNullOrBlank()) {
                        Log.d("ReminderListScreen", "Loading image: ${reminder.imageUrl}")
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = reminder.imageUrl.trim(),
                                error = painterResource(id = R.drawable.ic_launcher_foreground),
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatDateTime(reminder.dateTime),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
} 