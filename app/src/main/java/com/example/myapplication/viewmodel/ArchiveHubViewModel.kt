package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.BadgeData
import com.example.myapplication.model.AchievementData
import kotlinx.coroutines.flow.*

data class ArchiveProgressState(
    val badgesEarned: Int = 0,
    val totalBadges: Int = BadgeData.allBadges.size,
    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = AchievementData.allAchievements.size,
    val titlesEarned: Int = 0,
    val totalTitles: Int = 0,
    val completionPercentage: Int = 0
)

class ArchiveHubViewModel(private val repository: FitnessRepository) : ViewModel() {

    val progressState: StateFlow<ArchiveProgressState> = combine(
        repository.user.filterNotNull(),
        repository.allTitles
    ) { user, titles ->
        val badgesEarned = BadgeData.allBadges.count { user.level >= it.requiredLevel }
        val achievementsUnlocked = AchievementData.allAchievements.count { it.isUnlocked(user) }
        val titlesEarned = titles.count { it.isUnlocked }
        val totalTitles = titles.size

        val totalEarned = badgesEarned + achievementsUnlocked + titlesEarned
        val totalItems = BadgeData.allBadges.size + AchievementData.allAchievements.size + totalTitles
        
        val percentage = if (totalItems > 0) (totalEarned * 100) / totalItems else 0

        ArchiveProgressState(
            badgesEarned = badgesEarned,
            achievementsUnlocked = achievementsUnlocked,
            titlesEarned = titlesEarned,
            totalTitles = totalTitles,
            completionPercentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchiveProgressState())
}
