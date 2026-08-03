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
import com.example.myapplication.ui.components.ExorkDetailDialog
import com.example.myapplication.ui.theme.*
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
    ExorkDetailDialog(
        badgeName = "Shadow Monarch",
        rarity = "LEGENDARY",
        rarityColor = Color(0xFFFFD700),
        description = "The shadows bow to their new king. Long live the monarch of the eternal night.",
        imageRes = R.drawable.shadow_monarch,
        requiredLevel = 50,
        onDismiss = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterArchiveScreen(
    viewModel: BadgeViewModel,
    onNavigateBack: () -> Unit
) {
    val badges by viewModel.badges.collectAsState()
    var selectedShowcaseBadge by remember { mutableStateOf<Badge?>(null) }

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    if (selectedShowcaseBadge != null) {
        ExorkDetailDialog(
            badgeName = selectedShowcaseBadge!!.name,
            rarity = selectedShowcaseBadge!!.rarity.displayName,
            rarityColor = selectedShowcaseBadge!!.rarity.color,
            description = selectedShowcaseBadge!!.description,
            imageRes = selectedShowcaseBadge!!.imageRes,
            requiredLevel = selectedShowcaseBadge!!.requiredLevel,
            onDismiss = { selectedShowcaseBadge = null }
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
        containerColor = ObsidianVoid
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
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
    
    ExorkNeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
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
                                rarityColor.copy(alpha = 0.2f),
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
    ExorkNeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .alpha(0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = TitaniumGray,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "???",
                color = TitaniumGray,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "LEVEL ${badge.requiredLevel}",
                color = TitaniumGray.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// End of file


// End of file
