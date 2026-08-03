package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.model.*
import com.example.myapplication.ui.components.AvatarPreviewDialog
import com.example.myapplication.ui.components.NativeAvatarCropper
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.UiEvent
import java.io.File
import java.io.FileOutputStream
import java.util.*

import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenArchives: () -> Unit,
    onOpenHunterArchive: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenTitles: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTodayTraining: () -> Unit,
    onOpenCustomTraining: () -> Unit,
    onOpenHunterNotes: () -> Unit,
    onOpenHunterJourney: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val quests by viewModel.dailyQuests.collectAsState()
    val weeklyXp by viewModel.weeklyXp.collectAsState()
    val latestAchievement by viewModel.latestAchievement.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()

    var avatarUpdateKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAvatarPreview by remember { mutableStateOf(false) }
    var cropUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    // Safe internal file copying for avatar persistence
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            cropUri = selectedUri
        }
    }

    ExorkTheme {
        Scaffold(
            containerColor = ObsidianVoid,
            bottomBar = {
                ExorkNavigationBar(
                    modifier = Modifier.padding(bottom = 24.dp),
                    containerColor = ObsidianVoid.copy(alpha = 0.8f),
                    borderColor = ChromeSilver.copy(alpha = 0.4f)
                ) {
                    ExorkNavItem(Icons.Default.Home, "HOME", true) { }
                    ExorkNavItem(Icons.AutoMirrored.Filled.LibraryBooks, "ARCHIVES", false) {
                        soundManager.playClick()
                        onOpenArchives()
                    }
                    ExorkNavItem(Icons.Default.Person, "PROFILE", false) {
                        soundManager.playClick()
                        onOpenProfile()
                    }
                    ExorkNavItem(Icons.Default.Settings, "SETTINGS", false) {
                        soundManager.playClick()
                        onOpenSettings()
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianVoid)
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        end = paddingValues.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                    )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 20.dp,
                        bottom = 120.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Hunter Header
                    item {
                        ExorkProfileHeader(
                            user = user,
                            avatarUri = avatarUri,
                            updateKey = avatarUpdateKey,
                            onAvatarClick = { 
                                soundManager.playClick()
                                showAvatarPreview = true 
                            }
                        )
                    }

                    // 2. Featured Daily Mission
                    val topQuest = quests.firstOrNull { !it.isCompleted }
                    if (topQuest != null) {
                        item {
                            ExorkNeumorphicSectionHeader(title = "Featured Mission")
                            Spacer(modifier = Modifier.height(4.dp))
                            ExorkNeumorphicCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(topQuest.title.uppercase(), style = ExorkTypography.labelLarge, color = Color.White)
                                        Text(topQuest.goal, style = ExorkTypography.headlineSmall, color = TitaniumGray)
                                    }
                                    ExorkChromeButton(
                                        text = "+${topQuest.xpReward} XP",
                                        onClick = {
                                            viewModel.completeQuest(topQuest.id)
                                            soundManager.playQuestComplete()
                                        },
                                        height = 40.dp
                                    )
                                }
                            }
                        }
                    }

                    // 3. Primary CTA
                    item {
                        ExorkChromeButton(
                            text = "TODAY'S TRAINING",
                            onClick = {
                                soundManager.playClick()
                                onOpenTodayTraining()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 5. Operation Hub
                    item {
                        ExorkNeumorphicSectionHeader(title = "Operation Hub")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ExorkNeumorphicHubCard(
                                title = "Custom\nTraining",
                                icon = Icons.Default.FitnessCenter,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenCustomTraining()
                                }
                            )
                            ExorkNeumorphicHubCard(
                                title = "Hunter\nNotes",
                                icon = Icons.Default.AutoStories,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenHunterNotes()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ExorkNeumorphicHubCard(
                                title = "Hunter\nJourney",
                                icon = Icons.Default.Map,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenHunterJourney()
                                }
                            )
                            ExorkNeumorphicHubCard(
                                title = "Hunter\nStats",
                                icon = Icons.Default.Analytics,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenStatistics()
                                }
                            )
                        }
                    }

                    // 5.1 Hunter Vault (Refactored Layout)
                    item {
                        ExorkNeumorphicSectionHeader(title = "Hunter Vault")
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Tier 1: 50/50 Split for Archive and Titles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExorkNeumorphicHubCard(
                                title = "Archive",
                                icon = Icons.Default.Inventory2,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenHunterArchive()
                                }
                            )
                            ExorkNeumorphicHubCard(
                                title = "Titles",
                                icon = Icons.Default.MilitaryTech,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    soundManager.playClick()
                                    onOpenTitles()
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Tier 2: Full Width Achievements
                        ExorkNeumorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                soundManager.playClick()
                                onOpenAchievements()
                            },
                            cornerRadius = 24.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = ChromeSilver,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "ACHIEVEMENTS",
                                            style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            "View Unlocked Badges & Milestones",
                                            style = ExorkTypography.labelSmall,
                                            color = TitaniumGray
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TitaniumGray
                                )
                            }
                        }
                    }

                    // 6. Hunter Stats Summary
                    item {
                        ExorkNeumorphicSectionHeader(title = "Hunter Status")
                        Spacer(modifier = Modifier.height(4.dp))
                        ExorkNeumorphicStatCard(
                            label = "Current Streak",
                            value = "${user.streak} DAYS",
                            icon = Icons.Default.LocalFireDepartment
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ExorkNeumorphicStatCard(
                                label = "Total XP",
                                value = user.totalXpEarned.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            ExorkNeumorphicStatCard(
                                label = "Weekly XP",
                                value = weeklyXp.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 7. Achievement Preview
                    item {
                        ExorkNeumorphicSectionHeader(title = "Latest Achievement")
                        Spacer(modifier = Modifier.height(4.dp))
                        ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                            if (latestAchievement != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .background(ObsidianVoid, CircleShape)
                                            .border(1.5.dp, ChromeSilver, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏆", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column {
                                        Text(latestAchievement!!.name.uppercase(), style = ExorkTypography.titleLarge, color = Color.White)
                                        Text("ACHIEVEMENT UNLOCKED", style = ExorkTypography.labelMedium, color = TitaniumGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    latestAchievement!!.description,
                                    style = ExorkTypography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                ExorkNeumorphicProgressBar(
                                    progress = 1f,
                                    label = "STATUS",
                                    subLabel = "COMPLETED"
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .background(ObsidianVoid, CircleShape)
                                            .border(1.5.dp, ChromeSilver, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔒", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column {
                                        Text("NO ACHIEVEMENT YET", style = ExorkTypography.titleLarge, color = Color.White)
                                        Text("Every Hunter starts from E-Rank.", style = ExorkTypography.labelMedium, color = TitaniumGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Complete your first workout to unlock your first achievement.",
                                    style = ExorkTypography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                ExorkNeumorphicProgressBar(
                                    progress = 0f,
                                    label = "PROGRESS",
                                    subLabel = "0 / 1 WORKOUT"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAvatarPreview) {
            AvatarPreviewDialog(
                avatarUri = avatarUri,
                updateKey = avatarUpdateKey,
                onDismiss = { showAvatarPreview = false },
                onEdit = {
                    showAvatarPreview = false
                    photoPickerLauncher.launch("image/*")
                },
                onRemove = {
                    showAvatarPreview = false
                    try {
                        val file = File(context.filesDir, "custom_avatar.jpg")
                        if (file.exists()) file.delete()
                    } catch (e: Exception) {}
                    viewModel.updateAvatar(null)
                    avatarUpdateKey = System.currentTimeMillis()
                    soundManager.playClick()
                }
            )
        }

        if (cropUri != null) {
            NativeAvatarCropper(
                imageUri = cropUri!!,
                onCropSaved = { 
                    viewModel.updateAvatar(it.path)
                    avatarUpdateKey = System.currentTimeMillis()
                    cropUri = null
                },
                onDismiss = { cropUri = null }
            )
        }
    }
}

@Composable
fun ExorkNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                contentDescription = label
            }
    ) {
        Icon(
            icon,
            null,
            tint = if (isSelected) ChromeSilver else TitaniumGray,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 2.dp)
                    .background(ChromeSilver, CircleShape)
            )
        }
    }
}

