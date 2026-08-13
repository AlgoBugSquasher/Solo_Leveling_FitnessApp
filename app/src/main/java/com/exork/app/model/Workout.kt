package com.exork.app.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ExerciseTrackingType {
    REPS, SECONDS, DISTANCE
}

enum class ExerciseCategory {
    PUSHUPS, PULLUPS, PLANK, CARDIO, OTHER
}

@Entity(tableName = "workout_table")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val totalXpGained: Int
)

@Entity(
    tableName = "exercise_table",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.OTHER,
    val trackingType: ExerciseTrackingType = ExerciseTrackingType.REPS,
    val reps: Int? = null,
    val sets: Int,
    val duration: Int? = null,
    val distanceKm: Double? = null
)

data class WorkoutWithExercises(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<ExerciseEntity>
)

// UI models
data class Exercise(
    val name: String,
    val category: ExerciseCategory = ExerciseCategory.OTHER,
    val trackingType: ExerciseTrackingType = ExerciseTrackingType.REPS,
    val reps: Int? = null,
    val sets: Int,
    val duration: Int? = null,
    val distanceKm: Double? = null
)
