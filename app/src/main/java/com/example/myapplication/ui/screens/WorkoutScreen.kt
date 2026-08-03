package com.example.myapplication.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Exercise
import com.example.myapplication.model.ExerciseCategory
import com.example.myapplication.model.ExerciseTrackingType
import com.example.myapplication.ui.components.ExerciseTimerDialog
import com.example.myapplication.ui.components.ExerciseStopwatchDialog
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.WorkoutViewModel
import androidx.compose.material.icons.filled.Timer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel, 
    onNavigateBack: () -> Unit
) {
    val exercises by viewModel.exercises.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    var showXpGained by remember { mutableStateOf<Int?>(null) }
    var timerExercise by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is WorkoutViewModel.WorkoutEvent.WorkoutCompleted -> {
                    showXpGained = event.xpGained
                    soundManager.playQuestComplete()
                    delay(2500)
                    showXpGained = null
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = ObsidianVoid,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SYSTEM LOG: WORKOUT", 
                        style = TextStyle(
                            color = Color.White, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color.Black.copy(alpha = 0.5f), blurRadius = 10f)
                        )
                    ) 
                },
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Manual Exercise Creation Flow
                ExerciseCreationCard(
                    onAdd = { name, category, type, sets, reps, secs, dist ->
                        soundManager.playClick()
                        viewModel.addExercise(name, category, type, sets, reps, secs, dist)
                    }
                )

                Text(
                    "PENDING DATA", 
                    color = TitaniumGray, 
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(exercises, key = { it.hashCode() }) { exercise ->
                        ExerciseEntryItem(
                            exercise = exercise, 
                            onRemove = { 
                                soundManager.playClick()
                                viewModel.removeExercise(exercise) 
                            },
                            onStartTimer = if (exercise.trackingType == ExerciseTrackingType.SECONDS || exercise.trackingType == ExerciseTrackingType.DISTANCE) {
                                { 
                                    soundManager.playClick()
                                    timerExercise = exercise 
                                }
                            } else null
                        )
                    }
                }

                ExorkChromeButton(
                    text = "UPLOAD PROGRESS",
                    onClick = { 
                        soundManager.playClick()
                        viewModel.completeWorkout() 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // XP Celebration Overlay
            AnimatedVisibility(
                visible = showXpGained != null,
                enter = scaleIn(tween(500, easing = OvershootInterpolator().toEasing())) + fadeIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700).copy(alpha = 0.2f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "DATA RECORDED",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+$showXpGained XP",
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                shadow = Shadow(ElectricCyan, blurRadius = 20f)
                            )
                        )
                    }
                }
            }

            if (timerExercise != null) {
                if (timerExercise!!.trackingType == ExerciseTrackingType.SECONDS) {
                    ExerciseTimerDialog(
                        exerciseName = timerExercise!!.name,
                        totalSets = timerExercise!!.sets,
                        initialSeconds = timerExercise!!.duration ?: 60,
                        soundManager = soundManager,
                        onDismiss = { timerExercise = null },
                        onComplete = {
                            soundManager.playQuestComplete()
                            timerExercise = null
                        }
                    )
                } else if (timerExercise!!.trackingType == ExerciseTrackingType.DISTANCE) {
                    ExerciseStopwatchDialog(
                        exerciseName = timerExercise!!.name,
                        targetDistance = timerExercise!!.distanceKm ?: 0.0,
                        soundManager = soundManager,
                        onDismiss = { timerExercise = null },
                        onFinish = { timeTaken ->
                            val updated = timerExercise!!.copy(duration = timeTaken)
                            viewModel.updateExercise(timerExercise!!, updated)
                            timerExercise = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCreationCard(onAdd: (String, ExerciseCategory, ExerciseTrackingType, Int, Int?, Int?, Double?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExerciseCategory.OTHER) }
    var trackingType by remember { mutableStateOf(ExerciseTrackingType.REPS) }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "NEW ENTRY", 
                color = Color.White, 
                style = TextStyle(
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 2.sp,
                    shadow = Shadow(Color.Black, blurRadius = 8f)
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise Name", color = TitaniumGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = ObsidianVoid,
                    unfocusedContainerColor = ObsidianVoid,
                    focusedBorderColor = ChromeSilver,
                    unfocusedBorderColor = Color(0xFF3A3A3E),
                    cursorColor = ChromeSilver
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "CATEGORY", 
                color = ChromeSilver, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 4f))
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ExerciseChip("PUSHUPS", category == ExerciseCategory.PUSHUPS, Modifier.weight(1f)) { category = ExerciseCategory.PUSHUPS }
                ExerciseChip("PULLUPS", category == ExerciseCategory.PULLUPS, Modifier.weight(1f)) { category = ExerciseCategory.PULLUPS }
                ExerciseChip("PLANK", category == ExerciseCategory.PLANK, Modifier.weight(1f)) { category = ExerciseCategory.PLANK }
                ExerciseChip("CARDIO", category == ExerciseCategory.CARDIO, Modifier.weight(1f)) { category = ExerciseCategory.CARDIO }
                ExerciseChip("OTHER", category == ExerciseCategory.OTHER, Modifier.weight(1f)) { category = ExerciseCategory.OTHER }
            }

            Text(
                "TRACKING TYPE", 
                color = ChromeSilver, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black,
                style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 4f))
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExerciseChip("REPS", trackingType == ExerciseTrackingType.REPS, Modifier.weight(1f)) { trackingType = ExerciseTrackingType.REPS }
                ExerciseChip("SECONDS", trackingType == ExerciseTrackingType.SECONDS, Modifier.weight(1f)) { trackingType = ExerciseTrackingType.SECONDS }
                ExerciseChip("DISTANCE", trackingType == ExerciseTrackingType.DISTANCE, Modifier.weight(1f)) { trackingType = ExerciseTrackingType.DISTANCE }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (trackingType) {
                    ExerciseTrackingType.REPS -> {
                        WorkoutCompactInput("Sets", sets, { sets = it }, Modifier.weight(1f))
                        WorkoutCompactInput("Reps", reps, { reps = it }, Modifier.weight(1.5f))
                    }
                    ExerciseTrackingType.SECONDS -> {
                        WorkoutCompactInput("Sets", sets, { sets = it }, Modifier.weight(1f))
                        WorkoutCompactInput("Seconds", seconds, { seconds = it }, Modifier.weight(1.5f))
                    }
                    ExerciseTrackingType.DISTANCE -> {
                        WorkoutCompactInput("Distance (KM)", distance, { distance = it }, Modifier.fillMaxWidth(), isDecimal = true)
                    }
                }
            }

            ExorkChromeButton(
                text = "ADD TO RECORD",
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            name,
                            category,
                            trackingType,
                            sets.toIntOrNull() ?: 1,
                            reps.toIntOrNull(),
                            seconds.toIntOrNull(),
                            distance.toDoubleOrNull()
                        )
                        name = ""
                        sets = ""
                        reps = ""
                        seconds = ""
                        distance = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                height = 52.dp
            )
        }
    }
}

