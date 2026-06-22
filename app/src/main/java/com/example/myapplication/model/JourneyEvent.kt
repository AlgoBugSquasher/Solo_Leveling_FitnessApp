package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journey_event_table")
data class JourneyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "LEVEL_UP", "RANK_PROMOTION", "TITLE_UNLOCKED", etc.
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val icon: String
)
