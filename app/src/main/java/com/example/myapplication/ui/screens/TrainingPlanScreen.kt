package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.model.ExerciseTrackingType
import com.example.myapplication.model.PlannedExercise
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.TrainingPlanViewModel
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    viewModel: TrainingPlanViewModel,
    onNavigateBack: () -> Unit
) {
    val plan by viewModel.trainingPlan.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    val listState = rememberLazyListState()
    var hasScrolledToToday by remember { mutableStateOf(false) }
    var triggerHighlight by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F051D), Color(0xFF1A0B2E))
    )

    // Automatic Scroll to Today
    LaunchedEffect(plan) {
        if (plan.isNotEmpty() && !hasScrolledToToday) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 7
            }
            
            // Monday is index 1, Tuesday index 2, etc. (Header is index 0)
            val targetIndex = dayOfWeek
            
            delay(300) // Small delay for screen transition
            listState.animateScrollToItem(targetIndex)
            hasScrolledToToday = true
            triggerHighlight = true
            delay(2000)
            triggerHighlight = false
        }
    }

    var showBonusPopup by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<PlannedExercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<PlannedExercise?>(null) }
    var dayForNewExercise by remember { mutableStateOf<Int?>(null) }

    // Day Reward Animation State
    var dayRewardAnimation by remember { mutableStateOf<Int?>(null) }

    // Monitor for day completion reward celebration
    val previousDayReward = remember { mutableMapOf<Int, Int>() }
    val previousDayYear = remember { mutableMapOf<Int, Int>() }
    var isInitialLoad by remember { mutableStateOf(true) }
    
    LaunchedEffect(plan) {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        plan.forEach { day ->
            val rewardClaimedThisWeek = day.lastRewardWeek == currentWeek && day.lastRewardYear == currentYear
            val wasAlreadyNoted = previousDayReward[day.dayOfWeek] == currentWeek && previousDayYear[day.dayOfWeek] == currentYear
            
            if (rewardClaimedThisWeek && !wasAlreadyNoted && !isInitialLoad) {
                // Reward granted for the first time this week!
                soundManager.playBadgeUnlock(com.example.myapplication.model.BadgeRarity.COMMON)
                dayRewardAnimation = 200
            }
            previousDayReward[day.dayOfWeek] = day.lastRewardWeek
            previousDayYear[day.dayOfWeek] = day.lastRewardYear
        }
        isInitialLoad = false
    }

    LaunchedEffect(Unit) {
        viewModel.showBonusDialog.collect {
            showBonusPopup = it
            if (it) soundManager.playRankPromotion()
        }
    }

    if (dayRewardAnimation != null) {
        LaunchedEffect(dayRewardAnimation) {
            delay(3000)
            dayRewardAnimation = null
        }
    }

    if (showBonusPopup) {
        WeeklyBonusDialog(onDismiss = { showBonusPopup = false })
    }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                soundManager.playClick()
                exerciseToDelete = null 
            },
            title = { Text("Delete Exercise?") },
            text = { Text("Are you sure you want to remove '${exerciseToDelete?.name}' from your training plan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        soundManager.playBadgeUnlock(com.example.myapplication.model.BadgeRarity.COMMON)
                        viewModel.deleteExercise(exerciseToDelete!!)
                        exerciseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    soundManager.playClick()
                    exerciseToDelete = null 
                }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            containerColor = Color(0xFF1F1B24),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (dayForNewExercise != null) {
        AddEditExerciseDialog(
            dayOfWeek = dayForNewExercise!!,
            onDismiss = { 
                soundManager.playClick()
                dayForNewExercise = null 
            },
            onConfirm = { name, type, sets, reps, secs, dist ->
                soundManager.playQuestComplete()
                viewModel.addExercise(dayForNewExercise!!, name, type, sets, reps, secs, dist)
                dayForNewExercise = null
            }
        )
    }

    if (exerciseToEdit != null) {
        AddEditExerciseDialog(
            exercise = exerciseToEdit,
            dayOfWeek = exerciseToEdit!!.dayOfWeek,
            onDismiss = { 
                soundManager.playClick()
                exerciseToEdit = null 
            },
            onConfirm = { name, type, sets, reps, secs, dist ->
                soundManager.playClick()
                viewModel.updateExercise(exerciseToEdit!!.copy(
                    name = name,
                    trackingType = type,
                    sets = sets,
                    reps = reps,
                    seconds = secs,
                    distanceKm = dist
                ))
                exerciseToEdit = null
            },
            onDelete = {
                exerciseToDelete = exerciseToEdit
                exerciseToEdit = null
            }
        )
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
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    WeeklyProgressHeader(plan, allExercises)
                }

                items(plan) { day ->
                    val dayExercises = allExercises.filter { it.dayOfWeek == day.dayOfWeek }
                    
                    val calendar = Calendar.getInstance()
                    val todayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> 1
                        Calendar.TUESDAY -> 2
                        Calendar.WEDNESDAY -> 3
                        Calendar.THURSDAY -> 4
                        Calendar.FRIDAY -> 5
                        Calendar.SATURDAY -> 6
                        Calendar.SUNDAY -> 7
                        else -> 7
                    }
                    val isToday = day.dayOfWeek == todayOfWeek

                    TrainingDayCard(
                        day = day,
                        exercises = dayExercises,
                        onAddExercise = { 
                            soundManager.playClick()
                            dayForNewExercise = day.dayOfWeek 
                        },
                        onEditExercise = { 
                            soundManager.playClick()
                            exerciseToEdit = it 
                        },
                        onDeleteExercise = { 
                            soundManager.playClick()
                            exerciseToDelete = it 
                        },
                        onToggleExercise = { 
                            if (!it.isCompleted) soundManager.playQuestComplete()
                            viewModel.toggleExerciseCompletion(it) 
                        },
                        isHighlighted = isToday && triggerHighlight
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            // Day Reward Celebration Overlay
            AnimatedVisibility(
                visible = dayRewardAnimation != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 1.1f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                dayRewardAnimation?.let { xp ->
                    Surface(
                        color = Color(0xFF2D1B4E),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color(0xFF03DAC6)),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DAY COMPLETE", color = Color(0xFF03DAC6), fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("+$xp XP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressHeader(plan: List<TrainingDay>, allExercises: List<PlannedExercise>) {
    val calendar = Calendar.getInstance()
    val week = calendar.get(Calendar.WEEK_OF_YEAR)
    val year = calendar.get(Calendar.YEAR)
    
    val activeDayOfWeek = allExercises.map { it.dayOfWeek }.distinct()
    val completedCount = plan.count { 
        it.dayOfWeek in activeDayOfWeek && 
        it.isCompleted && 
        it.lastCompletedWeek == week && 
        it.lastCompletedYear == year 
    }
    val totalTrainingDays = activeDayOfWeek.size

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
fun TrainingDayCard(
    day: TrainingDay,
    exercises: List<PlannedExercise>,
    onAddExercise: () -> Unit,
    onEditExercise: (PlannedExercise) -> Unit,
    onDeleteExercise: (PlannedExercise) -> Unit,
    onToggleExercise: (PlannedExercise) -> Unit,
    isHighlighted: Boolean = false
) {
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

    val calendar = Calendar.getInstance()
    val todayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 7
    }

    val isCompletedThisWeek = day.isCompleted && 
            day.lastCompletedWeek == calendar.get(Calendar.WEEK_OF_YEAR) && 
            day.lastCompletedYear == calendar.get(Calendar.YEAR)

    val isToday = day.dayOfWeek == todayOfWeek
    val isPast = day.dayOfWeek < todayOfWeek
    val isFuture = day.dayOfWeek > todayOfWeek

    // Highlight Animation
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isToday) 2.dp else 1.dp,
            color = when {
                isHighlighted -> Color(0xFF03DAC6).copy(alpha = highlightAlpha)
                isToday -> Color(0xFF03DAC6)
                isCompletedThisWeek -> Color(0xFF03DAC6).copy(alpha = 0.3f)
                else -> Color.Gray.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dayName.uppercase(),
                        color = if (isToday) Color(0xFF03DAC6) else Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontSize = 16.sp
                    )
                    if (isToday) {
                        Surface(
                            modifier = Modifier.padding(start = 12.dp),
                            color = Color(0xFF03DAC6).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF03DAC6).copy(alpha = 0.5f))
                        ) {
                            Text(
                                "TODAY",
                                color = Color(0xFF03DAC6),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                IconButton(onClick = onAddExercise) {
                    Icon(Icons.Default.Add, contentDescription = "Add Exercise", tint = if (isToday) Color(0xFF03DAC6) else Color(0xFFBB86FC))
                }
            }

            if (exercises.isEmpty()) {
                Text(
                    "REST DAY",
                    color = Color.Gray.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                exercises.forEach { exercise ->
                    PlannedExerciseItem(
                        exercise = exercise,
                        onEdit = { onEditExercise(exercise) },
                        onDelete = { onDeleteExercise(exercise) },
                        onToggle = { onToggleExercise(exercise) }
                    )
                }
                
                if (isCompletedThisWeek) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF03DAC6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DAY COMPLETE ✓", color = Color(0xFF03DAC6), fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                    }
                } else if (isPast) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "MISSED DAY",
                        color = Color.Red.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (isFuture) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "LOCKED UNTIL SCHEDULED DAY",
                        color = Color.Gray.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun PlannedExerciseItem(
    exercise: PlannedExercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val todayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 7
    }

    val isCompletedNow = exercise.isCompleted && 
            exercise.lastCompletedWeek == calendar.get(Calendar.WEEK_OF_YEAR) && 
            exercise.lastCompletedYear == calendar.get(Calendar.YEAR)

    val isCompletable = exercise.dayOfWeek == todayOfWeek

    var showMenu by remember { mutableStateOf(false) }

    val completedBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF03DAC6).copy(alpha = 0.15f),
            Color(0xFF03DAC6).copy(alpha = 0.05f)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isCompletedNow) 2.dp else 1.dp,
            color = if (isCompletedNow) Color(0xFF03DAC6) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(if (isCompletedNow) completedBackground else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))))
                .clickable { onEdit() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    // Line 1: Name + Checkmark Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = exercise.name,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            style = TextStyle(
                                letterSpacing = 0.5.sp,
                                shadow = if (isCompletedNow) Shadow(Color(0xFF03DAC6).copy(alpha = 0.5f), blurRadius = 10f) else null
                            ),
                            maxLines = 1
                        )
                        if (isCompletedNow) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.CheckCircle, 
                                null, 
                                tint = Color(0xFF03DAC6), 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Line 2: Details
                    Text(
                        text = when (exercise.trackingType) {
                            ExerciseTrackingType.REPS -> "${exercise.sets} × ${exercise.reps} Reps"
                            ExerciseTrackingType.SECONDS -> "${exercise.sets} × ${exercise.seconds} Sec"
                            ExerciseTrackingType.DISTANCE -> "${exercise.distanceKm} KM"
                        },
                        color = if (isCompletedNow) Color(0xFF03DAC6) else Color(0xFFBB86FC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompletable || isCompletedNow) {
                        IconButton(
                            onClick = { onToggle() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompletedNow) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Completion",
                                tint = if (isCompletedNow) Color(0xFF03DAC6) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White.copy(alpha = 0.4f))
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF1F1B24))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFFBB86FC)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditExerciseDialog(
    exercise: PlannedExercise? = null,
    dayOfWeek: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, ExerciseTrackingType, Int?, Int?, Int?, Double?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var trackingType by remember { mutableStateOf(exercise?.trackingType ?: ExerciseTrackingType.REPS) }
    var sets by remember { mutableStateOf(exercise?.sets?.toString() ?: "") }
    var reps by remember { mutableStateOf(exercise?.reps?.toString() ?: "") }
    var seconds by remember { mutableStateOf(exercise?.seconds?.toString() ?: "") }
    var distance by remember { mutableStateOf(exercise?.distanceKm?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1B24)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    if (exercise == null) "NEW EXERCISE" else "EDIT EXERCISE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFBB86FC),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Tracking Type", color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseTypeChip("Reps", trackingType == ExerciseTrackingType.REPS) { trackingType = ExerciseTrackingType.REPS }
                    ExerciseTypeChip("Seconds", trackingType == ExerciseTrackingType.SECONDS) { trackingType = ExerciseTrackingType.SECONDS }
                    ExerciseTypeChip("Distance", trackingType == ExerciseTrackingType.DISTANCE) { trackingType = ExerciseTrackingType.DISTANCE }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (trackingType) {
                    ExerciseTrackingType.REPS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CompactInput("Sets", sets, { sets = it }, Modifier.weight(1f))
                            CompactInput("Reps", reps, { reps = it }, Modifier.weight(1f))
                        }
                    }
                    ExerciseTrackingType.SECONDS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CompactInput("Sets", sets, { sets = it }, Modifier.weight(1f))
                            CompactInput("Seconds", seconds, { seconds = it }, Modifier.weight(1f))
                        }
                    }
                    ExerciseTrackingType.DISTANCE -> {
                        CompactInput("Distance (KM)", distance, { distance = it }, Modifier.fillMaxWidth(), isDecimal = true)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (onDelete != null) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                        ) {
                            Text("DELETE", fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    trackingType,
                                    sets.toIntOrNull(),
                                    reps.toIntOrNull(),
                                    seconds.toIntOrNull(),
                                    distance.toDoubleOrNull()
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                    ) {
                        Text("CONFIRM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) Color(0xFFBB86FC) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) Color(0xFFBB86FC) else Color.Gray),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) Color.Black else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CompactInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, isDecimal: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (isDecimal) {
                if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") onValueChange(input)
            } else {
                if (input.all { it.isDigit() }) onValueChange(input)
            }
        },
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFFBB86FC),
            unfocusedBorderColor = Color.Gray
        )
    )
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
                        Text("+1000 XP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
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
