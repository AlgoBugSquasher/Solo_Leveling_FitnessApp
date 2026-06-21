package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeRarity
import com.example.myapplication.viewmodel.BadgeViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.util.SoundManager
import com.example.myapplication.R

@Preview
@Composable
fun UnlockedBadgeCardPreview() {
    Box(modifier = Modifier.padding(16.dp).width(200.dp)) {
        UnlockedBadgeCard(
            badge = Badge(
                name = "Shadow Monarch",
                requiredLevel = 50,
                rarity = BadgeRarity.LEGENDARY,
                description = "The shadows bow to their new king.",
                imageRes = R.drawable.shadow_monarch,
                isUnlocked = true
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LockedBadgeCardPreview() {
    Box(modifier = Modifier.padding(16.dp).width(200.dp)) {
        LockedBadgeCard(
            badge = Badge(
                name = "???",
                requiredLevel = 100,
                rarity = BadgeRarity.LEGENDARY,
                description = "Locked description.",
                imageRes = R.drawable.silent_killer,
                isUnlocked = false
            )
        )
    }
}

@Preview
@Composable
fun BadgeShowcasePreview() {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        BadgeShowcaseContent(
            badge = Badge(
                name = "Shadow Monarch",
                requiredLevel = 50,
                rarity = BadgeRarity.LEGENDARY,
                description = "The shadows bow to their new king. Long live the monarch of the eternal night.",
                imageRes = R.drawable.shadow_monarch,
                isUnlocked = true
            ),
            soundManager = soundManager,
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterArchiveScreen(
    viewModel: BadgeViewModel,
    onNavigateBack: () -> Unit
) {
    val badges by viewModel.badges.collectAsState()
    var unlockedBadgePopup by remember { mutableStateOf<Badge?>(null) }
    var selectedShowcaseBadge by remember { mutableStateOf<Badge?>(null) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    LaunchedEffect(Unit) {
        viewModel.newBadgeUnlocked.collect { badge ->
            unlockedBadgePopup = badge
            soundManager.playBadgeUnlock(badge.rarity)
            delay(4000)
            unlockedBadgePopup = null
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A0B2E), Color(0xFF0F051D))
    )

    if (selectedShowcaseBadge != null) {
        BadgeShowcaseDialog(
            badge = selectedShowcaseBadge!!,
            soundManager = soundManager,
            onDismiss = { 
                soundManager.playClick()
                selectedShowcaseBadge = null 
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Archive", color = Color.White, fontWeight = FontWeight.Bold) },
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(badges) { badge ->
                    BadgeCard(
                        badge = badge,
                        onClick = {
                            if (badge.isUnlocked) {
                                soundManager.playClick()
                                selectedShowcaseBadge = badge
                            }
                        }
                    )
                }
            }

            // Unlock Popup
            AnimatedVisibility(
                visible = unlockedBadgePopup != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 1.1f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                unlockedBadgePopup?.let { badge ->
                    BadgeUnlockPopup(badge)
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: Badge, onClick: () -> Unit) {
    if (badge.isUnlocked) {
        UnlockedBadgeCard(badge = badge, onClick = onClick)
    } else {
        LockedBadgeCard(badge = badge)
    }
}

@Composable
fun UnlockedBadgeCard(badge: Badge, onClick: () -> Unit) {
    val rarityColor = badge.rarity.color
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val cardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2D1B4E),
            Color(0xFF1A0B2E)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.6f)
            .clickable { onClick() }
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(rarityColor, rarityColor.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackground)
        ) {
            // Rarity Glow behind image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                rarityColor.copy(alpha = 0.25f * glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Artwork (65-75% of card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = badge.imageRes),
                        contentDescription = badge.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Text Layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = badge.name.uppercase(),
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                            shadow = Shadow(Color.Black, blurRadius = 4f)
                        ),
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = badge.rarity.displayName.uppercase(),
                        color = rarityColor,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LockedBadgeCard(badge: Badge) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // Smaller than unlocked
            .alpha(0.6f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "???",
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "LVL ${badge.requiredLevel}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun BadgeShowcaseDialog(badge: Badge, soundManager: SoundManager, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .blur(16.dp)
                .clickable { onDismiss() }
        )
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BadgeShowcaseAnimation(badge = badge, soundManager = soundManager, onDismiss = onDismiss)
        }
    }
}

@Composable
fun BadgeShowcaseAnimation(badge: Badge, soundManager: SoundManager, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        isVisible = true
        // soundManager.playBadgeUnlock(badge.rarity) // Optional: play sound when opening showcase
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500)) + scaleIn(tween(500, easing = LinearOutSlowInEasing), initialScale = 0.7f),
        exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.9f)
    ) {
        BadgeShowcaseContent(badge = badge, soundManager = soundManager, onDismiss = onDismiss)
    }
}

@Composable
fun BadgeShowcaseContent(badge: Badge, soundManager: SoundManager, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .clickable(enabled = false) { }
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2D1B4E), Color(0xFF120820))
                    )
                )
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(badge.rarity.color, Color.Transparent)
                    ),
                    RoundedCornerShape(32.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
                    // Radiant Rarity Glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = glowScale, scaleY = glowScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        badge.rarity.color.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    
                    Image(
                        painter = painterResource(id = badge.imageRes),
                        contentDescription = badge.name,
                        modifier = Modifier.size(260.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = badge.name.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                        shadow = Shadow(Color.Black, blurRadius = 8f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = badge.rarity.displayName.uppercase(),
                    color = badge.rarity.color,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = badge.description,
                    color = Color.LightGray.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "REQUIRED LEVEL: ${badge.requiredLevel}",
                    color = Color.White.copy(alpha = 0.5f),
                    style = TextStyle(
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                IconButton(
                    onClick = {
                        soundManager.playClick()
                        onDismiss()
                    },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1.2f))
    }
}


@Composable
fun BadgeUnlockPopup(badge: Badge) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        color = Color(0xFF2D1B4E),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, badge.rarity.color),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NEW ARCHIVE UNLOCKED",
                color = badge.rarity.color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(badge.rarity.color.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, badge.rarity.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = badge.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.8f),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                badge.name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                badge.rarity.displayName,
                color = badge.rarity.color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                badge.description,
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.8f)
            )
        }
    }
}
