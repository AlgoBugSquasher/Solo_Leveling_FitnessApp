package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.model.WeeklyBonusEntity
import com.example.myapplication.model.User
import com.example.myapplication.model.PlannedExercise
import com.example.myapplication.model.ExerciseTrackingType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class TrainingPlanViewModel(private val repository: FitnessRepository) : ViewModel() {

    val trainingPlan: StateFlow<List<TrainingDay>> = repository.trainingPlan
        .onEach { list ->
            if (list.isEmpty()) {
                seedTrainingPlan()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<PlannedExercise>> = repository.allPlannedExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyBonus: StateFlow<WeeklyBonusEntity?> = repository.weeklyBonus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showBonusDialog = MutableSharedFlow<Boolean>()
    val showBonusDialog = _showBonusDialog.asSharedFlow()

    private fun seedTrainingPlan() {
        viewModelScope.launch {
            val days = (1..7).map { TrainingDay(dayOfWeek = it) }
            repository.insertTrainingDays(days)
        }
    }

    fun addExercise(
        dayOfWeek: Int,
        name: String,
        trackingType: ExerciseTrackingType,
        sets: Int? = null,
        reps: Int? = null,
        seconds: Int? = null,
        distanceKm: Double? = null
    ) {
        viewModelScope.launch {
            val exercise = PlannedExercise(
                dayOfWeek = dayOfWeek,
                name = name,
                trackingType = trackingType,
                sets = sets,
                reps = reps,
                seconds = seconds,
                distanceKm = distanceKm
            )
            repository.insertPlannedExercise(exercise)
            // Reset day completion status when plan changes
            resetDayCompletion(dayOfWeek)
        }
    }

    fun updateExercise(exercise: PlannedExercise) {
        viewModelScope.launch {
            repository.updatePlannedExercise(exercise)
            checkDailyCompletion(exercise.dayOfWeek)
        }
    }

    fun deleteExercise(exercise: PlannedExercise) {
        viewModelScope.launch {
            repository.deletePlannedExercise(exercise)
            checkDailyCompletion(exercise.dayOfWeek)
        }
    }

    fun toggleExerciseCompletion(exercise: PlannedExercise) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val todayOfWeek = getTodayOfWeek()
            if (exercise.dayOfWeek != todayOfWeek) return@launch // Future day protection

            val wasCompletedInCurrentWeek = exercise.isCompleted && 
                    exercise.lastCompletedWeek == currentWeek && 
                    exercise.lastCompletedYear == currentYear

            val isNowCompleting = !wasCompletedInCurrentWeek

            val updatedExercise = exercise.copy(
                isCompleted = isNowCompleting,
                lastCompletedWeek = currentWeek,
                lastCompletedYear = currentYear
            )
            repository.updatePlannedExercise(updatedExercise)

            if (isNowCompleting) {
                // Record Stats to User Progression
                val sets = exercise.sets ?: 1
                val reps = exercise.reps ?: 0
                val seconds = exercise.seconds ?: 0
                val name = exercise.name.lowercase().trim()
                
                viewModelScope.launch {
                    if (repository.getEventCountByType("FIRST_TRAINING_PLAN_EXERCISE") == 0) {
                        repository.recordJourneyEvent("FIRST_TRAINING_PLAN_EXERCISE", "FIRST PLAN COMPLETED", "Successfully finished a planned training item.", "📋")
                    }
                }

                // Flexible mapping for various naming conventions
                val addedPushups = if (name.contains("pushup") || name.contains("push up") || name.contains("push-up")) reps * sets else 0
                val addedPullups = if (name.contains("pullup") || name.contains("pull up") || name.contains("pull-up") || 
                                       name.contains("chinup") || name.contains("chin up") || name.contains("chin-up")) reps * sets else 0
                val addedPlank = if (name.contains("plank")) seconds * sets else 0
                val addedDist = exercise.distanceKm ?: 0.0

                android.util.Log.d("TrainingPlanProgression", """
                    [EXERCISE COMPLETION DETECTED]
                    - Raw Name: ${exercise.name}
                    - Processed Name: $name
                    - Tracking Type: ${exercise.trackingType}
                    - Input Values: Sets($sets), Reps($reps), Sec($seconds), KM(${exercise.distanceKm})
                    - Mapped Statistics:
                        Pushups: $addedPushups
                        Pullups: $addedPullups
                        Plank Sec: $addedPlank
                        Distance KM: $addedDist
                """.trimIndent())

                repository.recordProgress(
                    pushups = addedPushups,
                    pullups = addedPullups,
                    plankSeconds = addedPlank,
                    distanceKm = addedDist,
                    isWorkout = false
                )
            }
            
            // To avoid race condition, we fetch the latest state or wait for flow
            // But for immediate feedback, we can calculate based on current snapshot + update
            val dayExercises = allExercises.value.filter { it.dayOfWeek == exercise.dayOfWeek }
            val updatedDayExercises = dayExercises.map { 
                if (it.id == exercise.id) updatedExercise else it 
            }
            
            performCheckDailyCompletion(exercise.dayOfWeek, updatedDayExercises)
        }
    }

    private suspend fun performCheckDailyCompletion(dayOfWeek: Int, dayExercises: List<PlannedExercise>) {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        if (dayExercises.isEmpty()) return

        val allCompleted = dayExercises.all { 
            it.isCompleted && it.lastCompletedWeek == currentWeek && it.lastCompletedYear == currentYear 
        }

        val plan = trainingPlan.value
        val day = plan.find { it.dayOfWeek == dayOfWeek }
        if (day != null) {
            val updatedDay = day.copy(
                isCompleted = allCompleted,
                lastCompletedWeek = if (allCompleted) currentWeek else day.lastCompletedWeek,
                lastCompletedYear = if (allCompleted) currentYear else day.lastCompletedYear
            )
            
            // Handle +200 XP Day Completion Reward
            var finalDay = updatedDay
            if (allCompleted && (updatedDay.lastRewardWeek != currentWeek || updatedDay.lastRewardYear != currentYear)) {
                finalDay = updatedDay.copy(
                    lastRewardWeek = currentWeek,
                    lastRewardYear = currentYear
                )
                repository.recordProgress(xpGained = 200)
            }

            repository.updateTrainingDay(finalDay)
            
            if (allCompleted) {
                // Similarly check weekly completion with updated day
                val updatedPlan = plan.map { if (it.dayOfWeek == dayOfWeek) finalDay else it }
                performCheckWeeklyCompletion(updatedPlan, allExercises.value)
            }
        }
    }

    private suspend fun checkDailyCompletion(dayOfWeek: Int) {
        val dayExercises = allExercises.value.filter { it.dayOfWeek == dayOfWeek }
        performCheckDailyCompletion(dayOfWeek, dayExercises)
    }

    private suspend fun performCheckWeeklyCompletion(plan: List<TrainingDay>, exercises: List<PlannedExercise>) {
        if (plan.isEmpty()) return

        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val activeDayOfWeeks = exercises.map { it.dayOfWeek }.distinct()
        if (activeDayOfWeeks.isEmpty()) return

        val allCompleted = activeDayOfWeeks.all { dayOfWeek ->
            val day = plan.find { it.dayOfWeek == dayOfWeek }
            day?.isCompleted == true && day.lastCompletedWeek == currentWeek && day.lastCompletedYear == currentYear
        }

        if (allCompleted) {
            val bonus = repository.getWeeklyBonusSync()
            if (bonus == null || bonus.lastBonusWeek != currentWeek || bonus.lastBonusYear != currentYear) {
                // Weekly Completion Bonus: +1000 XP
                repository.recordProgress(xpGained = 1000)
                repository.updateWeeklyBonus(WeeklyBonusEntity(id = 0, lastBonusWeek = currentWeek, lastBonusYear = currentYear))
                
                if (repository.getEventCountByType("FIRST_WEEKLY_COMPLETION") == 0) {
                    repository.recordJourneyEvent("FIRST_WEEKLY_COMPLETION", "WEEKLY REGIMEN COMPLETE", "A full week of discipline.", "🌟")
                }

                _showBonusDialog.emit(true)
            }
        }
    }

    fun checkWeeklyCompletion() {
        viewModelScope.launch {
            performCheckWeeklyCompletion(trainingPlan.value, allExercises.value)
        }
    }

    private suspend fun resetDayCompletion(dayOfWeek: Int) {
        val plan = trainingPlan.value
        val day = plan.find { it.dayOfWeek == dayOfWeek }
        if (day != null) {
            repository.updateTrainingDay(day.copy(isCompleted = false))
        }
    }

    private fun getTodayOfWeek(): Int {
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

    // This method is no longer needed in its previous form since exercises are manual now
    // But maybe keep for legacy or adaptation if needed.
    fun trackDailyProgress(addedPushups: Int, addedPullups: Int, addedPlankSeconds: Int) {
        // Implementation could check for exercises named "Pushups", etc. and auto-check them.
        // For now, let's stick to manual check as suggested by "✓ Complete" in requirements.
    }
}