@Composable
fun ExerciseChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color.Transparent else ObsidianVoid,
        border = if (selected) null else BorderStroke(1.dp, TitaniumGray.copy(alpha = 0.2f)),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(
            modifier = if (selected) Modifier.background(Brush.verticalGradient(listOf(ChromeSilver, TitaniumGray))) else Modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) ObsidianVoid else TitaniumGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun WorkoutCompactInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, isDecimal: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (isDecimal) {
                if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") onValueChange(input)
            } else {
                if (input.all { it.isDigit() }) onValueChange(input)
            }
        },
        label = { Text(label, color = TitaniumGray, fontSize = 10.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = ObsidianVoid,
            unfocusedContainerColor = ObsidianVoid,
            focusedBorderColor = ChromeSilver,
            unfocusedBorderColor = Color(0xFF3A3A3E)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ExerciseEntryItem(exercise: Exercise, onRemove: () -> Unit, onStartTimer: (() -> Unit)? = null) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ExorkNeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name.uppercase(), 
                        color = Color.White, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    val detailText = when (exercise.trackingType) {
                        ExerciseTrackingType.REPS -> "${exercise.sets} SETS × ${exercise.reps} REPS"
                        ExerciseTrackingType.SECONDS -> "${exercise.sets} SETS × ${exercise.duration} SEC"
                        ExerciseTrackingType.DISTANCE -> "${exercise.distanceKm} KM"
                    }
                    Text(
                        detailText,
                        color = ChromeSilver,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onStartTimer != null) {
                        val timerLabel = if (exercise.trackingType == ExerciseTrackingType.DISTANCE) "START RUN" else "START TIMER"
                        TextButton(
                            onClick = onStartTimer,
                            colors = ButtonDefaults.textButtonColors(contentColor = ChromeSilver),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(timerLabel, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

private fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }

