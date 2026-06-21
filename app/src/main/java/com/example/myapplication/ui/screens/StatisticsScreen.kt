package com.example.myapplication.ui.screens

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
import com.example.myapplication.util.SoundManager
import com.example.myapplication.model.Badge
import com.example.myapplication.model.User
import com.example.myapplication.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val unlockedBadges by viewModel.unlockedBadges.collectAsState()
    val highestBadge by viewModel.highestBadge.collectAsState()
    val totalBadges = viewModel.totalBadges

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

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
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            user?.let { currentUser ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Header: Player Status
                    item { PlayerStatusHeader(currentUser) }

                    // 2. Main Stats Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("LIFE STATISTICS", color = Color(0xFFBB86FC), fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
                            
                            StatGrid(currentUser)
                        }
                    }

                    // 3. Progress Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("PROGRESSION", color = Color(0xFFBB86FC), fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
                            
                            ProgressCard("Badge Collection", unlockedBadges.size, totalBadges, "Badges Unlocked")
                            
                            // Achievement Statistics Integration
                            val totalAchievements = com.example.myapplication.model.AchievementData.allAchievements.size
                            val unlockedAchievements = com.example.myapplication.model.AchievementData.allAchievements.count { it.isUnlocked(currentUser) }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "PLAYER STATUS",
                color = Color.White,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    shadow = Shadow(color = Color(0xFFBB86FC), blurRadius = 20f)
                )
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFBB86FC).copy(alpha = 0.3f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusItem("Level", user.level.toString(), Color.White)
                StatusItem("Rank", user.rank, Color(0xFFBB86FC))
                StatusItem("Streak", "${user.streak}d", Color(0xFF03DAC6))
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun StatGrid(user: User) {
    val stats = listOf(
        "Total XP" to user.totalXpEarned,
        "Workouts" to user.totalWorkouts,
        "Best Streak" to user.highestStreak,
        "Promotions" to user.totalPromotions,
        "Pushups" to user.pushups,
        "Pullups" to user.pullups,
        "Plank Sec" to user.plankTime
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
        animatedValue.animateTo(value.toFloat(), animationSpec = tween(1500))
    }

    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$current / $total", color = Color(0xFFBB86FC), fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Color(0xFFBB86FC),
                trackColor = Color.Black.copy(alpha = 0.3f)
            )
            Text(label, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun AchievementSummary(highestBadge: Badge?, user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ACHIEVEMENTS", color = Color(0xFFBB86FC), fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke(highestBadge != null)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text("HIGHEST TITLE EARNED", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(highestBadge?.name ?: "N/A", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(user.rank.uppercase(), color = Color(0xFFBB86FC), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (user.activeTitle != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text("EQUIPPED STREAK TITLE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(user.activeTitle.uppercase(), color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStroke(hasBadge: Boolean) = if (hasBadge) {
    androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
} else {
    androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent)
}
