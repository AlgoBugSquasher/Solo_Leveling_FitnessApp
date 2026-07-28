package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Exercise
import com.example.myapplication.model.ExerciseCategory
import com.example.myapplication.model.ExerciseTrackingType
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.util.XpCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: FitnessRepository) : ViewModel() {

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

            // 1. Calculate XP gained
            val xpGained = XpCalculator.calculateWorkoutXp(workoutExercises, currentUser.streak)

            // 2. Track total stats for this workout
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
                    ExerciseCategory.OTHER -> {
                        // For OTHER, we might want to still track if they chose a tracking type that fits
                        // But per requirements, we'll stick to manual category selection.
                    }
                }
            }

            // 3. Record Progress via Repository (Centralized Logic)
            repository.recordProgress(
                pushups = addedPushups,
                pullups = addedPullups,
                plankSeconds = addedPlankTime,
                distanceKm = addedDistance,
                xpGained = xpGained,
                isWorkout = true
            )

            // 4. Save Workout Entity for history
            val workoutEntity = WorkoutEntity(date = System.currentTimeMillis(), totalXpGained = xpGained)
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

            _eventFlow.emit(WorkoutEvent.WorkoutCompleted(xpGained))
            
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
