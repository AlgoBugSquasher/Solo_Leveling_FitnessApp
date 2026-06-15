package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Ability
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeData
import com.example.myapplication.model.Title
import com.example.myapplication.model.TitleData
import com.example.myapplication.model.DailyQuest
import com.example.myapplication.model.User
import com.example.myapplication.util.XpCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Events for the UI to handle, such as showing animations or dialogs.
 */
sealed class UiEvent {
    data class LevelUp(val oldLevel: Int, val newLevel: Int) : UiEvent()
    data class AbilityUnlocked(val ability: Ability) : UiEvent()
    data class TitleUnlocked(val title: Title) : UiEvent()
    data class BadgeUnlocked(val badge: Badge) : UiEvent()
}

/**
 * ViewModel for the Home Screen.
 * Handles user progress, XP calculation, and daily quest management.
 */
class HomeViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val badgeMilestones = listOf(1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 65, 70, 80, 90, 100)
    
    private var lastSeenStreak = 0

    val user: StateFlow<User> = repository.user
        .onEach { 
            if (it == null) {
                viewModelScope.launch {
                    repository.insertUser(User(id = 0, level = 1, xp = 0, streak = 0, rank = "Beginner"))
                }
            } else {
                // Monitor for title unlocks
                if (it.streak > lastSeenStreak) {
                    val newlyUnlocked = repository.checkAndUnlockTitles(it.streak)
                    newlyUnlocked.forEach { title ->
                        _uiEvent.emit(UiEvent.TitleUnlocked(title))
                    }
                    lastSeenStreak = it.streak
                }
            }
        }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User())

    init {
        seedTitles()
    }

    private fun seedTitles() {
        viewModelScope.launch {
            val list = repository.allTitles.first()
            if (list.isEmpty()) {
                repository.insertTitles(TitleData.allTitles)
            }
        }
    }

    private val _dailyQuests = MutableStateFlow(
        listOf(
            DailyQuest(1, "Push-ups", "20 reps", 50),
            DailyQuest(2, "Pull-ups", "10 reps", 75),
            DailyQuest(3, "Plank", "1 min", 40)
        )
    )
    val dailyQuests: StateFlow<List<DailyQuest>> = _dailyQuests.asStateFlow()

    fun completeQuest(questId: Int) {
        val currentQuests = _dailyQuests.value
        val quest = currentQuests.find { it.id == questId }
        if (quest != null && !quest.isCompleted) {
            val updatedQuests = currentQuests.map {
                if (it.id == questId) it.copy(isCompleted = true) else it
            }
            _dailyQuests.value = updatedQuests
            addXP(quest.xpReward)
            
            // Check if all completed for bonus
            if (updatedQuests.all { it.isCompleted }) {
                addXP(100) // Bonus XP
            }
        }
    }

    /**
     * Logic to add XP and handle leveling up.
     * Level thresholds increase as the user levels up.
     */
    private fun addXP(amount: Int) {
        viewModelScope.launch {
            val currentUser = user.value
            val previousLevel = currentUser.level
            var newXp = currentUser.xp + amount
            var newLevel = previousLevel

            while (newXp >= XpCalculator.calculateRequiredXP(newLevel)) {
                newXp -= XpCalculator.calculateRequiredXP(newLevel)
                newLevel++
            }

            // Detect level up and badge unlocks
            if (newLevel > previousLevel) {
                // 1. Emit Level Up Event first
                _uiEvent.emit(UiEvent.LevelUp(previousLevel, newLevel))

                // 2. Emit Badge Unlocks
                badgeMilestones.forEach { milestone ->
                    if (milestone in (previousLevel + 1)..newLevel) {
                        val unlockedBadge = BadgeData.allBadges.find { it.requiredLevel == milestone }
                        unlockedBadge?.let {
                            _uiEvent.emit(UiEvent.BadgeUnlocked(it))
                        }
                    }
                }
            }

            val newRank = when {
                newLevel >= 20 -> "Advanced"
                newLevel >= 10 -> "Intermediate"
                else -> "Beginner"
            }

            repository.updateUser(currentUser.copy(
                xp = newXp,
                level = newLevel,
                rank = newRank,
                totalXpEarned = currentUser.totalXpEarned + amount
            ))
        }
    }
}
