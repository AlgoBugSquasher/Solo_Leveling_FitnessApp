package com.exork.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exork.app.data.FitnessRepository
import com.exork.app.model.Achievement
import com.exork.app.model.AchievementData
import com.exork.app.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AchievementViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _newAchievementUnlocked = MutableSharedFlow<Achievement>()
    val newAchievementUnlocked = _newAchievementUnlocked.asSharedFlow()

    private val lastSeenStats = MutableStateFlow<User?>(null)

    init {
        // Monitor for new achievement unlocks
        viewModelScope.launch {
            repository.user.collect { currentUser ->
                if (currentUser != null) {
                    val previousUser = lastSeenStats.value
                    if (previousUser != null) {
                        AchievementData.allAchievements.forEach { achievement ->
                            val wasLocked = !achievement.isUnlocked(previousUser)
                            val isUnlocked = achievement.isUnlocked(currentUser)
                            if (wasLocked && isUnlocked) {
                                _newAchievementUnlocked.emit(achievement)
                            }
                        }
                    }
                    lastSeenStats.value = currentUser
                }
            }
        }
    }

    val unlockedCount: StateFlow<Int> = user
        .map { u -> u?.let { AchievementData.allAchievements.count { it.isUnlocked(u) } } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount = AchievementData.allAchievements.size
}
