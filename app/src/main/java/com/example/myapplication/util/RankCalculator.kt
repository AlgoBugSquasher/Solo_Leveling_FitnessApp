package com.example.myapplication.util

object RankCalculator {
    fun calculateRank(level: Int): String {
        return when {
            level >= 100 -> "Shadow Monarch"
            level >= 75 -> "National Level Hunter"
            level >= 50 -> "S-Rank Hunter"
            level >= 40 -> "A-Rank Hunter"
            level >= 30 -> "B-Rank Hunter"
            level >= 20 -> "C-Rank Hunter"
            level >= 10 -> "D-Rank Hunter"
            else -> "E-Rank Hunter"
        }
    }

    private val rankWeights = mapOf(
        "E-Rank Hunter" to 1,
        "D-Rank Hunter" to 2,
        "C-Rank Hunter" to 3,
        "B-Rank Hunter" to 4,
        "A-Rank Hunter" to 5,
        "S-Rank Hunter" to 6,
        "National Level Hunter" to 7,
        "Shadow Monarch" to 8
    )

    fun isPromotion(oldRank: String, newRank: String): Boolean {
        val oldWeight = rankWeights[oldRank] ?: 0
        val newWeight = rankWeights[newRank] ?: 0
        return newWeight > oldWeight
    }
    
    fun getHighestRank(rank1: String, rank2: String): String {
        val w1 = rankWeights[rank1] ?: 0
        val w2 = rankWeights[rank2] ?: 0
        return if (w2 > w1) rank2 else rank1
    }
}
