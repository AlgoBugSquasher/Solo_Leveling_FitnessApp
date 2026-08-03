package com.example.myapplication.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.UiEvent
import kotlinx.coroutines.*
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
    val coroutineScope = rememberCoroutineScope()

    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreData by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val data = viewModel.exportData()
                    if (data.isNotBlank()) {
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                OutputStreamWriter(outputStream).use { writer ->
                                    writer.write(data)
                                }
                            }
                        }
                        snackbarHostState.showSnackbar("Backup Saved Successfully")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Save Failed: ${e.message}")
                }
            }
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
        Dialog(
            onDismissRequest = { showRestoreConfirm = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xCC0D0D12),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(ChromeSilver, Color(0xFF3A3A3E))
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Restore Progress?",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            shadow = Shadow(Color.Black, blurRadius = 8f)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "This will overwrite your current progress with the data from the backup file. This action cannot be undone.",
                        color = TitaniumGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CANCEL BUTTON
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { 
                                    soundManager.playClick()
                                    showRestoreConfirm = false 
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, ChromeSilver.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("CANCEL", color = TitaniumGray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                        
                        // RESTORE BUTTON
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable {
                                    soundManager.playSystemAcceptSound()
                                    pendingRestoreData?.let { viewModel.importData(it) }
                                    showRestoreConfirm = false
                                    pendingRestoreData = null
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(ChromeSilver, Color(0xFF4A4A50)))
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("RESTORE", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }
        }
    }

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
        containerColor = ObsidianVoid
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    bottom = 120.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
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
        color = ChromeSilver,
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
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = if (checked) ChromeSilver else TitaniumGray)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, color = TitaniumGray, fontSize = 12.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ChromeSilver,
                    checkedTrackColor = ChromeSilver.copy(alpha = 0.3f),
                    uncheckedThumbColor = TitaniumGray,
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
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = ChromeSilver)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TitaniumGray, fontSize = 12.sp)
            }
        }
    }
}
