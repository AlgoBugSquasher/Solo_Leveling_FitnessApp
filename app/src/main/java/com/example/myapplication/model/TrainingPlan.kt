package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExerciseTrackingType {
    REPS, SECONDS, DISTANCE
}

@Entity(tableName = "planned_exercise_table")
data class PlannedExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val name: String,
    val trackingType: ExerciseTrackingType,
    val sets: Int? = null,
    val reps: Int? = null,
    val seconds: Int? = null,
    val distanceKm: Double? = null,
    val isCompleted: Boolean = false,
    val lastCompletedWeek: Int = -1, // Calendar.WEEK_OF_YEAR
    val lastCompletedYear: Int = -1  // Calendar.YEAR
)

@Entity(tableName = "training_plan_table")
data class TrainingDay(
    @PrimaryKey val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val isCompleted: Boolean = false,
    val lastCompletedWeek: Int = -1,
    val lastCompletedYear: Int = -1,
    val lastRewardWeek: Int = -1,
    val lastRewardYear: Int = -1
)

@Entity(tableName = "weekly_bonus_table")
data class WeeklyBonusEntity(
    @PrimaryKey val id: Int = 0,
    val lastBonusWeek: Int = -1,
    val lastBonusYear: Int = -1
)

