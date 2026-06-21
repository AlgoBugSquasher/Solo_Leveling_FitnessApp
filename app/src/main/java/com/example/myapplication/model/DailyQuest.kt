package com.example.myapplication.model

data class DailyQuest(
    val id: Int,
    val title: String,
    val goal: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val sets: Int? = null,
    val reps: Int? = null
) {
    companion object {
        fun createQuest(
            id: Int,
            title: String,
            totalGoal: Int,
            xpReward: Int
        ): DailyQuest {
            return when {
                title.lowercase().contains("plank") -> {
                    val cappedGoal = totalGoal.coerceIn(20, 100)
                    DailyQuest(
                        id = id,
                        title = title,
                        goal = "$cappedGoal seconds",
                        xpReward = xpReward
                    )
                }
                else -> {
                    val (sets, reps) = generateSetsAndReps(totalGoal, title)
                    DailyQuest(
                        id = id,
                        title = title,
                        goal = if (sets != null && reps != null) "$sets × $reps" else "$totalGoal reps",
                        xpReward = xpReward,
                        sets = sets,
                        reps = reps
                    )
                }
            }
        }

        private fun generateSetsAndReps(total: Int, title: String): Pair<Int?, Int?> {
            if (total <= 0) return null to null
            
            val isPullUp = title.lowercase().contains("pull")
            
            // Define realistic patterns
            val patterns = if (isPullUp) {
                when (total) {
                    10 -> listOf(2 to 5, 3 to 4) // 3x4 is 12, but closer than 1x10
                    12 -> listOf(3 to 4, 4 to 3)
                    15 -> listOf(3 to 5, 5 to 3)
                    else -> findFactors(total)
                }
            } else {
                when (total) {
                    20 -> listOf(2 to 10, 4 to 5)
                    30 -> listOf(3 to 10, 5 to 6)
                    40 -> listOf(4 to 10, 5 to 8)
                    50 -> listOf(5 to 10)
                    else -> findFactors(total)
                }
            }
            
            val pattern = if (patterns.isNotEmpty()) patterns.random() else null to null
            return pattern
        }

        private fun findFactors(total: Int): List<Pair<Int, Int>> {
            val factors = mutableListOf<Pair<Int, Int>>()
            for (i in 2..7) { // Prefer 2-7 sets
                if (total % i == 0) {
                    val reps = total / i
                    if (reps in 3..15) { // Prefer 3-15 reps per set
                        factors.add(i to reps)
                    }
                }
            }
            return factors
        }
    }
}
