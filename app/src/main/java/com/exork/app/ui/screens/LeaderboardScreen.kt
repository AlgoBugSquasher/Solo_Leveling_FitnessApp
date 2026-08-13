package com.exork.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.ui.theme.*
import com.exork.app.viewmodel.LeaderboardViewModel
import com.exork.app.viewmodel.HunterProfile
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onNavigateBack: () -> Unit
) {
    val topHunters by viewModel.topHunters.collectAsState()
    val userRank by viewModel.currentUserRank.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "GLOBAL LEADERBOARD", 
                        style = ExorkTypography.titleLarge.copy(
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color.Black, blurRadius = 8f)
                        ),
                        color = Color.White 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF0E1013) // Exact Home Screen Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading && topHunters.isEmpty()) {
                LeaderboardSkeleton()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Dynamic Champions Podium
                        item {
                            LeaderboardPodium(
                                topHunters = topHunters,
                                currentUserProfile = currentUserProfile,
                                currentUserId = currentUserId
                            )
                        }

                        item {
                            Text(
                                "RANKING LIST",
                                style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = TitaniumGray,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }

                        // 2. Full Ranking List (Starting from #1)
                        itemsIndexed(topHunters) { index, hunter ->
                            LeaderboardRowItem(
                                hunter = hunter,
                                rank = index + 1,
                                isCurrentUser = hunter.userId == currentUserId
                            )
                        }
                    }
                }
            }

            // 3. Pinned Standing Card
            currentUserProfile?.let { profile ->
                PinnedStandingCard(
                    profile = profile,
                    rank = userRank ?: 0
                )
            }
        }
    }
}

@Composable
fun LeaderboardPodium(topHunters: List<HunterProfile>, currentUserProfile: HunterProfile?, currentUserId: String?) {
    val displayList = remember(topHunters, currentUserProfile) {
        val podiumPositions = mutableListOf<HunterProfile?>(null, null, null) // #2, #1, #3
        podiumPositions[1] = topHunters.getOrNull(0) ?: currentUserProfile
        podiumPositions[0] = topHunters.getOrNull(1)
        podiumPositions[2] = topHunters.getOrNull(2)
        podiumPositions
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // #2 Hunter (Left)
        displayList[0]?.let { hunter ->
            PodiumMemberCard(
                hunter = hunter,
                rank = 2,
                accentColor = Color(0xFFC0C0C0), // Silver
                isCurrentUser = hunter.userId == currentUserId
            )
        } ?: PlaceholderPodiumCard("NO HUNTER YET", 2, Color(0xFFC0C0C0))

        // #1 Hunter (Center - Gold)
        displayList[1]?.let { hunter ->
            PodiumMemberCard(
                hunter = hunter,
                rank = 1,
                accentColor = Color(0xFFFFD700), // Gold
                isTop = true,
                isCurrentUser = hunter.userId == currentUserId,
                modifier = Modifier.offset(y = (-20).dp)
            )
        } ?: PlaceholderPodiumCard("NO HUNTER YET", 1, Color(0xFFFFD700))

        // #3 Hunter (Right)
        displayList[2]?.let { hunter ->
            PodiumMemberCard(
                hunter = hunter,
                rank = 3,
                accentColor = Color(0xFFCD7F32), // Bronze
                isCurrentUser = hunter.userId == currentUserId
            )
        } ?: PlaceholderPodiumCard("NO HUNTER YET", 3, Color(0xFFCD7F32))
    }
}

