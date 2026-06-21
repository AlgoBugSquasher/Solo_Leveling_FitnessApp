package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_plan_table")
data class TrainingDay(
    @PrimaryKey val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val pushups: Int = 0,
    val pullups: Int = 0,
    val plankSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val lastCompletedWeek: Int = -1, // Calendar.WEEK_OF_YEAR
    val lastCompletedYear: Int = -1  // Calendar.YEAR
)

data class WeeklyBonus(
    @PrimaryKey val id: Int = 0,
    val lastBonusWeek: Int = -1,
    val lastBonusYear: Int = -1
)

@Entity(tableName = "weekly_bonus_table")
data class WeeklyBonusEntity(
    @PrimaryKey val id: Int = 0,
    val lastBonusWeek: Int = -1,
    val lastBonusYear: Int = -1
)
