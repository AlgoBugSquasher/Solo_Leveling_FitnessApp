package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.ArchiveHubViewModel
import com.example.myapplication.viewmodel.ArchiveProgressState
import kotlinx.coroutines.delay
import kotlin.random.Random

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

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    // Header Animation
    var showHeader by remember { mutableStateOf(false) }
    val headerOffset by animateDpAsState(
        targetValue = if (showHeader) 0.dp else 15.dp,
        animationSpec = tween(500, easing = LinearOutSlowInEasing), label = ""
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (showHeader) 1f else 0f,
        animationSpec = tween(500), label = ""
    )

    // Button Sequential Animations
    var showButtons by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showHeader = true
        delay(200)
        showButtons = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Archives", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .offset(y = headerOffset)
                            .alpha(headerAlpha)
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            soundManager.playClick()
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .offset(y = headerOffset)
                            .alpha(headerAlpha)
                    ) {
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
            // Subtle Background Sparks
            ArchiveBackgroundEffects()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Card
                HunterCollectionCard(progressState, headerAlpha, headerOffset)

                Spacer(modifier = Modifier.height(32.dp))

                // Sequential Buttons
                ArchiveButton(
                    text = "HUNTER ARCHIVE",
                    color = Color(0xFF03DAC6),
                    onClick = onViewArchive,
                    soundManager = soundManager,
                    visible = showButtons,
                    delay = 0
                )

                Spacer(modifier = Modifier.height(16.dp))

                ArchiveButton(
                    text = "ACHIEVEMENTS",
                    color = Color(0xFFBB86FC),
                    onClick = onViewAchievements,
                    soundManager = soundManager,
                    visible = showButtons,
                    delay = 120
                )

                Spacer(modifier = Modifier.height(16.dp))

                ArchiveButton(
                    text = "HUNTER TITLES",
                    color = Color(0xFFFFD700),
                    onClick = onViewTitles,
                    soundManager = soundManager,
                    visible = showButtons,
                    delay = 240
                )
            }
        }
    }
}

@Composable
fun HunterCollectionCard(state: ArchiveProgressState, alpha: Float, offset: androidx.compose.ui.unit.Dp) {
    val progressAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(state.completionPercentage) {
        delay(500) // Start after header
        progressAnimation.animateTo(
            targetValue = state.completionPercentage / 100f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offset)
            .alpha(alpha)
            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "HUNTER COLLECTION",
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    shadow = Shadow(Color(0xFFBB86FC), blurRadius = 10f)
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            CollectionStatRow("Badges Earned", state.badgesEarned, state.totalBadges)
            CollectionStatRow("Achievements", state.achievementsUnlocked, state.totalAchievements)
            CollectionStatRow("Titles Earned", state.titlesEarned, state.totalTitles)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Collection Completion", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(progressAnimation.value * 100).toInt()}%", color = Color(0xFFBB86FC), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progressAnimation.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFFBB86FC),
                trackColor = Color.Black.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun CollectionStatRow(label: String, current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text("$current / $total", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ArchiveButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    soundManager: SoundManager,
    visible: Boolean,
    delay: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100), label = ""
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = delay), label = ""
    )
    val buttonOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 10.dp,
        animationSpec = tween(500, delayMillis = delay), label = ""
    )

    // Subtle Pulse Glow
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineOverlapEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .offset(y = buttonOffset)
            .alpha(buttonAlpha)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        soundManager.playClick()
                        onClick()
                    }
                )
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRoundRect(
                        color = color.copy(alpha = glowAlpha),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                },
            border = BorderStroke(1.dp, color.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Black,
                    color = color,
                    style = TextStyle(letterSpacing = 1.sp)
                )
            }
        }
    }
}

@Composable
fun ArchiveBackgroundEffects() {
    val sparks = remember { List(15) { SparkState() } }
    
    LaunchedEffect(Unit) {
        while(true) {
            sparks.forEach { it.update() }
            withFrameMillis { }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        sparks.forEach { spark ->
            drawCircle(
                color = Color(0xFFBB86FC).copy(alpha = spark.alpha * 0.08f),
                radius = spark.size,
                center = Offset(spark.x, spark.y)
            )
        }
    }
}

class SparkState {
    var x by mutableStateOf(Random.nextFloat() * 1500f)
    var y by mutableStateOf(Random.nextFloat() * 2500f)
    var size = Random.nextFloat() * 10f + 5f
    var speed = Random.nextFloat() * 0.5f + 0.2f
    var alpha by mutableStateOf(Random.nextFloat())
    var alphaSpeed = (Random.nextFloat() * 0.01f + 0.005f) * (if(Random.nextBoolean()) 1 else -1)

    fun update() {
        y -= speed
        alpha += alphaSpeed
        if (alpha > 1f || alpha < 0f) alphaSpeed *= -1
        if (y < -50f) {
            y = 2600f
            x = Random.nextFloat() * 1500f
        }
    }
}

private val SineOverlapEasing = Easing { x ->
    kotlin.math.sin(x * kotlin.math.PI.toFloat()).toDouble().toFloat()
}
