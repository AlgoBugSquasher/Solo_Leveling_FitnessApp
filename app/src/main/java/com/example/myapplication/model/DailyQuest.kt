package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_quest_table")
data class DailyQuest(
    @PrimaryKey val id: Int,
    val title: String,
    val currentProgress: Int = 0,
    val targetValue: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false
) {
    fun getProgressPercentage(): Float = if (targetValue > 0) currentProgress.toFloat() / targetValue.toFloat() else 0f
}
