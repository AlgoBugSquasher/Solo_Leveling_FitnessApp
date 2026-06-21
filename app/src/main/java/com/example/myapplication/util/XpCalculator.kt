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

    /**
     * Calculates the XP required to reach the next level.
     * Follows a quadratic progression for a long-term RPG journey:
     * Level 1 -> 2: 3000 XP
     * Level 2 -> 3: 4500 XP
     * Level 3 -> 4: 6500 XP
     * Level 4 -> 5: 9000 XP
     * Level 5 -> 6: 12000 XP
     */
    fun calculateRequiredXP(level: Int): Int {
        // Formula derived: 250*level^2 + 750*level + 2000
        return (250 * level * level) + (750 * level) + 2000
    }
}
