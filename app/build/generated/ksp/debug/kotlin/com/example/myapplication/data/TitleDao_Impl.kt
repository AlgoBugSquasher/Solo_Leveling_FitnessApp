package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.Title
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
public class TitleDao_Impl(
  __db: RoomDatabase,
) : TitleDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTitle: EntityInsertAdapter<Title>

  private val __updateAdapterOfTitle: EntityDeleteOrUpdateAdapter<Title>
  init {
    this.__db = __db
    this.__insertAdapterOfTitle = object : EntityInsertAdapter<Title>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `title_table` (`name`,`requiredStreak`,`isUnlocked`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Title) {
        statement.bindText(1, entity.name)
        statement.bindLong(2, entity.requiredStreak.toLong())
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(3, _tmp.toLong())
      }
    }
    this.__updateAdapterOfTitle = object : EntityDeleteOrUpdateAdapter<Title>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `title_table` SET `name` = ?,`requiredStreak` = ?,`isUnlocked` = ? WHERE `name` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Title) {
        statement.bindText(1, entity.name)
        statement.bindLong(2, entity.requiredStreak.toLong())
        val _tmp: Int = if (entity.isUnlocked) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindText(4, entity.name)
      }
    }
  }

  public override suspend fun insertTitles(titles: List<Title>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTitle.insert(_connection, titles)
  }

  public override suspend fun updateTitle(title: Title): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTitle.handle(_connection, title)
  }

  public override fun getAllTitles(): Flow<List<Title>> {
    val _sql: String = "SELECT * FROM title_table"
    return createFlow(__db, false, arrayOf("title_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfRequiredStreak: Int = getColumnIndexOrThrow(_stmt, "requiredStreak")
        val _columnIndexOfIsUnlocked: Int = getColumnIndexOrThrow(_stmt, "isUnlocked")
        val _result: MutableList<Title> = mutableListOf()
        while (_stmt.step()) {
          val _item: Title
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpRequiredStreak: Int
          _tmpRequiredStreak = _stmt.getLong(_columnIndexOfRequiredStreak).toInt()
          val _tmpIsUnlocked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUnlocked).toInt()
          _tmpIsUnlocked = _tmp != 0
          _item = Title(_tmpName,_tmpRequiredStreak,_tmpIsUnlocked)
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
