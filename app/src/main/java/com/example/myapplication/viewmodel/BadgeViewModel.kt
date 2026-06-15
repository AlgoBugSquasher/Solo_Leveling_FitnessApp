package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BadgeViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _newBadgeUnlocked = MutableSharedFlow<Badge>()
    val newBadgeUnlocked = _newBadgeUnlocked.asSharedFlow()

    private var lastObservedLevel = 1

    val badges: StateFlow<List<Badge>> = repository.user
        .filterNotNull()
        .map { user ->
            val currentLevel = user.level
            
            // Check for new unlocks
            if (currentLevel > lastObservedLevel) {
                BadgeData.allBadges.forEach { badge ->
                    if (badge.requiredLevel in (lastObservedLevel + 1)..currentLevel) {
                        viewModelScope.launch {
                            _newBadgeUnlocked.emit(badge)
                        }
                    }
                }
                lastObservedLevel = currentLevel
            }

            BadgeData.allBadges.map { badge ->
                badge.copy(isUnlocked = currentLevel >= badge.requiredLevel)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val initialUser = repository.user.first()
            if (initialUser != null) {
                lastObservedLevel = initialUser.level
            }
        }
    }
}
