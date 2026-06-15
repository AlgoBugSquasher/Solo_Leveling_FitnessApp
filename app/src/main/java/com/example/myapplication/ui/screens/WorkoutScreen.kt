package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Exercise
import com.example.myapplication.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel, onNavigateBack: () -> Unit) {
    val exercises by viewModel.exercises.collectAsState()
    
    val predefinedExercises = listOf(
        "Push-up", "Pull-up", "Chin-up", "Dips", "Pike Push-up",
        "Handstand Push-up", "Muscle-Up", "Plank", "L-sit", 
        "Front Lever Hold", "Planche Hold", "Hanging",
        "Front lever raises", "Planche lean"
    )

    var selectedExercise by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // High-quality auto-suggestion state
    var searchQuery by remember { mutableStateOf("") }
    val filteredExercises = remember { mutableStateListOf<String>() }

    // Debounce logic for smooth typing - Optimized
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            filteredExercises.clear()
            expanded = false
            return@LaunchedEffect
        }
        
        // 300ms debounce
        delay(300)
        
        // Filter and limit to 6 results for performance
        val filtered = predefinedExercises
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
                    delay(2500)
                    showXpGained = null
                    onNavigateBack() // Auto-return to home to see level-up/badges
                }
                is WorkoutViewModel.WorkoutEvent.LevelUp -> {
                    // Level Up is also handled by HomeScreen via User repository updates, 
                    // but we can show a simple indicator here if needed.
                    // For now, let Home handle the big animations.
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
                    IconButton(onClick = onNavigateBack) {
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
                    .padding(16.dp),
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

                        // High-Quality Auto-suggestion (Searchable / Auto-complete)
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { /* Controlled by searchQuery logic */ }
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    selectedExercise = it // Keep actual value in sync
                                },
                                label = { Text("Search or add exercise") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFBB86FC),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = Color(0xFFBB86FC),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF1F1B24))
                            ) {
                                // Show filtered suggestions
                                filteredExercises.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, color = Color.White) },
                                        onClick = {
                                            searchQuery = type
                                            selectedExercise = type
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Check if it's duration based
                            val isDurationBased = selectedExercise.contains("Plank", ignoreCase = true) ||
                                    selectedExercise.contains("L-sit", ignoreCase = true) ||
                                    selectedExercise.contains("Hanging", ignoreCase = true) ||
                                    selectedExercise.contains("Planche lean", ignoreCase = true) ||
                                    selectedExercise.contains("Front Lever", ignoreCase = true) ||
                                    selectedExercise.contains("Planche Hold", ignoreCase = true)

                            if (!isDurationBased) {
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = { reps = it },
                                    label = { Text("Reps") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            } else {
                                OutlinedTextField(
                                    value = duration,
                                    onValueChange = { duration = it },
                                    label = { Text("Seconds") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                            OutlinedTextField(
                                value = sets,
                                onValueChange = { sets = it },
                                label = { Text("Sets") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val r = reps.toIntOrNull()
                                val s = sets.toIntOrNull() ?: 0
                                val d = duration.toIntOrNull()
                                
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
                    onClick = { viewModel.completeWorkout() },
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
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }

    if (showLevelUp != null) {
        LevelUpDialog(level = showLevelUp!!) {
            showLevelUp = null
        }
    }
}

@Composable
fun LevelUpDialog(level: Int, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Great!")
            }
        },
        title = {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)) + fadeIn()
            ) {
                Text("Level Up!", color = Color(0xFFBB86FC), fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
            }
        },
        text = {
            Text("You reached level $level!", fontSize = 18.sp)
        },
        containerColor = Color(0xFF1E1E1E)
    )
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
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
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
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun QuickAddButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        color = Color(0xFF2D1B4E).copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(exercise.name, fontWeight = FontWeight.Bold, color = Color.Black)
                val detailText = when {
                    exercise.duration != null && (exercise.reps == null || exercise.reps == 0) -> 
                        "${exercise.duration} sec x ${exercise.sets} sets"
                    exercise.reps != null -> 
                        "${exercise.reps} reps x ${exercise.sets} sets"
                    else -> 
                        "${exercise.sets} sets"
                }
                Text(detailText, color = Color(0xFF2E7D32)) // A nice green color
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}
