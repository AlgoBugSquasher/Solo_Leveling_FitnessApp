package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.model.Ability
import com.example.myapplication.model.Title
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.User
import com.example.myapplication.model.WorkoutEntity

@Database(entities = [User::class, WorkoutEntity::class, ExerciseEntity::class, Ability::class, Title::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun abilityDao(): AbilityDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun titleDao(): TitleDao

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
