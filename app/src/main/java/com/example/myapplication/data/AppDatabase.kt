package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.*

@Database(entities = [User::class, WorkoutEntity::class, ExerciseEntity::class, Ability::class, Title::class, TrainingDay::class, WeeklyBonusEntity::class, PlannedExercise::class, JourneyEvent::class, DailyQuest::class], version = 18, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun abilityDao(): AbilityDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun titleDao(): TitleDao
    abstract fun trainingPlanDao(): TrainingPlanDao
    abstract fun journeyEventDao(): JourneyEventDao
    abstract fun dailyQuestDao(): DailyQuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
