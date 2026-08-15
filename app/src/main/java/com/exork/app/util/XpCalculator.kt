package com.exork.app.util

import com.exork.app.model.Exercise
import com.exork.app.model.ExerciseCategory
import com.exork.app.model.ExerciseTrackingType

object XpCalculator {

    fun calculateExerciseXp(exercise: Exercise): Int {
        val sets = exercise.sets
        
        val rawXp = when (exercise.trackingType) {
            ExerciseTrackingType.REPS -> {
                val category = exercise.category
                when (category) {
                    ExerciseCategory.PUSHUPS -> {
                        val reps = exercise.reps ?: 0
                        2 * reps * sets
                    }
                    ExerciseCategory.PULLUPS -> {
                        val reps = exercise.reps ?: 0
                        5 * reps * sets
                    }
                    else -> (exercise.reps ?: 0) * 1 * sets
                }
            }
            ExerciseTrackingType.SECONDS -> {
                val totalSeconds = (exercise.duration ?: 0) * sets
                totalSeconds / 30
            }
            ExerciseTrackingType.DISTANCE -> {
                val distance = exercise.distanceKm ?: 0.0
                (distance * 5).toInt()
            }
        }
        // Clamp maximum XP per standard set/exercise to a sane range to prevent formula overflow
        return rawXp.coerceIn(0, 100)
    }

    fun calculateWorkoutXp(exercises: List<Exercise>, streak: Int): Int {
        var totalXp = 0
        
        exercises.forEach {
            totalXp += calculateExerciseXp(it)
        }
        
        // Bonus for workout completion
        if (exercises.isNotEmpty()) {
            totalXp += 50
        }

        // Streak bonuses
        if (streak == 3) {
            totalXp += 20
        } else if (streak >= 7) {
            totalXp += 50
        }

        return totalXp
    }

    /**
     * Calculates the XP required to reach the next level.
     */
    fun calculateRequiredXP(level: Int): Int {
        return (250 * level * level) + (750 * level) + 2000
    }

    fun calculateCurrentLevelXp(totalXp: Int, level: Int): Int {
        var remainingXp = totalXp
        for (i in 1 until level) {
            remainingXp -= calculateRequiredXP(i)
        }
        return remainingXp.coerceAtLeast(0)
    }
}
