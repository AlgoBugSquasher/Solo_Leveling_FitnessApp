package com.exork.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.exork.app.model.WorkoutWithExercises
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import com.exork.app.viewmodel.WorkoutHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutHistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val workouts by viewModel.allWorkouts.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

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
        containerColor = ObsidianVoid
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workouts recorded yet.", color = TitaniumGray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 120.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workouts) { workout ->
                        WorkoutHistoryItem(workout)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryItem(workout: WorkoutWithExercises) {
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(workout.workout.date))

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateString, color = TitaniumGray, fontSize = 12.sp)
                Text("+${workout.workout.totalXpGained} XP", color = ChromeSilver, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            workout.exercises.forEach { ex ->
                val detail = when (ex.trackingType) {
                    com.exork.app.model.ExerciseTrackingType.REPS -> "${ex.sets}x ${ex.reps}r"
                    com.exork.app.model.ExerciseTrackingType.SECONDS -> "${ex.sets}x ${ex.duration}s"
                    com.exork.app.model.ExerciseTrackingType.DISTANCE -> "${ex.distanceKm} km"
                }
                Text(
                    text = "${ex.name.uppercase()}: $detail",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
