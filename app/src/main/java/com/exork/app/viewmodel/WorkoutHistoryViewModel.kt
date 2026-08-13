package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.WorkoutWithExercises
import kotlinx.coroutines.flow.*

class WorkoutHistoryViewModel(private val repository: FitnessRepository) : ViewModel() {
    val allWorkouts: StateFlow<List<WorkoutWithExercises>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
