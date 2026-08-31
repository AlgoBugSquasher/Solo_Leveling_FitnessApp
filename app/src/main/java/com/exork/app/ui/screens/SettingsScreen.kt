package com.exork.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.exork.app.ui.theme.*
import com.exork.app.ui.components.HunterAudioSliderRow
import com.exork.app.util.SoundManager
import com.exork.app.data.PreferencesManager
import com.exork.app.viewmodel.AuthViewModel
import com.exork.app.viewmodel.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    onViewAbout: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val soundManager = remember { SoundManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var sfxVolume by remember { mutableStateOf(preferencesManager.getSfxVolume()) }
    var bgmVolume by remember { mutableStateOf(preferencesManager.getBgmVolume()) }
    var voiceVolume by remember { mutableStateOf(preferencesManager.getVoiceVolume()) }
    var hapticsEnabled by remember { mutableStateOf(preferencesManager.isHapticsEnabled()) }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeletionWarning by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var isProcessingDeletion by remember { mutableStateOf(false) }

    val googleReauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = accountTask.result
                val idToken = account?.idToken
                if (idToken != null) {
                    val authResult = authViewModel.reauthenticateWithGoogle(idToken)
                    if (authResult.isSuccess) {
                        // Re-auth success, proceed to schedule deletion
                        scheduleDeletion(viewModel, onLogout, { isProcessingDeletion = it }, context)
                    } else {
                        Toast.makeText(context, "Re-authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
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
                    SettingsSectionTitle("GAME AUDIO / HUNTER PROTOCOLS")
                }
                
                item {
                    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            HunterAudioSliderRow(
                                label = "Master SFX",
                                value = sfxVolume,
                                onValueChange = { 
                                    sfxVolume = it
                                    preferencesManager.setSfxVolume(it)
                                }
                            )

                            HunterAudioSliderRow(
                                label = "Ambient Music",
                                value = bgmVolume,
                                onValueChange = { 
                                    bgmVolume = it
                                    preferencesManager.setBgmVolume(it)
                                }
                            )

                            HunterAudioSliderRow(
                                label = "Voice Alerts",
                                value = voiceVolume,
                                onValueChange = { 
                                    voiceVolume = it
                                    preferencesManager.setVoiceVolume(it)
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "HAPTIC FEEDBACK",
                                        style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text(
                                        "Vibration on interaction",
                                        style = ExorkTypography.labelSmall,
                                        color = TitaniumGray
                                    )
                                }
                                Switch(
                                    checked = hapticsEnabled,
                                    onCheckedChange = {
                                        hapticsEnabled = it
                                        preferencesManager.setHapticsEnabled(it)
                                        if (it) soundManager.playClick()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ElectricCyan,
                                        checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                                        uncheckedThumbColor = TitaniumGray,
                                        uncheckedTrackColor = Color(0xFF1C1C26)
                                    )
                                )
                            }
                        }
                    }
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

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SettingsSectionTitle("ACCOUNT")
                }

                item {
                    // LOGOUT CARD
                    ExorkNeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showLogoutDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = ChromeSilver,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Sign out from this Hunter device", color = TitaniumGray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    // DELETE ACCOUNT CARD
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x11FF3344),
                        border = BorderStroke(1.dp, Color(0x33FF3344)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeletionWarning = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF4455),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DELETE HUNTER ACCOUNT",
                                    style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFFF4455)
                                )
                                Text(
                                    text = "Schedule permanent account removal",
                                    style = ExorkTypography.labelSmall,
                                    color = TitaniumGray
                                )
                            }
                        }
                    }
                }
            }

            if (isProcessingDeletion) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricCyan)
                }
            }
        }

        // LOGOUT DIALOG
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = Color(0xFF0F0F16),
                title = { Text("SYSTEM LOGOUT", color = Color.White, fontWeight = FontWeight.Black) },
                text = { Text("Are you sure you want to end your hunter session?", color = TitaniumGray) },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }) {
                        Text("LOG OUT", color = Color(0xFFFF4455), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("CANCEL", color = TitaniumGray)
                    }
                }
            )
        }

        // DELETION WARNING DIALOG
        if (showDeletionWarning) {
            AlertDialog(
                onDismissRequest = { showDeletionWarning = false },
                containerColor = Color(0xFF0F0F16),
                title = { Text("⚠️ ACCOUNT DELETION", color = Color.White, fontWeight = FontWeight.Black) },
                text = {
                    Column {
                        Text(
                            "Your Hunter account will NOT be deleted immediately.\n\n" +
                            "After verification, your account will be scheduled for permanent deletion in 7 days.\n\n" +
                            "You can cancel the deletion at any time during this period by logging back into your account.",
                            color = TitaniumGray,
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeletionWarning = false
                            showReauthDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4455))
                    ) {
                        Text("CONTINUE", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletionWarning = false }) {
                        Text("CANCEL", color = TitaniumGray)
                    }
                }
            )
        }

        // RE-AUTHENTICATION DIALOG
        if (showReauthDialog) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val isGoogleUser = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
            
            if (isGoogleUser) {
                // GOOGLE RE-AUTH FLOW
                AlertDialog(
                    onDismissRequest = { showReauthDialog = false },
                    containerColor = Color(0xFF0F0F16),
                    title = { Text("VERIFY IDENTITY", color = Color.White, fontWeight = FontWeight.Black) },
                    text = { Text("To proceed with deletion, please re-authenticate with Google.", color = TitaniumGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showReauthDialog = false
                                googleReauthLauncher.launch(authViewModel.getGoogleSignInIntent(context))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("CONTINUE WITH GOOGLE", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReauthDialog = false }) {
                            Text("CANCEL", color = TitaniumGray)
                        }
                    }
                )
            } else {
                // EMAIL RE-AUTH FLOW
                var password by remember { mutableStateOf("") }
                var errorText by remember { mutableStateOf<String?>(null) }
                var isVerifying by remember { mutableStateOf(false) }

                Dialog(onDismissRequest = { if (!isVerifying) showReauthDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F0F16),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("VERIFY IDENTITY", color = Color.White, style = ExorkTypography.titleMedium, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Enter your password to authorize account deletion.", color = TitaniumGray, textAlign = TextAlign.Center, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedTextField(
                                value = firebaseUser?.email ?: "",
                                onValueChange = {},
                                enabled = false,
                                label = { Text("EMAIL") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    disabledContainerColor = Color.Transparent,
                                    disabledTextColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("PASSWORD") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            
                            if (errorText != null) {
                                Text(errorText!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isVerifying = true
                                        val result = authViewModel.reauthenticate(password)
                                        if (result.isSuccess) {
                                            showReauthDialog = false
                                            scheduleDeletion(viewModel, onLogout, { isProcessingDeletion = it }, context)
                                        } else {
                                            errorText = "Verification failed. Check password."
                                        }
                                        isVerifying = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = password.isNotEmpty() && !isVerifying,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4455))
                            ) {
                                if (isVerifying) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                else Text("VERIFY & CONTINUE", fontWeight = FontWeight.Black)
                            }
                            
                            TextButton(onClick = { showReauthDialog = false }, enabled = !isVerifying) {
                                Text("CANCEL", color = TitaniumGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun scheduleDeletion(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    setLoading: (Boolean) -> Unit,
    context: android.content.Context
) {
    setLoading(true)
    val result = viewModel.scheduleAccountDeletion()
    setLoading(false)
    if (result.isSuccess) {
        Toast.makeText(context, "Account scheduled for deletion. Signing out.", Toast.LENGTH_LONG).show()
        onLogout()
    } else {
        val exception = result.exceptionOrNull()
        val error = exception?.message
        if (error == "GUILD_MASTER_ERROR") {
            Toast.makeText(context, "Guild Master detected. Disband your guild first.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Scheduling failed: ${error ?: "Unknown error"}", Toast.LENGTH_LONG).show()
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
