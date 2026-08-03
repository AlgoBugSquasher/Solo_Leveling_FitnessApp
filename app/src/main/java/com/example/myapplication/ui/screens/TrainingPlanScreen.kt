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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.model.ExerciseTrackingType
import com.example.myapplication.model.PlannedExercise
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager
import com.example.myapplication.viewmodel.TrainingPlanViewModel
import com.example.myapplication.ui.components.ExerciseTimerDialog
import com.example.myapplication.ui.components.ExerciseStopwatchDialog
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    viewModel: TrainingPlanViewModel,
    onStartTodayTraining: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val plan by viewModel.trainingPlan.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    // Navigation State: null means Weekly Program View, Int means Day Details View for that dayOfWeek
    var selectedDayOfWeek by remember { mutableStateOf<Int?>(null) }

    var showBonusPopup by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<PlannedExercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<PlannedExercise?>(null) }
    var dayForNewExercise by remember { mutableStateOf<Int?>(null) }
    var timerExercise by remember { mutableStateOf<PlannedExercise?>(null) }

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
            containerColor = TranslucentSlate,
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

    if (timerExercise != null) {
        if (timerExercise!!.trackingType == ExerciseTrackingType.SECONDS) {
            ExerciseTimerDialog(
                exerciseName = timerExercise!!.name,
                totalSets = timerExercise!!.sets ?: 1,
                initialSeconds = timerExercise!!.seconds ?: 60,
                soundManager = soundManager,
                onDismiss = { 
                    soundManager.playClick()
                    timerExercise = null 
                },
                onComplete = {
                    soundManager.playQuestComplete()
                    viewModel.toggleExerciseCompletion(timerExercise!!)
                    timerExercise = null
                }
            )
        } else if (timerExercise!!.trackingType == ExerciseTrackingType.DISTANCE) {
            ExerciseStopwatchDialog(
                exerciseName = timerExercise!!.name,
                targetDistance = timerExercise!!.distanceKm ?: 0.0,
                soundManager = soundManager,
                onDismiss = {
                    soundManager.playClick()
                    timerExercise = null
                },
                onFinish = { _ ->
                    soundManager.playQuestComplete()
                    viewModel.toggleExerciseCompletion(timerExercise!!)
                    timerExercise = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (selectedDayOfWeek == null) "Today's Training" else "Today's Exercises", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        soundManager.playClick()
                        if (selectedDayOfWeek == null) {
                            onNavigateBack()
                        } else {
                            selectedDayOfWeek = null
                        }
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
            if (selectedDayOfWeek == null) {
                // Weekly Program View
                WeeklyProgramContent(
                    plan = plan,
                    allExercises = allExercises,
                    onDayClick = { dayOfWeek ->
                        soundManager.playClick()
                        selectedDayOfWeek = dayOfWeek
                    }
                )
            } else {
                // Day Details View
                val dayOfWeek = selectedDayOfWeek!!
                val dayExercises = allExercises.filter { it.dayOfWeek == dayOfWeek }
                DayDetailsContent(
                    dayOfWeek = dayOfWeek,
                    exercises = dayExercises,
                    onStartTodayTraining = onStartTodayTraining,
                    onAddExercise = {
                        soundManager.playClick()
                        dayForNewExercise = dayOfWeek
                    },
                    onEditExercise = { exercise ->
                        soundManager.playClick()
                        exerciseToEdit = exercise
                    },
                    onDeleteExercise = { exercise ->
                        soundManager.playClick()
                        exerciseToDelete = exercise
                    },
                    onToggleExercise = { exercise ->
                        if (!exercise.isCompleted) soundManager.playQuestComplete()
                        viewModel.toggleExerciseCompletion(exercise)
                    },
                    onStartTimer = { exercise ->
                        soundManager.playClick()
                        timerExercise = exercise
                    }
                )
            }

            // Day Reward Celebration Overlay
            AnimatedVisibility(
                visible = dayRewardAnimation != null,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 1.1f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                dayRewardAnimation?.let { xp ->
                    ExorkNeumorphicCard(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("DAY COMPLETE", color = ChromeSilver, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
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
fun WeeklyProgramContent(
    plan: List<TrainingDay>,
    allExercises: List<PlannedExercise>,
    onDayClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 24.dp,
            bottom = 120.dp,
            start = 24.dp,
            end = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WeeklyProgressHeader(plan, allExercises)
        }

        items(plan) { day ->
            val dayExercisesCount = allExercises.count { it.dayOfWeek == day.dayOfWeek }
            DayNavigationCard(
                day = day,
                exerciseCount = dayExercisesCount,
                onClick = { onDayClick(day.dayOfWeek) }
            )
        }
    }
}

@Composable
fun DayNavigationCard(
    day: TrainingDay,
    exerciseCount: Int,
    onClick: () -> Unit
) {
    val dayName = getDayName(day.dayOfWeek)
    val calendar = Calendar.getInstance()
    val todayOfWeek = getCurrentDayOfWeek()
    
    val isCompletedThisWeek = day.isCompleted && 
            day.lastCompletedWeek == calendar.get(Calendar.WEEK_OF_YEAR) && 
            day.lastCompletedYear == calendar.get(Calendar.YEAR)
    val isToday = day.dayOfWeek == todayOfWeek

    ExorkNeumorphicCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    dayName.uppercase(),
                    color = if (isToday) ChromeSilver else Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp
                )
                Text(
                    if (exerciseCount == 0) "Rest Day" else "$exerciseCount Exercises",
                    color = TitaniumGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCompletedThisWeek) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChromeSilver, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TitaniumGray)
            }
        }
    }
}

@Composable
fun DayDetailsContent(
    dayOfWeek: Int,
    exercises: List<PlannedExercise>,
    onStartTodayTraining: () -> Unit,
    onAddExercise: () -> Unit,
    onEditExercise: (PlannedExercise) -> Unit,
    onDeleteExercise: (PlannedExercise) -> Unit,
    onToggleExercise: (PlannedExercise) -> Unit,
    onStartTimer: (PlannedExercise) -> Unit
) {
    val todayOfWeek = getCurrentDayOfWeek()
    val isCompletable = dayOfWeek == todayOfWeek

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 120.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (exercises.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text("NO EXERCISES SCHEDULED", color = TitaniumGray.copy(alpha = 0.4f), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
        } else {
            items(exercises) { exercise ->
                PlannedExerciseItemCompact(
                    exercise = exercise,
                    isCompletable = isCompletable,
                    onEdit = { onEditExercise(exercise) },
                    onDelete = { onDeleteExercise(exercise) },
                    onToggle = { onToggleExercise(exercise) },
                    onStartTimer = { onStartTimer(exercise) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            ExorkChromeButton(
                text = "ADD EXERCISE",
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PlannedExerciseItemCompact(
    exercise: PlannedExercise,
    isCompletable: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onStartTimer: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val isCompletedNow = exercise.isCompleted && 
            exercise.lastCompletedWeek == calendar.get(Calendar.WEEK_OF_YEAR) && 
            exercise.lastCompletedYear == calendar.get(Calendar.YEAR)

    var showMenu by remember { mutableStateOf(false) }

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exercise.name.uppercase(),
                        color = if (isCompletedNow) ChromeSilver else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    if (isCompletedNow) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = ChromeSilver, modifier = Modifier.size(16.dp))
                    }
                }
                
                Text(
                    text = when (exercise.trackingType) {
                        ExerciseTrackingType.REPS -> "${exercise.sets} × ${exercise.reps} Reps"
                        ExerciseTrackingType.SECONDS -> "${exercise.sets} × ${exercise.seconds} Sec"
                        ExerciseTrackingType.DISTANCE -> "${exercise.distanceKm} KM"
                    },
                    color = TitaniumGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if ((exercise.trackingType == ExerciseTrackingType.SECONDS || exercise.trackingType == ExerciseTrackingType.DISTANCE) && !isCompletedNow && isCompletable) {
                    Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onStartTimer,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = ChromeSilver)
                        ) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (exercise.trackingType == ExerciseTrackingType.DISTANCE) "START RUN" else "START TIMER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCompletable || isCompletedNow) {
                    IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isCompletedNow) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle",
                            tint = if (isCompletedNow) ChromeSilver else TitaniumGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TitaniumGray, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(ObsidianVoid)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.White, fontWeight = FontWeight.Bold) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = ChromeSilver) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
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

    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "WEEKLY PROGRESS",
                color = TitaniumGray,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "$completedCount / $totalTrainingDays Days Completed",
                color = Color.White,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ExorkNeumorphicProgressBar(
                progress = if (totalTrainingDays > 0) completedCount.toFloat() / totalTrainingDays else 0f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xF2121215), // 95% opacity LeatherDeep
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TitaniumGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        1.5.dp, 
                        Brush.linearGradient(listOf(ChromeSilver, Color(0xFF3A3A3E)))
                    ), 
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = if (exercise == null) "NEW EXERCISE" else "EDIT EXERCISE",
                color = Color.White,
                style = TextStyle(
                    fontSize = 24.sp, 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 2.sp,
                    shadow = Shadow(Color.Black, blurRadius = 10f)
                )
            )

            // Exercise Name Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("NAME", color = ChromeSilver, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Diamond Pushups", color = TitaniumGray) },
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
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Tracking Type Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TRACKING TYPE", color = ChromeSilver, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseTypeChip(
                        label = "REPS", 
                        selected = trackingType == ExerciseTrackingType.REPS,
                        modifier = Modifier.weight(1f)
                    ) { trackingType = ExerciseTrackingType.REPS }
                    
                    ExerciseTypeChip(
                        label = "SECONDS", 
                        selected = trackingType == ExerciseTrackingType.SECONDS,
                        modifier = Modifier.weight(1f)
                    ) { trackingType = ExerciseTrackingType.SECONDS }
                    
                    ExerciseTypeChip(
                        label = "DISTANCE", 
                        selected = trackingType == ExerciseTrackingType.DISTANCE,
                        modifier = Modifier.weight(1f)
                    ) { trackingType = ExerciseTrackingType.DISTANCE }
                }
            }

            // Dynamic Quantitative Fields
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (trackingType) {
                    ExerciseTrackingType.REPS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SpaciousInput("SETS", sets, { sets = it }, Modifier.weight(1f))
                            SpaciousInput("REPS", reps, { reps = it }, Modifier.weight(1f))
                        }
                    }
                    ExerciseTrackingType.SECONDS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SpaciousInput("SETS", sets, { sets = it }, Modifier.weight(1f))
                            SpaciousInput("SECONDS", seconds, { seconds = it }, Modifier.weight(1f))
                        }
                    }
                    ExerciseTrackingType.DISTANCE -> {
                        SpaciousInput("DISTANCE (KM)", distance, { distance = it }, Modifier.fillMaxWidth(), isDecimal = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
                
                ExorkChromeButton(
                    text = if (exercise == null) "ADD EXERCISE" else "SAVE CHANGES",
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
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ExerciseTypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color.Transparent else Color(0xFF1A1A1E),
        border = if (selected) null else BorderStroke(1.dp, ChromeSilver.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = if (selected) Modifier.background(Brush.verticalGradient(listOf(ChromeSilver, TitaniumGray))) else Modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) ObsidianVoid else TitaniumGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SpaciousInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, isDecimal: Boolean = false) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label, 
            color = ChromeSilver, 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Bold,
            style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 4f))
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (isDecimal) {
                    if (input.isEmpty() || input.toDoubleOrNull() != null || input == ".") onValueChange(input)
                } else {
                    if (input.all { it.isDigit() }) onValueChange(input)
                }
            },
            modifier = Modifier.fillMaxWidth(),
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
}

@Composable
fun WeeklyBonusDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, ElectricCyan, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MonarchSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TRAINING REGIMEN COMPLETE",
                    color = ElectricCyan,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(ElectricCyan, blurRadius = 15f)
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
                    color = ManaPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ManaPurple)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BONUS REWARD", color = ManaPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = ManaPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONTINUE", color = MonarchSlate, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> ""
    }
}

private fun getCurrentDayOfWeek(): Int {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 7
    }
}
