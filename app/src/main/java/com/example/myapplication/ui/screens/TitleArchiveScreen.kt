package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.util.SoundManager
import com.example.myapplication.model.Title
import com.example.myapplication.viewmodel.TitleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleArchiveScreen(
    viewModel: TitleViewModel,
    onNavigateBack: () -> Unit
) {
    val titles by viewModel.titles.collectAsState()
    val user by viewModel.user.collectAsState()

    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A0B2E), Color(0xFF0F051D))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Titles", color = Color.White, fontWeight = FontWeight.Bold) },
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
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(titles) { title ->
                    TitleItem(
                        title = title,
                        isActive = user?.activeTitle == title.name,
                        onEquip = { 
                            soundManager.playClick()
                            viewModel.equipTitle(title.name) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TitleItem(title: Title, isActive: Boolean, onEquip: () -> Unit) {
    val isUnlocked = title.isUnlocked

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onEquip() }
            .then(
                if (isActive) Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF2D1B4E) else Color(0xFF1A1A1A).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isUnlocked) Color(0xFFBB86FC).copy(alpha = 0.1f) else Color.DarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text("T", color = Color(0xFFBB86FC), fontWeight = FontWeight.Black, fontSize = 20.sp)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUnlocked) title.name.uppercase() else "???",
                    color = if (isUnlocked) Color.White else Color.Gray,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isUnlocked) "UNLOCKED AT ${title.requiredStreak} DAY STREAK" else "REACH ${title.requiredStreak} DAY STREAK",
                    color = if (isUnlocked) Color(0xFFBB86FC) else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isUnlocked) {
                RadioButton(
                    selected = isActive,
                    onClick = onEquip,
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700))
                )
            }
        }
    }
}
