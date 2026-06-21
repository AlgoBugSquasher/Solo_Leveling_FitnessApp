package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterProfileScreen(
    viewModel: HomeViewModel,
    onViewStatistics: () -> Unit,
    onViewHistory: () -> Unit,
    onViewAbilities: () -> Unit,
    onViewSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager.playClick()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        soundManager.playClick()
                        onViewSettings()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = user.rank.uppercase(),
                                color = Color(0xFFBB86FC),
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    shadow = Shadow(color = Color(0xFFBB86FC), blurRadius = 15f)
                                )
                            )
                            
                            if (user.activeTitle != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = user.activeTitle!!.uppercase(),
                                    color = Color(0xFFFFD700),
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("LEVEL", color = Color.Gray, fontSize = 12.sp)
                                    Text(user.level.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("RANK", color = Color.Gray, fontSize = 12.sp)
                                    Text(user.rank.split("-")[0], color = Color(0xFFBB86FC), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("STREAK", color = Color.Gray, fontSize = 12.sp)
                                    Text("${user.streak}d", color = Color(0xFF03DAC6), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // Rank Info Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.2f)), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RANK", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(user.rank.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PROMOTIONS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(user.totalPromotions.toString(), color = Color(0xFFBB86FC), fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Personal Records Section
                item {
                    PersonalRecordsSection(user)
                }

                // Action Buttons
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnimatedButton(
                            onClick = {
                                soundManager.playClick()
                                onViewStatistics()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("STATISTICS", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                }
                            }
                        }

                        AnimatedButton(
                            onClick = {
                                soundManager.playClick()
                                onViewHistory()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                border = BorderStroke(1.dp, Color(0xFF03DAC6).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("HUNTER JOURNEY", fontWeight = FontWeight.Bold, color = Color(0xFF03DAC6))
                                }
                            }
                        }

                        AnimatedButton(
                            onClick = {
                                soundManager.playClick()
                                onViewAbilities()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                border = BorderStroke(1.dp, Color(0xFFBB86FC)),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("VIEW ABILITIES", fontWeight = FontWeight.Bold, color = Color(0xFFBB86FC))
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun PersonalRecordsSection(user: com.example.myapplication.model.User) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "PERSONAL RECORDS",
            color = Color(0xFFBB86FC),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = Shadow(Color(0xFFBB86FC).copy(alpha = 0.5f), blurRadius = 10f)
            )
        )
        
        val prs = listOf(
            Triple("Highest Pushups", user.maxPushupsSingleWorkout, "Reps"),
            Triple("Highest Pullups", user.maxPullupsSingleWorkout, "Reps"),
            Triple("Longest Plank", user.maxPlankSingleWorkout, "Sec"),
            Triple("Highest Workout XP", user.maxXpSingleWorkout, "XP")
        )

        prs.forEachIndexed { index, (label, value, unit) ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = index * 150)) + 
                        slideInHorizontally(tween(600, delayMillis = index * 150)) { -it / 2 }
            ) {
                PRCard(label, value, unit)
            }
        }
    }
}

@Composable
fun PRCard(label: String, value: Int, unit: String) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(2000, easing = FastOutSlowInEasing), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFFFFD700).copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0B2E).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFBB86FC).copy(alpha = 0.05f), Color.Transparent)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label.uppercase(),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = unit,
                        color = Color(0xFFBB86FC).copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = animatedValue.toString(),
                    color = Color(0xFFFFD700),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        shadow = Shadow(Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 15f),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
