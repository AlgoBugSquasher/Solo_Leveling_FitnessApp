package com.example.myapplication.data

import com.example.myapplication.model.Ability
import com.example.myapplication.model.Title
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.User
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FitnessRepository(
    private val userDao: UserDao,
    private val abilityDao: AbilityDao,
    private val workoutDao: WorkoutDao,
    private val titleDao: TitleDao
) {
    val user: Flow<User?> = userDao.getUser()
    val abilities: Flow<List<Ability>> = abilityDao.getAllAbilities()
    val allWorkouts: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkouts()
    val allTitles: Flow<List<Title>> = titleDao.getAllTitles()

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
