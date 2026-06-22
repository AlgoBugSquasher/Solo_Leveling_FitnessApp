package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.DailyQuest
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
public class DailyQuestDao_Impl(
  __db: RoomDatabase,
) : DailyQuestDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDailyQuest: EntityInsertAdapter<DailyQuest>

  private val __updateAdapterOfDailyQuest: EntityDeleteOrUpdateAdapter<DailyQuest>
  init {
    this.__db = __db
    this.__insertAdapterOfDailyQuest = object : EntityInsertAdapter<DailyQuest>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `daily_quest_table` (`id`,`title`,`goal`,`xpReward`,`isCompleted`,`sets`,`reps`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DailyQuest) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.goal)
        statement.bindLong(4, entity.xpReward.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        val _tmpSets: Int? = entity.sets
        if (_tmpSets == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSets.toLong())
        }
        val _tmpReps: Int? = entity.reps
        if (_tmpReps == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpReps.toLong())
        }
      }
    }
    this.__updateAdapterOfDailyQuest = object : EntityDeleteOrUpdateAdapter<DailyQuest>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `daily_quest_table` SET `id` = ?,`title` = ?,`goal` = ?,`xpReward` = ?,`isCompleted` = ?,`sets` = ?,`reps` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DailyQuest) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.goal)
        statement.bindLong(4, entity.xpReward.toLong())
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        val _tmpSets: Int? = entity.sets
        if (_tmpSets == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpSets.toLong())
        }
        val _tmpReps: Int? = entity.reps
        if (_tmpReps == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpReps.toLong())
        }
        statement.bindLong(8, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertQuests(quests: List<DailyQuest>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDailyQuest.insert(_connection, quests)
  }

  public override suspend fun updateQuest(quest: DailyQuest): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfDailyQuest.handle(_connection, quest)
  }

  public override fun getAllQuests(): Flow<List<DailyQuest>> {
    val _sql: String = "SELECT * FROM daily_quest_table ORDER BY id ASC"
    return createFlow(__db, false, arrayOf("daily_quest_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfGoal: Int = getColumnIndexOrThrow(_stmt, "goal")
        val _columnIndexOfXpReward: Int = getColumnIndexOrThrow(_stmt, "xpReward")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfSets: Int = getColumnIndexOrThrow(_stmt, "sets")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _result: MutableList<DailyQuest> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyQuest
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpGoal: String
          _tmpGoal = _stmt.getText(_columnIndexOfGoal)
          val _tmpXpReward: Int
          _tmpXpReward = _stmt.getLong(_columnIndexOfXpReward).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
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
          _item = DailyQuest(_tmpId,_tmpTitle,_tmpGoal,_tmpXpReward,_tmpIsCompleted,_tmpSets,_tmpReps)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllQuests() {
    val _sql: String = "DELETE FROM daily_quest_table"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
