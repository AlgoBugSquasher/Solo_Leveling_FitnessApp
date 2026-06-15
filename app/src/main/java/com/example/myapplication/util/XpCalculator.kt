package com.example.myapplication.util

import com.example.myapplication.model.Exercise

object XpCalculator {

    fun calculateExerciseXp(exercise: Exercise): Int {
        val name = exercise.name
        val sets = exercise.sets
        
        return when {
            name.contains("Push-up", ignoreCase = true) -> {
                val reps = exercise.reps ?: 0
                2 * reps * sets
            }
            name.contains("Pull-up", ignoreCase = true) || name.contains("Chin-up", ignoreCase = true) -> {
                val reps = exercise.reps ?: 0
                5 * reps * sets
            }
            name.contains("Plank", ignoreCase = true) -> {
                val duration = exercise.duration ?: 0
                1 * duration * sets
            }
            else -> {
                // Default for other exercises if any
                val reps = exercise.reps ?: 0
                val duration = exercise.duration ?: 0
                (reps * 2) + (duration / 5) * sets
            }
        }
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

    fun calculateRequiredXP(level: Int): Int {
        return 100 + (level * level * 25)
    }
}
