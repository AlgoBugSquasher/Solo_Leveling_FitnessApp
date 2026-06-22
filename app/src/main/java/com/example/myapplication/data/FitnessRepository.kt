package com.example.myapplication.data

import com.example.myapplication.model.Ability
import com.example.myapplication.model.Title
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.User
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.model.WorkoutWithExercises
import com.example.myapplication.util.RankCalculator
import com.example.myapplication.util.XpCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FitnessRepository(
    private val userDao: UserDao,
    private val abilityDao: AbilityDao,
    private val workoutDao: WorkoutDao,
    private val titleDao: TitleDao,
    private val trainingPlanDao: TrainingPlanDao,
    private val journeyEventDao: JourneyEventDao,
    private val dailyQuestDao: DailyQuestDao
) {
    val user: Flow<User?> = userDao.getUser()
    val abilities: Flow<List<Ability>> = abilityDao.getAllAbilities()
    val allWorkouts: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkouts()
    val allTitles: Flow<List<Title>> = titleDao.getAllTitles()
    val trainingPlan: Flow<List<com.example.myapplication.model.TrainingDay>> = trainingPlanDao.getTrainingPlan()
    val weeklyBonus: Flow<com.example.myapplication.model.WeeklyBonusEntity?> = trainingPlanDao.getWeeklyBonus()
    val allPlannedExercises: Flow<List<com.example.myapplication.model.PlannedExercise>> = trainingPlanDao.getAllPlannedExercises()
    val allJourneyEvents: Flow<List<com.example.myapplication.model.JourneyEvent>> = journeyEventDao.getAllEvents()
    val allDailyQuests: Flow<List<com.example.myapplication.model.DailyQuest>> = dailyQuestDao.getAllQuests()

    suspend fun insertDailyQuests(quests: List<com.example.myapplication.model.DailyQuest>) {
        dailyQuestDao.insertQuests(quests)
    }

    suspend fun updateDailyQuest(quest: com.example.myapplication.model.DailyQuest) {
        dailyQuestDao.updateQuest(quest)
    }

    suspend fun clearDailyQuests() {
        dailyQuestDao.deleteAllQuests()
    }

    suspend fun recordJourneyEvent(type: String, title: String, description: String, icon: String) {
        journeyEventDao.insertEvent(com.example.myapplication.model.JourneyEvent(
            type = type,
            title = title,
            description = description,
            icon = icon
        ))
    }

    suspend fun getEventCountByType(type: String): Int = journeyEventDao.getEventCountByType(type)

    suspend fun insertTrainingDays(days: List<com.example.myapplication.model.TrainingDay>) {
        trainingPlanDao.insertTrainingDays(days)
    }

    suspend fun updateTrainingDay(day: com.example.myapplication.model.TrainingDay) {
        trainingPlanDao.updateTrainingDay(day)
    }

    suspend fun insertPlannedExercise(exercise: com.example.myapplication.model.PlannedExercise) {
        trainingPlanDao.insertPlannedExercise(exercise)
    }

    suspend fun updatePlannedExercise(exercise: com.example.myapplication.model.PlannedExercise) {
        trainingPlanDao.updatePlannedExercise(exercise)
    }

    suspend fun deletePlannedExercise(exercise: com.example.myapplication.model.PlannedExercise) {
        trainingPlanDao.deletePlannedExercise(exercise)
    }

    suspend fun updateWeeklyBonus(bonus: com.example.myapplication.model.WeeklyBonusEntity) {
        trainingPlanDao.insertWeeklyBonus(bonus)
    }

    suspend fun getWeeklyBonusSync(): com.example.myapplication.model.WeeklyBonusEntity? {
        return trainingPlanDao.getWeeklyBonusSync()
    }

    /**
     * Unified progression system.
     * Updates user statistics and handles level/rank progression.
     */
    suspend fun recordProgress(
        pushups: Int = 0,
        pullups: Int = 0,
        plankSeconds: Int = 0,
        distanceKm: Double = 0.0,
        xpGained: Int = 0,
        isWorkout: Boolean = false
    ) {
        val currentUser = user.first() ?: return
        
        var newXp = currentUser.xp + xpGained
        var newLevel = currentUser.level
        
        // Handle Level Ups
        while (newXp >= XpCalculator.calculateRequiredXP(newLevel)) {
            newXp -= XpCalculator.calculateRequiredXP(newLevel)
            newLevel++
        }

        val newRank = RankCalculator.calculateRank(newLevel)
        val isRankPromotion = RankCalculator.isPromotion(currentUser.rank, newRank)

        // Streak calculation based on calendar days
        val newStreak = calculateNewStreak(currentUser.lastWorkoutDate, currentUser.streak)

        if (newLevel > currentUser.level) {
            recordJourneyEvent("LEVEL_UP", "LEVEL UP", "Reached Level $newLevel", "📈")
        }
        if (isRankPromotion) {
            recordJourneyEvent("RANK_PROMOTION", "RANK PROMOTION", "Promoted to $newRank", "⚔️")
        }
        if (isWorkout && currentUser.totalWorkouts == 0) {
            recordJourneyEvent("FIRST_WORKOUT", "FIRST WORKOUT", "The path of strength begins.", "🏁")
        }
        
        // Streak milestones
        val streakMilestones = listOf(3, 7, 15, 30, 50, 100, 365)
        if (newStreak > currentUser.streak && newStreak in streakMilestones) {
            recordJourneyEvent("STREAK_MILESTONE", "STREAK", "$newStreak Day Streak Achieved", "🔥")
        }

        // PR detection for physical records
        if (pushups > currentUser.maxPushupsSingleWorkout && currentUser.maxPushupsSingleWorkout > 0) {
            recordJourneyEvent("PERSONAL_RECORD", "PERSONAL RECORD", "Highest Pushups: $pushups", "💪")
        }
        if (pullups > currentUser.maxPullupsSingleWorkout && currentUser.maxPullupsSingleWorkout > 0) {
            recordJourneyEvent("PERSONAL_RECORD", "PERSONAL RECORD", "Highest Pullups: $pullups", "💪")
        }
        if (plankSeconds > currentUser.maxPlankSingleWorkout && currentUser.maxPlankSingleWorkout > 0) {
            recordJourneyEvent("PERSONAL_RECORD", "PERSONAL RECORD", "Longest Plank: $plankSeconds sec", "💪")
        }

        val updatedUser = currentUser.copy(
            xp = newXp,
            level = newLevel,
            rank = newRank,
            streak = newStreak,
            pushups = currentUser.pushups + pushups,
            pullups = currentUser.pullups + pullups,
            plankTime = currentUser.plankTime + plankSeconds,
            totalDistanceKm = currentUser.totalDistanceKm + distanceKm,
            totalXpEarned = currentUser.totalXpEarned + xpGained,
            totalWorkouts = if (isWorkout) currentUser.totalWorkouts + 1 else currentUser.totalWorkouts,
            highestStreak = maxOf(currentUser.highestStreak, newStreak),
            maxPushupsSingleWorkout = maxOf(currentUser.maxPushupsSingleWorkout, pushups),
            maxPullupsSingleWorkout = maxOf(currentUser.maxPullupsSingleWorkout, pullups),
            maxPlankSingleWorkout = maxOf(currentUser.maxPlankSingleWorkout, plankSeconds),
            maxXpSingleWorkout = maxOf(currentUser.maxXpSingleWorkout, xpGained),
            totalPromotions = if (isRankPromotion) currentUser.totalPromotions + 1 else currentUser.totalPromotions,
            highestRank = RankCalculator.getHighestRank(currentUser.highestRank, newRank),
            lastWorkoutDate = System.currentTimeMillis() // Update last activity time
        )

        updateUser(updatedUser)
        checkAndUnlockAbilities(updatedUser)
    }

    private fun calculateNewStreak(lastActivityDate: Long, currentStreak: Int): Int {
        val now = System.currentTimeMillis()
        if (lastActivityDate == 0L) return 1

        val lastDate = Calendar.getInstance().apply { timeInMillis = lastActivityDate }
        val currentDate = Calendar.getInstance().apply { timeInMillis = now }

        // Same day: streak stays same
        if (lastDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            lastDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return currentStreak
        }

        // Consecutive day: increment
        lastDate.add(Calendar.DAY_OF_YEAR, 1)
        if (lastDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            lastDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return currentStreak + 1
        }

        // Broken streak: reset to 1
        return 1
    }

    suspend fun insertWorkout(workout: WorkoutEntity, exercises: List<ExerciseEntity>) {
        workoutDao.insertWorkoutWithExercises(workout, exercises)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun updateAbilities(abilities: List<Ability>) {
        abilityDao.insertAbilities(abilities)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun insertTitles(titles: List<Title>) {
        titleDao.insertTitles(titles)
    }

    suspend fun updateTitle(title: Title) {
        titleDao.updateTitle(title)
    }

    suspend fun checkAndUnlockTitles(currentStreak: Int): List<Title> {
        val titles = allTitles.first()
        val newlyUnlocked = mutableListOf<Title>()
        
        titles.forEach { title ->
            if (!title.isUnlocked && currentStreak >= title.requiredStreak) {
                val unlocked = title.copy(isUnlocked = true)
                updateTitle(unlocked)
                newlyUnlocked.add(unlocked)
                recordJourneyEvent("TITLE_UNLOCKED", "TITLE UNLOCKED", unlocked.name, "👑")
            }
        }
        return newlyUnlocked
    }

    suspend fun checkAndUnlockAbilities(user: User) {
        val currentAbilities = abilityDao.getAllAbilities().first()
        val updatedAbilities = currentAbilities.map { ability ->
            if (!ability.isUnlocked && meetsConditions(ability, user, currentAbilities)) {
                ability.copy(isUnlocked = true)
            } else {
                ability
            }
        }
        if (updatedAbilities != currentAbilities) {
            updateAbilities(updatedAbilities)
        }
    }

    private fun meetsConditions(ability: Ability, user: User, allAbilities: List<Ability>): Boolean {
        val baseStatsMet = user.pushups >= ability.requiredPushups &&
                user.pullups >= ability.requiredPullups &&
                user.plankTime >= ability.requiredPlankTime &&
                user.level >= ability.requiredLevel &&
                user.streak >= ability.requiredStreak

        val dependenciesMet = when (ability.name) {
            "HSPU" -> allAbilities.find { it.name == "Handstand" }?.isUnlocked == true
            else -> true
        }

        return baseStatsMet && dependenciesMet
    }
}
