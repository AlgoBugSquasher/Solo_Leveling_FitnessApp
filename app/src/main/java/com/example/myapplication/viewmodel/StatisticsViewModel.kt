package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeData
import com.example.myapplication.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StatisticsViewModel(private val repository: FitnessRepository) : ViewModel() {

    val user: StateFlow<User?> = repository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedBadges: StateFlow<List<Badge>> = repository.user
        .filterNotNull()
        .map { user ->
            BadgeData.allBadges.filter { user.level >= it.requiredLevel }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBadges = BadgeData.allBadges.size

    val highestBadge: StateFlow<Badge?> = unlockedBadges
        .map { it.maxByOrNull { b -> b.requiredLevel } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
