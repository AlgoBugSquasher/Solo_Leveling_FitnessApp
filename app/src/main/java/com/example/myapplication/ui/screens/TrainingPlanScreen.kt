package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.TrainingPlanViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    viewModel: TrainingPlanViewModel,
    onNavigateBack: () -> Unit
) {
    val plan by viewModel.trainingPlan.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    var showBonusPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.showBonusDialog.collect {
            showBonusPopup = it
        }
    }

    if (showBonusPopup) {
        WeeklyBonusDialog(onDismiss = { showBonusPopup = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hunter Training Plan", color = Color.White, fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    WeeklyProgressHeader(plan)
                }

                items(plan) { day ->
                    TrainingDayCard(
                        day = day,
                        onUpdate = { viewModel.updateTrainingDay(it) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun WeeklyProgressHeader(plan: List<TrainingDay>) {
    val calendar = Calendar.getInstance()
    val week = calendar.get(Calendar.WEEK_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    
    val activeDays = plan.filter { it.pushups > 0 || it.pullups > 0 || it.plankSeconds > 0 }
    val completedCount = activeDays.count { it.isCompleted && it.lastCompletedWeek == week && it.lastCompletedYear == year }
    val totalTrainingDays = activeDays.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "WEEKLY PROGRESS",
                color = Color(0xFFBB86FC),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "$completedCount / $totalTrainingDays Days Completed",
                color = Color.White,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { if (totalTrainingDays > 0) completedCount.toFloat() / totalTrainingDays else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFBB86FC),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun TrainingDayCard(day: TrainingDay, onUpdate: (TrainingDay) -> Unit) {
    val dayName = when (day.dayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> ""
    }

    val isRestDay = day.pushups == 0 && day.pullups == 0 && day.plankSeconds == 0
    
    val calendar = Calendar.getInstance()
    val isCompletedThisWeek = day.isCompleted && 
            day.lastCompletedWeek == calendar.get(Calendar.WEEK_OF_YEAR) && 
            day.lastCompletedYear == calendar.get(Calendar.YEAR)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isCompletedThisWeek) Color(0xFF03DAC6).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dayName.uppercase(),
                    color = if (isCompletedThisWeek) Color(0xFF03DAC6) else Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                if (isCompletedThisWeek) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF03DAC6))
                } else if (isRestDay) {
                    Text("REST DAY", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TrainingInput(
                    label = "Pushups",
                    value = day.pushups,
                    onValueChange = { onUpdate(day.copy(pushups = it)) },
                    modifier = Modifier.weight(1f)
                )
                TrainingInput(
                    label = "Pullups",
                    value = day.pullups,
                    onValueChange = { onUpdate(day.copy(pullups = it)) },
                    modifier = Modifier.weight(1f)
                )
                TrainingInput(
                    label = "Plank (s)",
                    value = day.plankSeconds,
                    onValueChange = { onUpdate(day.copy(plankSeconds = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TrainingInput(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = if (value == 0) "" else value.toString(),
            onValueChange = { 
                val newValue = it.filter { char -> char.isDigit() }.take(3).toIntOrNull() ?: 0
                onValueChange(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                cursorColor = Color(0xFFBB86FC),
                focusedIndicatorColor = Color(0xFFBB86FC),
                unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f)
            ),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

@Composable
fun WeeklyBonusDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F051D)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TRAINING REGIMEN COMPLETE",
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color(0xFFFFD700), blurRadius = 15f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "7 / 7 Days Completed",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color(0xFFBB86FC).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBB86FC))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BONUS REWARD", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("+500 XP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Continue Your Growth, Hunter.",
                    color = Color.LightGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("COLLECT", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
