package com.example.myapplication.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.JourneyEvent
import javax.`annotation`.processing.Generated
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
public class JourneyEventDao_Impl(
  __db: RoomDatabase,
) : JourneyEventDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfJourneyEvent: EntityInsertAdapter<JourneyEvent>
  init {
    this.__db = __db
    this.__insertAdapterOfJourneyEvent = object : EntityInsertAdapter<JourneyEvent>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `journey_event_table` (`id`,`type`,`title`,`description`,`timestamp`,`icon`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: JourneyEvent) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.description)
        statement.bindLong(5, entity.timestamp)
        statement.bindText(6, entity.icon)
      }
    }
  }

  public override suspend fun insertEvent(event: JourneyEvent): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfJourneyEvent.insert(_connection, event)
  }

  public override fun getAllEvents(): Flow<List<JourneyEvent>> {
    val _sql: String = "SELECT * FROM journey_event_table ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("journey_event_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _result: MutableList<JourneyEvent> = mutableListOf()
        while (_stmt.step()) {
          val _item: JourneyEvent
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          _item = JourneyEvent(_tmpId,_tmpType,_tmpTitle,_tmpDescription,_tmpTimestamp,_tmpIcon)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEventCountByType(type: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM journey_event_table WHERE type = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllEvents() {
    val _sql: String = "DELETE FROM journey_event_table"
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
