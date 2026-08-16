package com.exork.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.ArchiveHubViewModel
import com.exork.app.viewmodel.ArchiveProgressState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveHubScreen(
    viewModel: ArchiveHubViewModel,
    onViewArchive: () -> Unit,
    onViewAchievements: () -> Unit,
    onViewTitles: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val progressState by viewModel.progressState.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Archives", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            soundManager.playClick()
                            onNavigateBack()
                        }
                    ) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Card
                HunterCollectionCard(progressState)

                Spacer(modifier = Modifier.height(32.dp))

                // Collection Breakdown Section
                Text(
                    "COLLECTION BREAKDOWN",
                    color = ChromeSilver,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExorkNeumorphicHubCard(
                        title = "Badges\n(${progressState.badgesEarned}/${progressState.totalBadges})",
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f),
                        onClick = onViewArchive
                    )
                    ExorkNeumorphicHubCard(
                        title = "Titles\n(${progressState.titlesEarned}/${progressState.totalTitles})",
                        icon = Icons.Default.MilitaryTech,
                        modifier = Modifier.weight(1f),
                        onClick = onViewTitles
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExorkNeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onViewAchievements,
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
                                    "Unlocked: ${progressState.achievementsUnlocked} / ${progressState.totalAchievements}",
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
        }
    }
}

@Composable
fun HunterCollectionCard(state: ArchiveProgressState) {
    val progressAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(state.completionPercentage) {
        progressAnimation.animateTo(
            targetValue = state.completionPercentage / 100f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                "HUNTER COLLECTION",
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    shadow = Shadow(Color.Black, blurRadius = 10f)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            CollectionStatRow("Badges Earned", state.badgesEarned, state.totalBadges)
            CollectionStatRow("Achievements", state.achievementsUnlocked, state.totalAchievements)
            CollectionStatRow("Titles Earned", state.titlesEarned, state.totalTitles)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Collection Completion", color = TitaniumGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(progressAnimation.value * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExorkNeumorphicProgressBar(
                progress = progressAnimation.value
            )
        }
    }
}

@Composable
private fun CollectionStatRow(label: String, current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TitaniumGray, fontSize = 14.sp)
        Text("$current / $total", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

