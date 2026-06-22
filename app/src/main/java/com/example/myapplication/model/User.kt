package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.util.XpCalculator

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val rank: String = "E-Rank Hunter",
    // Base stats
    val pushups: Int = 0,
    val pullups: Int = 0,
    val plankTime: Int = 0,
    val totalDistanceKm: Double = 0.0,
    // Mastery stats
    val totalPikePushups: Int = 0,
    val totalPseudoPlanchePushups: Int = 0,
    val totalHangingSeconds: Int = 0,
    val totalExplosivePullups: Int = 0,
    // Lifetime Statistics
    val totalXpEarned: Int = 0,
    val totalWorkouts: Int = 0,
    val highestStreak: Int = 0,
    val totalPromotions: Int = 0,
    val highestRank: String = "E-Rank Hunter",
    // Tracking
    val lastWorkoutDate: Long = 0,
    val lastQuestRefreshDate: Long = 0,
    val activeTitle: String? = null,
    val soundEnabled: Boolean = true,
    // Personal Records
    val maxPushupsSingleWorkout: Int = 0,
    val maxPullupsSingleWorkout: Int = 0,
    val maxPlankSingleWorkout: Int = 0,
    val maxXpSingleWorkout: Int = 0
) {
    fun xpToNextLevel(): Int = XpCalculator.calculateRequiredXP(level)

    fun getProgressPercentage(): Float {
        val total = xpToNextLevel().toFloat()
        return if (total > 0) (xp.toFloat() / total).coerceIn(0f, 1f) else 0f
    }
}
