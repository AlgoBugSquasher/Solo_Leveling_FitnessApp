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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterArchiveScreen(
    viewModel: BadgeViewModel,
    onNavigateBack: () -> Unit
) {
    val badges by viewModel.badges.collectAsState()
    var unlockedBadgePopup by remember { mutableStateOf<Badge?>(null) }
    var selectedShowcaseBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(Unit) {
        viewModel.newBadgeUnlocked.collect { badge ->
            unlockedBadgePopup = badge
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
            onDismiss = { selectedShowcaseBadge = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Archive", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
    val rarityColor = badge.rarity.color
    val isUnlocked = badge.isUnlocked

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(enabled = isUnlocked) { onClick() }
            .then(
                if (isUnlocked) {
                    Modifier.border(
                        1.dp,
                        rarityColor.copy(alpha = glowAlpha),
                        RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E1B24).copy(alpha = 0.9f) else Color(0xFF121212).copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    // Subtle Rarity Aura
                    BadgeAura(
                        rarity = badge.rarity,
                        pulseScale = auraScale,
                        alphaMult = glowAlpha * 0.5f,
                        modifier = Modifier.fillMaxSize(0.9f)
                    )

                    Image(
                        painter = painterResource(id = badge.imageRes),
                        contentDescription = badge.name,
                        modifier = Modifier.fillMaxSize(0.85f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Mysterious Locked View
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = badge.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize(0.7f)
                                .alpha(0.1f),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color.Black, androidx.compose.ui.graphics.BlendMode.SrcAtop)
                        )
                        
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Text Info Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (isUnlocked) badge.name.uppercase() else "???",
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.3f),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                if (isUnlocked) {
                    Text(
                        text = badge.rarity.displayName.uppercase(),
                        color = rarityColor,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )
                } else {
                    Text(
                        text = "LVL ${badge.requiredLevel}",
                        color = Color.White.copy(alpha = 0.4f),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun BadgeAura(
    rarity: BadgeRarity,
    modifier: Modifier = Modifier,
    pulseScale: Float = 1f,
    alphaMult: Float = 1f
) {
    val auraColors = when (rarity) {
        BadgeRarity.COMMON -> listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent)
        BadgeRarity.RARE -> listOf(Color(0xFF6200EE).copy(alpha = 0.4f * alphaMult), Color.Transparent)
        BadgeRarity.EPIC -> listOf(Color(0xFFBB86FC).copy(alpha = 0.6f * alphaMult), Color(0xFFD0BCFF).copy(alpha = 0.2f), Color.Transparent)
        BadgeRarity.LEGENDARY -> listOf(Color(0xFFFFD700).copy(alpha = 0.7f * alphaMult), Color(0xFFBB86FC).copy(alpha = 0.4f), Color.Transparent)
    }

    Box(
        modifier = modifier
            .size(120.dp)
            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            .background(
                Brush.radialGradient(auraColors),
                CircleShape
            )
    )
}

@Composable
fun BadgeShowcaseDialog(badge: Badge, onDismiss: () -> Unit) {
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
            BadgeShowcaseAnimation(badge = badge, onDismiss = onDismiss)
        }
    }
}

@Composable
fun BadgeShowcaseAnimation(badge: Badge, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = LinearOutSlowInEasing), initialScale = 0.4f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .clickable(enabled = false) { } // Prevent dismiss when clicking content
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E1B24).copy(alpha = 0.95f))
                    .border(1.dp, badge.rarity.color.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(360.dp)) {
                        // Radiant Aura
                        BadgeAura(
                            rarity = badge.rarity,
                            pulseScale = glowScale,
                            alphaMult = glowAlpha,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        Image(
                            painter = painterResource(id = badge.imageRes),
                            contentDescription = badge.name,
                            modifier = Modifier.size(280.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = badge.name.uppercase(),
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        color = badge.rarity.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badge.rarity.color.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = badge.rarity.displayName.uppercase(),
                            color = badge.rarity.color,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = badge.description,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "REQUIRED LEVEL: ${badge.requiredLevel}",
                        color = Color.White.copy(alpha = 0.4f),
                        style = TextStyle(
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(200.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CLOSE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
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
