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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Exercise
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.WorkoutViewModel
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

    var selectedExerciseName by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val exercisesList = listOf(
        "Push-up", "Diamond Push-up", "Wide Push-up", "Archer Push-up", "Pike Push-up", "Pseudo Planche Push-up",
        "Pull-up", "Chin-up", "Explosive Pull-up", "Muscle-up", "Wide Pull-up",
        "Plank", "Side Plank", "L-sit", "Hanging Leg Raise", "Front Lever Hold", "Planche Lean"
    )

    val filteredExercises = remember(selectedExerciseName) {
        if (selectedExerciseName.isEmpty()) emptyList()
        else exercisesList.filter { it.contains(selectedExerciseName, ignoreCase = true) }.take(5)
    }

    var showXpGained by remember { mutableStateOf<Int?>(null) }

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

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF050505), Color(0xFF121212))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SYSTEM LOG: WORKOUT", 
                        style = TextStyle(
                            color = Color(0xFFFFD700), 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 10f)
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
            .background(backgroundBrush)
            .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "NEW ENTRY", 
                            color = Color(0xFFFFD700), 
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickAddButton("PUSH-UPS", Modifier.weight(1f)) { selectedExerciseName = "Push-up" }
                            QuickAddButton("PULL-UPS", Modifier.weight(1f)) { selectedExerciseName = "Pull-up" }
                            QuickAddButton("PLANK", Modifier.weight(1f)) { selectedExerciseName = "Plank" }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedExerciseName,
                                onValueChange = { 
                                    selectedExerciseName = it
                                    expanded = true
                                },
                                label = { Text("Search Exercise", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFFFFD700)) },
                                trailingIcon = {
                                    if (selectedExerciseName.isNotEmpty()) {
                                        IconButton(onClick = { selectedExerciseName = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    cursorColor = Color(0xFFFFD700)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = expanded && filteredExercises.isNotEmpty(),
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color(0xFF222222))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
                            ) {
                                filteredExercises.forEach { exercise ->
                                    DropdownMenuItem(
                                        text = { Text(exercise, color = Color.White, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedExerciseName = exercise
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val isDurationBased = selectedExerciseName.lowercase().let { name ->
                            name.contains("plank") || name.contains("hold") || name.contains("lean") || 
                            name.contains("l-sit") || name.contains("lever") || name.contains("hanging")
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = sets,
                                onValueChange = { if (it.length <= 2) sets = it.filter { c -> c.isDigit() } },
                                label = { Text("Sets", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            OutlinedTextField(
                                value = if (isDurationBased) duration else reps,
                                onValueChange = { 
                                    if (it.length <= 4) {
                                        if (isDurationBased) duration = it.filter { c -> c.isDigit() }
                                        else reps = it.filter { c -> c.isDigit() }
                                    }
                                },
                                label = { Text(if (isDurationBased) "Seconds" else "Reps", color = Color.Gray) },
                                modifier = Modifier.weight(1.5f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (selectedExerciseName.isEmpty()) return@Button
                                soundManager.playClick()
                                val s = sets.toIntOrNull() ?: 1
                                val r = reps.toIntOrNull()
                                val d = duration.toIntOrNull()

                                viewModel.addExercise(selectedExerciseName, if (isDurationBased) null else r, s, if (isDurationBased) d else null)
                                
                                // Clear inputs
                                selectedExerciseName = ""
                                reps = ""
                                sets = ""
                                duration = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedExerciseName.isNotEmpty() && (reps.isNotEmpty() || duration.isNotEmpty() || sets.isNotEmpty())
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD TO RECORD", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Text(
                    "PENDING DATA", 
                    color = Color.Gray, 
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(exercises, key = { it.hashCode() }) { exercise ->
                        ExerciseEntryItem(
                            exercise = exercise, 
                            onRemove = { 
                                soundManager.playClick()
                                viewModel.removeExercise(exercise) 
                            }
                        )
                    }
                }

                AnimatedButton(
                    onClick = { 
                        soundManager.playClick()
                        viewModel.completeWorkout() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    enabled = exercises.isNotEmpty()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (exercises.isNotEmpty()) Color(0xFFFFD700) else Color.DarkGray,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "UPLOAD PROGRESS", 
                                style = TextStyle(
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = if (exercises.isNotEmpty()) Color.Black else Color.Gray
                                )
                            )
                        }
                    }
                }
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
                            color = Color(0xFFFFD700),
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
                                shadow = Shadow(Color(0xFFFFD700), blurRadius = 20f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

private fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        },
                        onTap = { onClick() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ExerciseEntryItem(exercise: Exercise, onRemove: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
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
                    Text(
                        if (exercise.reps != null) "${exercise.sets} SETS × ${exercise.reps} REPS"
                        else "${exercise.sets} SETS × ${exercise.duration} SEC",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
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
