package com.exork.app.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.exork.app.data.FitnessRepository
import com.exork.app.data.PreferencesManager
import com.exork.app.model.Ability
import com.exork.app.model.Badge
import com.exork.app.model.BadgeData
import com.exork.app.model.Title
import com.exork.app.model.TitleData
import com.exork.app.model.DailyQuest
import com.exork.app.model.User
import com.exork.app.model.Achievement
import com.exork.app.model.AchievementData
import com.exork.app.model.ExerciseEntity
import com.exork.app.model.WorkoutEntity
import com.exork.app.model.PlannedExercise
import com.exork.app.model.ExerciseTrackingType
import com.exork.app.util.RankCalculator
import com.exork.app.util.XpCalculator
import java.util.Calendar
import java.util.Date
import java.io.File
import java.time.LocalDate
import android.util.Base64
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

enum class DialogType { NONE, WELCOME, QUEST_INFO, USERNAME_SETUP }

enum class UsernameValidation { NONE, VALIDATING, AVAILABLE, TAKEN, ERROR }

/**
 * ViewModel for the Home Screen.
 */
class HomeViewModel(
    private val repository: FitnessRepository,
    private val preferencesManager: PreferencesManager,
    private val filesDir: File,
    private val context: Context
) : ViewModel() {

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _usernameValidation = MutableStateFlow(UsernameValidation.NONE)
    val usernameValidation: StateFlow<UsernameValidation> = _usernameValidation.asStateFlow()

    private val _dialogQueue = MutableStateFlow<List<DialogType>>(emptyList())
    val activeDialog = _dialogQueue.map { it.firstOrNull() ?: DialogType.NONE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DialogType.NONE)

    fun dismissActiveDialog() {
        _dialogQueue.value = _dialogQueue.value.drop(1)
    }

    private fun queueDialog(type: DialogType) {
        if (!_dialogQueue.value.contains(type)) {
            _dialogQueue.value = _dialogQueue.value + type
        }
    }

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

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _floatingXpReward = MutableStateFlow<Int?>(null)
    val floatingXpReward: StateFlow<Int?> = _floatingXpReward.asStateFlow()

    private var userSnapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun dismissQuestDialog() {
        _shouldShowQuestDialog.value = false
    }

    fun logout() {
        viewModelScope.launch {
            // 1. Force state reset FIRST to avoid race conditions
            _username.value = null
            _avatarUri.value = null
            
            // 2. Emit logout event to MainActivity
            _uiEvent.emit(UiEvent.Logout)
            
            // 3. Clear local database and preferences
            repository.clearAllDatabase()
            preferencesManager.setAvatarUri(null)
            preferencesManager.setFirstLaunch(true)
            preferencesManager.setHasAcceptedQualifications(false)
        }
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
            dismissActiveDialog()
            queueDialog(DialogType.QUEST_INFO)
        }
    }

    fun updateAvatar(uri: String?) {
        viewModelScope.launch {
            if (uri != null) {
                val cloudUrl = repository.uploadAvatar(uri, context)
                if (cloudUrl != null) {
                    _avatarUri.value = cloudUrl
                }
            } else {
                preferencesManager.setAvatarUri(null)
                _avatarUri.value = null
                // Clear in Firestore
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
                        .update("photoUrl", null)
                }
            }
        }
    }

    private val badgeMilestones = listOf(1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 65, 70, 80, 90, 100)
    
    private var lastSeenStreak = 0
    private var lastSeenUser: User? = null

    val user: StateFlow<User> = repository.user
        .onEach {
            if (it == null) {
                // ONLY create a default user if we have an active session
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    viewModelScope.launch {
                        repository.insertUser(User(id = 0, level = 1, xp = 0, streak = 0, rank = "E-Rank Hunter"))
                    }
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
                                    eventType = com.exork.app.model.JourneyEventType.BADGE,
                                    title = "BADGE UNLOCKED",
                                    description = badge.name,
                                    icon = "🏅",
                                    rarity = com.exork.app.model.JourneyRarity.RARE
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
                                    eventType = com.exork.app.model.JourneyEventType.ACHIEVEMENT,
                                    title = "ACHIEVEMENT UNLOCKED",
                                    description = achievement.name,
                                    icon = "🏆",
                                    rarity = com.exork.app.model.JourneyRarity.EPIC
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
        observeRestDayStatus()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            syncFromRemote()
        }
    }

    fun syncFromRemote() {
        viewModelScope.launch {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                _isSyncing.value = true
                
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = try { db.collection("users").document(firebaseUser.uid).get().await() } catch(e: Exception) { null }
                
                if (doc != null && doc.exists()) {
                    _username.value = doc.getString("username")
                    val remoteLastRefresh = doc.getLong("lastQuestRefreshDate") ?: 0L
                    val remoteXp = doc.getLong("totalXp")?.toInt() ?: doc.getLong("xp")?.toInt() ?: 0
                    val remoteLevel = doc.getLong("hunterLevel")?.toInt() ?: 1
                    val remoteStreak = doc.getLong("currentStreak")?.toInt() ?: 0
                    val remoteLastQuestCompletedDate = doc.getString("lastQuestCompletedDate") ?: ""
                    val remotePhotoUrl = doc.getString("photoUrl")
                    val remoteGuildId = doc.getString("guildId")
                    val remoteGuildName = doc.getString("guildName")
                    val remoteGuildTag = doc.getString("guildTag")

                    // 1. Check if local DB is empty
                    val currentUserSnapshot = repository.user.first()
                    if (currentUserSnapshot == null || (currentUserSnapshot.totalXpEarned == 0 && currentUserSnapshot.totalWorkouts == 0)) {
                        val remoteJson = doc.getString("backup_json")
                        if (remoteJson != null) {
                            // 2. Perform full restore from JSON
                            performDataRestore(remoteJson, isRemoteSync = true)
                        }
                    }

                    // 2.5 Pull workout history from cloud
                    repository.fetchWorkoutHistoryFromCloud()

                    // 3. FORCE OVERWRITE STALE JSON DATA WITH LIVE ROOT FIELDS
                    val userAfterRestore = repository.user.first() ?: User()
                    repository.updateUser(userAfterRestore.copy(
                        totalXpEarned = remoteXp,
                        xp = XpCalculator.calculateCurrentLevelXp(remoteXp, remoteLevel),
                        level = remoteLevel,
                        streak = remoteStreak,
                        lastQuestRefreshDate = remoteLastRefresh,
                        lastQuestCompletedDate = remoteLastQuestCompletedDate,
                        photoUrl = remotePhotoUrl ?: userAfterRestore.photoUrl,
                        guildId = remoteGuildId ?: userAfterRestore.guildId,
                        guildName = remoteGuildName ?: userAfterRestore.guildName,
                        guildTag = remoteGuildTag ?: userAfterRestore.guildTag
                    ))

                    // 4. Pull live quests if they are for today
                    val dailyQuestsMap = doc.get("dailyQuests") as? Map<String, Any>
                    if (dailyQuestsMap != null && !shouldRefreshQuests(remoteLastRefresh, System.currentTimeMillis())) {
                        val remoteQuests = dailyQuestsMap.values.mapNotNull { 
                            val q = it as? Map<String, Any> ?: return@mapNotNull null
                            DailyQuest(
                                id = (q["id"] as? Long)?.toInt() ?: 1,
                                title = q["title"] as? String ?: "QUEST",
                                currentProgress = (q["currentProgress"] as? Long)?.toInt() ?: 0,
                                targetValue = (q["targetValue"] as? Long)?.toInt() ?: 20,
                                xpReward = (q["xpReward"] as? Long)?.toInt() ?: 50,
                                isCompleted = q["isCompleted"] as? Boolean ?: false
                            )
                        }
                        if (remoteQuests.isNotEmpty()) {
                            repository.clearDailyQuests()
                            repository.insertDailyQuests(remoteQuests)
                        }
                    }

                    // 5. Pull live planned exercises
                    val remotePlannedMap = doc.get("plannedExercises") as? Map<String, Any>
                    if (remotePlannedMap != null) {
                        val remoteExercises = remotePlannedMap.values.mapNotNull {
                            val e = it as? Map<String, Any> ?: return@mapNotNull null
                            PlannedExercise(
                                dayOfWeek = (e["dayOfWeek"] as? Long)?.toInt() ?: 1,
                                name = e["name"] as? String ?: "EXERCISE",
                                trackingType = ExerciseTrackingType.valueOf(e["trackingType"] as? String ?: "REPS"),
                                sets = (e["sets"] as? Long)?.toInt(),
                                reps = (e["reps"] as? Long)?.toInt(),
                                seconds = (e["seconds"] as? Long)?.toInt(),
                                distanceKm = e["distanceKm"] as? Double,
                                isCompleted = e["isCompleted"] as? Boolean ?: false,
                                lastCompletedWeek = (e["lastCompletedWeek"] as? Long)?.toInt() ?: 0,
                                lastCompletedYear = (e["lastCompletedYear"] as? Long)?.toInt() ?: 0
                            )
                        }
                        if (remoteExercises.isNotEmpty()) {
                            repository.clearAllPlannedExercises()
                            repository.insertPlannedExercises(remoteExercises)
                        }
                    }

                    // 6. Pull live notes directly from sub-collection (Single Source of Truth)
                    repository.fetchNotesFromFirestore()

                    startRealTimeUserListener(firebaseUser.uid)
                } else {
                    repository.initializeUserInFirestore()
                }

                if (_username.value == null) {
                    queueDialog(DialogType.USERNAME_SETUP)
                }
                
                checkPenalty()
                checkAndRefreshQuests()
                _isSyncing.value = false
            } else {
                checkPenalty()
                checkAndRefreshQuests()
            }
        }
    }

    private fun startRealTimeUserListener(userId: String) {
        userSnapshotListener?.remove()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        userSnapshotListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val remoteXp = snapshot.getLong("totalXp")?.toInt() ?: snapshot.getLong("xp")?.toInt() ?: return@addSnapshotListener
                val remoteStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0
                val remoteLastQuestDate = snapshot.getString("lastQuestCompletedDate") ?: ""
                val remoteLevel = snapshot.getLong("hunterLevel")?.toInt() ?: 1
                val remoteRank = snapshot.getString("hunterRank") ?: "E-Rank Hunter"

                viewModelScope.launch {
                    val currentUser = repository.user.first()
                    if (currentUser != null) {
                        // Only update if remote is actually different/newer to avoid local loopbacks
                        if (remoteXp != currentUser.totalXpEarned || 
                            remoteStreak != currentUser.streak || 
                            remoteLastQuestDate != currentUser.lastQuestCompletedDate ||
                            remoteLevel != currentUser.level) {
                            
                            // If XP increased, emit gained event for global UI popup
                            if (remoteXp > currentUser.totalXpEarned) {
                                _uiEvent.emit(UiEvent.XpGained(remoteXp - currentUser.totalXpEarned))
                            }

                            repository.updateUser(currentUser.copy(
                                totalXpEarned = remoteXp,
                                streak = remoteStreak,
                                lastQuestCompletedDate = remoteLastQuestDate,
                                level = remoteLevel,
                                rank = remoteRank
                            ))
                        }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        userSnapshotListener?.remove()
    }

    private var validationJob: Job? = null
    fun validateUsername(name: String) {
        if (name.length < 3) {
            _usernameValidation.value = UsernameValidation.NONE
            return
        }
        
        validationJob?.cancel()
        validationJob = viewModelScope.launch {
            _usernameValidation.value = UsernameValidation.VALIDATING
            delay(500) // Debounce
            val isAvailable = repository.checkUsernameAvailability(name)
            _usernameValidation.value = if (isAvailable) UsernameValidation.AVAILABLE else UsernameValidation.TAKEN
        }
    }

    fun saveUsername(name: String) {
        viewModelScope.launch {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@launch
            repository.updateUsernameInFirestore(firebaseUser.uid, name)
            _username.value = name
            dismissActiveDialog()
        }
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
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            // Fix: Bulletproof Suppression
            if (currentUser.lastQuestCompletedDate == todayDateString) {
                android.util.Log.d("EXORK_QUEST", "Suppressed Quest Popup: Already completed today ($todayDateString)")
                return@launch
            }
            
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
                
                if (!preferencesManager.hasAcceptedQualifications()) {
                    queueDialog(DialogType.WELCOME)
                } else {
                    queueDialog(DialogType.QUEST_INFO)
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
        val intent = Intent("com.exork.app.ACTION_DATA_UPDATED")
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
                // Mark all as completed locally
                quests.forEach { quest ->
                    repository.updateDailyQuest(quest.copy(isCompleted = true))
                }
                
                // Grant XP and sync to Firestore root fields immediately
                repository.recordProgress(xpGained = 50, isQuestCompletion = true)
                
                _uiEvent.emit(UiEvent.XpGained(50))
                notifyWidget()
                
                repository.recordJourneyEvent(
                    eventType = com.exork.app.model.JourneyEventType.ACHIEVEMENT,
                    title = "DAILY QUEST COMPLETED",
                    description = "Preparation complete. You have gained a small amount of power.",
                    icon = "⚡",
                    rarity = com.exork.app.model.JourneyRarity.RARE
                )
                
                // ATOMIC WORKMANAGER CANCELLATION ON CLAIM/FINISH
                try {
                    WorkManager.getInstance(context).cancelUniqueWork("DAILY_QUEST_REMINDER")
                    WorkManager.getInstance(context).cancelAllWorkByTag("DAILY_QUEST_REMINDER_TAG")
                    
                    // COMPLETELY REMOVE / CANCEL LEGACY ALARMMANAGER
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    val intent = Intent(context, com.exork.app.receiver.NotificationReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 
                        700, // Legacy Request Code
                        intent, 
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager?.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EXORK_QUEST", "Failed to cancel work or alarm", e)
                }

                // Final full sync to ensure backup_json captures completion
                triggerFullCloudSync()
            }
        }
    }

    fun triggerFullCloudSync() {
        viewModelScope.launch {
            val json = exportData()
            if (json.isNotEmpty()) {
                repository.pushBackupToFirestore(json)
            }
        }
    }

    fun triggerXpAnimation(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            _floatingXpReward.value = amount
            _uiEvent.emit(UiEvent.XpGained(amount))
        }
    }

    fun clearFloatingXp() {
        _floatingXpReward.value = null
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
                put("lastQuestCompletedDate", user.lastQuestCompletedDate)
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

            // Notes (REMOVED: Sub-collection is the sole source of truth)

            // Avatar Backup (REMOVED: Now handled by photoUrl root field in Firestore)

            json.toString(4)
        } catch (e: Exception) {
            _uiEvent.emit(UiEvent.BackupError("Export failed: ${e.message}"))
            ""
        }
    }

    private suspend fun performDataRestore(jsonString: String, isRemoteSync: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonString)
            
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
                totalExplosivePullups = userJson.optInt("totalExplosivePullups", 0),
                lastQuestCompletedDate = userJson.optString("lastQuestCompletedDate", "")
            )

            // 2. Abilities
            val abilities = mutableListOf<Ability>()
            val currentAbilities = repository.abilities.first()
            if (json.has("abilities")) {
                val abilitiesArray = json.optJSONArray("abilities") ?: JSONArray()
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
            } else {
                abilities.addAll(currentAbilities)
            }

            // 3. Titles
            val titles = mutableListOf<Title>()
            val currentTitles = TitleData.allTitles
            if (json.has("titles")) {
                val titlesArray = json.optJSONArray("titles") ?: JSONArray()
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
            } else {
                titles.addAll(currentTitles)
            }

            // 4. Workouts
            val workouts = mutableListOf<com.exork.app.model.WorkoutWithExercises>()
            if (json.has("workouts")) {
                val workoutsArray = json.optJSONArray("workouts") ?: JSONArray()
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
                            category = try { com.exork.app.model.ExerciseCategory.valueOf(exJson.optString("category", "OTHER")) } catch(e: Exception) { com.exork.app.model.ExerciseCategory.OTHER },
                            trackingType = try { com.exork.app.model.ExerciseTrackingType.valueOf(exJson.optString("trackingType", "REPS")) } catch(e: Exception) { com.exork.app.model.ExerciseTrackingType.REPS },
                            reps = if (exJson.isNull("reps")) null else exJson.optInt("reps"),
                            sets = exJson.optInt("sets", 0),
                            duration = if (exJson.isNull("duration")) null else exJson.optInt("duration"),
                            distanceKm = if (exJson.isNull("distanceKm")) null else exJson.optDouble("distanceKm")
                        ))
                    }
                    workouts.add(com.exork.app.model.WorkoutWithExercises(workoutEntity, exercises))
                }
            }

            // 5. Journey Events
            val events = mutableListOf<com.exork.app.model.JourneyEvent>()
            if (json.has("journey_events")) {
                val eventsArray = json.optJSONArray("journey_events") ?: JSONArray()
                for (i in 0 until eventsArray.length()) {
                    val evJson = eventsArray.optJSONObject(i) ?: continue
                    events.add(com.exork.app.model.JourneyEvent(
                        eventType = try { com.exork.app.model.JourneyEventType.valueOf(evJson.optString("eventType", "SYSTEM")) } catch(e: Exception) { com.exork.app.model.JourneyEventType.SYSTEM },
                        title = evJson.optString("title", "EVENT"),
                        description = evJson.optString("description", ""),
                        timestamp = evJson.optLong("timestamp", 0),
                        icon = evJson.optString("icon", "📍"),
                        rarity = try { com.exork.app.model.JourneyRarity.valueOf(evJson.optString("rarity", "COMMON")) } catch(e: Exception) { com.exork.app.model.JourneyRarity.COMMON },
                        xpReward = if (evJson.isNull("xpReward")) null else evJson.optInt("xpReward")
                    ))
                }
            }

            // 6. Training Days
            val trainingDays = mutableListOf<com.exork.app.model.TrainingDay>()
            if (json.has("training_days")) {
                val daysArray = json.optJSONArray("training_days") ?: JSONArray()
                for (i in 0 until daysArray.length()) {
                    val dJson = daysArray.optJSONObject(i) ?: continue
                    trainingDays.add(com.exork.app.model.TrainingDay(
                        dayOfWeek = dJson.optInt("dayOfWeek", 1),
                        isCompleted = dJson.optBoolean("isCompleted", false),
                        lastCompletedWeek = dJson.optInt("lastCompletedWeek", 0),
                        lastCompletedYear = dJson.optInt("lastCompletedYear", 0),
                        lastRewardWeek = dJson.optInt("lastRewardWeek", 0),
                        lastRewardYear = dJson.optInt("lastRewardYear", 0)
                    ))
                }
            }

            // 7. Planned Exercises
            val plannedExercises = mutableListOf<com.exork.app.model.PlannedExercise>()
            if (json.has("planned_exercises")) {
                val plannedArray = json.optJSONArray("planned_exercises") ?: JSONArray()
                for (i in 0 until plannedArray.length()) {
                    val peJson = plannedArray.optJSONObject(i) ?: continue
                    plannedExercises.add(com.exork.app.model.PlannedExercise(
                        dayOfWeek = peJson.optInt("dayOfWeek", 1),
                        name = peJson.optString("name", "EXERCISE"),
                        trackingType = try { com.exork.app.model.ExerciseTrackingType.valueOf(peJson.optString("trackingType", "REPS")) } catch(e: Exception) { com.exork.app.model.ExerciseTrackingType.REPS },
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

            // 8. Weekly Bonus
            var weeklyBonus: com.exork.app.model.WeeklyBonusEntity? = null
            if (json.has("weekly_bonus")) {
                val wbJson = json.optJSONObject("weekly_bonus")
                if (wbJson != null) {
                    weeklyBonus = com.exork.app.model.WeeklyBonusEntity(
                        lastBonusWeek = wbJson.optInt("lastBonusWeek", 0),
                        lastBonusYear = wbJson.optInt("lastBonusYear", 0)
                    )
                }
            }

            // 9. Daily Quests
            val dailyQuestsLocal = mutableListOf<com.exork.app.model.DailyQuest>()
            if (json.has("daily_quests")) {
                val questsArray = json.optJSONArray("daily_quests") ?: JSONArray()
                for (i in 0 until questsArray.length()) {
                    val qJson = questsArray.optJSONObject(i) ?: continue
                    val target = qJson.optInt("targetValue", 20)
                    val progress = qJson.optInt("currentProgress", 0)
                    dailyQuestsLocal.add(com.exork.app.model.DailyQuest(
                        id = qJson.optInt("id", i + 1),
                        title = qJson.optString("title", "QUEST"),
                        currentProgress = progress,
                        targetValue = target,
                        xpReward = qJson.optInt("xpReward", 50),
                        isCompleted = qJson.optBoolean("isCompleted", false)
                    ))
                }
            }

            // 10. Notes (REMOVED: Sub-collection is the sole source of truth)
            val notes = emptyList<com.exork.app.model.Note>()

            // 11. Avatar Restore (REMOVED: Now handled by photoUrl root field in Firestore)

            // Atomic Restore - WAIT for completion
            repository.restoreDatabase(
                user = importedUser,
                abilities = abilities,
                workouts = workouts,
                titles = titles,
                trainingDays = trainingDays,
                plannedExercises = plannedExercises,
                weeklyBonus = weeklyBonus,
                journeyEvents = events,
                dailyQuests = dailyQuestsLocal,
                notes = notes
            )

            if (!isRemoteSync) {
                repository.pushBackupToFirestore(jsonString)
            }

            withContext(Dispatchers.Main) {
                _uiEvent.emit(UiEvent.BackupSuccess(if (isRemoteSync) "Sync Complete" else "Backup Restored Successfully"))
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Restore failed", e)
            withContext(Dispatchers.Main) {
                _uiEvent.emit(UiEvent.BackupError("Restore failed: ${e.message}"))
            }
        }
    }
}
