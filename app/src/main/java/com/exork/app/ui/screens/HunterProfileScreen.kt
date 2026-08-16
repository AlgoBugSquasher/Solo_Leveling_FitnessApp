package com.exork.app.ui.screens

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
import com.exork.app.ui.components.ExorkDetailDialog
import com.exork.app.ui.components.HunterRankDialog
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterProfileScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val username by viewModel.username.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val avatarUri by viewModel.avatarUri.collectAsState()
    val avatarUpdateKey = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val showRankDialog by viewModel.showRankDialog.collectAsState()

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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Header Card
                item {
                    ExorkProfileHeader(
                        user = user,
                        avatarUri = avatarUri,
                        updateKey = avatarUpdateKey.longValue,
                        username = username,
                        onAvatarClick = { /* Click handled in HomeScreen, or add logic here if needed */ },
                        onRankClick = {
                            soundManager.playClick()
                            viewModel.openRankDialog()
                        }
                    )
                }

                // Summary Card
                item {
                    ExorkNeumorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = user.rank.uppercase(),
                                color = ChromeSilver,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    shadow = Shadow(color = Color.Black, blurRadius = 15f)
                                )
                            )
                            
                            if (user.activeTitle != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = user.activeTitle!!.uppercase(),
                                    color = TitaniumGray,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("LEVEL", color = TitaniumGray, fontSize = 12.sp)
                                    Text(user.level.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("RANK", color = TitaniumGray, fontSize = 12.sp)
                                    Text(user.rank.split("-")[0], color = ChromeSilver, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("STREAK", color = TitaniumGray, fontSize = 12.sp)
                                    Text("${user.streak}d", color = ChromeSilver, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // Rank Info Card
                item {
                    ExorkNeumorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RANK", color = TitaniumGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(user.rank.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PROMOTIONS", color = TitaniumGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(user.totalPromotions.toString(), color = ChromeSilver, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Personal Records Section
                item {
                    PersonalRecordsSection(user)
                }

                // Action Buttons Removed: Statistics and Hunter Journey now have dedicated entry points.
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        if (showRankDialog) {
            HunterRankDialog(
                user = user,
                onDismiss = { viewModel.dismissRankDialog() }
            )
        }
    }
}

@Composable
fun PersonalRecordsSection(user: com.exork.app.model.User) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "PERSONAL RECORDS",
            color = ChromeSilver,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = Shadow(Color.Black, blurRadius = 10f)
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
                enter = fadeIn(tween(300, delayMillis = index * 75)) + 
                        slideInHorizontally(tween(300, delayMillis = index * 75)) { -it / 2 }
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
        animationSpec = tween(1000, easing = FastOutSlowInEasing), label = ""
    )

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label.uppercase(),
                    color = TitaniumGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = unit,
                    color = ChromeSilver.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = animatedValue.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = Shadow(Color.Black, blurRadius = 15f),
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
