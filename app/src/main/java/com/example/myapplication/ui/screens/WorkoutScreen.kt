package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.model.Exercise
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.WorkoutViewModel
import com.example.myapplication.viewmodel.TrainingPlanViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel, 
    trainingViewModel: TrainingPlanViewModel,
    onNavigateBack: () -> Unit
) {
    val exercises by viewModel.exercises.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    var selectedExercise by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val exercisesList = listOf(
        "Push-up", "Diamond Push-up", "Wide Push-up", "Archer Push-up", "Pike Push-up", "Pseudo Planche Push-up",
        "Pull-up", "Chin-up", "Explosive Pull-up", "Muscle-up", "Wide Pull-up",
        "Plank", "Side Plank", "L-sit", "Hanging Leg Raise", "Front Lever Hold", "Planche Lean"
    )

    val filteredExercises = remember { mutableStateListOf<String>() }

    LaunchedEffect(searchQuery) {
        val filtered = exercisesList
            .filter { it.contains(searchQuery, ignoreCase = true) }
            .take(6)
        
        filteredExercises.clear()
        filteredExercises.addAll(filtered)
        
        expanded = filteredExercises.isNotEmpty()
    }

    var showXpGained by remember { mutableStateOf<Int?>(null) }
    var showLevelUp by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is WorkoutViewModel.WorkoutEvent.WorkoutCompleted -> {
                    showXpGained = event.xpGained
                    // Track training plan progress
                    trainingViewModel.trackDailyProgress(
                        addedPushups = exercises.filter { it.name.contains("Push-up", ignoreCase = true) }.sumOf { (it.reps ?: 0) * it.sets },
                        addedPullups = exercises.filter { it.name.contains("Pull-up", ignoreCase = true) || it.name.contains("Chin-up", ignoreCase = true) }.sumOf { (it.reps ?: 0) * it.sets },
                        addedPlankSeconds = exercises.filter { it.name.contains("Plank", ignoreCase = true) }.sumOf { (it.duration ?: 0) * it.sets }
                    )
                    delay(2500)
                    showXpGained = null
                    onNavigateBack() // Auto-return to home to see level-up/badges
                }
                is WorkoutViewModel.WorkoutEvent.LevelUp -> {
                    // Level Up is also handled by HomeScreen via User repository updates, 
                    // but we can show a simple indicator here if needed.
                    // For now, let Home handle the big animations.
                }
                is WorkoutViewModel.WorkoutEvent.NewPersonalRecord -> {
                    // Handled by HomeViewModel when user returns to HomeScreen
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212), // Dark Background
        topBar = {
            TopAppBar(
                title = { Text("Log Workout", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager.playClick()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1B24),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B24)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Quick Add", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickAddButton("Push-up", Modifier.weight(1f)) { searchQuery = "Push-up"; selectedExercise = "Push-up" }
                            QuickAddButton("Pull-up", Modifier.weight(1f)) { searchQuery = "Pull-up"; selectedExercise = "Pull-up" }
                            QuickAddButton("Plank", Modifier.weight(1f)) { searchQuery = "Plank"; selectedExercise = "Plank" }
                        }

                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                        Text("Search or Custom", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)

                        Box {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Exercise Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedBorderColor = Color(0xFFBB86FC)
                                )
                            )
                            
                            DropdownMenu(
                                expanded = expanded && searchQuery.isNotEmpty(),
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF2D2D2D))
                            ) {
                                filteredExercises.forEach { exercise ->
                                    DropdownMenuItem(
                                        text = { Text(exercise, color = Color.White) },
                                        onClick = {
                                            selectedExercise = exercise
                                            searchQuery = exercise
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sets,
                                onValueChange = { sets = it.filter { c -> c.isDigit() } },
                                label = { Text("Sets") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                            )
                            
                            val isPlank = selectedExercise.contains("Plank", ignoreCase = true) || 
                                         selectedExercise.contains("L-sit", ignoreCase = true) ||
                                         selectedExercise.contains("Hold", ignoreCase = true) ||
                                         selectedExercise.contains("Lean", ignoreCase = true) ||
                                         selectedExercise.contains("Lever", ignoreCase = true)

                            if (isPlank) {
                                OutlinedTextField(
                                    value = duration,
                                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                                    label = { Text("Sec") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                                )
                            } else {
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = { reps = it.filter { c -> c.isDigit() } },
                                    label = { Text("Reps") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedExercise.isEmpty()) return@Button
                                val s = sets.toIntOrNull() ?: 1
                                val r = reps.toIntOrNull() ?: 0
                                val d = duration.toIntOrNull() ?: 0

                                // Logic: if it's duration based, pass reps as null and duration as d.
                                // Else pass reps as r and duration as null.
                                val isDurationBased = selectedExercise.contains("Plank", ignoreCase = true) ||
                                        selectedExercise.contains("L-sit", ignoreCase = true) ||
                                        selectedExercise.contains("Hanging", ignoreCase = true) ||
                                        selectedExercise.contains("Planche lean", ignoreCase = true) ||
                                        selectedExercise.contains("Front Lever", ignoreCase = true) ||
                                        selectedExercise.contains("Planche Hold", ignoreCase = true)

                                if (isDurationBased) {
                                    viewModel.addExercise(selectedExercise, null, s, d)
                                } else {
                                    viewModel.addExercise(selectedExercise, r, s, null)
                                }
                                
                                reps = ""
                                sets = ""
                                duration = ""
                                searchQuery = "" // Reset search bar
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add Exercise", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray)

                Text("Current Workout Plan", color = Color.White, fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises) { exercise ->
                        ExerciseItem(exercise, onRemove = { viewModel.removeExercise(exercise) })
                    }
                }

                Button(
                    onClick = { 
                        soundManager.playClick()
                        viewModel.completeWorkout() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = exercises.isNotEmpty()
                ) {
                    Text("Complete Workout", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }

            // XP Gained Overlay
            AnimatedVisibility(
                visible = showXpGained != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    color = Color(0xFFBB86FC).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp
                ) {
                    Text(
                        "+$showXpGained XP",
                        color = Color.Black,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B24)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFFBB86FC))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("LEVEL UP!", color = Color(0xFFBB86FC), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reached Level $level", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                ) {
                    Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
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
fun QuickAddButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1B4E)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    if (exercise.reps != null) "${exercise.sets} sets × ${exercise.reps} reps"
                    else "${exercise.sets} sets × ${exercise.duration} sec",
                    color = Color(0xFF03DAC6),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}
