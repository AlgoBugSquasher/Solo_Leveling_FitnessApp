package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.JourneyEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class JourneyViewModel(private val repository: FitnessRepository) : ViewModel() {

    val journeyEvents: StateFlow<List<JourneyEvent>> = repository.allJourneyEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
