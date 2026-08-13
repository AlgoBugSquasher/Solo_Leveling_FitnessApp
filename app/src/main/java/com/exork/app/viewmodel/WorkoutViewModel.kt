package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.Exercise
import com.exork.app.model.ExerciseCategory
import com.exork.app.model.ExerciseTrackingType
import com.exork.app.model.ExerciseEntity
import com.exork.app.model.WorkoutEntity
import com.exork.app.util.XpCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises = _exercises.asStateFlow()

    private val _eventFlow = MutableSharedFlow<WorkoutEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun addExercise(
        name: String,
        category: ExerciseCategory,
        trackingType: ExerciseTrackingType,
        sets: Int,
        reps: Int?,
        duration: Int?,
        distanceKm: Double?
    ) {
        val newExercise = Exercise(name, category, trackingType, reps, sets, duration, distanceKm)
        _exercises.value = _exercises.value + newExercise
    }

    fun removeExercise(exercise: Exercise) {
        _exercises.value = _exercises.value - exercise
    }

    fun updateExercise(oldExercise: Exercise, newExercise: Exercise) {
        _exercises.value = _exercises.value.map { if (it == oldExercise) newExercise else it }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            val currentUser = repository.user.first() ?: return@launch
            val workoutExercises = _exercises.value
            if (workoutExercises.isEmpty()) return@launch

            // 1. Calculate raw XP gained
            val rawXpGained = XpCalculator.calculateWorkoutXp(workoutExercises, currentUser.streak)

            // 2. Synchronize with Daily Cap (+250 XP)
            val maxCustomXp = 250
            val currentCustomXpToday = currentUser.customXpEarnedToday
            val remainingCap = (maxCustomXp - currentCustomXpToday).coerceAtLeast(0)
            val actualXpToAward = rawXpGained.coerceAtMost(remainingCap)

            // 3. Track total stats for this workout
            var addedPushups = 0
            var addedPullups = 0
            var addedPlankTime = 0
            var addedDistance = 0.0

            workoutExercises.forEach { ex ->
                when (ex.category) {
                    ExerciseCategory.PUSHUPS -> addedPushups += (ex.reps ?: 0) * ex.sets
                    ExerciseCategory.PULLUPS -> addedPullups += (ex.reps ?: 0) * ex.sets
                    ExerciseCategory.PLANK -> addedPlankTime += (ex.duration ?: 0) * ex.sets
                    ExerciseCategory.CARDIO -> addedDistance += (ex.distanceKm ?: 0.0)
                    ExerciseCategory.OTHER -> {}
                }
            }

            // 4. Record Progress via Repository
            // We pass the raw XP, the repository will handle the capping internally again for safety,
            // but we ensure consistency by calculating actualXpToAward here for the UI.
            repository.recordProgress(
                pushups = addedPushups,
                pullups = addedPullups,
                plankSeconds = addedPlankTime,
                distanceKm = addedDistance,
                xpGained = rawXpGained,
                isWorkout = true
            )

            // 5. Save Workout Entity for history
            val workoutEntity = WorkoutEntity(date = System.currentTimeMillis(), totalXpGained = actualXpToAward)
            val exerciseEntities = workoutExercises.map { 
                ExerciseEntity(
                    workoutId = 0,
                    name = it.name,
                    category = it.category,
                    trackingType = it.trackingType,
                    reps = it.reps,
                    sets = it.sets,
                    duration = it.duration,
                    distanceKm = it.distanceKm
                )
            }
            repository.insertWorkout(workoutEntity, exerciseEntities)

            // Emit the actual awarded XP for the celebratory popup
            _eventFlow.emit(WorkoutEvent.WorkoutCompleted(actualXpToAward))
            
            // Reset exercises
            _exercises.value = emptyList()
        }
    }

    sealed class WorkoutEvent {
        data class WorkoutCompleted(val xpGained: Int) : WorkoutEvent()
        data class LevelUp(val newLevel: Int) : WorkoutEvent()
        data class NewPersonalRecord(val recordName: String, val oldValue: Int, val newValue: Int) : WorkoutEvent()
    }
}
