package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.JourneyEvent
import com.example.myapplication.model.JourneyEventType
import com.example.myapplication.model.JourneyRarity
import kotlinx.coroutines.flow.*

enum class JourneyFilter(val displayName: String) {
    ALL("All"),
    LEVELS("Levels"),
    ACHIEVEMENTS("Achievements"),
    RANKS("Ranks"),
    RECORDS("Records"),
    SYSTEM("System"),
    EVENTS("Events")
}

data class JourneySummary(
    val startDate: Long,
    val currentRank: String,
    val currentLevel: Int,
    val totalMilestones: Int,
    val legendaryUnlocks: Int,
    val totalXpEarned: Int,
    val longestStreak: Int
)

class JourneyViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(JourneyFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    val user = repository.user.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val journeyEvents: StateFlow<List<JourneyEvent>> = combine(
        repository.allJourneyEvents,
        _selectedFilter
    ) { events, filter ->
        when (filter) {
            JourneyFilter.ALL -> events
            JourneyFilter.LEVELS -> events.filter { it.eventType == JourneyEventType.LEVEL_UP }
            JourneyFilter.ACHIEVEMENTS -> events.filter { it.eventType == JourneyEventType.ACHIEVEMENT || it.eventType == JourneyEventType.BADGE }
            JourneyFilter.RANKS -> events.filter { it.eventType == JourneyEventType.RANK_UP }
            JourneyFilter.RECORDS -> events.filter { it.eventType == JourneyEventType.PR }
            JourneyFilter.SYSTEM -> events.filter { it.eventType == JourneyEventType.SYSTEM || it.eventType == JourneyEventType.JOURNEY_START }
            JourneyFilter.EVENTS -> events.filter { it.eventType == JourneyEventType.SPECIAL_EVENT || it.eventType == JourneyEventType.BOSS_EVENT }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<JourneySummary?> = combine(
        repository.allJourneyEvents,
        user
    ) { events, user ->
        if (user == null) return@combine null
        
        JourneySummary(
            startDate = events.lastOrNull()?.timestamp ?: System.currentTimeMillis(),
            currentRank = user.rank,
            currentLevel = user.level,
            totalMilestones = events.size,
            legendaryUnlocks = events.count { it.rarity == JourneyRarity.LEGENDARY },
            totalXpEarned = user.totalXpEarned,
            longestStreak = user.highestStreak
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setFilter(filter: JourneyFilter) {
        _selectedFilter.value = filter
    }
}
