package com.example.chronos.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.chronos.data.Reminder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.example.chronos.data.AIGreetingApi
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chronos.presentation.formatDateTime
import coil.compose.AsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.example.chronos.R
import android.util.Log

@Composable
fun ReminderDetailScreen(
    reminder: Reminder,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (reminder.imageUrl != null && reminder.imageUrl.isNotBlank()) {
            Log.d("ReminderDetailScreen", "Loading image: ${reminder.imageUrl}")
            val painter = rememberAsyncImagePainter(
                model = reminder.imageUrl.trim(),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
            )
            Image(
                painter = painter,
                contentDescription = "Uploaded Image",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(reminder.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(formatDateTime(reminder.dateTime), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (!reminder.notes.isNullOrBlank()) {
            Text(text = reminder.notes.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var isSharing by remember { mutableStateOf(false) }
        var shareError by remember { mutableStateOf<String?>(null) }
        Row {
            Button(onClick = { onEdit(reminder) }) {
                Text("Edit")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { onDelete(reminder) }) {
                Text("Delete")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                isSharing = true
                shareError = null
                coroutineScope.launch {
                    val message = AIGreetingApi.fetchGreeting("write a short motivational message for ${reminder.title}")
                    isSharing = false
                    if (message != null) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.applicationContext.startActivity(shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } else {
                        shareError = "Failed to fetch AI message."
                    }
                }
            }, enabled = !isSharing) {
                Text(if (isSharing) "Sharing..." else "Share AI Message")
            }
        }
        shareError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
} 