package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.DayPerformance
import com.exork.app.data.FitnessRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

import kotlinx.coroutines.ExperimentalCoroutinesApi

class AnalyticsViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.US).format(Date()))
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val workoutDays: StateFlow<Set<String>> = _currentMonth
        .flatMapLatest { repository.getMonthlyWorkoutDays(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val todayCategoryDistribution: StateFlow<Map<String, Float>> = repository.getTodayCategoryDistribution()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val rolling7DayPerformance: StateFlow<List<DayPerformance>> = repository.getRolling7DayPerformance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        val calendar = getCalendarFromMonth(_currentMonth.value)
        calendar.add(Calendar.MONTH, 1)
        _currentMonth.value = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
    }

    fun previousMonth() {
        val calendar = getCalendarFromMonth(_currentMonth.value)
        calendar.add(Calendar.MONTH, -1)
        _currentMonth.value = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
    }

    fun resetToCurrentMonth() {
        _currentMonth.value = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    private fun getCalendarFromMonth(month: String): Calendar {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val date = sdf.parse(month) ?: Date()
        return Calendar.getInstance().apply { time = date }
    }
}
