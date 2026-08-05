package com.example.myapplication.data

import com.example.myapplication.model.*
import com.example.myapplication.util.RankCalculator
import com.example.myapplication.util.XpCalculator
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FitnessRepository(
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val abilityDao: AbilityDao,
    private val workoutDao: WorkoutDao,
    private val titleDao: TitleDao,
    private val trainingPlanDao: TrainingPlanDao,
    private val journeyEventDao: JourneyEventDao,
    private val dailyQuestDao: DailyQuestDao,
    private val noteDao: NoteDao
) {
    val user: Flow<User?> = userDao.getUser()
    val abilities: Flow<List<Ability>> = abilityDao.getAllAbilities()
    val allWorkouts: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkouts()
    val allTitles: Flow<List<Title>> = titleDao.getAllTitles()
    val trainingPlan: Flow<List<TrainingDay>> = trainingPlanDao.getTrainingPlan()
    val weeklyBonus: Flow<WeeklyBonusEntity?> = trainingPlanDao.getWeeklyBonus()
    val allPlannedExercises: Flow<List<PlannedExercise>> = trainingPlanDao.getAllPlannedExercises()
    val allJourneyEvents: Flow<List<JourneyEvent>> = journeyEventDao.getAllEvents()
    val allDailyQuests: Flow<List<DailyQuest>> = dailyQuestDao.getAllQuests()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun clearAllDatabase() {
        database.withTransaction {
            userDao.deleteAllUsers()
            abilityDao.deleteAllAbilities()
            workoutDao.deleteAllWorkouts()
            workoutDao.deleteAllExercises()
            titleDao.deleteAllTitles()
            trainingPlanDao.deleteAllTrainingDays()
            trainingPlanDao.deleteAllPlannedExercises()
            trainingPlanDao.deleteWeeklyBonus()
            journeyEventDao.deleteAllEvents()
            dailyQuestDao.deleteAllQuests()
            noteDao.deleteAllNotes()
        }
    }

    suspend fun restoreDatabase(
        user: User,
        abilities: List<Ability>,
        workouts: List<WorkoutWithExercises>,
        titles: List<Title>,
        trainingDays: List<TrainingDay>,
        plannedExercises: List<PlannedExercise>,
        weeklyBonus: WeeklyBonusEntity?,
        journeyEvents: List<JourneyEvent>,
        dailyQuests: List<DailyQuest>,
        notes: List<Note>
    ) {
        database.withTransaction {
            // 1. Clear
            userDao.deleteAllUsers()
            abilityDao.deleteAllAbilities()
            workoutDao.deleteAllWorkouts()
            workoutDao.deleteAllExercises()
            titleDao.deleteAllTitles()
            trainingPlanDao.deleteAllTrainingDays()
            trainingPlanDao.deleteAllPlannedExercises()
            trainingPlanDao.deleteWeeklyBonus()
            journeyEventDao.deleteAllEvents()
            dailyQuestDao.deleteAllQuests()
            noteDao.deleteAllNotes()

            // 2. Restore
            userDao.insertUser(user)
            abilityDao.insertAbilities(abilities)
            titles.forEach { titleDao.updateTitle(it) } // titles might be pre-seeded, but we want restored state
            titleDao.insertTitles(titles)
            
            trainingPlanDao.insertTrainingDays(trainingDays)
            trainingPlanDao.insertPlannedExercises(plannedExercises)
            weeklyBonus?.let { trainingPlanDao.insertWeeklyBonus(it) }
            
            journeyEventDao.insertEvents(journeyEvents)
            dailyQuestDao.insertQuests(dailyQuests)
            noteDao.insertNotes(notes)

            // Workouts need to preserve ID relationships
            workouts.forEach { w ->
                workoutDao.insertWorkoutWithExercises(w.workout, w.exercises)
            }
        }
    }

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun getWeeklyXp(): Flow<Int> {
        return allWorkouts.combine(allJourneyEvents) { workouts, events ->
            val calendar = Calendar.getInstance()
            val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentYear = calendar.get(Calendar.YEAR)

            val workoutXp = workouts.filter {
                calendar.timeInMillis = it.workout.date
                calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && calendar.get(Calendar.YEAR) == currentYear
            }.sumOf { it.workout.totalXpGained }

            val eventXp = events.filter {
                calendar.timeInMillis = it.timestamp
                calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek && calendar.get(Calendar.YEAR) == currentYear
            }.sumOf { it.xpReward ?: 0 }

            workoutXp + eventXp
        }
    }

    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun insertDailyQuests(quests: List<DailyQuest>) {
        dailyQuestDao.insertQuests(quests)
    }

    suspend fun updateDailyQuest(quest: DailyQuest) {
        dailyQuestDao.updateQuest(quest)
    }

    suspend fun clearDailyQuests() {
        dailyQuestDao.deleteAllQuests()
    }

    suspend fun recordJourneyEvent(
        eventType: JourneyEventType,
        title: String,
        description: String,
        icon: String,
        rarity: JourneyRarity = JourneyRarity.COMMON,
        xpReward: Int? = null,
        isUnique: Boolean = false
    ) {
        if (isUnique) {
            // Simple check by eventType and title to prevent duplicates of major milestones
            val exists = allJourneyEvents.first().any { it.eventType == eventType && it.title == title }
            if (exists) return
        }

        journeyEventDao.insertEvent(JourneyEvent(
            eventType = eventType,
            title = title,
            description = description,
            icon = icon,
            rarity = rarity,
            xpReward = xpReward
        ))
    }

    suspend fun getEventCountByType(eventType: JourneyEventType): Int {
        // We'll need to update Dao for this, or just use Flow
        return allJourneyEvents.first().count { it.eventType == eventType }
    }

    suspend fun insertTrainingDays(days: List<TrainingDay>) {
        trainingPlanDao.insertTrainingDays(days)
    }

    suspend fun updateTrainingDay(day: TrainingDay) {
        trainingPlanDao.updateTrainingDay(day)
    }

    suspend fun insertPlannedExercise(exercise: PlannedExercise) {
        trainingPlanDao.insertPlannedExercise(exercise)
    }

    suspend fun updatePlannedExercise(exercise: PlannedExercise) {
        trainingPlanDao.updatePlannedExercise(exercise)
    }

    suspend fun deletePlannedExercise(exercise: PlannedExercise) {
        trainingPlanDao.deletePlannedExercise(exercise)
    }

    suspend fun updateWeeklyBonus(bonus: WeeklyBonusEntity) {
        trainingPlanDao.insertWeeklyBonus(bonus)
    }

    suspend fun getWeeklyBonusSync(): WeeklyBonusEntity? {
        return trainingPlanDao.getWeeklyBonusSync()
    }

    suspend fun checkStreakReset(): Boolean {
        val currentUser = user.first() ?: return false
        if (currentUser.lastWorkoutDate == 0L) return false

        val lastDate = Calendar.getInstance().apply {
            timeInMillis = currentUser.lastWorkoutDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastDate.timeInMillis == currentDate.timeInMillis) return false

        val plannedExercises = allPlannedExercises.first()
        if (plannedExercises.isEmpty()) return false

        val workoutDays = plannedExercises.map { it.dayOfWeek }.toSet()
        val checkDate = (lastDate.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

        var missedWorkoutDay = false
        while (checkDate.before(currentDate)) {
            val dayOfWeek = when (checkDate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 7
            }

            if (workoutDays.contains(dayOfWeek)) {
                missedWorkoutDay = true
                break
            }
            checkDate.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (missedWorkoutDay && currentUser.streak > 0) {
            updateUser(currentUser.copy(streak = 0))
            return true
        }
        return false
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
            
            // Record Level Up Milestone
            recordJourneyEvent(
                eventType = JourneyEventType.LEVEL_UP,
                title = "LEVEL $newLevel REACHED",
                description = "Your power continues to grow. New limits established.",
                icon = "📈",
                rarity = if (newLevel % 10 == 0) JourneyRarity.EPIC else JourneyRarity.RARE,
                isUnique = true
            )
        }

        val newRank = RankCalculator.calculateRank(newLevel)
        val isRankPromotion = RankCalculator.isPromotion(currentUser.rank, newRank)

        if (isRankPromotion) {
            recordJourneyEvent(
                eventType = JourneyEventType.RANK_UP,
                title = "RANK PROMOTION: $newRank",
                description = "You have ascended to a higher class of Hunter.",
                icon = "⚔️",
                rarity = JourneyRarity.LEGENDARY,
                isUnique = true
            )
        }

        // Streak calculation
        // Daily Quest completion ALWAYS increments streak (+1)
        // If it's a workout session (isWorkout=true), we also use standard streak logic
        val newStreak = if (xpGained >= 50 && !isWorkout) {
            currentUser.streak + 1
        } else {
            calculateNewStreak(currentUser.lastWorkoutDate, currentUser.streak)
        }
        
        // Streak milestones
        val streakMilestones = listOf(7, 30, 100, 365)
        if (newStreak > currentUser.streak && newStreak in streakMilestones) {
            recordJourneyEvent(
                eventType = JourneyEventType.PR,
                title = "$newStreak DAY STREAK",
                description = "Relentless discipline. Immortal focus.",
                icon = "🔥",
                rarity = if (newStreak >= 100) JourneyRarity.LEGENDARY else JourneyRarity.EPIC,
                isUnique = true
            )
        }

        // First Workout
        if (isWorkout && currentUser.totalWorkouts == 0) {
            recordJourneyEvent(
                eventType = JourneyEventType.JOURNEY_START,
                title = "JOURNEY BEGUN",
                description = "The first step onto the path of the Shadow Monarch.",
                icon = "🏁",
                rarity = JourneyRarity.RARE,
                isUnique = true
            )
        }

        // PR detection
        if (pushups > currentUser.maxPushupsSingleWorkout && currentUser.totalWorkouts > 0) {
            recordJourneyEvent(
                eventType = JourneyEventType.PR,
                title = "NEW PUSHUP RECORD",
                description = "$pushups Push-ups in a single session.",
                icon = "💪",
                rarity = JourneyRarity.RARE
            )
        }
        // Similar for pullups/plank... (omitting for brevity or can add)

        // XP Milestones
        val totalXpAfter = currentUser.totalXpEarned + xpGained
        checkXpMilestones(currentUser.totalXpEarned, totalXpAfter)

        val updatedUser = currentUser.copy(
            xp = newXp,
            level = newLevel,
            rank = newRank,
            streak = newStreak,
            pushups = currentUser.pushups + pushups,
            pullups = currentUser.pullups + pullups,
            plankTime = currentUser.plankTime + plankSeconds,
            totalDistanceKm = currentUser.totalDistanceKm + distanceKm,
            totalXpEarned = totalXpAfter,
            totalWorkouts = if (isWorkout) currentUser.totalWorkouts + 1 else currentUser.totalWorkouts,
            highestStreak = maxOf(currentUser.highestStreak, newStreak),
            maxPushupsSingleWorkout = maxOf(currentUser.maxPushupsSingleWorkout, pushups),
            maxPullupsSingleWorkout = maxOf(currentUser.maxPullupsSingleWorkout, pullups),
            maxPlankSingleWorkout = maxOf(currentUser.maxPlankSingleWorkout, plankSeconds),
            maxXpSingleWorkout = maxOf(currentUser.maxXpSingleWorkout, xpGained),
            totalPromotions = if (isRankPromotion) currentUser.totalPromotions + 1 else currentUser.totalPromotions,
            highestRank = RankCalculator.getHighestRank(currentUser.highestRank, newRank),
            lastWorkoutDate = System.currentTimeMillis()
        )

        updateUser(updatedUser)
        checkAndUnlockAbilities(updatedUser)
    }

    private suspend fun checkXpMilestones(oldXp: Int, newXp: Int) {
        val milestones = listOf(1000, 5000, 10000, 50000, 100000)
        milestones.forEach { m ->
            if (oldXp < m && newXp >= m) {
                recordJourneyEvent(
                    eventType = JourneyEventType.XP_MILESTONE,
                    title = "${m / 1000}K XP ACCUMULATED",
                    description = "A massive reserve of mana has been gathered.",
                    icon = "💎",
                    rarity = if (m >= 10000) JourneyRarity.EPIC else JourneyRarity.RARE,
                    isUnique = true
                )
            }
        }
    }

    private suspend fun calculateNewStreak(lastActivityDate: Long, currentStreak: Int): Int {
        if (lastActivityDate == 0L) return 1

        val lastDate = Calendar.getInstance().apply {
            timeInMillis = lastActivityDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (lastDate.timeInMillis == currentDate.timeInMillis) {
            return currentStreak
        }

        val nextDay = (lastDate.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        if (nextDay.timeInMillis == currentDate.timeInMillis) {
            return currentStreak + 1
        }

        val plannedExercises = allPlannedExercises.first()
        if (plannedExercises.isEmpty()) {
            return 1
        }

        val workoutDays = plannedExercises.map { it.dayOfWeek }.toSet()
        val checkDate = (lastDate.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

        while (checkDate.before(currentDate)) {
            val dayOfWeek = when (checkDate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 7
            }

            if (workoutDays.contains(dayOfWeek)) {
                return 1
            }
            checkDate.add(Calendar.DAY_OF_YEAR, 1)
        }

        return currentStreak + 1
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
                recordJourneyEvent(
                    eventType = JourneyEventType.ACHIEVEMENT,
                    title = "TITLE EARNED: ${unlocked.name}",
                    description = "A new legacy identifier has been unlocked.",
                    icon = "👑",
                    rarity = JourneyRarity.EPIC,
                    isUnique = true
                )
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
