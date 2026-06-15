package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.WorkoutWithExercises
import kotlinx.coroutines.flow.*

class WorkoutHistoryViewModel(private val repository: FitnessRepository) : ViewModel() {
    val allWorkouts: StateFlow<List<WorkoutWithExercises>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
