package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.JourneyEvent
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.JourneyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HunterJourneyScreen(
    viewModel: JourneyViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.journeyEvents.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Journey", color = Color.White, fontWeight = FontWeight.Bold) },
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
            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Your journey has just begun, Hunter.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    itemsIndexed(events) { index, event ->
                        JourneyTimelineItem(
                            event = event,
                            isLast = index == events.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JourneyTimelineItem(event: JourneyEvent, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            // Icon / Dot
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBB86FC).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFBB86FC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(event.icon, fontSize = 16.sp)
            }
            
            // Connecting Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFBB86FC), Color.Transparent)
                            )
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content Column
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .weight(1f)
        ) {
            Text(
                text = event.title,
                color = Color(0xFFBB86FC),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Text(
                text = event.description,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(event.timestamp))
            Text(
                text = dateStr,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
