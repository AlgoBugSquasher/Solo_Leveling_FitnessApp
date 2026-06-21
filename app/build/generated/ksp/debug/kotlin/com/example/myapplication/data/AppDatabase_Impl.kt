package com.example.myapplication.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _userDao: Lazy<UserDao> = lazy {
    UserDao_Impl(this)
  }

  private val _abilityDao: Lazy<AbilityDao> = lazy {
    AbilityDao_Impl(this)
  }

  private val _workoutDao: Lazy<WorkoutDao> = lazy {
    WorkoutDao_Impl(this)
  }

  private val _titleDao: Lazy<TitleDao> = lazy {
    TitleDao_Impl(this)
  }

  private val _trainingPlanDao: Lazy<TrainingPlanDao> = lazy {
    TrainingPlanDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(13, "29516bb96180e73bd6b449cdf98a417d", "92f7dbada46e457a2022552a1c199c06") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_table` (`id` INTEGER NOT NULL, `xp` INTEGER NOT NULL, `level` INTEGER NOT NULL, `streak` INTEGER NOT NULL, `rank` TEXT NOT NULL, `pushups` INTEGER NOT NULL, `pullups` INTEGER NOT NULL, `plankTime` INTEGER NOT NULL, `totalPikePushups` INTEGER NOT NULL, `totalPseudoPlanchePushups` INTEGER NOT NULL, `totalHangingSeconds` INTEGER NOT NULL, `totalExplosivePullups` INTEGER NOT NULL, `totalXpEarned` INTEGER NOT NULL, `totalWorkouts` INTEGER NOT NULL, `highestStreak` INTEGER NOT NULL, `totalPromotions` INTEGER NOT NULL, `highestRank` TEXT NOT NULL, `lastWorkoutDate` INTEGER NOT NULL, `activeTitle` TEXT, `soundEnabled` INTEGER NOT NULL, `maxPushupsSingleWorkout` INTEGER NOT NULL, `maxPullupsSingleWorkout` INTEGER NOT NULL, `maxPlankSingleWorkout` INTEGER NOT NULL, `maxXpSingleWorkout` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `totalXpGained` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutId` INTEGER NOT NULL, `name` TEXT NOT NULL, `reps` INTEGER, `sets` INTEGER NOT NULL, `duration` INTEGER, FOREIGN KEY(`workoutId`) REFERENCES `workout_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `ability_table` (`name` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, `requiredPushups` INTEGER NOT NULL, `requiredPullups` INTEGER NOT NULL, `requiredPlankTime` INTEGER NOT NULL, `requiredLevel` INTEGER NOT NULL, `requiredStreak` INTEGER NOT NULL, PRIMARY KEY(`name`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `title_table` (`name` TEXT NOT NULL, `requiredStreak` INTEGER NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`name`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `training_plan_table` (`dayOfWeek` INTEGER NOT NULL, `pushups` INTEGER NOT NULL, `pullups` INTEGER NOT NULL, `plankSeconds` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `lastCompletedWeek` INTEGER NOT NULL, `lastCompletedYear` INTEGER NOT NULL, PRIMARY KEY(`dayOfWeek`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `weekly_bonus_table` (`id` INTEGER NOT NULL, `lastBonusWeek` INTEGER NOT NULL, `lastBonusYear` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '29516bb96180e73bd6b449cdf98a417d')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `user_table`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_table`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise_table`")
        connection.execSQL("DROP TABLE IF EXISTS `ability_table`")
        connection.execSQL("DROP TABLE IF EXISTS `title_table`")
        connection.execSQL("DROP TABLE IF EXISTS `training_plan_table`")
        connection.execSQL("DROP TABLE IF EXISTS `weekly_bonus_table`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsUserTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserTable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("xp", TableInfo.Column("xp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("level", TableInfo.Column("level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("streak", TableInfo.Column("streak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("rank", TableInfo.Column("rank", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("pushups", TableInfo.Column("pushups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("pullups", TableInfo.Column("pullups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("plankTime", TableInfo.Column("plankTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalPikePushups", TableInfo.Column("totalPikePushups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalPseudoPlanchePushups", TableInfo.Column("totalPseudoPlanchePushups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalHangingSeconds", TableInfo.Column("totalHangingSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalExplosivePullups", TableInfo.Column("totalExplosivePullups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalXpEarned", TableInfo.Column("totalXpEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalWorkouts", TableInfo.Column("totalWorkouts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("highestStreak", TableInfo.Column("highestStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("totalPromotions", TableInfo.Column("totalPromotions", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("highestRank", TableInfo.Column("highestRank", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("lastWorkoutDate", TableInfo.Column("lastWorkoutDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("activeTitle", TableInfo.Column("activeTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("soundEnabled", TableInfo.Column("soundEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("maxPushupsSingleWorkout", TableInfo.Column("maxPushupsSingleWorkout", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("maxPullupsSingleWorkout", TableInfo.Column("maxPullupsSingleWorkout", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("maxPlankSingleWorkout", TableInfo.Column("maxPlankSingleWorkout", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserTable.put("maxXpSingleWorkout", TableInfo.Column("maxXpSingleWorkout", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserTable: TableInfo = TableInfo("user_table", _columnsUserTable, _foreignKeysUserTable, _indicesUserTable)
        val _existingUserTable: TableInfo = read(connection, "user_table")
        if (!_infoUserTable.equals(_existingUserTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_table(com.example.myapplication.model.User).
              | Expected:
              |""".trimMargin() + _infoUserTable + """
              |
              | Found:
              |""".trimMargin() + _existingUserTable)
        }
        val _columnsWorkoutTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutTable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutTable.put("date", TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutTable.put("totalXpGained", TableInfo.Column("totalXpGained", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWorkoutTable: TableInfo = TableInfo("workout_table", _columnsWorkoutTable, _foreignKeysWorkoutTable, _indicesWorkoutTable)
        val _existingWorkoutTable: TableInfo = read(connection, "workout_table")
        if (!_infoWorkoutTable.equals(_existingWorkoutTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_table(com.example.myapplication.model.WorkoutEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutTable + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutTable)
        }
        val _columnsExerciseTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExerciseTable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseTable.put("workoutId", TableInfo.Column("workoutId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseTable.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseTable.put("reps", TableInfo.Column("reps", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseTable.put("sets", TableInfo.Column("sets", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExerciseTable.put("duration", TableInfo.Column("duration", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExerciseTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysExerciseTable.add(TableInfo.ForeignKey("workout_table", "CASCADE", "NO ACTION", listOf("workoutId"), listOf("id")))
        val _indicesExerciseTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExerciseTable: TableInfo = TableInfo("exercise_table", _columnsExerciseTable, _foreignKeysExerciseTable, _indicesExerciseTable)
        val _existingExerciseTable: TableInfo = read(connection, "exercise_table")
        if (!_infoExerciseTable.equals(_existingExerciseTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise_table(com.example.myapplication.model.ExerciseEntity).
              | Expected:
              |""".trimMargin() + _infoExerciseTable + """
              |
              | Found:
              |""".trimMargin() + _existingExerciseTable)
        }
        val _columnsAbilityTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAbilityTable.put("name", TableInfo.Column("name", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("isUnlocked", TableInfo.Column("isUnlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("requiredPushups", TableInfo.Column("requiredPushups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("requiredPullups", TableInfo.Column("requiredPullups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("requiredPlankTime", TableInfo.Column("requiredPlankTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("requiredLevel", TableInfo.Column("requiredLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAbilityTable.put("requiredStreak", TableInfo.Column("requiredStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAbilityTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAbilityTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAbilityTable: TableInfo = TableInfo("ability_table", _columnsAbilityTable, _foreignKeysAbilityTable, _indicesAbilityTable)
        val _existingAbilityTable: TableInfo = read(connection, "ability_table")
        if (!_infoAbilityTable.equals(_existingAbilityTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |ability_table(com.example.myapplication.model.Ability).
              | Expected:
              |""".trimMargin() + _infoAbilityTable + """
              |
              | Found:
              |""".trimMargin() + _existingAbilityTable)
        }
        val _columnsTitleTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTitleTable.put("name", TableInfo.Column("name", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTitleTable.put("requiredStreak", TableInfo.Column("requiredStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTitleTable.put("isUnlocked", TableInfo.Column("isUnlocked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTitleTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTitleTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTitleTable: TableInfo = TableInfo("title_table", _columnsTitleTable, _foreignKeysTitleTable, _indicesTitleTable)
        val _existingTitleTable: TableInfo = read(connection, "title_table")
        if (!_infoTitleTable.equals(_existingTitleTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |title_table(com.example.myapplication.model.Title).
              | Expected:
              |""".trimMargin() + _infoTitleTable + """
              |
              | Found:
              |""".trimMargin() + _existingTitleTable)
        }
        val _columnsTrainingPlanTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTrainingPlanTable.put("dayOfWeek", TableInfo.Column("dayOfWeek", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("pushups", TableInfo.Column("pushups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("pullups", TableInfo.Column("pullups", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("plankSeconds", TableInfo.Column("plankSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("lastCompletedWeek", TableInfo.Column("lastCompletedWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTrainingPlanTable.put("lastCompletedYear", TableInfo.Column("lastCompletedYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTrainingPlanTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTrainingPlanTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTrainingPlanTable: TableInfo = TableInfo("training_plan_table", _columnsTrainingPlanTable, _foreignKeysTrainingPlanTable, _indicesTrainingPlanTable)
        val _existingTrainingPlanTable: TableInfo = read(connection, "training_plan_table")
        if (!_infoTrainingPlanTable.equals(_existingTrainingPlanTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |training_plan_table(com.example.myapplication.model.TrainingDay).
              | Expected:
              |""".trimMargin() + _infoTrainingPlanTable + """
              |
              | Found:
              |""".trimMargin() + _existingTrainingPlanTable)
        }
        val _columnsWeeklyBonusTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWeeklyBonusTable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeeklyBonusTable.put("lastBonusWeek", TableInfo.Column("lastBonusWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWeeklyBonusTable.put("lastBonusYear", TableInfo.Column("lastBonusYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWeeklyBonusTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWeeklyBonusTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWeeklyBonusTable: TableInfo = TableInfo("weekly_bonus_table", _columnsWeeklyBonusTable, _foreignKeysWeeklyBonusTable, _indicesWeeklyBonusTable)
        val _existingWeeklyBonusTable: TableInfo = read(connection, "weekly_bonus_table")
        if (!_infoWeeklyBonusTable.equals(_existingWeeklyBonusTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |weekly_bonus_table(com.example.myapplication.model.WeeklyBonusEntity).
              | Expected:
              |""".trimMargin() + _infoWeeklyBonusTable + """
              |
              | Found:
              |""".trimMargin() + _existingWeeklyBonusTable)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "user_table", "workout_table", "exercise_table", "ability_table", "title_table", "training_plan_table", "weekly_bonus_table")
  }

  public override fun clearAllTables() {
    super.performClear(true, "user_table", "workout_table", "exercise_table", "ability_table", "title_table", "training_plan_table", "weekly_bonus_table")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(UserDao::class, UserDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AbilityDao::class, AbilityDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WorkoutDao::class, WorkoutDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TitleDao::class, TitleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TrainingPlanDao::class, TrainingPlanDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun userDao(): UserDao = _userDao.value

  public override fun abilityDao(): AbilityDao = _abilityDao.value

  public override fun workoutDao(): WorkoutDao = _workoutDao.value

  public override fun titleDao(): TitleDao = _titleDao.value

  public override fun trainingPlanDao(): TrainingPlanDao = _trainingPlanDao.value
}
