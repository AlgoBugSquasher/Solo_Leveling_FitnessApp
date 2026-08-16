package com.exork.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.model.JourneyEvent
import com.exork.app.model.JourneyRarity
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.JourneyFilter
import com.exork.app.viewmodel.JourneySummary
import com.exork.app.viewmodel.JourneyViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterJourneyScreen(
    viewModel: JourneyViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.journeyEvents.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    ExorkTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HUNTER JOURNEY", style = ExorkTypography.headlineMedium, color = Color.White) },
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
                    )
                ) {
                    // 1. Journey Summary Header
                    item {
                        summary?.let { 
                            JourneySummaryCard(it) 
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // 2. Filters
                    item {
                        ExorkSectionHeader(title = "Timeline Archive")
                        Spacer(modifier = Modifier.height(16.dp))
                        JourneyFilterRow(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { viewModel.setFilter(it) }
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // 3. Timeline Events
                    if (events.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "Your legend has just begun, Hunter.",
                                    style = ExorkTypography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        itemsIndexed(events) { index, event ->
                            TimelineMilestone(
                                event = event,
                                isLast = index == events.size - 1
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }
    }
}

@Composable
fun JourneySummaryCard(summary: JourneySummary) {
    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                "PLAYER LOGS",
                style = ExorkTypography.labelSmall,
                color = ChromeSilver,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("Rank", summary.currentRank, ChromeSilver)
                SummaryStat("Level", summary.currentLevel.toString(), Color.White)
                SummaryStat("Legends", summary.legendaryUnlocks.toString(), ChromeSilver)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = ChromeSilver.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("JOURNEY STARTED", style = ExorkTypography.labelMedium, color = TitaniumGray)
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(summary.startDate))
                    Text(dateStr, style = ExorkTypography.titleLarge, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL XP", style = ExorkTypography.labelMedium, color = TitaniumGray)
                    Text(summary.totalXpEarned.toString(), style = ExorkTypography.titleLarge, color = ChromeSilver)
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, color: Color) {
    Column {
        Text(label.uppercase(), style = ExorkTypography.labelMedium, color = TitaniumGray)
        Text(value, style = ExorkTypography.headlineMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun JourneyFilterRow(selectedFilter: JourneyFilter, onFilterSelected: (JourneyFilter) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(JourneyFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName, style = ExorkTypography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = ChromeSilver,
                    labelColor = TitaniumGray,
                    selectedLabelColor = ObsidianVoid
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = ChromeSilver.copy(alpha = 0.2f),
                    selectedBorderColor = ChromeSilver,
                    enabled = true,
                    selected = selectedFilter == filter
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun TimelineMilestone(event: JourneyEvent, isLast: Boolean) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "legendGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 50 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Timeline Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(48.dp)
            ) {
                // Glow Node
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (event.rarity == JourneyRarity.LEGENDARY) 
                                event.rarity.color.copy(alpha = glowAlpha) 
                            else event.rarity.color.copy(alpha = 0.2f)
                        )
                        .border(
                            2.dp, 
                            if (event.rarity == JourneyRarity.LEGENDARY) event.rarity.color else Color.White.copy(alpha = 0.3f), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(8.dp).background(event.rarity.color, CircleShape))
                }
                
                // Connection Line
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(event.rarity.color.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Event Card
            ExorkNeumorphicCard(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 32.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(event.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    event.title.uppercase(),
                                    style = ExorkTypography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    event.rarity.displayName.uppercase(),
                                    style = ExorkTypography.labelSmall,
                                    color = TitaniumGray
                                )
                            }
                        }
                        
                        val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(event.timestamp))
                        Text(dateStr, style = ExorkTypography.labelMedium, color = TitaniumGray)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        event.description,
                        style = ExorkTypography.bodyLarge,
                        color = TitaniumGray,
                        lineHeight = 22.sp
                    )
                    
                    event.xpReward?.let { reward ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = ChromeSilver, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$reward XP REWARD", style = ExorkTypography.labelMedium, color = ChromeSilver)
                        }
                    }
                }
            }
        }
    }
}
