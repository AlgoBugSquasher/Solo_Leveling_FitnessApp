package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Exercise
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
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

            // 1. Calculate new streak
            val newStreak = calculateNewStreak(currentUser.lastWorkoutDate, currentUser.streak)

            // 2. Calculate XP gained
            val xpGained = XpCalculator.calculateWorkoutXp(workoutExercises, newStreak)

            // 3. Track total stats
            var addedPushups = 0
            var addedPullups = 0
            var addedPlankTime = 0

            workoutExercises.forEach { ex ->
                val totalReps = (ex.reps ?: 0) * ex.sets
                val totalDuration = (ex.duration ?: 0) * ex.sets
                
                when {
                    ex.name.contains("Push-up", ignoreCase = true) -> addedPushups += totalReps
                    ex.name.contains("Pull-up", ignoreCase = true) || ex.name.contains("Chin-up", ignoreCase = true) -> addedPullups += totalReps
                    ex.name.contains("Plank", ignoreCase = true) -> addedPlankTime += totalDuration
                }
            }

            // 4. Update Level and XP
            var newXp = currentUser.xp + xpGained
            var newLevel = currentUser.level
            var leveledUp = false

            while (newXp >= XpCalculator.calculateRequiredXP(newLevel)) {
                newXp -= XpCalculator.calculateRequiredXP(newLevel)
                newLevel++
                leveledUp = true
            }

            val newRank = when {
                newLevel >= 20 -> "Advanced"
                newLevel >= 10 -> "Intermediate"
                else -> "Beginner"
            }

            val updatedUser = currentUser.copy(
                xp = newXp,
                level = newLevel,
                streak = newStreak,
                rank = newRank,
                pushups = currentUser.pushups + addedPushups,
                pullups = currentUser.pullups + addedPullups,
                plankTime = currentUser.plankTime + addedPlankTime,
                totalXpEarned = currentUser.totalXpEarned + xpGained,
                totalWorkouts = currentUser.totalWorkouts + 1,
                highestStreak = maxOf(currentUser.highestStreak, newStreak),
                lastWorkoutDate = System.currentTimeMillis()
            )

            // 5. Save Workout to DB
            val workoutEntity = WorkoutEntity(date = System.currentTimeMillis(), totalXpGained = xpGained)
            val exerciseEntities = workoutExercises.map { 
                ExerciseEntity(
                    workoutId = 0, // Will be set by DAO
                    name = it.name,
                    reps = it.reps,
                    sets = it.sets,
                    duration = it.duration
                )
            }
            repository.insertWorkout(workoutEntity, exerciseEntities)

            // 6. Update User
            repository.updateUser(updatedUser)
            
            // 7. Check Title Unlocks
            val newlyUnlockedTitles = repository.checkAndUnlockTitles(newStreak)
            newlyUnlockedTitles.forEach { title ->
                // HomeViewModel usually handles UI events, but WorkoutViewModel can emit some or Home can observe user/titles.
                // For simplicity, let's have WorkoutViewModel emit a new event type if needed, 
                // OR better: HomeScreen collects uiEvent from HomeViewModel. 
                // Let's add a mechanism to notify about title unlocks.
            }

            // Check Abilities
            repository.checkAndUnlockAbilities(updatedUser)

            _eventFlow.emit(WorkoutEvent.WorkoutCompleted(xpGained))
            if (leveledUp) {
                _eventFlow.emit(WorkoutEvent.LevelUp(newLevel))
            }
            
            // Reset exercises
            _exercises.value = emptyList()
        }
    }

    private fun calculateNewStreak(lastWorkoutDate: Long, currentStreak: Int): Int {
        val now = System.currentTimeMillis()
        if (lastWorkoutDate == 0L) return 1

        val lastDate = Calendar.getInstance().apply { timeInMillis = lastWorkoutDate }
        val currentDate = Calendar.getInstance().apply { timeInMillis = now }

        // Check if same day
        if (lastDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            lastDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return currentStreak
        }

        // Check if yesterday
        lastDate.add(Calendar.DAY_OF_YEAR, 1)
        if (lastDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            lastDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return currentStreak + 1
        }

        return 1 // Streak broken
    }

    sealed class WorkoutEvent {
        data class WorkoutCompleted(val xpGained: Int) : WorkoutEvent()
        data class LevelUp(val newLevel: Int) : WorkoutEvent()
    }
}
