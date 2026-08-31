package com.exork.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exork.app.model.*

@Database(
    entities = [
        User::class, WorkoutEntity::class, ExerciseEntity::class, Ability::class, Title::class,
        TrainingDay::class, WeeklyBonusEntity::class, PlannedExercise::class, JourneyEvent::class,
        DailyQuest::class, Note::class
    ],
    version = 28,
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

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `customXpEarnedToday` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `lastQuestCompletedDate` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `note_table`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `note_table` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `photoUrl` TEXT")
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `guildId` TEXT")
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `guildName` TEXT")
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `guildTag` TEXT")
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `workout_table` ADD COLUMN `remoteId` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `deletionRequested` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `deletionRequestedAt` INTEGER")
                db.execSQL("ALTER TABLE `user_table` ADD COLUMN `scheduledDeletionAt` INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_database"
                )
                    .addMigrations(MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
