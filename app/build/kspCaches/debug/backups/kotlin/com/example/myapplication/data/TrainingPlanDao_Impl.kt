package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.model.WeeklyBonusEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
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

  private val __insertAdapterOfWeeklyBonusEntity: EntityInsertAdapter<WeeklyBonusEntity>

  private val __updateAdapterOfTrainingDay: EntityDeleteOrUpdateAdapter<TrainingDay>
  init {
    this.__db = __db
    this.__insertAdapterOfTrainingDay = object : EntityInsertAdapter<TrainingDay>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `training_plan_table` (`dayOfWeek`,`pushups`,`pullups`,`plankSeconds`,`isCompleted`,`lastCompletedWeek`,`lastCompletedYear`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TrainingDay) {
        statement.bindLong(1, entity.dayOfWeek.toLong())
        statement.bindLong(2, entity.pushups.toLong())
        statement.bindLong(3, entity.pullups.toLong())
        statement.bindLong(4, entity.plankSeconds.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.lastCompletedWeek.toLong())
        statement.bindLong(7, entity.lastCompletedYear.toLong())
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
    this.__updateAdapterOfTrainingDay = object : EntityDeleteOrUpdateAdapter<TrainingDay>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `training_plan_table` SET `dayOfWeek` = ?,`pushups` = ?,`pullups` = ?,`plankSeconds` = ?,`isCompleted` = ?,`lastCompletedWeek` = ?,`lastCompletedYear` = ? WHERE `dayOfWeek` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TrainingDay) {
        statement.bindLong(1, entity.dayOfWeek.toLong())
        statement.bindLong(2, entity.pushups.toLong())
        statement.bindLong(3, entity.pullups.toLong())
        statement.bindLong(4, entity.plankSeconds.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.lastCompletedWeek.toLong())
        statement.bindLong(7, entity.lastCompletedYear.toLong())
        statement.bindLong(8, entity.dayOfWeek.toLong())
      }
    }
  }

  public override suspend fun insertTrainingDays(days: List<TrainingDay>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTrainingDay.insert(_connection, days)
  }

  public override suspend fun insertWeeklyBonus(bonus: WeeklyBonusEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfWeeklyBonusEntity.insert(_connection, bonus)
  }

  public override suspend fun updateTrainingDay(day: TrainingDay): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTrainingDay.handle(_connection, day)
  }

  public override fun getTrainingPlan(): Flow<List<TrainingDay>> {
    val _sql: String = "SELECT * FROM training_plan_table ORDER BY dayOfWeek ASC"
    return createFlow(__db, false, arrayOf("training_plan_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDayOfWeek: Int = getColumnIndexOrThrow(_stmt, "dayOfWeek")
        val _columnIndexOfPushups: Int = getColumnIndexOrThrow(_stmt, "pushups")
        val _columnIndexOfPullups: Int = getColumnIndexOrThrow(_stmt, "pullups")
        val _columnIndexOfPlankSeconds: Int = getColumnIndexOrThrow(_stmt, "plankSeconds")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfLastCompletedWeek: Int = getColumnIndexOrThrow(_stmt, "lastCompletedWeek")
        val _columnIndexOfLastCompletedYear: Int = getColumnIndexOrThrow(_stmt, "lastCompletedYear")
        val _result: MutableList<TrainingDay> = mutableListOf()
        while (_stmt.step()) {
          val _item: TrainingDay
          val _tmpDayOfWeek: Int
          _tmpDayOfWeek = _stmt.getLong(_columnIndexOfDayOfWeek).toInt()
          val _tmpPushups: Int
          _tmpPushups = _stmt.getLong(_columnIndexOfPushups).toInt()
          val _tmpPullups: Int
          _tmpPullups = _stmt.getLong(_columnIndexOfPullups).toInt()
          val _tmpPlankSeconds: Int
          _tmpPlankSeconds = _stmt.getLong(_columnIndexOfPlankSeconds).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpLastCompletedWeek: Int
          _tmpLastCompletedWeek = _stmt.getLong(_columnIndexOfLastCompletedWeek).toInt()
          val _tmpLastCompletedYear: Int
          _tmpLastCompletedYear = _stmt.getLong(_columnIndexOfLastCompletedYear).toInt()
          _item = TrainingDay(_tmpDayOfWeek,_tmpPushups,_tmpPullups,_tmpPlankSeconds,_tmpIsCompleted,_tmpLastCompletedWeek,_tmpLastCompletedYear)
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
