package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.Ability
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
public class AbilityDao_Impl(
  __db: RoomDatabase,
) : AbilityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAbility: EntityInsertAdapter<Ability>

  private val __updateAdapterOfAbility: EntityDeleteOrUpdateAdapter<Ability>
  init {
    this.__db = __db
    this.__insertAdapterOfAbility = object : EntityInsertAdapter<Ability>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `ability_table` (`name`,`isUnlocked`,`requiredPushups`,`requiredPullups`,`requiredPlankTime`,`requiredLevel`,`requiredStreak`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Ability) {
        statement.bindText(1, entity.name)
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindLong(3, entity.requiredPushups.toLong())
        statement.bindLong(4, entity.requiredPullups.toLong())
        statement.bindLong(5, entity.requiredPlankTime.toLong())
        statement.bindLong(6, entity.requiredLevel.toLong())
        statement.bindLong(7, entity.requiredStreak.toLong())
      }
    }
    this.__updateAdapterOfAbility = object : EntityDeleteOrUpdateAdapter<Ability>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `ability_table` SET `name` = ?,`isUnlocked` = ?,`requiredPushups` = ?,`requiredPullups` = ?,`requiredPlankTime` = ?,`requiredLevel` = ?,`requiredStreak` = ? WHERE `name` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Ability) {
        statement.bindText(1, entity.name)
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindLong(3, entity.requiredPushups.toLong())
        statement.bindLong(4, entity.requiredPullups.toLong())
        statement.bindLong(5, entity.requiredPlankTime.toLong())
        statement.bindLong(6, entity.requiredLevel.toLong())
        statement.bindLong(7, entity.requiredStreak.toLong())
        statement.bindText(8, entity.name)
      }
    }
  }

  public override suspend fun insertAbilities(abilities: List<Ability>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAbility.insert(_connection, abilities)
  }

  public override suspend fun updateAbility(ability: Ability): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfAbility.handle(_connection, ability)
  }

  public override fun getAllAbilities(): Flow<List<Ability>> {
    val _sql: String = "SELECT * FROM ability_table"
    return createFlow(__db, false, arrayOf("ability_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _columnIndexOfRequiredPushups: Int = getColumnIndexOrThrow(_stmt, "requiredPushups")
        val _columnIndexOfRequiredPullups: Int = getColumnIndexOrThrow(_stmt, "requiredPullups")
        val _columnIndexOfRequiredPlankTime: Int = getColumnIndexOrThrow(_stmt, "requiredPlankTime")
        val _columnIndexOfRequiredLevel: Int = getColumnIndexOrThrow(_stmt, "requiredLevel")
        val _columnIndexOfRequiredStreak: Int = getColumnIndexOrThrow(_stmt, "requiredStreak")
        val _result: MutableList<Ability> = mutableListOf()
        while (_stmt.step()) {
          val _item: Ability
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          val _tmpRequiredPushups: Int
          _tmpRequiredPushups = _stmt.getLong(_columnIndexOfRequiredPushups).toInt()
          val _tmpRequiredPullups: Int
          _tmpRequiredPullups = _stmt.getLong(_columnIndexOfRequiredPullups).toInt()
          val _tmpRequiredPlankTime: Int
          _tmpRequiredPlankTime = _stmt.getLong(_columnIndexOfRequiredPlankTime).toInt()
          val _tmpRequiredLevel: Int
          _tmpRequiredLevel = _stmt.getLong(_columnIndexOfRequiredLevel).toInt()
          val _tmpRequiredStreak: Int
          _tmpRequiredStreak = _stmt.getLong(_columnIndexOfRequiredStreak).toInt()
          _item = Ability(_tmpName,_tmpIsUnlocked,_tmpRequiredPushups,_tmpRequiredPullups,_tmpRequiredPlankTime,_tmpRequiredLevel,_tmpRequiredStreak)
          _result.add(_item)
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
