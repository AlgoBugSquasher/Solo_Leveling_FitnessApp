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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.util.SoundManager
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.model.*
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.UiEvent
import com.example.myapplication.viewmodel.TrainingPlanViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
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
    trainingViewModel: TrainingPlanViewModel,
    onStartWorkout: () -> Unit,
    onViewArchiveHub: () -> Unit,
    onViewProfileHub: () -> Unit,
    onOpenTrainingPlan: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val quests by viewModel.dailyQuests.collectAsState()
    val plan by trainingViewModel.trainingPlan.collectAsState()

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    var unlockedBadge by remember { mutableStateOf<Badge?>(null) }
    var unlockedTitle by remember { mutableStateOf<Title?>(null) }
    var unlockedAchievement by remember { mutableStateOf<Achievement?>(null) }
    var newPersonalRecord by remember { mutableStateOf<UiEvent.NewPersonalRecord?>(null) }
    var rankPromotion by remember { mutableStateOf<UiEvent.RankPromotion?>(null) }
    
    // Level Up Animation state
    var levelUpData by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // XP Gain Animation state
    val floatingXpEvents = remember { mutableStateListOf<FloatingXpEvent>() }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.LevelUp -> {
                    levelUpData = event.oldLevel to event.newLevel
                    soundManager.playLevelUp()
                }
                is UiEvent.TitleUnlocked -> {
                    unlockedTitle = event.title
                    soundManager.playPromotion()
                }
                is UiEvent.BadgeUnlocked -> {
                    unlockedBadge = event.badge
                }
                is UiEvent.AchievementUnlocked -> {
                    unlockedAchievement = event.achievement
                    soundManager.playPromotion()
                }
                is UiEvent.NewPersonalRecord -> {
                    newPersonalRecord = event
                    soundManager.playPersonalRecord()
                }
                is UiEvent.RankPromotion -> {
                    rankPromotion = event
                    soundManager.playRankPromotion()
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
            soundManager = soundManager,
            onDismiss = { unlockedBadge = null }
        )
    }

    if (unlockedTitle != null) {
        TitleUnlockDialog(
            title = unlockedTitle!!,
            onDismiss = { unlockedTitle = null }
        )
    }

    if (unlockedAchievement != null) {
        AchievementUnlockDialog(
            achievement = unlockedAchievement!!,
            onDismiss = { unlockedAchievement = null }
        )
    }

    if (newPersonalRecord != null) {
        PersonalRecordDialog(
            record = newPersonalRecord!!,
            onDismiss = { newPersonalRecord = null }
        )
    }

    if (rankPromotion != null) {
        RankPromotionDialog(
            promotion = rankPromotion!!,
            onDismiss = { rankPromotion = null }
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
                
                UserHeader(user)
                
                Spacer(modifier = Modifier.height(24.dp))

                Box {
                    XpProgressBar(user)
                    
                    floatingXpEvents.forEach { event ->
                        FloatingXpAnimation(
                            event = event,
                            onAnimationFinished = { floatingXpEvents.remove(event) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        TrainingPlanPreviewCard(plan, onOpenTrainingPlan)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Text(
                            text = "Daily Quests",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(quests) { quest ->
                        QuestItem(quest) {
                            val wasAllCompleted = quests.all { it.isCompleted }
                            viewModel.completeQuest(quest.id)
                            soundManager.playQuestComplete()
                            
                            floatingXpEvents.add(FloatingXpEvent(amount = quest.xpReward))
                            
                            if (!wasAllCompleted && quests.all { it.isCompleted }) {
                                floatingXpEvents.add(FloatingXpEvent(amount = 100, isBonus = true))
                                soundManager.playPromotion()
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedButton(
                    onClick = {
                        soundManager.playClick()
                        onStartWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFBB86FC),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("START WORKOUT", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedButton(
                    onClick = {
                        soundManager.playClick()
                        onViewArchiveHub()
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
                            Text("ARCHIVES", fontWeight = FontWeight.Bold, color = Color(0xFF03DAC6))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedButton(
                    onClick = {
                        soundManager.playClick()
                        onViewProfileHub()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        border = BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("HUNTER PROFILE", fontWeight = FontWeight.Bold, color = Color(0xFFBB86FC))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingPlanPreviewCard(plan: List<com.example.myapplication.model.TrainingDay>, onOpen: () -> Unit) {
    val calendar = Calendar.getInstance()
    val week = calendar.get(Calendar.WEEK_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 7
    }

    val activeDays = plan.filter { it.pushups > 0 || it.pullups > 0 || it.plankSeconds > 0 }
    val completedCount = activeDays.count { it.isCompleted && it.lastCompletedWeek == week && it.lastCompletedYear == year }
    val todayPlan = plan.find { it.dayOfWeek == dayOfWeek }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TRAINING PLAN",
                    color = Color(0xFFBB86FC),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                )
                Text(
                    "$completedCount / ${activeDays.size} Days",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (todayPlan != null && (todayPlan.pushups > 0 || todayPlan.pullups > 0 || todayPlan.plankSeconds > 0)) {
                Text("Today's Training:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (todayPlan.pushups > 0) TrainingStat("${todayPlan.pushups} Pushups")
                    if (todayPlan.pullups > 0) TrainingStat("${todayPlan.pullups} Pullups")
                    if (todayPlan.plankSeconds > 0) TrainingStat("${todayPlan.plankSeconds}s Plank")
                }
            } else {
                Text("REST DAY", color = Color(0xFF03DAC6), fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text("OPEN PLAN >", color = Color(0xFFBB86FC), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun TrainingStat(text: String) {
    Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun RankPromotionDialog(
    promotion: UiEvent.RankPromotion,
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
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            RankPromotionAnimation(
                promotion = promotion,
                onDismiss = onDismiss,
                modifier = Modifier.clickable(enabled = false) { }
            )
        }
    }
}

@Composable
fun RankPromotionAnimation(
    promotion: UiEvent.RankPromotion,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(500)
        stage = 1 // Energy particles
        delay(800)
        stage = 2 // Old Rank
        delay(1200)
        stage = 3 // Transition (New Rank)
        delay(1000)
        stage = 4 // Final text & Button
    }

    val infiniteTransition = rememberInfiniteTransition(label = "promotionPulse")
    val energyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "energyScale"
    )

    val particles = remember { List(60) { EnhancedParticleState(Color(0xFFBB86FC), 2) } }
    LaunchedEffect(particles) {
        val startTime = System.currentTimeMillis()
        while(true) {
            val now = System.currentTimeMillis()
            particles.forEach { p ->
                val elapsed = (now - startTime - p.delay) % p.duration
                val progress = elapsed.toFloat() / p.duration
                p.alpha = if (progress < 0.2f) progress / 0.2f else if (progress > 0.8f) 1f - (progress - 0.8f) / 0.2f else 1f
                p.currentX = p.startX + p.driftX * progress
                p.currentY = p.startY - 1600f * progress
            }
            delay(16)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha * 0.4f),
                    radius = p.size,
                    center = Offset(p.currentX, p.currentY)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (stage >= 4) {
                Text(
                    "RANK PROMOTION",
                    color = Color(0xFFFFD700),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp,
                        shadow = Shadow(Color(0xFFFFD700), blurRadius = 20f)
                    )
                )
                Spacer(modifier = Modifier.height(48.dp))
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                this@Column.AnimatedVisibility(
                    visible = stage >= 3,
                    enter = scaleIn(tween(1000)) + fadeIn(tween(1000))
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .graphicsLayer(scaleX = energyScale, scaleY = energyScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFFBB86FC).copy(alpha = 0.3f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                }

                Box {
                    this@Column.AnimatedVisibility(
                        visible = stage == 2,
                        enter = fadeIn(),
                        exit = fadeOut(tween(800)) + scaleOut(targetScale = 1.5f)
                    ) {
                        RankEmblem(rank = promotion.oldRank, modifier = Modifier.size(180.dp))
                    }

                    this@Column.AnimatedVisibility(
                        visible = stage >= 3,
                        enter = scaleIn(tween(800, easing = OvershootInterpolator(1.2f).toEasing()), initialScale = 0.5f) + fadeIn(tween(800))
                    ) {
                        RankEmblem(rank = promotion.newRank, modifier = Modifier.size(220.dp), isNew = true)
                    }
                }
            }

            if (stage >= 3) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    promotion.newRank.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        shadow = Shadow(Color(0xFFBB86FC), blurRadius = 15f)
                    )
                )
            }

            if (stage >= 4) {
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun RankEmblem(rank: String, modifier: Modifier = Modifier, isNew: Boolean = false) {
    val glowColor = if (isNew) Color(0xFFBB86FC) else Color.Gray
    val emblemText = rank.first().toString()
    
    Box(
        modifier = modifier
            .shadow(if (isNew) 30.dp else 0.dp, CircleShape, spotColor = glowColor)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2D1B4E), Color(0xFF1A1A1A))
                ),
                CircleShape
            )
            .border(2.dp, if (isNew) Color(0xFFBB86FC) else Color.DarkGray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emblemText,
            color = if (isNew) Color.White else Color.Gray,
            style = TextStyle(
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                shadow = if (isNew) Shadow(Color(0xFFBB86FC), blurRadius = 20f) else null
            )
        )
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            LevelUpAnimation(oldLevel, newLevel, onDismiss)
        }
    }
}

@Composable
fun LevelUpAnimation(oldLevel: Int, newLevel: Int, onDismiss: () -> Unit) {
    var showNewLevel by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(800)
        showNewLevel = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            "LEVEL UP",
            color = Color(0xFFBB86FC),
            style = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                shadow = Shadow(Color(0xFFBB86FC), blurRadius = 25f)
            )
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                oldLevel.toString(),
                color = Color.Gray,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black
            )
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFBB86FC),
                modifier = Modifier.padding(horizontal = 24.dp).size(48.dp)
            )

            AnimatedContent(
                targetState = showNewLevel,
                transitionSpec = {
                    (scaleIn(tween(500, easing = OvershootInterpolator().toEasing())) + fadeIn())
                        .togetherWith(fadeOut())
                }, label = "levelAnim"
            ) { target ->
                if (target) {
                    Text(
                        newLevel.toString(),
                        color = Color.White,
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(shadow = Shadow(Color(0xFFBB86FC), blurRadius = 30f))
                    )
                } else {
                    Box(modifier = Modifier.size(84.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            "YOUR POWER INCREASES",
            color = Color.LightGray,
            style = TextStyle(letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun BadgeUnlockDialog(
    badge: Badge,
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            BadgeUnlockAnimation(badge, soundManager, onDismiss)
        }
    }
}

@Composable
fun BadgeUnlockAnimation(
    badge: Badge,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        soundManager.playBadgeUnlock(badge.rarity)
        delay(300)
        stage = 1 // Particle burst
        delay(700)
        stage = 2 // Badge appear
        delay(1000)
        stage = 3 // Text appear
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "auraScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp)
    ) {
        AnimatedVisibility(
            visible = stage >= 3,
            enter = fadeIn() + expandVertically()
        ) {
            Text(
                "NEW BADGE UNLOCKED",
                color = Color(0xFFFFD700),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            // Aura effect
            AuraLayers(badge.rarity, auraScale, if (stage >= 2) 1f else 0f)

                                            androidx.compose.animation.AnimatedVisibility(
                visible = stage >= 2,
                enter = scaleIn(tween(600, easing = OvershootInterpolator().toEasing())) + fadeIn()
            ) {
                Image(
                    painter = painterResource(id = badge.imageRes),
                    contentDescription = badge.name,
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(20.dp, CircleShape, spotColor = badge.rarity.color),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = stage >= 3,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 20 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    badge.name.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        shadow = Shadow(badge.rarity.color, blurRadius = 10f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = badge.rarity.color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, badge.rarity.color.copy(alpha = 0.5f))
                ) {
                    Text(
                        badge.rarity.displayName.uppercase(),
                        color = badge.rarity.color,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    badge.description,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("COLLECT", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TitleUnlockDialog(
    title: Title,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            TitleUnlockAnimation(title, onDismiss)
        }
    }
}

@Composable
fun TitleUnlockAnimation(
    title: Title,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(500)
        stage = 1
        delay(800)
        stage = 2
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            "NEW TITLE EARNED",
            color = Color(0xFFBB86FC),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp
            )
        )

        Spacer(modifier = Modifier.height(64.dp))

        AnimatedVisibility(
            visible = stage >= 1,
            enter = fadeIn(tween(1000)) + expandHorizontally(tween(1000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFBB86FC).copy(alpha = 0.2f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title.name.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        shadow = Shadow(Color(0xFFBB86FC), blurRadius = 20f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        AnimatedVisibility(
            visible = stage >= 2,
            enter = fadeIn(tween(800))
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CLAIM TITLE", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun AchievementUnlockDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AchievementUnlockAnimation(achievement, onDismiss)
    }
}

@Composable
fun AchievementUnlockAnimation(achievement: Achievement, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Color(0xFFBB86FC), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B24)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ACHIEVEMENT UNLOCKED",
                color = Color(0xFFBB86FC),
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                achievement.icon,
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                achievement.name,
                color = Color.White,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                achievement.description,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
            ) {
                Text("EXCELLENT", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PersonalRecordDialog(
    record: UiEvent.NewPersonalRecord,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PersonalRecordAnimation(record, onDismiss)
    }
}

@Composable
fun PersonalRecordAnimation(
    record: UiEvent.NewPersonalRecord,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B24)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NEW PERSONAL RECORD",
                color = Color(0xFFFFD700),
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "🏆",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                record.recordName,
                color = Color.White,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(record.oldValue.toString(), color = Color.Gray, fontSize = 24.sp)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.padding(horizontal = 16.dp))
                Text(record.newValue.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("WITNESS ME", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FloatingXpAnimation(
    event: FloatingXpEvent,
    onAnimationFinished: () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(
                targetValue = -150f,
                animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            delay(1000)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000)
            )
            onAnimationFinished()
        }
    }

    Text(
        text = if (event.isBonus) "BONUS +${event.amount} XP" else "+${event.amount} XP",
        color = if (event.isBonus) Color(0xFFFFD700) else Color(0xFFBB86FC),
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .alpha(alpha.value),
        style = TextStyle(
            fontSize = if (event.isBonus) 20.sp else 16.sp,
            shadow = Shadow(color = Color.Black, blurRadius = 4f)
        )
    )
}

@Composable
fun TitleBanner() {
    // Placeholder or simplified banner logic
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBB86FC).copy(alpha = 0.2f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "HUNTER DASHBOARD",
            color = Color.White,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        )
    }
}

@Composable
fun AuraLayers(rarity: BadgeRarity, scale: Float, alpha: Float) {
    Box(contentAlignment = Alignment.Center) {
        // Outer aura
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(rarity.color.copy(alpha = 0.15f * alpha), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        // Inner aura
        Box(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer(scaleX = scale * 0.8f, scaleY = scale * 0.8f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(rarity.color.copy(alpha = 0.25f * alpha), Color.Transparent)
                    ),
                    CircleShape
                )
        )
    }
}

@Composable
fun PremiumButton(onClick: () -> Unit) {
    // Reusable premium button style
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
fun UserHeader(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFF3700B3),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🔥 ${user.streak}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun XpProgressBar(user: User) {
    val progress = user.getProgressPercentage()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xpProgress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${user.xp} XP",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${user.xpToNextLevel()} XP",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6200EE), Color(0xFFBB86FC))
                        ),
                        RoundedCornerShape(6.dp)
                    )
                    .shadow(8.dp, RoundedCornerShape(6.dp), spotColor = Color(0xFFBB86FC))
            )
        }
    }
}

@Composable
fun QuestItem(quest: DailyQuest, onComplete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (quest.isCompleted) Color(0xFF1F1B24).copy(alpha = 0.5f) else Color(0xFF1F1B24)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (quest.isCompleted) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title.uppercase(),
                    color = if (quest.isCompleted) Color.Gray else Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = quest.goal,
                    color = if (quest.isCompleted) Color.Gray.copy(alpha = 0.5f) else Color(0xFFBB86FC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (quest.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF03DAC6),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("+${quest.xpReward} XP", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
