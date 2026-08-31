package com.exork.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.exork.app.model.*
import com.exork.app.ui.components.AvatarPreviewDialog
import com.exork.app.ui.components.ExorkSystemDialog
import com.exork.app.ui.components.HunterRankDialog
import com.exork.app.ui.components.NativeAvatarCropper
import com.exork.app.ui.components.QuestInfoDialog
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.AnalyticsViewModel
import com.exork.app.viewmodel.HomeViewModel
import com.exork.app.viewmodel.UiEvent
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    analyticsViewModel: AnalyticsViewModel,
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
    onOpenStatistics: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenNetwork: () -> Unit,
    onOpenGuild: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val username by viewModel.username.collectAsState()
    val quests by viewModel.dailyQuests.collectAsState()
    val weeklyXp by viewModel.weeklyXp.collectAsState()
    val latestAchievement by viewModel.latestAchievement.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()
    val isTodayRestDay by viewModel.isTodayRestDay.collectAsState()
    val showRankDialog by viewModel.showRankDialog.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val floatingXpReward by viewModel.floatingXpReward.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    val dashboardListState = rememberLazyListState()
    val analyticsListState = rememberLazyListState()

    val activeScrollValue = if (pagerState.currentPage == 0) {
        dashboardListState.firstVisibleItemScrollOffset
    } else {
        analyticsListState.firstVisibleItemScrollOffset
    }

    val hudAlpha by animateFloatAsState(
        targetValue = if (activeScrollValue > 50) 0.45f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "hudAlpha"
    )

    var avatarUpdateKey by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showAvatarPreview by remember { mutableStateOf(false) }
    var showRemoveAvatarConfirm by remember { mutableStateOf(false) }
    var cropUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager.getInstance(context) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            cropUri = selectedUri
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.XpGained -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                else -> {}
            }
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
                    ExorkNavItem(Icons.Default.Home, "HOME", pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction < 0.5f) {
                        coroutineScope.launch { pagerState.animateScrollToPage(0, animationSpec = tween(180, easing = LinearOutSlowInEasing)) }
                    }
                    ExorkNavItem(Icons.Default.Description, "NOTES", false) {
                        soundManager.playClick()
                        onOpenHunterNotes()
                    }
                    
                    // Center Guild Hub
                    ExorkNavItem(
                        icon = Icons.Default.Shield,
                        label = "GUILD",
                        isSelected = false,
                        isCenterHub = true
                    ) {
                        soundManager.playClick()
                        onOpenGuild()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianVoid)
                    .statusBarsPadding()
            ) {
                // Static Top Toggle with Gliding Indicator
                TopHudSegmentedControl(
                    pagerState = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .graphicsLayer { alpha = hudAlpha }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    userScrollEnabled = true
                ) { page ->
                    if (page == 0) {
                        // DASHBOARD PAGE
                        if (isSyncing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = ElectricCyan)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "SYNCING HUNTER DATA...",
                                        style = ExorkTypography.labelMedium,
                                        color = TitaniumGray,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = dashboardListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 10.dp,
                                    bottom = 120.dp,
                                    start = 20.dp,
                                    end = 20.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // 1. Hunter Header
                                item {
                                    Box(contentAlignment = Alignment.Center) {
                                        ExorkProfileHeader(
                                            user = user,
                                            avatarUri = avatarUri,
                                            updateKey = avatarUpdateKey,
                                            username = username,
                                            onAvatarClick = { 
                                                soundManager.playClick()
                                                showAvatarPreview = true 
                                            },
                                            onRankClick = {
                                                soundManager.playClick()
                                                viewModel.openRankDialog()
                                            }
                                        )

                                        // Floating XP Animation
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = floatingXpReward != null && (floatingXpReward ?: 0) > 0,
                                            enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                                            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                                        ) {
                                            if (floatingXpReward != null && (floatingXpReward ?: 0) > 0) {
                                                LaunchedEffect(floatingXpReward) {
                                                    kotlinx.coroutines.delay(1200)
                                                    viewModel.clearFloatingXp()
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .offset(y = (-40).dp)
                                                        .background(
                                                            Brush.radialGradient(
                                                                listOf(ChromeSilver.copy(alpha = 0.4f), Color.Transparent)
                                                            ),
                                                            CircleShape
                                                        )
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "+$floatingXpReward XP GAINED!",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        style = MaterialTheme.typography.headlineSmall.copy(
                                                            shadow = Shadow(Color.Black, blurRadius = 8f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Daily Quest Section
                                val todayDateString = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                val isQuestDoneToday = user.lastQuestCompletedDate == todayDateString

                                if (quests.isNotEmpty() && quests.any { !it.isCompleted } && !isQuestDoneToday) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ExorkNeumorphicSectionHeader(title = "DAILY QUEST")
                                            if (isTodayRestDay) {
                                                Text(
                                                    "REST DAY (Optional: +1 Streak & +50 XP)",
                                                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = ChromeSilver.copy(alpha = 0.7f),
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ExorkNeumorphicCard {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                quests.forEach { quest ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable(enabled = !quest.isCompleted) {
                                                                soundManager.playClick()
                                                                viewModel.toggleQuestProgress(quest.id)
                                                            },
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                quest.title.uppercase(),
                                                                style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                                                                color = Color.White
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            ExorkNeumorphicProgressBar(
                                                                progress = quest.getProgressPercentage()
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(
                                                            "[ ${quest.targetValue} ]",
                                                            style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = ChromeSilver
                                                        )
                                                    }
                                                }

                                                if (quests.all { it.currentProgress >= it.targetValue && !it.isCompleted }) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    ExorkGlowingChromeButton(
                                                        text = "CLAIM REWARD (+50 XP)",
                                                        onClick = {
                                                            soundManager.playLevelUp()
                                                            viewModel.claimDailyQuestReward()
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        ExorkNeumorphicHubCard(
                                            title = "Global\nLeaderboard",
                                            icon = Icons.Default.EmojiEvents,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                soundManager.playClick()
                                                onOpenLeaderboard()
                                            }
                                        )
                                        ExorkNeumorphicHubCard(
                                            title = "Hunter\nNetwork",
                                            icon = Icons.Default.Group,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                soundManager.playClick()
                                                onOpenNetwork()
                                            }
                                        )
                                    }
                                }

                                // 5.1 Hunter Vault
                                item {
                                    ExorkNeumorphicSectionHeader(title = "Hunter Vault")
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                                    Text("ACHIEVEMENTS", style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                                    Text("View Unlocked Badges & Milestones", style = ExorkTypography.labelSmall, color = TitaniumGray)
                                                }
                                            }
                                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TitaniumGray)
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
                                            Text(latestAchievement!!.description, style = ExorkTypography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
                                            Spacer(modifier = Modifier.height(20.dp))
                                            ExorkNeumorphicProgressBar(progress = 1f, label = "STATUS", subLabel = "COMPLETED")
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
                                            Text("Complete your first workout to unlock your first achievement.", style = ExorkTypography.bodyLarge, color = Color.White.copy(alpha = 0.7f))
                                            Spacer(modifier = Modifier.height(20.dp))
                                            ExorkNeumorphicProgressBar(progress = 0f, label = "PROGRESS", subLabel = "0 / 1 WORKOUT")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ANALYTICS PAGE
                        AnalyticsScreen(
                            viewModel = analyticsViewModel,
                            listState = analyticsListState
                        )
                    }
                }
            }
        }

        if (showAvatarPreview) {
            AvatarPreviewDialog(
                avatarUri = user.photoUrl ?: avatarUri,
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

        if (showRankDialog) {
            HunterRankDialog(user = user, onDismiss = { viewModel.dismissRankDialog() })
        }
    }
}

@Composable
fun ExorkGlowingChromeButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "glowIntensity"
    )
    Surface(
        modifier = modifier.height(58.dp).shadow(16.dp, RoundedCornerShape(12.dp), ambientColor = Color.White.copy(alpha = 0.4f * glowIntensity), spotColor = ChromeSilver.copy(alpha = 0.6f * glowIntensity))
            .border(2.dp, Brush.linearGradient(listOf(ChromeSilver, Color.White.copy(alpha = glowIntensity), ChromeSilver)), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp), color = Color.Transparent
    ) {
        Box(modifier = Modifier.background(Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFF8E8E93), Color(0xFF48484A)))), contentAlignment = Alignment.Center) {
            Text(text = text.uppercase(), style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.5.sp, shadow = Shadow(Color.White.copy(alpha = 0.5f), blurRadius = 8f)))
        }
    }
}

