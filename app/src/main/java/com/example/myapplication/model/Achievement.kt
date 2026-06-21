package com.example.myapplication.model

enum class AchievementCategory {
    STRENGTH, PULL_UP, CORE, WORKOUT, STREAK, LEVEL
}

data class Achievement(
    val id: String,
    val name: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val description: String,
    val icon: String // Emoji or symbol
) {
    fun getCurrentValue(user: User): Int {
        return when (category) {
            AchievementCategory.STRENGTH -> user.pushups
            AchievementCategory.PULL_UP -> user.pullups
            AchievementCategory.CORE -> user.plankTime
            AchievementCategory.WORKOUT -> user.totalWorkouts
            AchievementCategory.STREAK -> user.highestStreak
            AchievementCategory.LEVEL -> user.level
        }
    }

    fun isUnlocked(user: User): Boolean {
        return getCurrentValue(user) >= targetValue
    }

    fun getProgress(user: User): Float {
        return (getCurrentValue(user).toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    }
}

object AchievementData {
    val allAchievements = listOf(
        // Strength Achievements (Pushups)
        Achievement("s1", "First Blood", AchievementCategory.STRENGTH, 100, "Perform 100 total pushups.", "🩸"),
        Achievement("s2", "Warrior", AchievementCategory.STRENGTH, 500, "Perform 500 total pushups.", "⚔️"),
        Achievement("s3", "Iron Fists", AchievementCategory.STRENGTH, 1000, "Perform 1,000 total pushups.", "👊"),
        Achievement("s4", "Beast Crusher", AchievementCategory.STRENGTH, 5000, "Perform 5,000 total pushups.", "🦁"),
        Achievement("s5", "Monster Slayer", AchievementCategory.STRENGTH, 10000, "Perform 10,000 total pushups.", "👹"),

        // Pull-up Achievements
        Achievement("p1", "Rookie Climber", AchievementCategory.PULL_UP, 25, "Perform 25 total pullups.", "🧗"),
        Achievement("p2", "Sky Walker", AchievementCategory.PULL_UP, 100, "Perform 100 total pullups.", "☁️"),
        Achievement("p3", "Gravity Defier", AchievementCategory.PULL_UP, 500, "Perform 500 total pullups.", "🌎"),
        Achievement("p4", "Aerial Hunter", AchievementCategory.PULL_UP, 1000, "Perform 1,000 total pullups.", "🦅"),
        Achievement("p5", "Ascended Hunter", AchievementCategory.PULL_UP, 5000, "Perform 5,000 total pullups.", "😇"),

        // Core Achievements (Plank Seconds)
        Achievement("c1", "Stone Core", AchievementCategory.CORE, 600, "10 minutes total plank time.", "🪨"),
        Achievement("c2", "Iron Core", AchievementCategory.CORE, 3600, "1 hour total plank time.", "🛡️"),
        Achievement("c3", "Titan Core", AchievementCategory.CORE, 18000, "5 hours total plank time.", "🔱"),
        Achievement("c4", "Monarch Core", AchievementCategory.CORE, 36000, "10 hours total plank time.", "👑"),

        // Workout Achievements
        Achievement("w1", "First Workout", AchievementCategory.WORKOUT, 1, "Complete your first workout.", "🏁"),
        Achievement("w2", "Dedicated Hunter", AchievementCategory.WORKOUT, 10, "Complete 10 workouts.", "📅"),
        Achievement("w3", "Veteran Hunter", AchievementCategory.WORKOUT, 50, "Complete 50 workouts.", "🎖️"),
        Achievement("w4", "Elite Hunter", AchievementCategory.WORKOUT, 100, "Complete 100 workouts.", "💎"),
        Achievement("w5", "Legendary Hunter", AchievementCategory.WORKOUT, 500, "Complete 500 workouts.", "🌟"),

        // Streak Achievements
        Achievement("st1", "Consistent", AchievementCategory.STREAK, 3, "Maintain a 3-day streak.", "🔥"),
        Achievement("st2", "Unbroken", AchievementCategory.STREAK, 7, "Maintain a 7-day streak.", "🔗"),
        Achievement("st3", "Relentless", AchievementCategory.STREAK, 30, "Maintain a 30-day streak.", "⏳"),
        Achievement("st4", "Immortal Discipline", AchievementCategory.STREAK, 100, "Maintain a 100-day streak.", "♾️"),
        Achievement("st5", "Eternal Hunter", AchievementCategory.STREAK, 365, "Maintain a 365-day streak.", "☀️"),

        // Level Achievements
        Achievement("l1", "Novice Hunter", AchievementCategory.LEVEL, 10, "Reach Level 10.", "🔟"),
        Achievement("l2", "Capable Hunter", AchievementCategory.LEVEL, 25, "Reach Level 25.", "2️⃣5️⃣"),
        Achievement("l3", "Senior Hunter", AchievementCategory.LEVEL, 50, "Reach Level 50.", "5️⃣0️⃣"),
        Achievement("l4", "Master Hunter", AchievementCategory.LEVEL, 75, "Reach Level 75.", "7️⃣5️⃣"),
        Achievement("l5", "Shadow Monarch", AchievementCategory.LEVEL, 100, "Reach Level 100.", "🌌")
    )
}
