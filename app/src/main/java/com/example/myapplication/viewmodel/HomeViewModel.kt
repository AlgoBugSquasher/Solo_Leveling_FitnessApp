package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.data.PreferencesManager
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
import java.io.File
import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Home Screen.
 */
class HomeViewModel(
    private val repository: FitnessRepository,
    private val preferencesManager: PreferencesManager,
    private val filesDir: File,
    private val context: Context
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 64)
    val uiEvent = _uiEvent.asSharedFlow()

    private val _avatarUri = MutableStateFlow(preferencesManager.getAvatarUri())
    val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

    private val _shouldShowQuestDialog = MutableStateFlow(false)
    val shouldShowQuestDialog: StateFlow<Boolean> = _shouldShowQuestDialog.asStateFlow()

    private val _showQualificationsDialog = MutableStateFlow(false)
    val showQualificationsDialog: StateFlow<Boolean> = _showQualificationsDialog.asStateFlow()

    private val _showRankDialog = MutableStateFlow(false)
    val showRankDialog: StateFlow<Boolean> = _showRankDialog.asStateFlow()

    private val _isTodayRestDay = MutableStateFlow(false)
    val isTodayRestDay: StateFlow<Boolean> = _isTodayRestDay.asStateFlow()

    fun dismissQuestDialog() {
        _shouldShowQuestDialog.value = false
    }

    fun openRankDialog() {
        _showRankDialog.value = true
    }

    fun dismissRankDialog() {
        _showRankDialog.value = false
    }

    fun acceptQualifications() {
        viewModelScope.launch {
            preferencesManager.setHasAcceptedQualifications(true)
            _showQualificationsDialog.value = false
            _shouldShowQuestDialog.value = true
        }
    }

    fun updateAvatar(uri: String?) {
        preferencesManager.setAvatarUri(uri)
        _avatarUri.value = uri
    }

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

                    // Monitor for badge unlocks (Level based)
                    BadgeData.allBadges.forEach { badge ->
                        val wasReached = previousUser.level >= badge.requiredLevel
                        val isReached = it.level >= badge.requiredLevel
                        if (!wasReached && isReached) {
                            _uiEvent.emit(UiEvent.BadgeUnlocked(badge))
                            viewModelScope.launch {
                                repository.recordJourneyEvent(
                                    eventType = com.example.myapplication.model.JourneyEventType.BADGE,
                                    title = "BADGE UNLOCKED",
                                    description = badge.name,
                                    icon = "🏅",
                                    rarity = com.example.myapplication.model.JourneyRarity.RARE
                                )
                            }
                        }
                    }

                    AchievementData.allAchievements.forEach { achievement ->
                        val wasLocked = !achievement.isUnlocked(previousUser)
                        val isUnlocked = achievement.isUnlocked(it)
                        if (wasLocked && isUnlocked) {
                            _uiEvent.emit(UiEvent.AchievementUnlocked(achievement))
                            viewModelScope.launch {
                                repository.recordJourneyEvent(
                                    eventType = com.example.myapplication.model.JourneyEventType.ACHIEVEMENT,
                                    title = "ACHIEVEMENT UNLOCKED",
                                    description = achievement.name,
                                    icon = "🏆",
                                    rarity = com.example.myapplication.model.JourneyRarity.EPIC
                                )
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
        observeRestDayStatus()
        checkPenalty()
    }

    private fun observeRestDayStatus() {
        viewModelScope.launch {
            repository.allPlannedExercises.collect { exercises ->
                val calendar = Calendar.getInstance()
                val today = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 7
                }
                _isTodayRestDay.value = exercises.none { it.dayOfWeek == today }
            }
        }
    }

    private fun checkPenalty() {
        viewModelScope.launch {
            if (repository.checkStreakReset()) {
                _uiEvent.emit(UiEvent.PenaltyTriggered)
            }
        }
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
                    DailyQuest(id = 1, title = "PUSH-UPS", targetValue = (15..30).random(), xpReward = 50),
                    DailyQuest(id = 2, title = "PULL-UPS", targetValue = (10..15).random(), xpReward = 50),
                    DailyQuest(id = 3, title = "PLANK", targetValue = (60..90).random(), xpReward = 50)
                )
                repository.clearDailyQuests()
                repository.insertDailyQuests(newQuests)
                repository.updateUser(currentUser.copy(lastQuestRefreshDate = now, customXpEarnedToday = 0))
                notifyWidget()
                
                // Sequential Flow Logic
                if (!preferencesManager.hasAcceptedQualifications()) {
                    _showQualificationsDialog.value = true
                } else {
                    _shouldShowQuestDialog.value = true
                }
            } else {
                val quests = repository.allDailyQuests.first()
                if (quests.isNotEmpty() && quests.any { !it.isCompleted }) {
                    if (!preferencesManager.hasAcceptedQualifications()) {
                        _showQualificationsDialog.value = true
                    } else {
                        _shouldShowQuestDialog.value = true
                    }
                }
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

    val weeklyXp: StateFlow<Int> = repository.getWeeklyXp()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val latestAchievement: StateFlow<Achievement?> = repository.user
        .filterNotNull()
        .map { user ->
            AchievementData.allAchievements
                .filter { it.isUnlocked(user) }
                .maxByOrNull { achievement -> 
                    // We don't have an unlock date in User, so we just pick the hardest or last in list
                    // In a real app, we'd store achievement unlock timestamps.
                    // For now, let's pick the one with the highest requirement.
                    achievement.id
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
                val updatedQuest = quest.copy(currentProgress = quest.targetValue)
                repository.updateDailyQuest(updatedQuest)
                notifyWidget()
            }
        }
    }

    fun toggleQuestProgress(questId: Int) {
        viewModelScope.launch {
            val quests = dailyQuests.value
            val quest = quests.find { it.id == questId }
            if (quest != null && !quest.isCompleted) {
                val newProgress = if (quest.currentProgress < quest.targetValue) {
                    quest.targetValue
                } else {
                    0
                }
                repository.updateDailyQuest(quest.copy(currentProgress = newProgress))
                notifyWidget()
            }
        }
    }

    private fun notifyWidget() {
        val intent = Intent("com.example.myapplication.ACTION_DATA_UPDATED")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    fun incrementQuestProgress(exerciseTitle: String, amount: Int) {
        viewModelScope.launch {
            val quests = dailyQuests.value
            val quest = quests.find { it.title.equals(exerciseTitle, ignoreCase = true) }
            if (quest != null && !quest.isCompleted) {
                val newProgress = (quest.currentProgress + amount).coerceAtMost(quest.targetValue)
                val updatedQuest = quest.copy(currentProgress = newProgress)
                repository.updateDailyQuest(updatedQuest)
                notifyWidget()
            }
        }
    }

    fun claimDailyQuestReward() {
        viewModelScope.launch {
            val quests = dailyQuests.value
            if (quests.isNotEmpty() && quests.all { it.currentProgress >= it.targetValue && !it.isCompleted }) {
                // Mark all as completed
                quests.forEach { quest ->
                    repository.updateDailyQuest(quest.copy(isCompleted = true))
                }
                
                // Grant XP: 50 XP base + 100 XP bonus for all done? 
                // User said "CLAIM REWARD (+50 XP)". Let's stick to +50 XP or clarify.
                // Usually it's +50 per quest? No, quests.forEach above is for marking.
                // Let's grant 50 XP as requested on the button.
                repository.recordProgress(xpGained = 50)
                _uiEvent.emit(UiEvent.XpGained(50))
                notifyWidget()
                
                repository.recordJourneyEvent(
                    eventType = com.example.myapplication.model.JourneyEventType.ACHIEVEMENT,
                    title = "DAILY QUEST COMPLETED",
                    description = "Preparation complete. You have gained a small amount of power.",
                    icon = "⚡",
                    rarity = com.example.myapplication.model.JourneyRarity.RARE
                )
            }
        }
    }

    suspend fun exportData(): String = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject()
            json.put("version", 21)
            json.put("timestamp", System.currentTimeMillis())
            json.put("has_accepted_qualifications", preferencesManager.hasAcceptedQualifications())

            val user = user.value
            val userJson = JSONObject().apply {
                put("xp", user.xp)
                put("level", user.level)
                put("streak", user.streak)
                put("rank", user.rank)
                put("pushups", user.pushups)
                put("pullups", user.pullups)
                put("plankTime", user.plankTime)
                put("totalDistanceKm", user.totalDistanceKm)
                put("totalXpEarned", user.totalXpEarned)
                put("totalWorkouts", user.totalWorkouts)
                put("highestStreak", user.highestStreak)
                put("totalPromotions", user.totalPromotions)
                put("highestRank", user.highestRank)
                put("lastWorkoutDate", user.lastWorkoutDate)
                put("lastQuestRefreshDate", user.lastQuestRefreshDate)
                put("activeTitle", user.activeTitle ?: JSONObject.NULL)
                put("soundEnabled", user.soundEnabled)
                put("maxPushupsSingleWorkout", user.maxPushupsSingleWorkout)
                put("maxPullupsSingleWorkout", user.maxPullupsSingleWorkout)
                put("maxPlankSingleWorkout", user.maxPlankSingleWorkout)
                put("maxXpSingleWorkout", user.maxXpSingleWorkout)
                put("totalPikePushups", user.totalPikePushups)
                put("totalPseudoPlanchePushups", user.totalPseudoPlanchePushups)
                put("totalHangingSeconds", user.totalHangingSeconds)
                put("totalExplosivePullups", user.totalExplosivePullups)
            }
            json.put("user", userJson)

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
                            put("category", ex.category.name)
                            put("trackingType", ex.trackingType.name)
                            put("reps", ex.reps ?: JSONObject.NULL)
                            put("sets", ex.sets)
                            put("duration", ex.duration ?: JSONObject.NULL)
                            put("distanceKm", ex.distanceKm ?: JSONObject.NULL)
                        })
                    }
                    put("exercises", exArray)
                }
                workoutsArray.put(wJson)
            }
            json.put("workouts", workoutsArray)

            val events = repository.allJourneyEvents.first()
            val eventsArray = JSONArray()
            events.forEach { ev ->
                eventsArray.put(JSONObject().apply {
                    put("eventType", ev.eventType.name)
                    put("title", ev.title)
                    put("description", ev.description)
                    put("timestamp", ev.timestamp)
                    put("icon", ev.icon)
                    put("rarity", ev.rarity.name)
                    put("xpReward", ev.xpReward ?: JSONObject.NULL)
                })
            }
            json.put("journey_events", eventsArray)

            val trainingDays = repository.trainingPlan.first()
            val daysArray = JSONArray()
            trainingDays.forEach { d ->
                daysArray.put(JSONObject().apply {
                    put("dayOfWeek", d.dayOfWeek)
                    put("isCompleted", d.isCompleted)
                    put("lastCompletedWeek", d.lastCompletedWeek)
                    put("lastCompletedYear", d.lastCompletedYear)
                    put("lastRewardWeek", d.lastRewardWeek)
                    put("lastRewardYear", d.lastRewardYear)
                })
            }
            json.put("training_days", daysArray)

            val plannedExercises = repository.allPlannedExercises.first()
            val plannedArray = JSONArray()
            plannedExercises.forEach { pe ->
                plannedArray.put(JSONObject().apply {
                    put("dayOfWeek", pe.dayOfWeek)
                    put("name", pe.name)
                    put("trackingType", pe.trackingType.name)
                    put("sets", pe.sets ?: JSONObject.NULL)
                    put("reps", pe.reps ?: JSONObject.NULL)
                    put("seconds", pe.seconds ?: JSONObject.NULL)
                    put("distanceKm", pe.distanceKm ?: JSONObject.NULL)
                    put("isCompleted", pe.isCompleted)
                    put("lastCompletedWeek", pe.lastCompletedWeek)
                    put("lastCompletedYear", pe.lastCompletedYear)
                })
            }
            json.put("planned_exercises", plannedArray)

            val weeklyBonus = repository.getWeeklyBonusSync()
            if (weeklyBonus != null) {
                json.put("weekly_bonus", JSONObject().apply {
                    put("lastBonusWeek", weeklyBonus.lastBonusWeek)
                    put("lastBonusYear", weeklyBonus.lastBonusYear)
                })
            }

            val quests = repository.allDailyQuests.first()
            val questsArray = JSONArray()
            quests.forEach { q ->
                questsArray.put(JSONObject().apply {
                    put("id", q.id)
                    put("title", q.title)
                    put("currentProgress", q.currentProgress)
                    put("targetValue", q.targetValue)
                    put("xpReward", q.xpReward)
                    put("isCompleted", q.isCompleted)
                })
            }
            json.put("daily_quests", questsArray)

            val notes = repository.allNotes.first()
            val notesArray = JSONArray()
            notes.forEach { n ->
                notesArray.put(JSONObject().apply {
                    put("title", n.title)
                    put("content", n.content)
                    put("timestamp", n.timestamp)
                })
            }
            json.put("notes", notesArray)

            // Avatar Backup
            val currentAvatarPath = preferencesManager.getAvatarUri()
            if (currentAvatarPath != null) {
                val avatarFile = File(currentAvatarPath)
                if (avatarFile.exists()) {
                    try {
                        val bytes = avatarFile.readBytes()
                        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                        json.put("avatar_base64", base64)
                    } catch (e: Exception) {
                        // Log or ignore if image too large, but we try
                    }
                }
            }

            json.toString(4)
        } catch (e: Exception) {
            _uiEvent.emit(UiEvent.BackupError("Export failed: ${e.message}"))
            ""
        }
    }

    fun importData(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject(jsonString)
                val version = json.optInt("version", 1)

                // 0. Preferences
                val hasAccepted = json.optBoolean("has_accepted_qualifications", false)
                preferencesManager.setHasAcceptedQualifications(hasAccepted)
                if (hasAccepted) preferencesManager.setFirstLaunch(false)
                
                // 1. User
                val userJson = json.optJSONObject("user") ?: JSONObject()
                val importedUser = User(
                    id = 0,
                    xp = userJson.optInt("xp", 0),
                    level = userJson.optInt("level", 1),
                    streak = userJson.optInt("streak", 0),
                    rank = userJson.optString("rank", "E-Rank Hunter"),
                    pushups = userJson.optInt("pushups", 0),
                    pullups = userJson.optInt("pullups", 0),
                    plankTime = userJson.optInt("plankTime", 0),
                    totalDistanceKm = userJson.optDouble("totalDistanceKm", 0.0),
                    totalXpEarned = userJson.optInt("totalXpEarned", 0),
                    totalWorkouts = userJson.optInt("totalWorkouts", 0),
                    highestStreak = userJson.optInt("highestStreak", 0),
                    totalPromotions = userJson.optInt("totalPromotions", 0),
                    highestRank = userJson.optString("highestRank", "E-Rank Hunter"),
                    lastWorkoutDate = userJson.optLong("lastWorkoutDate", 0),
                    lastQuestRefreshDate = userJson.optLong("lastQuestRefreshDate", 0),
                    activeTitle = if (userJson.isNull("activeTitle")) null else userJson.optString("activeTitle"),
                    soundEnabled = userJson.optBoolean("soundEnabled", true),
                    maxPushupsSingleWorkout = userJson.optInt("maxPushupsSingleWorkout", 0),
                    maxPullupsSingleWorkout = userJson.optInt("maxPullupsSingleWorkout", 0),
                    maxPlankSingleWorkout = userJson.optInt("maxPlankSingleWorkout", 0),
                    maxXpSingleWorkout = userJson.optInt("maxXpSingleWorkout", 0),
                    totalPikePushups = userJson.optInt("totalPikePushups", 0),
                    totalPseudoPlanchePushups = userJson.optInt("totalPseudoPlanchePushups", 0),
                    totalHangingSeconds = userJson.optInt("totalHangingSeconds", 0),
                    totalExplosivePullups = userJson.optInt("totalExplosivePullups", 0)
                )

                // 2. Abilities
                val abilities = mutableListOf<com.example.myapplication.model.Ability>()
                if (json.has("abilities")) {
                    val abilitiesArray = json.optJSONArray("abilities")
                    if (abilitiesArray != null) {
                        val currentAbilities = repository.abilities.first()
                        currentAbilities.forEach { ab ->
                            var isUnlocked = ab.isUnlocked
                            for (i in 0 until abilitiesArray.length()) {
                                val abJson = abilitiesArray.optJSONObject(i) ?: continue
                                if (abJson.optString("name") == ab.name) {
                                    isUnlocked = abJson.optBoolean("isUnlocked", false)
                                    break
                                }
                            }
                            abilities.add(ab.copy(isUnlocked = isUnlocked))
                        }
                    }
                }

                // 3. Titles
                val titles = mutableListOf<com.example.myapplication.model.Title>()
                if (json.has("titles")) {
                    val titlesArray = json.optJSONArray("titles")
                    if (titlesArray != null) {
                        val currentTitles = TitleData.allTitles
                        currentTitles.forEach { t ->
                            var isUnlocked = t.isUnlocked
                            for (i in 0 until titlesArray.length()) {
                                val tJson = titlesArray.optJSONObject(i) ?: continue
                                if (tJson.optString("name") == t.name) {
                                    isUnlocked = tJson.optBoolean("isUnlocked", false)
                                    break
                                }
                            }
                            titles.add(t.copy(isUnlocked = isUnlocked))
                        }
                    }
                }

                // 4. Workouts
                val workouts = mutableListOf<com.example.myapplication.model.WorkoutWithExercises>()
                if (json.has("workouts")) {
                    val workoutsArray = json.optJSONArray("workouts")
                    if (workoutsArray != null) {
                        for (i in 0 until workoutsArray.length()) {
                            val wJson = workoutsArray.optJSONObject(i) ?: continue
                            val workoutEntity = WorkoutEntity(
                                date = wJson.optLong("date", 0),
                                totalXpGained = wJson.optInt("totalXpGained", 0)
                            )
                            val exArray = wJson.optJSONArray("exercises") ?: JSONArray()
                            val exercises = mutableListOf<ExerciseEntity>()
                            for (j in 0 until exArray.length()) {
                                val exJson = exArray.optJSONObject(j) ?: continue
                                exercises.add(ExerciseEntity(
                                    workoutId = 0,
                                    name = exJson.optString("name", "EXERCISE"),
                                    category = try { com.example.myapplication.model.ExerciseCategory.valueOf(exJson.optString("category", "OTHER")) } catch(e: Exception) { com.example.myapplication.model.ExerciseCategory.OTHER },
                                    trackingType = try { com.example.myapplication.model.ExerciseTrackingType.valueOf(exJson.optString("trackingType", "REPS")) } catch(e: Exception) { com.example.myapplication.model.ExerciseTrackingType.REPS },
                                    reps = if (exJson.isNull("reps")) null else exJson.optInt("reps"),
                                    sets = exJson.optInt("sets", 0),
                                    duration = if (exJson.isNull("duration")) null else exJson.optInt("duration"),
                                    distanceKm = if (exJson.isNull("distanceKm")) null else exJson.optDouble("distanceKm")
                                ))
                            }
                            workouts.add(com.example.myapplication.model.WorkoutWithExercises(workoutEntity, exercises))
                        }
                    }
                }

                // 5. Journey Events
                val events = mutableListOf<com.example.myapplication.model.JourneyEvent>()
                if (json.has("journey_events")) {
                    val eventsArray = json.optJSONArray("journey_events")
                    if (eventsArray != null) {
                        for (i in 0 until eventsArray.length()) {
                            val evJson = eventsArray.optJSONObject(i) ?: continue
                            events.add(com.example.myapplication.model.JourneyEvent(
                                eventType = try { com.example.myapplication.model.JourneyEventType.valueOf(evJson.optString("eventType", "SYSTEM")) } catch(e: Exception) { com.example.myapplication.model.JourneyEventType.SYSTEM },
                                title = evJson.optString("title", "EVENT"),
                                description = evJson.optString("description", ""),
                                timestamp = evJson.optLong("timestamp", 0),
                                icon = evJson.optString("icon", "📍"),
                                rarity = try { com.example.myapplication.model.JourneyRarity.valueOf(evJson.optString("rarity", "COMMON")) } catch(e: Exception) { com.example.myapplication.model.JourneyRarity.COMMON },
                                xpReward = if (evJson.isNull("xpReward")) null else evJson.optInt("xpReward")
                            ))
                        }
                    }
                }

                // 6. Training Days
                val trainingDays = mutableListOf<com.example.myapplication.model.TrainingDay>()
                if (json.has("training_days")) {
                    val daysArray = json.optJSONArray("training_days")
                    if (daysArray != null) {
                        for (i in 0 until daysArray.length()) {
                            val dJson = daysArray.optJSONObject(i) ?: continue
                            trainingDays.add(com.example.myapplication.model.TrainingDay(
                                dayOfWeek = dJson.optInt("dayOfWeek", 1),
                                isCompleted = dJson.optBoolean("isCompleted", false),
                                lastCompletedWeek = dJson.optInt("lastCompletedWeek", 0),
                                lastCompletedYear = dJson.optInt("lastCompletedYear", 0),
                                lastRewardWeek = dJson.optInt("lastRewardWeek", 0),
                                lastRewardYear = dJson.optInt("lastRewardYear", 0)
                            ))
                        }
                    }
                }

                // 7. Planned Exercises
                val plannedExercises = mutableListOf<com.example.myapplication.model.PlannedExercise>()
                if (json.has("planned_exercises")) {
                    val plannedArray = json.optJSONArray("planned_exercises")
                    if (plannedArray != null) {
                        for (i in 0 until plannedArray.length()) {
                            val peJson = plannedArray.optJSONObject(i) ?: continue
                            plannedExercises.add(com.example.myapplication.model.PlannedExercise(
                                dayOfWeek = peJson.optInt("dayOfWeek", 1),
                                name = peJson.optString("name", "EXERCISE"),
                                trackingType = try { com.example.myapplication.model.ExerciseTrackingType.valueOf(peJson.optString("trackingType", "REPS")) } catch(e: Exception) { com.example.myapplication.model.ExerciseTrackingType.REPS },
                                sets = if (peJson.isNull("sets")) null else peJson.optInt("sets"),
                                reps = if (peJson.isNull("reps")) null else peJson.optInt("reps"),
                                seconds = if (peJson.isNull("seconds")) null else peJson.optInt("seconds"),
                                distanceKm = if (peJson.isNull("distanceKm")) null else peJson.optDouble("distanceKm"),
                                isCompleted = peJson.optBoolean("isCompleted", false),
                                lastCompletedWeek = peJson.optInt("lastCompletedWeek", 0),
                                lastCompletedYear = peJson.optInt("lastCompletedYear", 0)
                            ))
                        }
                    }
                }

                // 8. Weekly Bonus
                var weeklyBonus: com.example.myapplication.model.WeeklyBonusEntity? = null
                if (json.has("weekly_bonus")) {
                    val wbJson = json.optJSONObject("weekly_bonus")
                    if (wbJson != null) {
                        weeklyBonus = com.example.myapplication.model.WeeklyBonusEntity(
                            lastBonusWeek = wbJson.optInt("lastBonusWeek", 0),
                            lastBonusYear = wbJson.optInt("lastBonusYear", 0)
                        )
                    }
                }

                // 9. Daily Quests
                val dailyQuests = mutableListOf<com.example.myapplication.model.DailyQuest>()
                if (json.has("daily_quests")) {
                    val questsArray = json.optJSONArray("daily_quests")
                    if (questsArray != null) {
                        for (i in 0 until questsArray.length()) {
                            val qJson = questsArray.optJSONObject(i) ?: continue

                            val target = when {
                                qJson.has("targetValue") -> qJson.optInt("targetValue", 20)
                                qJson.has("goal") -> qJson.optInt("goal", 20)
                                qJson.has("reps") -> qJson.optInt("reps", 20)
                                else -> 20
                            }

                            val progress = when {
                                qJson.has("currentProgress") -> qJson.optInt("currentProgress", 0)
                                qJson.has("progress") -> qJson.optInt("progress", 0)
                                qJson.has("completed") -> if (qJson.optBoolean("completed", false)) target else 0
                                else -> 0
                            }

                            dailyQuests.add(com.example.myapplication.model.DailyQuest(
                                id = qJson.optInt("id", i + 1),
                                title = qJson.optString("title", "QUEST"),
                                currentProgress = progress,
                                targetValue = target,
                                xpReward = qJson.optInt("xpReward", 50),
                                isCompleted = qJson.optBoolean("isCompleted", false)
                            ))
                        }
                    }
                }

                // 10. Notes
                val notes = mutableListOf<com.example.myapplication.model.Note>()
                if (json.has("notes")) {
                    val notesArray = json.optJSONArray("notes")
                    if (notesArray != null) {
                        for (i in 0 until notesArray.length()) {
                            val nJson = notesArray.optJSONObject(i) ?: continue
                            notes.add(com.example.myapplication.model.Note(
                                title = nJson.optString("title", "NOTE"),
                                content = nJson.optString("content", ""),
                                timestamp = nJson.optLong("timestamp", 0)
                            ))
                        }
                    }
                }

                // 11. Avatar Restore
                if (json.has("avatar_base64")) {
                    try {
                        val base64 = json.getString("avatar_base64")
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val avatarFile = File(filesDir, "custom_avatar.jpg")
                        avatarFile.writeBytes(bytes)
                        preferencesManager.setAvatarUri(avatarFile.absolutePath)
                        _avatarUri.value = avatarFile.absolutePath
                    } catch (e: Exception) {}
                }

                // Atomic Restore
                repository.restoreDatabase(
                    user = importedUser,
                    abilities = abilities,
                    workouts = workouts,
                    titles = titles,
                    trainingDays = trainingDays,
                    plannedExercises = plannedExercises,
                    weeklyBonus = weeklyBonus,
                    journeyEvents = events,
                    dailyQuests = dailyQuests,
                    notes = notes
                )

                _uiEvent.emit(UiEvent.BackupSuccess("Backup Restored Successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.BackupError("Restore failed: ${e.message}"))
            }
        }
    }
}
