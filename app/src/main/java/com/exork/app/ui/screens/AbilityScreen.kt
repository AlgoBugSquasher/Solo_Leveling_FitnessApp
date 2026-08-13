package com.exork.app.ui.screens

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
import com.exork.app.model.Ability
import com.exork.app.model.User
import com.exork.app.ui.theme.*
import com.exork.app.viewmodel.AbilityViewModel

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
                title = { Text("Abilities", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = ObsidianVoid
    ) { padding ->
        user?.let { currentUser ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(abilities) { ability ->
                    AbilityRow(ability, currentUser)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ChromeSilver)
        }
    }
}

@Composable
fun AbilityRow(ability: Ability, user: User) {
    ExorkNeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (ability.isUnlocked) Modifier.border(1.5.dp, ChromeSilver, RoundedCornerShape(20.dp))
                else Modifier
            )
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ability.name.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (ability.isUnlocked) Color.White else Color.Gray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (ability.isUnlocked) "UNLOCKED" else "LOCKED",
                    color = if (ability.isUnlocked) ChromeSilver else TitaniumGray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

            if (!ability.isUnlocked) {
                Spacer(modifier = Modifier.height(16.dp))
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
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = label, fontSize = 12.sp, color = TitaniumGray, fontWeight = FontWeight.Bold)
                Text(
                    text = "$current / $required",
                    fontSize = 12.sp,
                    color = if (current >= required) Color.White else TitaniumGray,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ExorkNeumorphicProgressBar(
                progress = (current.toFloat() / required.toFloat()).coerceIn(0f, 1f)
            )
        }
    }
}
