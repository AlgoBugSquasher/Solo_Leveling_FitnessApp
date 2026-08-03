package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.model.*

@Database(
    entities = [
        User::class, WorkoutEntity::class, ExerciseEntity::class, Ability::class, Title::class,
        TrainingDay::class, WeeklyBonusEntity::class, PlannedExercise::class, JourneyEvent::class,
        DailyQuest::class, Note::class
    ],
    version = 20,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun abilityDao(): AbilityDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun titleDao(): TitleDao
    abstract fun trainingPlanDao(): TrainingPlanDao
    abstract fun journeyEventDao(): JourneyEventDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `note_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to journey_event_table
                db.execSQL("ALTER TABLE `journey_event_table` ADD COLUMN `eventType` TEXT NOT NULL DEFAULT 'SYSTEM'")
                db.execSQL("ALTER TABLE `journey_event_table` ADD COLUMN `rarity` TEXT NOT NULL DEFAULT 'COMMON'")
                db.execSQL("ALTER TABLE `journey_event_table` ADD COLUMN `xpReward` INTEGER")
                
                // Map old 'type' data to 'eventType' if possible, or just keep default
                db.execSQL("UPDATE `journey_event_table` SET `eventType` = `type` WHERE `type` IN ('LEVEL_UP', 'RANK_PROMOTION', 'TITLE_UNLOCKED', 'ACHIEVEMENT_UNLOCKED', 'BADGE_UNLOCKED', 'FIRST_QUEST', 'FIRST_WORKOUT', 'STREAK_MILESTONE', 'PERSONAL_RECORD')")
                
                // Normalize some old types to new enum names
                db.execSQL("UPDATE `journey_event_table` SET `eventType` = 'RANK_UP' WHERE `eventType` = 'RANK_PROMOTION'")
                db.execSQL("UPDATE `journey_event_table` SET `eventType` = 'ACHIEVEMENT' WHERE `eventType` = 'ACHIEVEMENT_UNLOCKED'")
                db.execSQL("UPDATE `journey_event_table` SET `eventType` = 'BADGE' WHERE `eventType` = 'BADGE_UNLOCKED'")
                db.execSQL("UPDATE `journey_event_table` SET `eventType` = 'TRAINING' WHERE `eventType` = 'FIRST_QUEST'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .addMigrations(MIGRATION_18_19, MIGRATION_19_20)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
