package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.ExerciseTrackingType
import com.example.myapplication.model.PlannedExercise
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.model.WeeklyBonusEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TrainingPlanDao_Impl(
  __db: RoomDatabase,
) : TrainingPlanDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTrainingDay: EntityInsertAdapter<TrainingDay>

  private val __insertAdapterOfPlannedExercise: EntityInsertAdapter<PlannedExercise>

  private val __insertAdapterOfWeeklyBonusEntity: EntityInsertAdapter<WeeklyBonusEntity>

  private val __deleteAdapterOfPlannedExercise: EntityDeleteOrUpdateAdapter<PlannedExercise>

  private val __updateAdapterOfTrainingDay: EntityDeleteOrUpdateAdapter<TrainingDay>

  private val __updateAdapterOfPlannedExercise: EntityDeleteOrUpdateAdapter<PlannedExercise>
  init {
    this.__db = __db
    this.__insertAdapterOfTrainingDay = object : EntityInsertAdapter<TrainingDay>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `training_plan_table` (`dayOfWeek`,`isCompleted`,`lastCompletedWeek`,`lastCompletedYear`,`lastRewardWeek`,`lastRewardYear`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TrainingDay) {
        statement.bindLong(1, entity.dayOfWeek.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindLong(3, entity.lastCompletedWeek.toLong())
        statement.bindLong(4, entity.lastCompletedYear.toLong())
        statement.bindLong(5, entity.lastRewardWeek.toLong())
        statement.bindLong(6, entity.lastRewardYear.toLong())
      }
    }
    this.__insertAdapterOfPlannedExercise = object : EntityInsertAdapter<PlannedExercise>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `planned_exercise_table` (`id`,`dayOfWeek`,`name`,`trackingType`,`sets`,`reps`,`seconds`,`distanceKm`,`isCompleted`,`lastCompletedWeek`,`lastCompletedYear`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlannedExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dayOfWeek.toLong())
        statement.bindText(3, entity.name)
        statement.bindText(4, __ExerciseTrackingType_enumToString(entity.trackingType))
        val _tmpSets: Int? = entity.sets
        if (_tmpSets == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpSets.toLong())
        }
        val _tmpReps: Int? = entity.reps
        if (_tmpReps == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpReps.toLong())
        }
        val _tmpSeconds: Int? = entity.seconds
        if (_tmpSeconds == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSeconds.toLong())
        }
        val _tmpDistanceKm: Double? = entity.distanceKm
        if (_tmpDistanceKm == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpDistanceKm)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.lastCompletedWeek.toLong())
        statement.bindLong(11, entity.lastCompletedYear.toLong())
      }
    }
    this.__insertAdapterOfWeeklyBonusEntity = object : EntityInsertAdapter<WeeklyBonusEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `weekly_bonus_table` (`id`,`lastBonusWeek`,`lastBonusYear`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WeeklyBonusEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.lastBonusWeek.toLong())
        statement.bindLong(3, entity.lastBonusYear.toLong())
      }
    }
    this.__deleteAdapterOfPlannedExercise = object : EntityDeleteOrUpdateAdapter<PlannedExercise>() {
      protected override fun createQuery(): String = "DELETE FROM `planned_exercise_table` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlannedExercise) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfTrainingDay = object : EntityDeleteOrUpdateAdapter<TrainingDay>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `training_plan_table` SET `dayOfWeek` = ?,`isCompleted` = ?,`lastCompletedWeek` = ?,`lastCompletedYear` = ?,`lastRewardWeek` = ?,`lastRewardYear` = ? WHERE `dayOfWeek` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TrainingDay) {
        statement.bindLong(1, entity.dayOfWeek.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindLong(3, entity.lastCompletedWeek.toLong())
        statement.bindLong(4, entity.lastCompletedYear.toLong())
        statement.bindLong(5, entity.lastRewardWeek.toLong())
        statement.bindLong(6, entity.lastRewardYear.toLong())
        statement.bindLong(7, entity.dayOfWeek.toLong())
      }
    }
    this.__updateAdapterOfPlannedExercise = object : EntityDeleteOrUpdateAdapter<PlannedExercise>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `planned_exercise_table` SET `id` = ?,`dayOfWeek` = ?,`name` = ?,`trackingType` = ?,`sets` = ?,`reps` = ?,`seconds` = ?,`distanceKm` = ?,`isCompleted` = ?,`lastCompletedWeek` = ?,`lastCompletedYear` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlannedExercise) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dayOfWeek.toLong())
        statement.bindText(3, entity.name)
        statement.bindText(4, __ExerciseTrackingType_enumToString(entity.trackingType))
        val _tmpSets: Int? = entity.sets
        if (_tmpSets == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpSets.toLong())
        }
        val _tmpReps: Int? = entity.reps
        if (_tmpReps == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpReps.toLong())
        }
        val _tmpSeconds: Int? = entity.seconds
        if (_tmpSeconds == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpSeconds.toLong())
        }
        val _tmpDistanceKm: Double? = entity.distanceKm
        if (_tmpDistanceKm == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpDistanceKm)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.lastCompletedWeek.toLong())
        statement.bindLong(11, entity.lastCompletedYear.toLong())
        statement.bindLong(12, entity.id)
      }
    }
  }

  public override suspend fun insertTrainingDays(days: List<TrainingDay>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTrainingDay.insert(_connection, days)
  }

  public override suspend fun insertPlannedExercise(exercise: PlannedExercise): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlannedExercise.insert(_connection, exercise)
  }

  public override suspend fun insertWeeklyBonus(bonus: WeeklyBonusEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWeeklyBonusEntity.insert(_connection, bonus)
  }

  public override suspend fun deletePlannedExercise(exercise: PlannedExercise): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPlannedExercise.handle(_connection, exercise)
  }

  public override suspend fun updateTrainingDay(day: TrainingDay): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTrainingDay.handle(_connection, day)
  }

  public override suspend fun updatePlannedExercise(exercise: PlannedExercise): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlannedExercise.handle(_connection, exercise)
  }

  public override fun getTrainingPlan(): Flow<List<TrainingDay>> {
    val _sql: String = "SELECT * FROM training_plan_table ORDER BY dayOfWeek ASC"
    return createFlow(__db, false, arrayOf("training_plan_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDayOfWeek: Int = getColumnIndexOrThrow(_stmt, "dayOfWeek")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfLastCompletedWeek: Int = getColumnIndexOrThrow(_stmt, "lastCompletedWeek")
        val _columnIndexOfLastCompletedYear: Int = getColumnIndexOrThrow(_stmt, "lastCompletedYear")
        val _columnIndexOfLastRewardWeek: Int = getColumnIndexOrThrow(_stmt, "lastRewardWeek")
        val _columnIndexOfLastRewardYear: Int = getColumnIndexOrThrow(_stmt, "lastRewardYear")
        val _result: MutableList<TrainingDay> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrainingDay
          val _tmpDayOfWeek: Int
          _tmpDayOfWeek = _stmt.getLong(_columnIndexOfDayOfWeek).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpLastCompletedWeek: Int
          _tmpLastCompletedWeek = _stmt.getLong(_columnIndexOfLastCompletedWeek).toInt()
          val _tmpLastCompletedYear: Int
          _tmpLastCompletedYear = _stmt.getLong(_columnIndexOfLastCompletedYear).toInt()
          val _tmpLastRewardWeek: Int
          _tmpLastRewardWeek = _stmt.getLong(_columnIndexOfLastRewardWeek).toInt()
          val _tmpLastRewardYear: Int
          _tmpLastRewardYear = _stmt.getLong(_columnIndexOfLastRewardYear).toInt()
          _item = TrainingDay(_tmpDayOfWeek,_tmpIsCompleted,_tmpLastCompletedWeek,_tmpLastCompletedYear,_tmpLastRewardWeek,_tmpLastRewardYear)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllPlannedExercises(): Flow<List<PlannedExercise>> {
    val _sql: String = "SELECT * FROM planned_exercise_table ORDER BY dayOfWeek ASC, id ASC"
    return createFlow(__db, false, arrayOf("planned_exercise_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDayOfWeek: Int = getColumnIndexOrThrow(_stmt, "dayOfWeek")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTrackingType: Int = getColumnIndexOrThrow(_stmt, "trackingType")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfSeconds: Int = getColumnIndexOrThrow(_stmt, "seconds")
        val _columnIndexOfDistanceKm: Int = getColumnIndexOrThrow(_stmt, "distanceKm")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfLastCompletedWeek: Int = getColumnIndexOrThrow(_stmt, "lastCompletedWeek")
        val _columnIndexOfLastCompletedYear: Int = getColumnIndexOrThrow(_stmt, "lastCompletedYear")
        val _result: MutableList<PlannedExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlannedExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDayOfWeek: Int
          _tmpDayOfWeek = _stmt.getLong(_columnIndexOfDayOfWeek).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTrackingType: ExerciseTrackingType
          _tmpTrackingType = __ExerciseTrackingType_stringToEnum(_stmt.getText(_columnIndexOfTrackingType))
          val _tmpSets: Int?
          if (_stmt.isNull(_columnIndexOfSets)) {
            _tmpSets = null
          } else {
            _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          }
          val _tmpReps: Int?
          if (_stmt.isNull(_columnIndexOfReps)) {
            _tmpReps = null
          } else {
            _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          }
          val _tmpSeconds: Int?
          if (_stmt.isNull(_columnIndexOfSeconds)) {
            _tmpSeconds = null
          } else {
            _tmpSeconds = _stmt.getLong(_columnIndexOfSeconds).toInt()
          }
          val _tmpDistanceKm: Double?
          if (_stmt.isNull(_columnIndexOfDistanceKm)) {
            _tmpDistanceKm = null
          } else {
            _tmpDistanceKm = _stmt.getDouble(_columnIndexOfDistanceKm)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpLastCompletedWeek: Int
          _tmpLastCompletedWeek = _stmt.getLong(_columnIndexOfLastCompletedWeek).toInt()
          val _tmpLastCompletedYear: Int
          _tmpLastCompletedYear = _stmt.getLong(_columnIndexOfLastCompletedYear).toInt()
          _item = PlannedExercise(_tmpId,_tmpDayOfWeek,_tmpName,_tmpTrackingType,_tmpSets,_tmpReps,_tmpSeconds,_tmpDistanceKm,_tmpIsCompleted,_tmpLastCompletedWeek,_tmpLastCompletedYear)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlannedExercisesForDay(dayOfWeek: Int): Flow<List<PlannedExercise>> {
    val _sql: String = "SELECT * FROM planned_exercise_table WHERE dayOfWeek = ? ORDER BY id ASC"
    return createFlow(__db, false, arrayOf("planned_exercise_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, dayOfWeek.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDayOfWeek: Int = getColumnIndexOrThrow(_stmt, "dayOfWeek")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTrackingType: Int = getColumnIndexOrThrow(_stmt, "trackingType")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfSeconds: Int = getColumnIndexOrThrow(_stmt, "seconds")
        val _columnIndexOfDistanceKm: Int = getColumnIndexOrThrow(_stmt, "distanceKm")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfLastCompletedWeek: Int = getColumnIndexOrThrow(_stmt, "lastCompletedWeek")
        val _columnIndexOfLastCompletedYear: Int = getColumnIndexOrThrow(_stmt, "lastCompletedYear")
        val _result: MutableList<PlannedExercise> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlannedExercise
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDayOfWeek: Int
          _tmpDayOfWeek = _stmt.getLong(_columnIndexOfDayOfWeek).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTrackingType: ExerciseTrackingType
          _tmpTrackingType = __ExerciseTrackingType_stringToEnum(_stmt.getText(_columnIndexOfTrackingType))
          val _tmpSets: Int?
          if (_stmt.isNull(_columnIndexOfSets)) {
            _tmpSets = null
          } else {
            _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          }
          val _tmpReps: Int?
          if (_stmt.isNull(_columnIndexOfReps)) {
            _tmpReps = null
          } else {
            _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          }
          val _tmpSeconds: Int?
          if (_stmt.isNull(_columnIndexOfSeconds)) {
            _tmpSeconds = null
          } else {
            _tmpSeconds = _stmt.getLong(_columnIndexOfSeconds).toInt()
          }
          val _tmpDistanceKm: Double?
          if (_stmt.isNull(_columnIndexOfDistanceKm)) {
            _tmpDistanceKm = null
          } else {
            _tmpDistanceKm = _stmt.getDouble(_columnIndexOfDistanceKm)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpLastCompletedWeek: Int
          _tmpLastCompletedWeek = _stmt.getLong(_columnIndexOfLastCompletedWeek).toInt()
          val _tmpLastCompletedYear: Int
          _tmpLastCompletedYear = _stmt.getLong(_columnIndexOfLastCompletedYear).toInt()
          _item = PlannedExercise(_tmpId,_tmpDayOfWeek,_tmpName,_tmpTrackingType,_tmpSets,_tmpReps,_tmpSeconds,_tmpDistanceKm,_tmpIsCompleted,_tmpLastCompletedWeek,_tmpLastCompletedYear)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getWeeklyBonus(): Flow<WeeklyBonusEntity?> {
    val _sql: String = "SELECT * FROM weekly_bonus_table WHERE id = 0"
    return createFlow(__db, false, arrayOf("weekly_bonus_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfLastBonusWeek: Int = getColumnIndexOrThrow(_stmt, "lastBonusWeek")
        val _columnIndexOfLastBonusYear: Int = getColumnIndexOrThrow(_stmt, "lastBonusYear")
        val _result: WeeklyBonusEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpLastBonusWeek: Int
          _tmpLastBonusWeek = _stmt.getLong(_columnIndexOfLastBonusWeek).toInt()
          val _tmpLastBonusYear: Int
          _tmpLastBonusYear = _stmt.getLong(_columnIndexOfLastBonusYear).toInt()
          _result = WeeklyBonusEntity(_tmpId,_tmpLastBonusWeek,_tmpLastBonusYear)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getWeeklyBonusSync(): WeeklyBonusEntity? {
    val _sql: String = "SELECT * FROM weekly_bonus_table WHERE id = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfLastBonusWeek: Int = getColumnIndexOrThrow(_stmt, "lastBonusWeek")
        val _columnIndexOfLastBonusYear: Int = getColumnIndexOrThrow(_stmt, "lastBonusYear")
        val _result: WeeklyBonusEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpLastBonusWeek: Int
          _tmpLastBonusWeek = _stmt.getLong(_columnIndexOfLastBonusWeek).toInt()
          val _tmpLastBonusYear: Int
          _tmpLastBonusYear = _stmt.getLong(_columnIndexOfLastBonusYear).toInt()
          _result = WeeklyBonusEntity(_tmpId,_tmpLastBonusWeek,_tmpLastBonusYear)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __ExerciseTrackingType_enumToString(_value: ExerciseTrackingType): String = when (_value) {
    ExerciseTrackingType.REPS -> "REPS"
    ExerciseTrackingType.SECONDS -> "SECONDS"
    ExerciseTrackingType.DISTANCE -> "DISTANCE"
  }

  private fun __ExerciseTrackingType_stringToEnum(_value: String): ExerciseTrackingType = when (_value) {
    "REPS" -> ExerciseTrackingType.REPS
    "SECONDS" -> ExerciseTrackingType.SECONDS
    "DISTANCE" -> ExerciseTrackingType.DISTANCE
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
