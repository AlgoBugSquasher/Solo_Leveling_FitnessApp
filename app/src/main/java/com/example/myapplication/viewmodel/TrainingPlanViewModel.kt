package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.model.WeeklyBonusEntity
import com.example.myapplication.model.User
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

    fun updateTrainingDay(day: TrainingDay) {
        viewModelScope.launch {
            repository.updateTrainingDay(day)
        }
    }

    fun checkWeeklyCompletion() {
        viewModelScope.launch {
            val plan = trainingPlan.value
            if (plan.isEmpty()) return@launch

            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            val activeTrainingDays = plan.filter { it.pushups > 0 || it.pullups > 0 || it.plankSeconds > 0 }
            if (activeTrainingDays.isEmpty()) return@launch

            val allCompleted = activeTrainingDays.all { 
                it.isCompleted && it.lastCompletedWeek == currentWeek && it.lastCompletedYear == currentYear 
            }

            if (allCompleted) {
                val bonus = weeklyBonus.value
                if (bonus == null || bonus.lastBonusWeek != currentWeek || bonus.lastBonusYear != currentYear) {
                    // Grant Bonus
                    val currentUser = user.value ?: return@launch
                    repository.updateUser(currentUser.copy(
                        xp = currentUser.xp + 500,
                        totalXpEarned = currentUser.totalXpEarned + 500
                    ))
                    repository.updateWeeklyBonus(WeeklyBonusEntity(id = 0, lastBonusWeek = currentWeek, lastBonusYear = currentYear))
                    _showBonusDialog.emit(true)
                }
            }
        }
    }

    fun trackDailyProgress(addedPushups: Int, addedPullups: Int, addedPlankSeconds: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 7
            }
            
            val plan = trainingPlan.value
            val todayPlan = plan.find { it.dayOfWeek == dayOfWeek } ?: return@launch
            
            if (todayPlan.pushups == 0 && todayPlan.pullups == 0 && todayPlan.plankSeconds == 0) return@launch
            
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)
            
            // Check if already completed this week
            if (todayPlan.isCompleted && todayPlan.lastCompletedWeek == currentWeek && todayPlan.lastCompletedYear == currentYear) return@launch
            
            if (addedPushups >= todayPlan.pushups && addedPullups >= todayPlan.pullups && addedPlankSeconds >= todayPlan.plankSeconds) {
                repository.updateTrainingDay(todayPlan.copy(
                    isCompleted = true,
                    lastCompletedWeek = currentWeek,
                    lastCompletedYear = currentYear
                ))
                checkWeeklyCompletion()
            }
        }
    }
}
