package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About System", color = Color.White, fontWeight = FontWeight.Bold) },
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
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing), initialScale = 0.9f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 24.dp,
                        bottom = 120.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // App Logo & Name
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ExorkNeumorphicCard(
                                modifier = Modifier.size(110.dp),
                                cornerRadius = 55.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_launcher_monarch),
                                        contentDescription = "eXork Logo",
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                "eXork",
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    shadow = Shadow(Color.Black, blurRadius = 20f)
                                )
                            )
                            
                            Text(
                                "Version 3.1.0 - SYSTEM ACTIVE",
                                color = TitaniumGray,
                                style = TextStyle(
                                    fontSize = 13.sp, 
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    // Description Card
                    item {
                        AboutInfoCard(
                            title = "SYSTEM PROTOCOL",
                            content = "An advanced Hunter RPG interface powered by eXork system protocols. Level up, complete daily quests, earn achievements, and rise through the hunter ranks."
                        )
                    }

                    // Credits Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionTitle("THE ARCHITECTS")
                            
                            CreditItem(role = "Developer", name = "OM KRISHALI", color = ElectricCyan)
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
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "© 2026 OM KRISHALI",
                            color = Color.Gray.copy(alpha = 0.4f),
                            style = TextStyle(
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 2.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
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
        color = ChromeSilver,
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            shadow = Shadow(Color.Black, blurRadius = 10f)
        ),
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun AboutInfoCard(title: String, content: String) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                title,
                color = ChromeSilver,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                content,
                color = TitaniumGray,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Composable
fun CreditItem(role: String, name: String, color: Color) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(role.uppercase(), color = TitaniumGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(name, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SocialLinkItem(platform: String, handle: String, url: String, color: Color, onOpen: (String) -> Unit) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpen(url) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(12.dp))
                Text(platform, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Text(handle, color = TitaniumGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
