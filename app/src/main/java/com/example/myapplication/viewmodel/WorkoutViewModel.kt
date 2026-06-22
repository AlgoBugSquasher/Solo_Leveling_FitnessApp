package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Exercise
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.util.RankCalculator
import com.example.myapplication.util.XpCalculator
import java.util.Calendar
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

    fun addExercise(name: String, reps: Int?, sets: Int, duration: Int?) {
        val newExercise = Exercise(name, reps, sets, duration)
        _exercises.value = _exercises.value + newExercise
    }

    fun removeExercise(exercise: Exercise) {
        _exercises.value = _exercises.value - exercise
    }

    fun completeWorkout() {
        viewModelScope.launch {
            val currentUser = repository.user.first() ?: return@launch
            val workoutExercises = _exercises.value
            if (workoutExercises.isEmpty()) return@launch

            // 1. Calculate XP gained
            // Using placeholder streak 0 for XP calc because repository will handle real streak
            // but we need a value for XpCalculator. Maybe repository should handle xp calc too?
            // For now, let's just use current streak to keep it simple.
            val xpGained = XpCalculator.calculateWorkoutXp(workoutExercises, currentUser.streak)

            // 2. Track total stats for this workout
            var addedPushups = 0
            var addedPullups = 0
            var addedPlankTime = 0
            var addedDistance = 0.0

            workoutExercises.forEach { ex ->
                val totalReps = (ex.reps ?: 0) * ex.sets
                val totalDuration = (ex.duration ?: 0) * ex.sets
                
                when {
                    ex.name.contains("Push-up", ignoreCase = true) -> addedPushups += totalReps
                    ex.name.contains("Pull-up", ignoreCase = true) || ex.name.contains("Chin-up", ignoreCase = true) -> addedPullups += totalReps
                    ex.name.contains("Plank", ignoreCase = true) -> addedPlankTime += totalDuration
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
                    reps = it.reps,
                    sets = it.sets,
                    duration = it.duration
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
