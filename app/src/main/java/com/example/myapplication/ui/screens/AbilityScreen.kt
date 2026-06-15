package com.example.myapplication.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Ability
import com.example.myapplication.model.User
import com.example.myapplication.viewmodel.AbilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityScreen(viewModel: AbilityViewModel, onNavigateBack: () -> Unit) {
    val abilities by viewModel.abilities.collectAsState()
    val user by viewModel.user.collectAsState()

    LaunchedEffect(user) {
        user?.let { viewModel.checkAndUnlockAbilities(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Abilities") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        user?.let { currentUser ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(abilities) { ability ->
                    AbilityRow(ability, currentUser)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun AbilityRow(ability: Ability, user: User) {
    val backgroundColor by animateColorAsState(
        targetValue = if (ability.isUnlocked) Color(0xFF2D1B4E) else Color(0xFF1E1E1E),
        animationSpec = tween(1000),
        label = "AbilityBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (ability.isUnlocked) Color(0xFFBB86FC) else Color.Transparent,
        animationSpec = tween(1000),
        label = "AbilityBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (ability.isUnlocked) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ability.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (ability.isUnlocked) Color.White else Color.Gray
                )
                Text(
                    text = if (ability.isUnlocked) "UNLOCKED" else "LOCKED",
                    color = if (ability.isUnlocked) Color(0xFFBB86FC) else Color.DarkGray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

            if (!ability.isUnlocked) {
                Spacer(modifier = Modifier.height(8.dp))
                RequirementProgress("Push-ups", user.pushups, ability.requiredPushups)
                RequirementProgress("Pull-ups", user.pullups, ability.requiredPullups)
                RequirementProgress("Plank Time", user.plankTime, ability.requiredPlankTime)
                RequirementProgress("Level", user.level, ability.requiredLevel)
                RequirementProgress("Streak", user.streak, ability.requiredStreak)
            }
        }
    }
}

@Composable
fun RequirementProgress(label: String, current: Int, required: Int) {
    if (required > 0) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp)
            Text(
                text = "$current / $required",
                fontSize = 14.sp,
                color = if (current >= required) Color(0xFF2E7D32) else Color.Red
            )
        }
        LinearProgressIndicator(
            progress = { (current.toFloat() / required.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = if (current >= required) Color(0xFFBB86FC) else Color(0xFF3700B3),
            trackColor = Color.Black.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}
