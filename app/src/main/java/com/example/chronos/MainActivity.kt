package com.example.chronos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.chronos.data.CloudinaryUploader
import com.example.chronos.ui.theme.ChronosTheme
import java.io.File
import com.example.chronos.presentation.AddReminderScreen
import com.example.chronos.presentation.ReminderViewModel
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import com.example.chronos.presentation.ReminderListScreen
import com.example.chronos.presentation.ReminderListViewModel
import com.example.chronos.presentation.ReminderDetailScreen
import com.example.chronos.data.Reminder
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.chronos.presentation.AuthViewModel
import com.example.chronos.presentation.LoginScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.activity.compose.BackHandler
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Toast.makeText(context, "Please enable notification permission in app settings.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            var showSnackbar by remember { mutableStateOf(false) }
            var snackbarMessage by remember { mutableStateOf("") }
            var themeMode by remember { mutableStateOf(AppThemeMode.SYSTEM) }
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            ChronosTheme(darkTheme = isDarkTheme) {
                val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val user by authViewModel.user.collectAsState()
                if (user == null) {
                    LoginScreen { intent, activity ->
                        authViewModel.onAuthResult(intent, activity)
                    }
                } else {
                    val listViewModel: ReminderListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    val addViewModel: ReminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                    val snackbarHostState = remember { SnackbarHostState() }
                    var navState by remember { mutableStateOf<NavState>(NavState.List) }
                    var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
                    var themeMenuExpanded by remember { mutableStateOf(false) }
                    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
                    BackHandler(enabled = navState != NavState.List) {
                        navState = NavState.List
                    }
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(WindowInsets.systemBars.asPaddingValues()),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.reminderlogo),
                                        contentDescription = "App Logo",
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Chronos",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { themeMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Theme Toggle"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = themeMenuExpanded,
                                        onDismissRequest = { themeMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("System Default") },
                                            onClick = {
                                                themeMode = AppThemeMode.SYSTEM
                                                themeMenuExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Light") },
                                            onClick = {
                                                themeMode = AppThemeMode.LIGHT
                                                themeMenuExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Dark") },
                                            onClick = {
                                                themeMode = AppThemeMode.DARK
                                                themeMenuExpanded = false
                                            }
                                        )
                                    }
                                    TextButton(onClick = { authViewModel.logout() }) {
                                        Text("Logout")
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        when (navState) {
                            is NavState.List -> {
                                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                    ReminderListScreen(listViewModel) { reminder ->
                                        selectedReminder = reminder
                                        navState = NavState.Detail
                                    }
                                    Button(
                                        onClick = { navState = NavState.Add },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(24.dp)
                                    ) {
                                        Text("Add Reminder")
                                    }
                                }
                            }
                            is NavState.Add -> {
                                AddReminderScreen(
                                    viewModel = addViewModel,
                                    onReminderAdded = {
                                        snackbarMessage = if (editingReminder != null) "Reminder updated!" else "Reminder added!"
                                        showSnackbar = true
                                        navState = NavState.List
                                        listViewModel.loadReminders()
                                        editingReminder = null
                                    },
                                    existingReminder = editingReminder,
                                    isEdit = editingReminder != null,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            is NavState.Detail -> {
                                selectedReminder?.let { reminder ->
                                    Box(modifier = Modifier.padding(innerPadding)) {
                                        ReminderDetailScreen(
                                            reminder = reminder,
                                            onEdit = {
                                                editingReminder = it
                                                navState = NavState.Add
                                            },
                                            onDelete = {
                                                val repo = listViewModel
                                                repo.viewModelScope.launch {
                                                    repo.repository.deleteReminder(reminder.id)
                                                    snackbarMessage = "Reminder deleted!"
                                                    showSnackbar = true
                                                    navState = NavState.List
                                                    listViewModel.loadReminders()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (showSnackbar) {
                            LaunchedEffect(snackbarHostState, snackbarMessage) {
                                snackbarHostState.showSnackbar(snackbarMessage)
                                showSnackbar = false
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class NavState {
    object List : NavState()
    object Add : NavState()
    object Detail : NavState()
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ImagePickerDemo() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadedUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        error = null
        if (uri != null) {
            val file = uriToFile(uri, context)
            if (file != null) {
                isUploading = true
                CloudinaryUploader.uploadImage(file) { result ->
                    isUploading = false
                    result.onSuccess { url ->
                        uploadedUrl = url
                    }.onFailure {
                        error = it.message
                    }
                }
            } else {
                error = "Failed to get file from URI"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pick Image")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isUploading) {
            CircularProgressIndicator()
        }
        uploadedUrl?.let { url ->
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Uploaded Image",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it")
        }
    }
}

// Helper to convert Uri to File
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChronosTheme {
        Greeting("Android")
    }
}