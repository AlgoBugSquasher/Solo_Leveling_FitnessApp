package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Achievement
import com.example.myapplication.model.AchievementData
import com.example.myapplication.model.User
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.AchievementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementArchiveScreen(
    viewModel: AchievementViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievement Archive", color = Color.White, fontWeight = FontWeight.Bold) },
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 120.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(AchievementData.allAchievements) { achievement ->
                        AchievementCard(achievement, currentUser)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement, user: User) {
    val isUnlocked = achievement.isUnlocked(user)
    val progress = achievement.getProgress(user)
    
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(ObsidianVoid)
                    .border(
                        1.5.dp,
                        if (isUnlocked) Brush.sweepGradient(listOf(ChromeSilver, DarkSteel, ChromeSilver))
                        else Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.icon,
                    fontSize = 32.sp,
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name.uppercase(),
                    color = if (isUnlocked) Color.White else Color.Gray,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        shadow = if (isUnlocked) Shadow(Color.Black, blurRadius = 10f) else null
                    )
                )
                
                Text(
                    text = achievement.description,
                    color = if (isUnlocked) TitaniumGray else Color.Gray.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                ExorkNeumorphicProgressBar(
                    progress = progress,
                    label = "${achievement.getCurrentValue(user)} / ${achievement.targetValue}",
                    subLabel = if (isUnlocked) "COMPLETED" else null
                )
            }
        }
    }
}
