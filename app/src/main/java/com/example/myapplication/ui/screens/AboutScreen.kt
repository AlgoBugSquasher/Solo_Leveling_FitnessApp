package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", color = Color.White, fontWeight = FontWeight.Bold) },
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
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 10 }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // App Logo & Name
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(20.dp, CircleShape, spotColor = Color(0xFFBB86FC))
                                    .background(Color(0xFF2D1B4E), CircleShape)
                                    .border(2.dp, Color(0xFFBB86FC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "⚡",
                                    fontSize = 48.sp,
                                    style = TextStyle(shadow = Shadow(Color(0xFFBB86FC), blurRadius = 20f))
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                "SOLO LEVELING FITNESS",
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    shadow = Shadow(Color(0xFFBB86FC), blurRadius = 15f)
                                )
                            )
                            
                            Text(
                                "Version 2.1.0",
                                color = Color.Gray,
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    // Description Card
                    item {
                        AboutInfoCard(
                            title = "MISSION",
                            content = "A Solo Leveling inspired fitness RPG where users level up, complete quests, earn achievements, unlock titles, and rise through the hunter ranks."
                        )
                    }

                    // Credits Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionTitle("THE ARCHITECTS")
                            
                            CreditItem(role = "Developer", name = "OM KRISHALI", color = Color(0xFFBB86FC))
                            CreditItem(role = "Tester", name = "Ashu [ Player E1 ]", color = Color(0xFF03DAC6))
                        }
                    }

                    // Social Links Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionTitle("CONNECT")
                            
                            SocialLinkItem(
                                platform = "GitHub",
                                handle = "@AlgoBugSquasher",
                                url = "https://github.com/AlgoBugSquasher",
                                color = Color.White,
                                onOpen = { 
                                    soundManager.playClick()
                                    uriHandler.openUri(it) 
                                }
                            )
                            
                            SocialLinkItem(
                                platform = "Instagram",
                                handle = "@omkrishali",
                                url = "https://www.instagram.com/omkrishali/",
                                color = Color(0xFFE4405F),
                                onOpen = { 
                                    soundManager.playClick()
                                    uriHandler.openUri(it) 
                                }
                            )
                        }
                    }

                    // Footer
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "© 2026 OM KRISHALI",
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFFBB86FC),
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            shadow = Shadow(Color(0xFFBB86FC).copy(alpha = 0.5f), blurRadius = 10f)
        ),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@Composable
fun AboutInfoCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                color = Color(0xFFBB86FC),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                content,
                color = Color.LightGray,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun CreditItem(role: String, name: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(role.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(name, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SocialLinkItem(platform: String, handle: String, url: String, color: Color, onOpen: (String) -> Unit) {
    Card(
        onClick = { onOpen(url) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(platform, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(handle, color = Color.Gray, fontSize = 14.sp)
        }
    }
}
