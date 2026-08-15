package com.exork.app.data

import com.exork.app.model.*
import com.exork.app.model.HunterProfile
import com.exork.app.util.RankCalculator
import com.exork.app.util.XpCalculator
import androidx.room.withTransaction
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

            userDao.insertUser(user)
            abilityDao.insertAbilities(abilities)
            titleDao.insertTitles(titles)
            trainingPlanDao.insertTrainingDays(trainingDays)
            trainingPlanDao.insertPlannedExercises(plannedExercises)
            weeklyBonus?.let { trainingPlanDao.insertWeeklyBonus(it) }
            journeyEventDao.insertEvents(journeyEvents)
            dailyQuestDao.insertQuests(dailyQuests)
            noteDao.insertNotes(notes)

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

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
        syncNoteToFirestore(note)
    }

    suspend fun insertNoteLocal(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
        syncNoteToFirestore(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
        deleteNoteFromFirestore(note.id)
    }

    suspend fun deleteNoteLocal(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun insertDailyQuests(quests: List<DailyQuest>) {
        dailyQuestDao.insertQuests(quests)
        val currentUser = user.first()
        if (currentUser != null) {
            syncToFirestore(currentUser)
        }
    }

    suspend fun updateDailyQuest(quest: DailyQuest) {
        dailyQuestDao.updateQuest(quest)
        val currentUser = user.first()
        if (currentUser != null) {
            syncToFirestore(currentUser)
        }
    }

    suspend fun clearDailyQuests() {
        dailyQuestDao.deleteAllQuests()
        syncDailyQuestsToFirestore(emptyList())
    }

    suspend fun clearAllPlannedExercises() {
        trainingPlanDao.deleteAllPlannedExercises()
    }

    suspend fun insertPlannedExercises(exercises: List<PlannedExercise>) {
        trainingPlanDao.insertPlannedExercises(exercises)
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
            val exists = allJourneyEvents.first().any { it.eventType == eventType && it.title == title }
            if (exists) return
        }

        val event = JourneyEvent(
            eventType = eventType,
            title = title,
            description = description,
            icon = icon,
            rarity = rarity,
            xpReward = xpReward
        )
        journeyEventDao.insertEvent(event)
        syncJourneyEventToFirestore(event)
    }

    suspend fun getEventCountByType(eventType: JourneyEventType): Int {
        return journeyEventDao.getEventCountByType(eventType)
    }

    suspend fun insertTrainingDays(days: List<TrainingDay>) {
        trainingPlanDao.insertTrainingDays(days)
        syncTrainingPlanToFirestore(days, emptyList())
    }

    suspend fun updateTrainingDay(day: TrainingDay) {
        trainingPlanDao.updateTrainingDay(day)
        val all = trainingPlanDao.getTrainingPlan().first()
        syncTrainingPlanToFirestore(all, emptyList())
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
            val updatedUser = currentUser.copy(streak = 0)
            updateUser(updatedUser)
            syncToFirestore(updatedUser)
            return true
        }
        return false
    }

    suspend fun recordProgress(
        pushups: Int = 0,
        pullups: Int = 0,
        plankSeconds: Int = 0,
        distanceKm: Double = 0.0,
        xpGained: Int = 0,
        isWorkout: Boolean = false,
        isQuestCompletion: Boolean = false
    ) {
        val currentUser = user.first() ?: return
        
        var effectiveXpGained = xpGained
        var updatedCustomXpToday = currentUser.customXpEarnedToday
        
        if (isWorkout) {
            val maxCustomXp = 250
            val remainingCap = (maxCustomXp - currentUser.customXpEarnedToday).coerceAtLeast(0)
            effectiveXpGained = xpGained.coerceAtMost(remainingCap)
            updatedCustomXpToday += effectiveXpGained
        }

        var newXp = currentUser.xp + effectiveXpGained
        var newLevel = currentUser.level
        
        while (newXp >= XpCalculator.calculateRequiredXP(newLevel)) {
            newXp -= XpCalculator.calculateRequiredXP(newLevel)
            newLevel++
            
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

        val newStreak = if (xpGained >= 50 && !isWorkout) {
            currentUser.streak + 1
        } else {
            calculateNewStreak(currentUser.lastWorkoutDate, currentUser.streak)
        }
        
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val updatedUser = currentUser.copy(
            xp = newXp,
            level = newLevel,
            rank = newRank,
            streak = newStreak,
            pushups = currentUser.pushups + pushups,
            pullups = currentUser.pullups + pullups,
            plankTime = currentUser.plankTime + plankSeconds,
            totalDistanceKm = currentUser.totalDistanceKm + distanceKm,
            totalXpEarned = currentUser.totalXpEarned + effectiveXpGained,
            totalWorkouts = if (isWorkout) currentUser.totalWorkouts + 1 else currentUser.totalWorkouts,
            highestStreak = maxOf(currentUser.highestStreak, newStreak),
            maxPushupsSingleWorkout = maxOf(currentUser.maxPushupsSingleWorkout, pushups),
            maxPullupsSingleWorkout = maxOf(currentUser.maxPullupsSingleWorkout, pullups),
            maxPlankSingleWorkout = maxOf(currentUser.maxPlankSingleWorkout, plankSeconds),
            maxXpSingleWorkout = maxOf(currentUser.maxXpSingleWorkout, effectiveXpGained),
            totalPromotions = if (isRankPromotion) currentUser.totalPromotions + 1 else currentUser.totalPromotions,
            highestRank = RankCalculator.getHighestRank(currentUser.highestRank, newRank),
            lastWorkoutDate = System.currentTimeMillis(),
            customXpEarnedToday = updatedCustomXpToday,
            lastQuestCompletedDate = if (isQuestCompletion) todayDateString else currentUser.lastQuestCompletedDate
        )

        updateUser(updatedUser)
        checkAndUnlockAbilities(updatedUser)
        
        // Forced Direct XP Flush to Firestore Root Fields
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val db = FirebaseFirestore.getInstance()
            try {
                val updateMap = mutableMapOf<String, Any>(
                    "totalXp" to updatedUser.totalXpEarned,
                    "xp" to updatedUser.totalXpEarned,
                    "hunterLevel" to updatedUser.level,
                    "hunterRank" to updatedUser.rank,
                    "currentStreak" to updatedUser.streak,
                    "lastWorkoutDate" to updatedUser.lastWorkoutDate,
                    "lastQuestCompletedDate" to updatedUser.lastQuestCompletedDate
                )
                
                db.collection("users").document(firebaseUser.uid).update(updateMap).await()
                
                android.util.Log.d("EXORK_QUEST", "Quest saved. Total XP is now: ${updatedUser.totalXpEarned} on date: ${updatedUser.lastQuestCompletedDate}")
            } catch (e: Exception) {
                syncToFirestore(updatedUser) // Fallback
            }
        }
    }

    suspend fun checkUsernameAvailability(username: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        return try {
            val query = db.collection("users")
                .whereEqualTo("username", username.lowercase())
                .limit(1)
                .get()
                .await()
            query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUsernameInFirestore(userId: String, username: String) {
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("users").document(userId).update("username", username.lowercase(), "displayName", username).await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Username Update Failed", e)
        }
    }

    suspend fun initializeUserInFirestore() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        val currentUser = user.first() ?: User()
        
        val profile = mutableMapOf(
            "userId" to firebaseUser.uid,
            "displayName" to (firebaseUser.displayName ?: "Hunter"),
            "hunterRank" to currentUser.rank,
            "hunterLevel" to currentUser.level,
            "totalXp" to currentUser.totalXpEarned,
            "currentStreak" to currentUser.streak,
            "lastSync" to System.currentTimeMillis()
        )
        
        try {
            db.collection("users").document(firebaseUser.uid).set(profile, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Initialization failed", e)
        }
    }

    suspend fun syncToFirestore(user: User) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        // Fetch current quests to include in sync
        val quests = dailyQuestDao.getAllQuests().first()
        val questMap = quests.associate { it.id.toString() to mapOf(
            "id" to it.id,
            "title" to it.title,
            "currentProgress" to it.currentProgress,
            "targetValue" to it.targetValue,
            "xpReward" to it.xpReward,
            "isCompleted" to it.isCompleted
        )}

        val plannedExercises = trainingPlanDao.getAllPlannedExercises().first()
        val plannedMap = plannedExercises.associate { "${it.dayOfWeek}_${it.name}" to mapOf(
            "dayOfWeek" to it.dayOfWeek,
            "name" to it.name,
            "trackingType" to it.trackingType.name,
            "sets" to it.sets,
            "reps" to it.reps,
            "seconds" to it.seconds,
            "distanceKm" to it.distanceKm,
            "isCompleted" to it.isCompleted,
            "lastCompletedWeek" to it.lastCompletedWeek,
            "lastCompletedYear" to it.lastCompletedYear
        )}

        val profile = mutableMapOf(
            "userId" to firebaseUser.uid,
            "hunterRank" to user.rank,
            "hunterLevel" to user.level,
            "totalXp" to user.totalXpEarned,
            "xp" to user.totalXpEarned, 
            "currentStreak" to user.streak,
            "lastWorkoutDate" to user.lastWorkoutDate,
            "lastQuestRefreshDate" to user.lastQuestRefreshDate,
            "lastQuestCompletedDate" to user.lastQuestCompletedDate,
            "activeTitle" to user.activeTitle,
            "soundEnabled" to user.soundEnabled,
            "photoUrl" to user.photoUrl,
            "dailyQuests" to questMap,
            "plannedExercises" to plannedMap,
            "lastSync" to System.currentTimeMillis()
        )

        try {
            db.collection("users").document(firebaseUser.uid).set(profile, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Sync failed", e)
        }
    }

    suspend fun uploadAvatar(uriString: String, context: android.content.Context): String? = withContext(Dispatchers.IO) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@withContext null
        try {
            val bitmap = if (uriString.startsWith("content://")) {
                val uri = android.net.Uri.parse(uriString)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } else {
                android.graphics.BitmapFactory.decodeFile(uriString)
            } ?: return@withContext null

            // Scale down to max 200x200
            val maxDimension = 200
            val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            val width = Math.round(ratio * bitmap.width)
            val height = Math.round(ratio * bitmap.height)
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)

            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val base64Image = "data:image/jpeg;base64," + android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)

            // 1. Update Firestore root field
            FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
                .update("photoUrl", base64Image).await()

            // 2. Update Local Room DB
            val currentUser = user.first()
            if (currentUser != null) {
                updateUser(currentUser.copy(photoUrl = base64Image))
            }

            base64Image
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Avatar compression/upload failed", e)
            null
        }
    }

    suspend fun pushBackupToFirestore(jsonString: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            val data = mapOf(
                "backup_json" to jsonString,
                "lastBackupSync" to System.currentTimeMillis()
            )
            db.collection("users").document(firebaseUser.uid).set(data, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Backup Push Failed", e)
        }
    }

    suspend fun fetchBackupFromFirestore(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return null
        val db = FirebaseFirestore.getInstance()
        return try {
            val doc = db.collection("users").document(firebaseUser.uid).get().await()
            doc.getString("backup_json")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncNoteToFirestore(note: Note) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("users").document(firebaseUser.uid)
                .collection("notes").document(note.id)
                .set(note, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Note sync failed", e)
        }
    }

    suspend fun deleteNoteFromFirestore(noteId: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("users").document(firebaseUser.uid)
                .collection("notes").document(noteId)
                .delete()
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Note deletion failed", e)
        }
    }

    suspend fun fetchNotesFromFirestore() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("users").document(firebaseUser.uid)
                .collection("notes").get().await()
            val remoteNotes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Note::class.java)
            }
            if (remoteNotes.isNotEmpty()) {
                noteDao.insertNotes(remoteNotes)
            }
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Notes fetch failed", e)
        }
    }

    suspend fun syncDailyQuestsToFirestore(quests: List<DailyQuest>) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val questMap = quests.associate { it.id.toString() to mapOf(
            "id" to it.id,
            "title" to it.title,
            "currentProgress" to it.currentProgress,
            "targetValue" to it.targetValue,
            "xpReward" to it.xpReward,
            "isCompleted" to it.isCompleted
        )}
        try {
            db.collection("users").document(firebaseUser.uid)
                .update("dailyQuests", questMap).await()
        } catch (e: Exception) {
            db.collection("users").document(firebaseUser.uid)
                .set(mapOf("dailyQuests" to questMap), com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    suspend fun syncJourneyEventToFirestore(event: JourneyEvent) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("users").document(firebaseUser.uid)
                .collection("journeyEvents").document(event.timestamp.toString())
                .set(event, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {}
    }

    suspend fun syncTrainingPlanToFirestore(days: List<TrainingDay>, exercises: List<PlannedExercise>) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val data = mapOf(
            "trainingPlan" to days.associate { it.dayOfWeek.toString() to it },
            "plannedExercises" to exercises.associate { "${it.dayOfWeek}_${it.name}" to it }
        )
        try {
            db.collection("users").document(firebaseUser.uid).set(data, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {}
    }

    fun startRealTimeSync(): Flow<Unit> = callbackFlow {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: run {
            close()
            return@callbackFlow
        }
        val db = FirebaseFirestore.getInstance()
        
        val userListener = db.collection("users").document(firebaseUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val remoteUser = snapshot.toObject(User::class.java)
                if (remoteUser != null) {
                    database.runInTransaction {
                        // MVP: This listener just signals changes or merges root fields
                    }
                }
            }
        
        awaitClose { userListener.remove() }
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

    private fun calculateLevelFromXp(totalXp: Long): Int {
        var remainingXp = totalXp
        var level = 1
        while (true) {
            val required = XpCalculator.calculateRequiredXP(level).toLong()
            if (remainingXp >= required) {
                remainingXp -= required
                level++
            } else {
                break
            }
        }
        return level
    }

    private fun mapDocumentToHunterProfile(doc: com.google.firebase.firestore.DocumentSnapshot): HunterProfile {
        val rawXp = doc.getLong("totalXp") ?: doc.getLong("xp") ?: 0L
        val calculatedLevel = calculateLevelFromXp(rawXp)
        val username = doc.getString("username") ?: doc.getString("displayName") ?: "Hunter"
        val photoUrl = doc.getString("photoUrl") ?: doc.getString("profilePicture") ?: doc.getString("photoUri") ?: ""
        
        android.util.Log.d("AllyDebug", "User: $username, XP: $rawXp, Calculated Level: $calculatedLevel, Photo: $photoUrl")
        
        return HunterProfile(
            userId = doc.id,
            displayName = doc.getString("displayName") ?: username,
            username = username,
            hunterLevel = doc.getLong("hunterLevel")?.toInt() ?: calculatedLevel,
            totalXp = rawXp.toInt(),
            hunterRank = doc.getString("hunterRank") ?: RankCalculator.calculateRank(calculatedLevel),
            photoUrl = photoUrl,
            currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
            activeTitle = doc.getString("activeTitle"),
            maxPushupsSingleWorkout = doc.getLong("maxPushupsSingleWorkout")?.toInt() ?: 0,
            maxPullupsSingleWorkout = doc.getLong("maxPullupsSingleWorkout")?.toInt() ?: 0,
            maxPlankSingleWorkout = doc.getLong("maxPlankSingleWorkout")?.toInt() ?: 0,
            totalWorkouts = doc.getLong("totalWorkouts")?.toInt() ?: 0,
            totalPromotions = doc.getLong("totalPromotions")?.toInt() ?: 0
        )
    }

    suspend fun removeAlly(allyUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        try {
            db.runTransaction { transaction ->
                val currentUserRef = db.collection("users").document(currentUserId)
                val allyUserRef = db.collection("users").document(allyUserId)
                transaction.delete(currentUserRef.collection("allies").document(allyUserId))
                transaction.delete(allyUserRef.collection("allies").document(currentUserId))
            }.await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Remove Ally Failed", e)
        }
    }

    suspend fun sendManaToAlly(allyId: String, senderUsername: String): Boolean {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val db = FirebaseFirestore.getInstance()
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val manaDocRef = db.collection("users").document(allyId)
            .collection("incoming_mana").document(currentUserId)

        return try {
            val snapshot = manaDocRef.get().await()
            val lastSentDate = snapshot.getString("sentDate")
            if (lastSentDate == todayDate) {
                return false // Already sent today
            }

            val payload = mapOf(
                "fromUserId" to currentUserId,
                "fromUsername" to senderUsername,
                "amount" to 10,
                "sentDate" to todayDate,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            manaDocRef.set(payload).await()
            true
        } catch (e: Exception) {
            android.util.Log.e("MANA_SYNC", "Failed to send mana", e)
            false
        }
    }

    suspend fun claimIncomingMana(): Int {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return 0
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("users").document(currentUserId)
                .collection("incoming_mana").get().await()
            if (snapshot.isEmpty) return 0

            val totalManaGained = snapshot.documents.size * 10
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            if (totalManaGained > 0) {
                recordProgress(xpGained = totalManaGained)
            }
            totalManaGained
        } catch (e: Exception) {
            0
        }
    }

    suspend fun searchHunters(query: String): List<HunterProfile> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("users")
                .whereGreaterThanOrEqualTo("username", query.lowercase())
                .whereLessThanOrEqualTo("username", query.lowercase() + "\uf8ff")
                .limit(20)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { mapDocumentToHunterProfile(it) }
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Search Hunters Failed", e)
            emptyList()
        }
    }

    fun getSentRequestsFlow(): Flow<List<String>> = callbackFlow {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("users").document(currentUserId).collection("sent_requests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    trySend(snapshot.documents.map { it.id })
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendAllyRequest(targetUserId: String, fromUsername: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        if (currentUserId == targetUserId) {
            cleanupInvalidRequests(currentUserId)
            return
        }

        val db = FirebaseFirestore.getInstance()
        try {
            val request = mapOf(
                "fromUserId" to currentUserId,
                "fromUsername" to fromUsername,
                "toUserId" to targetUserId,
                "status" to "pending",
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            db.runTransaction { transaction ->
                val inboxRef = db.collection("users").document(targetUserId).collection("friend_requests").document(currentUserId)
                val outboxRef = db.collection("users").document(currentUserId).collection("sent_requests").document(targetUserId)
                
                transaction.set(inboxRef, request, com.google.firebase.firestore.SetOptions.merge())
                transaction.set(outboxRef, request, com.google.firebase.firestore.SetOptions.merge())
            }.await()
            
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Ally Request Failed", e)
        }
    }

    private suspend fun cleanupInvalidRequests(userId: String) {
        val db = FirebaseFirestore.getInstance()
        try {
            db.collection("users").document(userId).collection("friend_requests").document(userId).delete()
            db.collection("users").document(userId).collection("sent_requests").document(userId).delete()
        } catch (e: Exception) { /* ignore */ }
    }

    fun getIncomingRequestsFlow(): Flow<List<HunterProfile>> = callbackFlow {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val db = FirebaseFirestore.getInstance()
        var profileListener: com.google.firebase.firestore.ListenerRegistration? = null

        val requestsListener = db.collection("users").document(currentUserId).collection("friend_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("REQUESTS_TAB_BUG", "Error listening to requests", e)
                    return@addSnapshotListener
                }
                
                val senderIds = snapshot?.documents?.map { it.id } ?: emptyList()
                profileListener?.remove()

                if (senderIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    profileListener = db.collection("users")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), senderIds)
                        .addSnapshotListener { profiles, pe ->
                            if (pe != null) return@addSnapshotListener
                            val list = profiles?.documents?.mapNotNull { mapDocumentToHunterProfile(it) } ?: emptyList()
                            trySend(list)
                        }
                }
            }

        awaitClose {
            requestsListener.remove()
            profileListener?.remove()
        }
    }

    suspend fun acceptAllyRequest(senderUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        try {
            db.runTransaction { transaction ->
                val currentUserRef = db.collection("users").document(currentUserId)
                val senderUserRef = db.collection("users").document(senderUserId)

                transaction.set(currentUserRef.collection("allies").document(senderUserId), mapOf("userId" to senderUserId))
                transaction.set(senderUserRef.collection("allies").document(currentUserId), mapOf("userId" to currentUserId))
                
                transaction.delete(currentUserRef.collection("friend_requests").document(senderUserId))
                transaction.delete(senderUserRef.collection("sent_requests").document(currentUserId))
            }.await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Accept Request Failed", e)
        }
    }

    suspend fun declineAllyRequest(senderUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        try {
            db.runTransaction { transaction ->
                val currentUserRef = db.collection("users").document(currentUserId)
                val senderUserRef = db.collection("users").document(senderUserId)

                transaction.delete(currentUserRef.collection("friend_requests").document(senderUserId))
                transaction.delete(senderUserRef.collection("sent_requests").document(currentUserId))
            }.await()
        } catch (e: Exception) {
            android.util.Log.e("FitnessRepository", "Decline Request Failed", e)
        }
    }

    fun getAlliesFlow(): Flow<List<HunterProfile>> = callbackFlow {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val db = FirebaseFirestore.getInstance()
        var profileListener: com.google.firebase.firestore.ListenerRegistration? = null

        val friendsListener = db.collection("users").document(currentUserId).collection("allies")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("ALLIES_TAB_BUG", "Error listening to allies", e)
                    return@addSnapshotListener
                }
                
                val allyIds = snapshot?.documents?.map { it.id } ?: emptyList()
                profileListener?.remove()

                if (allyIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    profileListener = db.collection("users")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), allyIds)
                        .addSnapshotListener { profiles, pe ->
                            if (pe != null) {
                                android.util.Log.e("ALLIES_TAB_BUG", "Error fetching ally profiles", pe)
                                return@addSnapshotListener
                            }
                            
                            val list = profiles?.documents?.mapNotNull { mapDocumentToHunterProfile(it) } ?: emptyList()
                            trySend(list)
                        }
                }
            }

        awaitClose {
            friendsListener.remove()
            profileListener?.remove()
        }
    }
}
