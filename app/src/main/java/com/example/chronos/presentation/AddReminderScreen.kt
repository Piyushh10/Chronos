package com.example.chronos.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.chronos.data.CloudinaryUploader
import java.io.File
import java.util.*
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.chronos.notifications.NotificationScheduler
import android.widget.Toast
import com.example.chronos.data.Reminder
import androidx.compose.ui.res.painterResource
import com.example.chronos.R
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    onReminderAdded: () -> Unit,
    existingReminder: Reminder? = null,
    isEdit: Boolean = false,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(existingReminder?.title ?: "") }
    var notes by remember { mutableStateOf(existingReminder?.notes ?: "") }
    var dateTime by remember { mutableStateOf(existingReminder?.dateTime ?: 0L) }
    var dateTimeDisplay by remember { mutableStateOf(if (existingReminder != null) formatDateTime(existingReminder.dateTime) else "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf(existingReminder?.imageUrl) }
    var isUploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val reminderAdded by viewModel.reminderAdded.collectAsState()

    LaunchedEffect(reminderAdded) {
        if (reminderAdded) {
            onReminderAdded()
            viewModel.resetReminderAdded()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        error = null
        if (uri != null) {
            val file = uriToFile(uri, context)
            if (file != null) {
                isUploading = true
                CloudinaryUploader.uploadImage(file) { result ->
                    isUploading = false
                    result.onSuccess { url ->
                        imageUrl = url
                    }.onFailure {
                        error = it.message
                    }
                }
            } else {
                error = "Failed to get file from URI"
            }
        }
    }

    if (reminderAdded) {
        onReminderAdded()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            showDateTimePicker(context) { millis, display ->
                dateTime = millis
                dateTimeDisplay = display
            }
        }, enabled = !isUploading) {
            Text(if (dateTimeDisplay.isEmpty()) "Pick Date & Time" else dateTimeDisplay)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { imagePickerLauncher.launch("image/*") }, enabled = !isUploading) {
            Text("Pick Image")
        }
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }
        imageUrl?.let { url ->
            Spacer(modifier = Modifier.height(8.dp))
            val painter = rememberAsyncImagePainter(
                model = url,
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
            )
            Image(
                painter = painter,
                contentDescription = "Uploaded Image",
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Log.d("AddReminderScreen", "Saving reminder with imageUrl: $imageUrl")
                if (isEdit && existingReminder != null) {
                    val updated = existingReminder.copy(
                        title = title,
                        dateTime = dateTime,
                        notes = notes.ifBlank { null },
                        imageUrl = imageUrl
                    )
                    viewModel.updateReminder(updated)
                } else {
                    viewModel.addReminder(title, dateTime, notes.ifBlank { null }, imageUrl)
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    if (dateTime > System.currentTimeMillis()) {
                        NotificationScheduler.scheduleReminder(
                            context = context,
                            title = title,
                            message = notes,
                            triggerAtMillis = dateTime
                        )
                        Toast.makeText(context, "Reminder scheduled!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Reminder time must be in the future", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = title.isNotBlank() && dateTime > 0 && !isUploading && (imageUrl != null || imageUri == null)
        ) {
            Text(if (isEdit) "Update Reminder" else "Save Reminder")
        }
    }
}

fun uriToFile(uri: Uri, context: android.content.Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("picked_image", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        tempFile
    } catch (e: Exception) {
        null
    }
}

fun showDateTimePicker(context: android.content.Context, onDateTimeSelected: (Long, String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(context, { _, year, month, day ->
        TimePickerDialog(context, { _, hour, minute ->
            calendar.set(year, month, day, hour, minute)
            val millis = calendar.timeInMillis
            val display = "%04d-%02d-%02d %02d:%02d".format(year, month + 1, day, hour, minute)
            onDateTimeSelected(millis, display)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
} 