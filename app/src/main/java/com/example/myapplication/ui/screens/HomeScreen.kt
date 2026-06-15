package com.example.myapplication.ui.screens

import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeData
import com.example.myapplication.model.BadgeRarity
import com.example.myapplication.model.DailyQuest
import com.example.myapplication.model.Title
import com.example.myapplication.model.User
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.UiEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.random.Random

/**
 * Data class for tracking floating XP events.
 */
data class FloatingXpEvent(
    val id: Long = System.currentTimeMillis() + Random.nextLong(),
    val amount: Int,
    val isBonus: Boolean = false
)

/**
 * The main dashboard screen showing user stats, XP progress, and daily quests.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartWorkout: () -> Unit,
    onViewAbilities: () -> Unit,
    onViewArchive: () -> Unit,
    onViewStatistics: () -> Unit,
    onViewHistory: () -> Unit,
    onViewTitles: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val quests by viewModel.dailyQuests.collectAsState()

    var unlockedBadge by remember { mutableStateOf<Badge?>(null) }
    var unlockedTitle by remember { mutableStateOf<Title?>(null) }
    
    // Level Up Animation state
    var levelUpData by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // XP Gain Animation state
    val floatingXpEvents = remember { mutableStateListOf<FloatingXpEvent>() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.LevelUp -> {
                    levelUpData = event.oldLevel to event.newLevel
                }
                is UiEvent.TitleUnlocked -> {
                    unlockedTitle = event.title
                }
                is UiEvent.BadgeUnlocked -> {
                    unlockedBadge = event.badge
                }
                else -> {}
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF121212), Color(0xFF2D1B4E))
    )

    if (unlockedBadge != null) {
        BadgeUnlockDialog(
            badge = unlockedBadge!!,
            onDismiss = { unlockedBadge = null }
        )
    }

    if (unlockedTitle != null) {
        TitleUnlockDialog(
            title = unlockedTitle!!,
            onDismiss = { unlockedTitle = null }
        )
    }

    if (levelUpData != null) {
        LevelUpDialog(
            oldLevel = levelUpData!!.first,
            newLevel = levelUpData!!.second,
            onDismiss = { levelUpData = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Header: Level and Rank
                UserHeader(user)
                
                Spacer(modifier = Modifier.height(24.dp))

                // XP Bar with Floating Animation Container
                Box {
                    XpProgressBar(user)
                    
                    // Floating XP Text layer
                    floatingXpEvents.forEach { event ->
                        FloatingXpAnimation(
                            event = event,
                            onAnimationFinished = { floatingXpEvents.remove(event) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Daily Quests",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(quests) { quest ->
                        QuestItem(quest) {
                            val wasAllCompleted = quests.all { it.isCompleted }
                            viewModel.completeQuest(quest.id)
                            
                            // Trigger XP Animation
                            floatingXpEvents.add(FloatingXpEvent(amount = quest.xpReward))
                            
                            // Trigger Bonus if applicable
                            if (!wasAllCompleted && quests.all { it.isCompleted }) {
                                floatingXpEvents.add(FloatingXpEvent(amount = 100, isBonus = true))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedButton(
                    onClick = {
                        onStartWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFBB86FC),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("START WORKOUT", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AnimatedButton(
                    onClick = {
                        onViewAbilities()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        onViewArchive()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(1.dp, Color(0xFF03DAC6)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("HUNTER ARCHIVE", fontWeight = FontWeight.Bold, color = Color(0xFF03DAC6))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        onViewTitles()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("HUNTER TITLES", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        onViewHistory()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedButton(
                    onClick = {
                        onViewStatistics()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("PLAYER STATISTICS", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelUpDialog(
    oldLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            LevelUpAnimation(
                oldLevel = oldLevel,
                newLevel = newLevel,
                onFinished = onDismiss
            )
        }
    }
}

@Composable
fun LevelUpAnimation(
    oldLevel: Int,
    newLevel: Int,
    onFinished: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "levelPulse")
    
    val energyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "energyScale"
    )

    // Reuse Particle logic for Level Up (simpler purple energy)
    val particles = remember { List(40) { EnhancedParticleState(Color(0xFFBB86FC), 1) } }
    
    LaunchedEffect(particles) {
        isVisible = true
        val startTime = System.currentTimeMillis()
        while(true) {
            val now = System.currentTimeMillis()
            particles.forEach { p ->
                val elapsed = (now - startTime - p.delay) % p.duration
                if (elapsed >= 0) {
                    val progress = elapsed.toFloat() / p.duration
                    p.currentX = p.startX + (progress * p.driftX)
                    p.currentY = p.startY - (progress * 1400f)
                    p.alpha = if (progress < 0.15f) progress * 6.6f else 1f - progress
                } else {
                    p.alpha = 0f
                }
            }
            withFrameMillis { }
        }
    }

    LaunchedEffect(Unit) {
        delay(2200) // Duration of animation
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Particle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.6f),
                    radius = p.size,
                    center = Offset(p.currentX, p.currentY)
                )
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = OvershootInterpolator(1.2f).toEasing()), initialScale = 0.5f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Energy Burst behind text
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .graphicsLayer(scaleX = energyScale, scaleY = energyScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFBB86FC).copy(alpha = 0.4f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                    
                    Text(
                        text = "LEVEL UP",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            shadow = Shadow(
                                color = Color(0xFFBB86FC),
                                blurRadius = 30f
                            )
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "LEVEL $oldLevel",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFBB86FC),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(32.dp)
                    )

                    Text(
                        "LEVEL $newLevel",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 32.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            shadow = Shadow(Color(0xFFBB86FC), blurRadius = 10f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeUnlockDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            BadgeUnlockAnimation(
                badge = badge,
                onDismiss = onDismiss,
                modifier = Modifier.clickable(enabled = false) { }
            )
        }
    }
}

@Composable
fun BadgeUnlockAnimation(
    badge: Badge,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showFlash by remember { mutableStateOf(false) }
    var shakeOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        isVisible = true
        showFlash = true
        delay(100) 
        if (badge.rarity == BadgeRarity.EPIC || badge.rarity == BadgeRarity.LEGENDARY) {
            repeat(5) {
                shakeOffset = Offset(Random.nextFloat() * 10 - 5, Random.nextFloat() * 10 - 5)
                delay(30)
            }
            shakeOffset = Offset.Zero
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineProgress"
    )

    val flashAlpha by animateFloatAsState(
        targetValue = if (showFlash) 0f else 1f,
        animationSpec = tween(
            durationMillis = when (badge.rarity) {
                BadgeRarity.LEGENDARY -> 500
                BadgeRarity.EPIC -> 300
                BadgeRarity.RARE -> 200
                else -> 0
            },
            easing = LinearOutSlowInEasing
        ),
        finishedListener = { showFlash = false },
        label = "flashAlpha"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val particleDensityMult = when(badge.rarity) {
        BadgeRarity.COMMON -> 0.3f
        BadgeRarity.RARE -> 1.0f
        BadgeRarity.EPIC -> 2.0f
        BadgeRarity.LEGENDARY -> 3.5f
    }
    
    val totalParticles = (250 * particleDensityMult).toInt()
    
    val particleColors = when(badge.rarity) {
        BadgeRarity.COMMON -> listOf(Color.Gray, Color.LightGray)
        BadgeRarity.RARE -> listOf(Color(0xFFBB86FC), Color(0xFF6200EE), Color(0xFFD0BCFF))
        BadgeRarity.EPIC -> listOf(Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFBB86FC))
        BadgeRarity.LEGENDARY -> listOf(Color(0xFFFFD700), Color(0xFFFF9800), Color(0xFFBB86FC), Color.White)
    }

    val particles = remember(badge) { 
        List(totalParticles) { index ->
            val layer = when {
                index < totalParticles * 0.7 -> 1 
                index < totalParticles * 0.95 -> 2 
                else -> 3 
            }
            EnhancedParticleState(
                color = particleColors.random(),
                layer = layer
            ) 
        } 
    }
    
    LaunchedEffect(particles) {
        val startTime = System.currentTimeMillis()
        while(true) {
            val now = System.currentTimeMillis()
            particles.forEach { p ->
                val elapsed = (now - startTime - p.delay) % p.duration
                if (elapsed < 0) {
                    p.alpha = 0f
                } else {
                    val progress = elapsed.toFloat() / p.duration
                    p.currentX = p.startX + (progress * p.driftX)
                    p.currentY = p.startY - (progress * 1400f) 
                    p.alpha = if (progress < 0.15f) progress * 6.6f else 1f - progress
                }
            }
            withFrameMillis { } 
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(800)) + scaleIn(
            animationSpec = tween(800, easing = OvershootInterpolator(1.2f).toEasing()),
            initialScale = 0.3f
        ),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showFlash && badge.rarity != BadgeRarity.COMMON) {
                val flashColor = when (badge.rarity) {
                    BadgeRarity.LEGENDARY -> Color.White
                    BadgeRarity.EPIC -> Color(0xFFBB86FC)
                    BadgeRarity.RARE -> Color(0xFF6200EE)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(flashColor.copy(alpha = flashAlpha))
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(p.color.copy(alpha = p.alpha * 0.4f), Color.Transparent),
                            center = Offset(p.currentX, p.currentY),
                            radius = p.size * 4f
                        ),
                        radius = p.size * 4f,
                        center = Offset(p.currentX, p.currentY)
                    )
                    
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.currentX, p.currentY)
                    )

                    if (p.layer == 3) {
                        drawLine(
                            color = p.color.copy(alpha = p.alpha * 0.5f),
                            start = Offset(p.currentX, p.currentY),
                            end = Offset(p.currentX - (p.driftX * 0.05f), p.currentY + 40f),
                            strokeWidth = p.size * 0.8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .offset { IntOffset(shakeOffset.x.toInt(), shakeOffset.y.toInt()) },
                verticalArrangement = Arrangement.Center
            ) {
                TitleBanner()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(580.dp)) {
                    AuraLayers(badge.rarity, glowScale, glowAlpha)
                    
                    Image(
                        painter = painterResource(id = badge.imageRes),
                        contentDescription = badge.name,
                        modifier = Modifier
                            .size(420.dp)
                            .shadow(elevation = 24.dp, shape = CircleShape, ambientColor = Color(0xFFBB86FC), spotColor = Color(0xFFBB86FC))
                            .drawWithContent {
                                drawContent()
                                if (badge.rarity != BadgeRarity.COMMON) {
                                    val shineAlpha = when (badge.rarity) {
                                        BadgeRarity.LEGENDARY -> 0.8f
                                        BadgeRarity.EPIC -> 0.6f
                                        else -> 0.3f
                                    }
                                    val shineWidth = size.width * 0.5f
                                    val shineBrush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0f),
                                            Color.White.copy(alpha = shineAlpha),
                                            Color.White.copy(alpha = 0f)
                                        ),
                                        start = Offset(size.width * shineProgress, 0f),
                                        end = Offset(size.width * shineProgress + shineWidth, size.height)
                                    )
                                    clipRect {
                                        drawRect(brush = shineBrush, blendMode = BlendMode.Overlay)
                                    }
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = badge.name.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp,
                        shadow = Shadow(
                            color = Color(0xFFBB86FC).copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 25f
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val rarityFontSize = when(badge.rarity) {
                    BadgeRarity.COMMON -> 14.sp
                    BadgeRarity.RARE -> 20.sp
                    BadgeRarity.EPIC -> 26.sp
                    BadgeRarity.LEGENDARY -> 32.sp
                }
                
                Surface(
                    color = badge.rarity.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, badge.rarity.color.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = badge.rarity.displayName.uppercase(),
                        color = badge.rarity.color,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        style = TextStyle(
                            fontSize = rarityFontSize,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 5.sp,
                            shadow = Shadow(color = badge.rarity.color, blurRadius = 15f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                PremiumButton(onClick = onDismiss)
            }
        }
    }
}

@Composable
fun FloatingXpAnimation(
    event: FloatingXpEvent,
    onAnimationFinished: () -> Unit
) {
    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(event) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
        onAnimationFinished()
    }

    val progress = animProgress.value
    val yOffset = progress * -100f 
    val alpha = if (progress < 0.2f) progress * 5f else 1f - progress
    val scale = when {
        progress < 0.2f -> 0.8f + (progress * 2f) 
        progress < 0.4f -> 1.2f - ((progress - 0.2f) * 1f) 
        else -> 1.0f
    }

    val textColor = if (event.isBonus) Color(0xFFFFD700) else Color(0xFFBB86FC)
    val text = if (event.isBonus) "+${event.amount} BONUS XP" else "+${event.amount} XP"
    val fontSize = if (event.isBonus) 24.sp else 18.sp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = yOffset.dp)
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = textColor.copy(alpha = 0.8f),
                    blurRadius = 20f
                )
            )
        )
    }
}

@Composable
fun TitleBanner() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .width(60.dp)
            .height(2.dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFBB86FC))))
        )
        
        Box(modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(12.dp)
            .rotate(45f)
            .border(2.dp, Color(0xFFBB86FC))
            .background(Color.Black)
        )

        Text(
            "NEW TITLE UNLOCKED",
            color = Color(0xFFBB86FC),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp,
                shadow = Shadow(color = Color(0xFFBB86FC), blurRadius = 15f)
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(12.dp)
            .rotate(45f)
            .border(2.dp, Color(0xFFBB86FC))
            .background(Color.Black)
        )

        Box(modifier = Modifier
            .width(60.dp)
            .height(2.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFBB86FC), Color.Transparent)))
        )
    }
}

@Composable
fun AuraLayers(rarity: BadgeRarity, scale: Float, alpha: Float) {
    val sizeMult = when(rarity) {
        BadgeRarity.LEGENDARY -> 1.4f
        BadgeRarity.EPIC -> 1.2f
        else -> 1.0f
    }
    
    val outerColors = when(rarity) {
        BadgeRarity.COMMON -> listOf(Color.Gray.copy(alpha = 0.15f * alpha), Color.Transparent)
        BadgeRarity.RARE -> listOf(Color(0xFF6200EE).copy(alpha = 0.2f * alpha), Color.Transparent)
        BadgeRarity.EPIC -> listOf(Color(0xFFBB86FC).copy(alpha = 0.35f * alpha), Color(0xFFFF9800).copy(alpha = 0.1f), Color.Transparent)
        BadgeRarity.LEGENDARY -> listOf(Color(0xFFFFD700).copy(alpha = 0.5f * alpha), Color(0xFFBB86FC).copy(alpha = 0.2f), Color.Transparent)
    }

    val middleColors = when(rarity) {
        BadgeRarity.COMMON -> listOf(Color.Gray.copy(alpha = 0.3f * alpha), Color.Transparent)
        BadgeRarity.RARE -> listOf(Color(0xFF6200EE).copy(alpha = 0.4f * alpha), Color.Transparent)
        BadgeRarity.EPIC -> listOf(Color(0xFFBB86FC).copy(alpha = 0.6f * alpha), Color(0xFFFF5722).copy(alpha = 0.2f), Color.Transparent)
        BadgeRarity.LEGENDARY -> listOf(Color(0xFFFFD700).copy(alpha = 0.8f * alpha), Color(0xFFBB86FC).copy(alpha = 0.4f), Color.Transparent)
    }

    val innerColors = when(rarity) {
        BadgeRarity.COMMON -> listOf(Color.Gray.copy(alpha = 0.6f), Color.Transparent)
        BadgeRarity.RARE -> listOf(Color(0xFF6200EE).copy(alpha = 0.7f), Color.Transparent)
        BadgeRarity.EPIC -> listOf(Color(0xFFBB86FC).copy(alpha = 0.8f), Color.Transparent)
        BadgeRarity.LEGENDARY -> listOf(Color(0xFFFFD700).copy(alpha = 1.0f), Color(0xFFBB86FC).copy(alpha = 0.6f), Color.Transparent)
    }

    Box(
        modifier = Modifier
            .size(580.dp * sizeMult)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(colors = outerColors)
                )
            }
    )
    
    Box(
        modifier = Modifier
            .size(480.dp * sizeMult)
            .graphicsLayer(scaleX = scale * 0.9f, scaleY = scale * 0.9f)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(colors = middleColors)
                )
            }
    )

    Box(
        modifier = Modifier
            .size(380.dp * sizeMult)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(colors = innerColors)
                )
            }
    )
}

@Composable
fun PremiumButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "")

    Button(
        onClick = onClick,
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .width(240.dp)
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFBB86FC)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(listOf(Color(0xFFBB86FC), Color(0xFF6200EE))),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "CONTINUE",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            )
        }
    }
}

class EnhancedParticleState(val color: Color, val layer: Int) {
    val startX = Random.nextFloat() * 1200f - 100f 
    val startY = 1600f 
    val size = when(layer) {
        1 -> Random.nextFloat() * 2 + 1
        2 -> Random.nextFloat() * 4 + 3
        else -> Random.nextFloat() * 8 + 6
    }
    val driftX = Random.nextFloat() * 400f - 200f 
    val duration = when(layer) {
        1 -> Random.nextInt(1500, 2500)
        2 -> Random.nextInt(2500, 4000)
        else -> Random.nextInt(4000, 6000)
    }
    val delay = Random.nextInt(0, 3000)

    var currentX by mutableStateOf(0f)
    var currentY by mutableStateOf(0f)
    var alpha by mutableStateOf(0f)
}

private fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
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
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TitleUnlockDialog(title: Title, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            TitleUnlockAnimation(
                title = title, 
                onDismiss = onDismiss,
                modifier = Modifier.clickable(enabled = false) { }
            )
        }
    }
}

@Composable
fun TitleUnlockAnimation(
    title: Title, 
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    // Simplified particles for Title
    val particles = remember { List(40) { EnhancedParticleState(Color(0xFFFFD700), 1) } }
    LaunchedEffect(particles) {
        val startTime = System.currentTimeMillis()
        while(true) {
            val now = System.currentTimeMillis()
            particles.forEach { p ->
                val elapsed = (now - startTime - p.delay) % p.duration
                if (elapsed >= 0) {
                    val progress = elapsed.toFloat() / p.duration
                    p.currentX = p.startX + (progress * p.driftX)
                    p.currentY = p.startY - (progress * 1400f)
                    p.alpha = if (progress < 0.15f) progress * 6.6f else 1f - progress
                }
            }
            withFrameMillis { }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(800)) + scaleIn(tween(800, easing = OvershootInterpolator(1.2f).toEasing()), initialScale = 0.4f),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha * 0.5f),
                        radius = p.size,
                        center = Offset(p.currentX, p.currentY)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "NEW TITLE UNLOCKED",
                    color = Color(0xFFFFD700),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        shadow = Shadow(color = Color(0xFFFFD700), blurRadius = 15f)
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    // Title Aura
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer(scaleX = glowScale, scaleY = glowScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFD700).copy(alpha = 0.3f * glowAlpha), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFFFD700).copy(alpha = 0.1f), CircleShape)
                            .border(2.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("T", color = Color(0xFFFFD700), fontSize = 60.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = title.name.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )
                )

                Text(
                    text = "${title.requiredStreak} DAY STREAK REACHED",
                    color = Color(0xFFFFD700),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(48.dp))

                PremiumButton(onClick = onDismiss)
            }
        }
    }
}

@Composable
fun UserHeader(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Level ${user.level}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = user.rank,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFBB86FC)
            )
            if (user.activeTitle != null) {
                Text(
                    text = user.activeTitle.uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Streak indicator
        Surface(
            color = Color(0xFF3700B3),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🔥 ${user.streak} Day Streak",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun XpProgressBar(user: User) {
    val progress by animateFloatAsState(
        targetValue = user.getProgressPercentage(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "XP Progress"
    )
    
    val percentage = (user.getProgressPercentage() * 100).toInt()
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "XP: ${user.xp} / ${user.xpToNextLevel()} XP", color = Color.LightGray, fontSize = 12.sp)
            Text(text = "$percentage%", color = Color(0xFFBB86FC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = Color(0xFFBB86FC),
            trackColor = Color.DarkGray,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun QuestItem(quest: DailyQuest, onComplete: () -> Unit) {
    Surface(
        color = if (quest.isCompleted) Color(0xFF1E1E1E) else Color(0xFF2C2C2C),
        shape = RoundedCornerShape(12.dp),
        onClick = { if (!quest.isCompleted) onComplete() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    color = if (quest.isCompleted) Color.Gray else Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quest.goal,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${quest.xpReward} XP",
                    color = Color(0xFFBB86FC),
                    fontWeight = FontWeight.Bold
                )
                if (quest.isCompleted) {
                    Text("DONE", color = Color(0xFF03DAC6), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
