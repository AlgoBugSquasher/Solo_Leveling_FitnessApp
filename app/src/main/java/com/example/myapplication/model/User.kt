package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val rank: String = "Beginner",
    // Base stats
    val pushups: Int = 0,
    val pullups: Int = 0,
    val plankTime: Int = 0,
    // Mastery stats
    val totalPikePushups: Int = 0,
    val totalPseudoPlanchePushups: Int = 0,
    val totalHangingSeconds: Int = 0,
    val totalExplosivePullups: Int = 0,
    // Lifetime Statistics
    val totalXpEarned: Int = 0,
    val totalWorkouts: Int = 0,
    val highestStreak: Int = 0,
    // Tracking
    val lastWorkoutDate: Long = 0,
    val activeTitle: String? = null
) {
    fun xpToNextLevel(): Int = 100 + (level * level * 25)

    fun getProgressPercentage(): Float {
        val total = xpToNextLevel().toFloat()
        return if (total > 0) (xp.toFloat() / total).coerceIn(0f, 1f) else 0f
    }
}