@Composable
fun TopHudSegmentedControl(pagerState: androidx.compose.foundation.pager.PagerState, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = 2.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xD90D0D14), // Translucent Obsidian Glass
            border = BorderStroke(1.dp, Color(0xFF262636).copy(alpha = 0.6f)),
            shadowElevation = 0.dp
        ) {
            BoxWithConstraints(modifier = Modifier.width(260.dp).height(42.dp).padding(3.dp)) {
                val tabWidth = maxWidth / 2
                val indicatorOffset = tabWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))))
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch { pagerState.animateScrollToPage(0, animationSpec = tween(180, easing = LinearOutSlowInEasing)) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "DASHBOARD", style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black), color = if (pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction < 0.5f) Color(0xFF0A0A0E) else Color(0xFF8E8E9E))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch { pagerState.animateScrollToPage(1, animationSpec = tween(180, easing = LinearOutSlowInEasing)) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "ANALYTICS", style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black), color = if (pagerState.currentPage == 1 || pagerState.currentPageOffsetFraction >= 0.5f) Color(0xFF0A0A0E) else Color(0xFF8E8E9E))
                    }
                }
            }
        }
    }
}

@Composable
fun ExorkNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, isCenterHub: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.semantics(mergeDescendants = true) { contentDescription = label }) {
        val tint = if (isCenterHub) ElectricCyan else if (isSelected) ChromeSilver else TitaniumGray
        Box(modifier = if (isCenterHub) Modifier.size(42.dp).border(1.dp, Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.6f), Color.Transparent)), CircleShape).shadow(8.dp, CircleShape, spotColor = ElectricCyan.copy(alpha = 0.4f)) else Modifier, contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(if (isCenterHub) 28.dp else 24.dp))
        }
        if (isSelected && !isCenterHub) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(width = 12.dp, height = 2.dp).background(ChromeSilver, CircleShape))
        }
    }
}
