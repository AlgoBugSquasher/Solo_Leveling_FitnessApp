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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _pendingExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises = _pendingExercises.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

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
        _pendingExercises.value = _pendingExercises.value + newExercise
    }

    fun removeExercise(exercise: Exercise) {
        _pendingExercises.value = _pendingExercises.value - exercise
    }

    fun updateExercise(oldExercise: Exercise, newExercise: Exercise) {
        _pendingExercises.value = _pendingExercises.value.map { if (it == oldExercise) newExercise else it }
    }

    fun uploadProgress(onComplete: (Int) -> Unit) {
        if (_isUploading.value) return // Prevent multi-click spam
        val currentPending = _pendingExercises.value
        if (currentPending.isEmpty()) return

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val currentUser = repository.user.first() ?: return@launch

                // 1. Calculate XP from pending exercises
                val calculatedXp = XpCalculator.calculateWorkoutXp(currentPending, currentUser.streak)
                
                // 2. Insert Workout & Exercises into Room and update stats
                val workout = WorkoutEntity(date = System.currentTimeMillis(), totalXpGained = calculatedXp)
                val exerciseEntities = currentPending.map { 
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
                
                // Track total stats for this workout
                var addedPushups = 0
                var addedPullups = 0
                var addedPlankTime = 0
                var addedDistance = 0.0

                currentPending.forEach { ex ->
                    when (ex.category) {
                        ExerciseCategory.PUSHUPS -> addedPushups += (ex.reps ?: 0) * ex.sets
                        ExerciseCategory.PULLUPS -> addedPullups += (ex.reps ?: 0) * ex.sets
                        ExerciseCategory.PLANK -> addedPlankTime += (ex.duration ?: 0) * ex.sets
                        ExerciseCategory.CARDIO -> addedDistance += (ex.distanceKm ?: 0.0)
                        ExerciseCategory.OTHER -> {}
                    }
                }

                repository.insertWorkout(workout, exerciseEntities)
                repository.recordProgress(
                    pushups = addedPushups,
                    pullups = addedPullups,
                    plankSeconds = addedPlankTime,
                    distanceKm = addedDistance,
                    xpGained = calculatedXp,
                    isWorkout = true
                )

                // 3. Emit XP Gained UI Event for Animation & Sound
                _uiEvent.emit(UiEvent.XpGained(calculatedXp))

                // 4. CRITICAL: Clear Pending Data
                _pendingExercises.value = emptyList()

                // 5. Trigger sync & Navigate Back
                val updatedUser = repository.user.first()
                if (updatedUser != null) {
                    repository.syncToFirestore(updatedUser)
                }
                
                withContext(Dispatchers.Main) {
                    // Pass the full calculated XP for the UI animation feedback, 
                    // even if the repository correctly caps the actual balance update.
                    onComplete(calculatedXp)
                }
            } catch (e: Exception) {
                android.util.Log.e("WORKOUT_LOG", "Failed to upload progress", e)
            } finally {
                _isUploading.value = false
            }
        }
    }
}
