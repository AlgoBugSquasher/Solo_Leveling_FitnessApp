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
import java.util.Calendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * ViewModel for the Home Screen.
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
                    // Monitor for level ups
                    if (it.level > previousUser.level) {
                        _uiEvent.emit(UiEvent.LevelUp(previousUser.level, it.level))
                    }

                    AchievementData.allAchievements.forEach { achievement ->
                        val wasLocked = !achievement.isUnlocked(previousUser)
                        val isUnlocked = achievement.isUnlocked(it)
                        if (wasLocked && isUnlocked) {
                            _uiEvent.emit(UiEvent.AchievementUnlocked(achievement))
                            viewModelScope.launch {
                                repository.recordJourneyEvent("ACHIEVEMENT_UNLOCKED", "ACHIEVEMENT UNLOCKED", achievement.name, "🏆")
                            }
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
        checkAndRefreshQuests()
    }

    private fun seedTitles() {
        viewModelScope.launch {
            val list = repository.allTitles.first()
            if (list.isEmpty()) {
                repository.insertTitles(TitleData.allTitles)
            }
        }
    }

    private fun checkAndRefreshQuests() {
        viewModelScope.launch {
            val currentUser = repository.user.filterNotNull().first()
            val now = System.currentTimeMillis()
            
            if (shouldRefreshQuests(currentUser.lastQuestRefreshDate, now)) {
                val newQuests = listOf(
                    DailyQuest.createQuest(1, "Push-ups", 20, 50),
                    DailyQuest.createQuest(2, "Pull-ups", 10, 75),
                    DailyQuest.createQuest(3, "Plank", 60, 40)
                )
                repository.clearDailyQuests()
                repository.insertDailyQuests(newQuests)
                repository.updateUser(currentUser.copy(lastQuestRefreshDate = now))
            }
        }
    }

    private fun shouldRefreshQuests(lastRefresh: Long, now: Long): Boolean {
        if (lastRefresh == 0L) return true
        
        val last = Calendar.getInstance().apply { timeInMillis = lastRefresh }
        val current = Calendar.getInstance().apply { timeInMillis = now }
        
        return last.get(Calendar.YEAR) != current.get(Calendar.YEAR) ||
               last.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR)
    }

    val dailyQuests: StateFlow<List<DailyQuest>> = repository.allDailyQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSound() {
        viewModelScope.launch {
            val currentUser = user.value
            repository.updateUser(currentUser.copy(soundEnabled = !currentUser.soundEnabled))
        }
    }

    fun completeQuest(questId: Int) {
        viewModelScope.launch {
            val currentQuests = dailyQuests.value
            val quest = currentQuests.find { it.id == questId }
            if (quest != null && !quest.isCompleted) {
                val updatedQuest = quest.copy(isCompleted = true)
                repository.updateDailyQuest(updatedQuest)
                
                val sets = quest.sets ?: 1
                val reps = quest.reps ?: 0
                val addedPushups = if (quest.title.contains("Push-up", ignoreCase = true)) reps * sets else 0
                val addedPullups = if (quest.title.contains("Pull-up", ignoreCase = true)) reps * sets else 0
                val addedPlank = if (quest.title.contains("Plank", ignoreCase = true)) quest.goal.filter { it.isDigit() }.toIntOrNull() ?: 0 else 0

                val isFirstQuest = repository.getEventCountByType("FIRST_QUEST") == 0
                if (isFirstQuest) {
                    repository.recordJourneyEvent("FIRST_QUEST", "FIRST QUEST COMPLETED", "Your first mission is a success.", "📜")
                }

                repository.recordProgress(
                    pushups = addedPushups,
                    pullups = addedPullups,
                    plankSeconds = addedPlank,
                    xpGained = quest.xpReward
                )
                
                // Check if all completed for bonus
                val allQuestsAfterUpdate = repository.allDailyQuests.first()
                if (allQuestsAfterUpdate.all { it.isCompleted }) {
                    repository.recordProgress(xpGained = 100) // Bonus XP
                }
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
}
