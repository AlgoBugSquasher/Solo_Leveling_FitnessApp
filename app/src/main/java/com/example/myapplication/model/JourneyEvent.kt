package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.*

enum class JourneyRarity(val displayName: String, val color: Color) {
    COMMON("Common", RankE),
    RARE("Rare", RankB),
    EPIC("Epic", RankC),
    LEGENDARY("Legendary", MonarchGold)
}

enum class JourneyEventType {
    JOURNEY_START,
    LEVEL_UP,
    RANK_UP,
    ACHIEVEMENT,
    BADGE,
    PR,
    XP_MILESTONE,
    TRAINING,
    SYSTEM,
    SPECIAL_EVENT,
    BOSS_EVENT
}

@Entity(tableName = "journey_event_table")
data class JourneyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: JourneyEventType = JourneyEventType.SYSTEM,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val icon: String,
    val rarity: JourneyRarity = JourneyRarity.COMMON,
    val xpReward: Int? = null
)
