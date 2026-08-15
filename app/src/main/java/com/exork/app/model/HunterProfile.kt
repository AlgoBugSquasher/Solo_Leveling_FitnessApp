package com.exork.app.model

data class HunterProfile(
    val userId: String = "",
    val displayName: String = "Unknown Hunter",
    val username: String? = null,
    val hunterRank: String = "E-Rank Hunter",
    val hunterLevel: Int = 1,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val photoUrl: String? = null,
    val activeTitle: String? = null,
    val maxPushupsSingleWorkout: Int = 0,
    val maxPullupsSingleWorkout: Int = 0,
    val maxPlankSingleWorkout: Int = 0,
    val totalWorkouts: Int = 0,
    val totalPromotions: Int = 0
)
