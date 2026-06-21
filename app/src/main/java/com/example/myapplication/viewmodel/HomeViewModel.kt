package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.model.Ability
import com.example.myapplication.model.Badge
import com.example.myapplication.model.BadgeData
import com.example.myapplication.model.Title
import com.example.myapplication.model.TitleData
import com.example.myapplication.model.DailyQuest
import com.example.myapplication.model.User
import com.example.myapplication.model.Achievement
import com.example.myapplication.model.AchievementData
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.util.RankCalculator
import com.example.myapplication.util.XpCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Events for the UI to handle, such as showing animations or dialogs.
 */
sealed class UiEvent {
    data class LevelUp(val oldLevel: Int, val newLevel: Int) : UiEvent()
    data class AbilityUnlocked(val ability: Ability) : UiEvent()
    data class TitleUnlocked(val title: Title) : UiEvent()
    data class BadgeUnlocked(val badge: Badge) : UiEvent()
    data class AchievementUnlocked(val achievement: Achievement) : UiEvent()
    data class NewPersonalRecord(val recordName: String, val oldValue: Int, val newValue: Int) : UiEvent()
    data class RankPromotion(val oldRank: String, val newRank: String) : UiEvent()
    data class BackupSuccess(val message: String) : UiEvent()
    data class BackupError(val message: String) : UiEvent()
}

/**
 * ViewModel for the Home Screen.
 * Handles user progress, XP calculation, and daily quest management.
 */
class HomeViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val badgeMilestones = listOf(1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 65, 70, 80, 90, 100)
    
    private var lastSeenStreak = 0
    private var lastSeenUser: User? = null

    val user: StateFlow<User> = repository.user
        .onEach { 
            if (it == null) {
                viewModelScope.launch {
                    repository.insertUser(User(id = 0, level = 1, xp = 0, streak = 0, rank = "E-Rank Hunter"))
                }
            } else {
                // Monitor for title unlocks
                if (it.streak > lastSeenStreak) {
                    val newlyUnlocked = repository.checkAndUnlockTitles(it.streak)
                    newlyUnlocked.forEach { title ->
                        _uiEvent.emit(UiEvent.TitleUnlocked(title))
                    }
                    lastSeenStreak = it.streak
                }

                // Monitor for achievement unlocks
                val previousUser = lastSeenUser
                if (previousUser != null) {
                    AchievementData.allAchievements.forEach { achievement ->
                        val wasLocked = !achievement.isUnlocked(previousUser)
                        val isUnlocked = achievement.isUnlocked(it)
                        if (wasLocked && isUnlocked) {
                            _uiEvent.emit(UiEvent.AchievementUnlocked(achievement))
                        }
                    }

                    // Monitor for personal record breaks
                    if (it.maxPushupsSingleWorkout > previousUser.maxPushupsSingleWorkout) {
                        _uiEvent.emit(UiEvent.NewPersonalRecord("Highest Pushups", previousUser.maxPushupsSingleWorkout, it.maxPushupsSingleWorkout))
                    }
                    if (it.maxPullupsSingleWorkout > previousUser.maxPullupsSingleWorkout) {
                        _uiEvent.emit(UiEvent.NewPersonalRecord("Highest Pullups", previousUser.maxPullupsSingleWorkout, it.maxPullupsSingleWorkout))
                    }
                    if (it.maxPlankSingleWorkout > previousUser.maxPlankSingleWorkout) {
                        _uiEvent.emit(UiEvent.NewPersonalRecord("Longest Plank", previousUser.maxPlankSingleWorkout, it.maxPlankSingleWorkout))
                    }
                    if (it.maxXpSingleWorkout > previousUser.maxXpSingleWorkout) {
                        _uiEvent.emit(UiEvent.NewPersonalRecord("Highest Workout XP", previousUser.maxXpSingleWorkout, it.maxXpSingleWorkout))
                    }

                    // Monitor for rank promotions
                    if (RankCalculator.isPromotion(previousUser.rank, it.rank)) {
                        _uiEvent.emit(UiEvent.RankPromotion(previousUser.rank, it.rank))
                    }
                }
                lastSeenUser = it
            }
        }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User())

    init {
        seedTitles()
    }

    private fun seedTitles() {
        viewModelScope.launch {
            val list = repository.allTitles.first()
            if (list.isEmpty()) {
                repository.insertTitles(TitleData.allTitles)
            }
        }
    }

    private val _dailyQuests = MutableStateFlow(
        listOf(
            DailyQuest.createQuest(1, "Push-ups", 20, 50),
            DailyQuest.createQuest(2, "Pull-ups", 10, 75),
            DailyQuest.createQuest(3, "Plank", 60, 40)
        )
    )
    val dailyQuests: StateFlow<List<DailyQuest>> = _dailyQuests.asStateFlow()

    fun toggleSound() {
        viewModelScope.launch {
            val currentUser = user.value
            repository.updateUser(currentUser.copy(soundEnabled = !currentUser.soundEnabled))
        }
    }

    fun completeQuest(questId: Int) {
        val currentQuests = _dailyQuests.value
        val quest = currentQuests.find { it.id == questId }
        if (quest != null && !quest.isCompleted) {
            val updatedQuests = currentQuests.map {
                if (it.id == questId) it.copy(isCompleted = true) else it
            }
            _dailyQuests.value = updatedQuests
            addXP(quest.xpReward)
            
            // Check if all completed for bonus
            if (updatedQuests.all { it.isCompleted }) {
                addXP(100) // Bonus XP
            }
        }
    }

    fun exportData(): String {
        return try {
            val json = JSONObject()
            json.put("version", 1)
            json.put("timestamp", System.currentTimeMillis())

            val user = user.value
            val userJson = JSONObject().apply {
                put("xp", user.xp)
                put("level", user.level)
                put("streak", user.streak)
                put("rank", user.rank)
                put("pushups", user.pushups)
                put("pullups", user.pullups)
                put("plankTime", user.plankTime)
                put("totalXpEarned", user.totalXpEarned)
                put("totalWorkouts", user.totalWorkouts)
                put("highestStreak", user.highestStreak)
                put("totalPromotions", user.totalPromotions)
                put("highestRank", user.highestRank)
                put("lastWorkoutDate", user.lastWorkoutDate)
                put("activeTitle", user.activeTitle ?: JSONObject.NULL)
                put("maxPushupsSingleWorkout", user.maxPushupsSingleWorkout)
                put("maxPullupsSingleWorkout", user.maxPullupsSingleWorkout)
                put("maxPlankSingleWorkout", user.maxPlankSingleWorkout)
                put("maxXpSingleWorkout", user.maxXpSingleWorkout)
            }
            json.put("user", userJson)

            viewModelScope.launch {
                val abilities = repository.abilities.first()
                val abilitiesArray = JSONArray()
                abilities.forEach { ab ->
                    abilitiesArray.put(JSONObject().apply {
                        put("name", ab.name)
                        put("isUnlocked", ab.isUnlocked)
                    })
                }
                json.put("abilities", abilitiesArray)

                val titles = repository.allTitles.first()
                val titlesArray = JSONArray()
                titles.forEach { t ->
                    titlesArray.put(JSONObject().apply {
                        put("name", t.name)
                        put("isUnlocked", t.isUnlocked)
                    })
                }
                json.put("titles", titlesArray)

                val workouts = repository.allWorkouts.first()
                val workoutsArray = JSONArray()
                workouts.forEach { w ->
                    val wJson = JSONObject().apply {
                        put("date", w.workout.date)
                        put("totalXpGained", w.workout.totalXpGained)
                        val exArray = JSONArray()
                        w.exercises.forEach { ex ->
                            exArray.put(JSONObject().apply {
                                put("name", ex.name)
                                put("reps", ex.reps ?: JSONObject.NULL)
                                put("sets", ex.sets)
                                put("duration", ex.duration ?: JSONObject.NULL)
                            })
                        }
                        put("exercises", exArray)
                    }
                    workoutsArray.put(wJson)
                }
                json.put("workouts", workoutsArray)
            }

            json.toString(4)
        } catch (e: Exception) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.BackupError("Export failed: ${e.message}")) }
            ""
        }
    }

    fun importData(jsonString: String) {
        viewModelScope.launch {
            try {
                val json = JSONObject(jsonString)
                val version = json.optInt("version", 1)
                
                val userJson = json.getJSONObject("user")
                val importedUser = User(
                    id = 0,
                    xp = userJson.getInt("xp"),
                    level = userJson.getInt("level"),
                    streak = userJson.getInt("streak"),
                    rank = userJson.getString("rank"),
                    pushups = userJson.getInt("pushups"),
                    pullups = userJson.getInt("pullups"),
                    plankTime = userJson.getInt("plankTime"),
                    totalXpEarned = userJson.getInt("totalXpEarned"),
                    totalWorkouts = userJson.getInt("totalWorkouts"),
                    highestStreak = userJson.getInt("highestStreak"),
                    totalPromotions = userJson.getInt("totalPromotions"),
                    highestRank = userJson.getString("highestRank"),
                    lastWorkoutDate = userJson.getLong("lastWorkoutDate"),
                    activeTitle = if (userJson.isNull("activeTitle")) null else userJson.getString("activeTitle"),
                    maxPushupsSingleWorkout = userJson.getInt("maxPushupsSingleWorkout"),
                    maxPullupsSingleWorkout = userJson.getInt("maxPullupsSingleWorkout"),
                    maxPlankSingleWorkout = userJson.getInt("maxPlankSingleWorkout"),
                    maxXpSingleWorkout = userJson.getInt("maxXpSingleWorkout")
                )

                // 1. Clear and Update User
                repository.updateUser(importedUser)

                // 2. Update Abilities
                if (json.has("abilities")) {
                    val abilitiesArray = json.getJSONArray("abilities")
                    val currentAbilities = repository.abilities.first()
                    val updatedAbilities = currentAbilities.map { ab ->
                        var isUnlocked = ab.isUnlocked
                        for (i in 0 until abilitiesArray.length()) {
                            val abJson = abilitiesArray.getJSONObject(i)
                            if (abJson.getString("name") == ab.name) {
                                isUnlocked = abJson.getBoolean("isUnlocked")
                                break
                            }
                        }
                        ab.copy(isUnlocked = isUnlocked)
                    }
                    repository.updateAbilities(updatedAbilities)
                }

                // 3. Update Titles
                if (json.has("titles")) {
                    val titlesArray = json.getJSONArray("titles")
                    val currentTitles = repository.allTitles.first()
                    val updatedTitles = currentTitles.map { t ->
                        var isUnlocked = t.isUnlocked
                        for (i in 0 until titlesArray.length()) {
                            val tJson = titlesArray.getJSONObject(i)
                            if (tJson.getString("name") == t.name) {
                                isUnlocked = tJson.getBoolean("isUnlocked")
                                break
                            }
                        }
                        t.copy(isUnlocked = isUnlocked)
                    }
                    repository.insertTitles(updatedTitles)
                    // Update unlocked state individually if needed
                    updatedTitles.forEach { repository.updateTitle(it) }
                }

                // 4. Update Workouts (This is complex because we need to clear existing if we want a full restore)
                // For a true "Restore", we might want to clear existing history.
                // For now, let's assume we replace history if backup exists.
                if (json.has("workouts")) {
                    val workoutsArray = json.getJSONArray("workouts")
                    // Note: DAO might need a "clearAllWorkouts" method for true restore.
                    // But we can just add them.
                    for (i in 0 until workoutsArray.length()) {
                        val wJson = workoutsArray.getJSONObject(i)
                        val workoutEntity = WorkoutEntity(
                            date = wJson.getLong("date"),
                            totalXpGained = wJson.getInt("totalXpGained")
                        )
                        val exArray = wJson.getJSONArray("exercises")
                        val exercises = mutableListOf<ExerciseEntity>()
                        for (j in 0 until exArray.length()) {
                            val exJson = exArray.getJSONObject(j)
                            exercises.add(ExerciseEntity(
                                workoutId = 0,
                                name = exJson.getString("name"),
                                reps = if (exJson.isNull("reps")) null else exJson.getInt("reps"),
                                sets = exJson.getInt("sets"),
                                duration = if (exJson.isNull("duration")) null else exJson.getInt("duration")
                            ))
                        }
                        repository.insertWorkout(workoutEntity, exercises)
                    }
                }

                _uiEvent.emit(UiEvent.BackupSuccess("Progress Restored Successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.BackupError("Restore failed: ${e.message}"))
            }
        }
    }

    /**
     * Logic to add XP and handle leveling up.
     * Level thresholds increase as the user levels up.
     */
    private fun addXP(amount: Int) {
        viewModelScope.launch {
            val currentUser = user.value
            val previousLevel = currentUser.level
            var newXp = currentUser.xp + amount
            var newLevel = previousLevel

            while (newXp >= XpCalculator.calculateRequiredXP(newLevel)) {
                newXp -= XpCalculator.calculateRequiredXP(newLevel)
                newLevel++
            }

            // Detect level up and badge unlocks
            if (newLevel > previousLevel) {
                // 1. Emit Level Up Event first
                _uiEvent.emit(UiEvent.LevelUp(previousLevel, newLevel))

                // 2. Emit Badge Unlocks
                badgeMilestones.forEach { milestone ->
                    if (milestone in (previousLevel + 1)..newLevel) {
                        val unlockedBadge = BadgeData.allBadges.find { it.requiredLevel == milestone }
                        unlockedBadge?.let {
                            _uiEvent.emit(UiEvent.BadgeUnlocked(it))
                        }
                    }
                }
            }

            val newRank = RankCalculator.calculateRank(newLevel)
            val isRankPromotion = RankCalculator.isPromotion(currentUser.rank, newRank)

            repository.updateUser(currentUser.copy(
                xp = newXp,
                level = newLevel,
                rank = newRank,
                totalXpEarned = currentUser.totalXpEarned + amount,
                totalPromotions = if (isRankPromotion) currentUser.totalPromotions + 1 else currentUser.totalPromotions,
                highestRank = RankCalculator.getHighestRank(currentUser.highestRank, newRank)
            ))
        }
    }
}