@Composable
fun PodiumMemberCard(
    hunter: HunterProfile,
    rank: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isTop: Boolean = false,
    isCurrentUser: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(if (isTop) 130.dp else 95.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            // Crown Icon for #1
            if (isTop && (hunter.totalXp > 0 || hunter.username != null)) {
                Text(
                    text = "👑",
                    fontSize = 38.sp,
                    modifier = Modifier.offset(y = (-38).dp)
                )
            }

            // Glow Effect for #1
            if (isTop && (hunter.totalXp > 0 || hunter.username != null)) {
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
            }

            // Neumorphic Avatar Frame
            ExorkNeumorphicCard(
                modifier = Modifier.size(if (isTop) 95.dp else 75.dp),
                cornerRadius = 100.dp,
                borderColor = accentColor,
                elevation = if (isTop) 22.dp else 8.dp,
                containerColor = ObsidianVoid
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isTop && hunter.totalXp > 0) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Emblem",
                            tint = accentColor.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    } else {
                        Text(
                            text = (hunter.username ?: hunter.displayName).take(1).uppercase(),
                            style = if (isTop) ExorkTypography.headlineLarge else ExorkTypography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Rank Badge Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 14.dp),
                color = accentColor,
                shape = CircleShape,
                shadowElevation = 10.dp
            ) {
                Text(
                    text = "#$rank",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hunter Identity
        Text(
            text = (hunter.username ?: hunter.displayName).uppercase(),
            style = if (isTop) ExorkTypography.titleMedium.copy(fontWeight = FontWeight.Black) 
                    else ExorkTypography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = if (isCurrentUser) ElectricCyan else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        // XP (Electric Cyan)
        Text(
            text = "${hunter.totalXp} XP",
            style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF00E5FF)
        )

        if (hunter.hunterLevel > 0) {
            Text(
                text = "LVL ${hunter.hunterLevel}",
                style = ExorkTypography.labelSmall,
                color = TitaniumGray
            )
        }
    }
}

@Composable
fun PlaceholderPodiumCard(text: String, rank: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .border(1.5.dp, color.copy(alpha = 0.3f), CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp),
                color = color.copy(alpha = 0.3f),
                shape = CircleShape
            ) {
                Text(
                    text = "#$rank",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = text,
            color = Color.Gray.copy(alpha = 0.6f),
            style = ExorkTypography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LeaderboardRowItem(hunter: HunterProfile, rank: Int, isCurrentUser: Boolean) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isCurrentUser) ElectricCyan.copy(alpha = 0.6f) else null,
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "$rank",
                style = ExorkTypography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = if (isCurrentUser) ElectricCyan else Color.Gray,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center
            )

            // Mini Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ObsidianVoid)
                    .border(1.5.dp, ChromeSilver.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hunter.totalXp > 0 || hunter.username != null) {
                    Text(
                        text = (hunter.username ?: hunter.displayName).take(1).uppercase(),
                        style = ExorkTypography.labelLarge,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = TitaniumGray.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (hunter.username ?: hunter.displayName).uppercase(),
                    style = ExorkTypography.bodyLarge.copy(fontWeight = FontWeight.Black),
                    color = if (isCurrentUser) ElectricCyan else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "LEVEL ${hunter.hunterLevel} • ${hunter.hunterRank}",
                    style = ExorkTypography.labelSmall,
                    color = TitaniumGray
                )
            }

            // XP Display (Electric Cyan)
            Text(
                text = "${hunter.totalXp} XP",
                style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = Color(0xFF00E5FF),
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun PinnedStandingCard(profile: HunterProfile, rank: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                    )
                )
                .padding(16.dp)
        ) {
            ExorkNeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MonarchSlate,
                borderColor = ElectricCyan,
                elevation = 25.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "YOUR CURRENT STANDING",
                            style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                            color = TitaniumGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (rank > 0) "#$rank" else "--",
                                style = ExorkTypography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = (profile.username ?: profile.displayName).uppercase(),
                                style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${profile.totalXp} XP",
                            style = ExorkTypography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "LEVEL ${profile.hunterLevel}",
                            style = ExorkTypography.labelSmall,
                            color = TitaniumGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(3) { Box(modifier = Modifier.size(85.dp, 120.dp).clip(RoundedCornerShape(12.dp)).background(LeatherDark)) }
        }
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LeatherDark)
            )
        }
    }
}
