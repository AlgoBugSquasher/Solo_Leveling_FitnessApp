package com.exork.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.exork.app.model.Badge
import com.exork.app.model.User
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val unlockedBadges by viewModel.unlockedBadges.collectAsState()
    val highestBadge by viewModel.highestBadge.collectAsState()
    val weeklyXp by viewModel.weeklyXp.collectAsState()
    val totalBadges = viewModel.totalBadges

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Stats", color = Color.White, fontWeight = FontWeight.Bold) },
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
            user?.let { currentUser ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 120.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Header: Player Status
                    item { PlayerStatusHeader(currentUser) }

                    // 2. Main Stats Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("LIFE STATISTICS", color = ChromeSilver, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
                            
                            StatGrid(currentUser, weeklyXp)
                        }
                    }

                    // 3. Progress Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("PROGRESSION", color = ChromeSilver, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
                            
                            ProgressCard("Badge Collection", unlockedBadges.size, totalBadges, "Badges Unlocked")
                            
                            // Achievement Statistics Integration
                            val totalAchievements = com.exork.app.model.AchievementData.allAchievements.size
                            val unlockedAchievements = com.exork.app.model.AchievementData.allAchievements.count { it.isUnlocked(currentUser) }
                            ProgressCard("Achievement Hunter", unlockedAchievements, totalAchievements, "Achievements Unlocked")

                            ProgressCard("Next Level Reach", currentUser.xp, currentUser.xpToNextLevel(), "XP Progress")
                        }
                    }

                    // 4. Achievement Summary
                    item {
                        AchievementSummary(highestBadge, currentUser)
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun PlayerStatusHeader(user: User) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "PLAYER STATUS",
                color = Color.White,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    shadow = Shadow(color = Color.Black, blurRadius = 20f)
                )
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = ChromeSilver.copy(alpha = 0.2f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusItem("Level", user.level.toString(), Color.White)
                StatusItem("Rank", user.rank, ChromeSilver)
                StatusItem("Streak", "${user.streak}d", ChromeSilver)
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TitaniumGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun StatGrid(user: User, weeklyXp: Int) {
    val stats = listOf(
        "Total XP" to user.totalXpEarned,
        "Weekly XP" to weeklyXp,
        "Workouts" to user.totalWorkouts,
        "Best Streak" to user.highestStreak,
        "Promotions" to user.totalPromotions,
        "Pushups" to user.pushups,
        "Pullups" to user.pullups,
        "Plank Sec" to user.plankTime,
        "Distance KM" to user.totalDistanceKm.toInt()
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in stats.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(stats[i].first, stats[i].second, modifier = Modifier.weight(1f))
                if (i + 1 < stats.size) {
                    StatCard(stats[i+1].first, stats[i+1].second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    val animatedValue = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        animatedValue.animateTo(value.toFloat(), animationSpec = tween(750))
    }

    ExorkNeumorphicCard(
        modifier = modifier.height(80.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = TitaniumGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(
                animatedValue.value.toInt().toString(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ProgressCard(title: String, current: Int, total: Int, label: String) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$current / $total", color = ChromeSilver, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            ExorkNeumorphicProgressBar(
                progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f),
                subLabel = label
            )
        }
    }
}

@Composable
fun AchievementSummary(highestBadge: Badge?, user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ACHIEVEMENTS", color = ChromeSilver, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
        
        ExorkNeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(ObsidianVoid, CircleShape)
                        .border(1.5.dp, ChromeSilver.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text("HIGHEST TITLE EARNED", color = TitaniumGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(highestBadge?.name ?: "N/A", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(user.rank.uppercase(), color = ChromeSilver, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (user.activeTitle != null) {
            ExorkNeumorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(ObsidianVoid, CircleShape)
                            .border(1.5.dp, ChromeSilver.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text("EQUIPPED STREAK TITLE", color = TitaniumGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(user.activeTitle.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

