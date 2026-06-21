package com.example.myapplication.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onViewAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreData by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val data = viewModel.exportData()
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(data)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val data = reader.readText()
                        pendingRestoreData = data
                        showRestoreConfirm = true
                    }
                }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.BackupSuccess -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.BackupError -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restore Progress?") },
            text = { Text("This will overwrite your current progress with the data from the backup file. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreData?.let { viewModel.importData(it) }
                    showRestoreConfirm = false
                    pendingRestoreData = null
                }) {
                    Text("RESTORE", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestoreConfirm = false
                    pendingRestoreData = null
                }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1F1B24),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager.playClick()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SettingsSectionTitle("AUDIO")
                }
                
                item {
                    SettingsToggleItem(
                        title = "Sound Effects",
                        subtitle = "Play UI and animation sounds",
                        icon = if (user.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        checked = user.soundEnabled,
                        onCheckedChange = { 
                            viewModel.toggleSound()
                            if (!user.soundEnabled) soundManager.playClick()
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SettingsSectionTitle("DATA MANAGEMENT")
                }

                item {
                    SettingsActionItem(
                        title = "Create Backup",
                        subtitle = "Export progress to JSON",
                        icon = Icons.Default.Save,
                        onClick = {
                            soundManager.playClick()
                            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            createBackupLauncher.launch("HunterBackup_$dateStr.json")
                        }
                    )
                }

                item {
                    SettingsActionItem(
                        title = "Restore Backup",
                        subtitle = "Import from previously saved file",
                        icon = Icons.Default.Restore,
                        onClick = {
                            soundManager.playClick()
                            restoreBackupLauncher.launch(arrayOf("application/json"))
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SettingsSectionTitle("APPLICATION")
                }

                item {
                    SettingsActionItem(
                        title = "About",
                        subtitle = "Developer and version info",
                        icon = Icons.Default.Info,
                        onClick = {
                            soundManager.playClick()
                            onViewAbout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFFBB86FC),
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color(0xFF1A1A1A).copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = if (checked) Color(0xFFBB86FC) else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFBB86FC),
                    checkedTrackColor = Color(0xFFBB86FC).copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1A1A1A).copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFBB86FC))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
