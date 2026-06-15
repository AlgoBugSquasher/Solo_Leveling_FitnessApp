package com.example.myapplication.`data`

import androidx.collection.LongSparseArray
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.model.WorkoutWithExercises
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
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WorkoutDao_Impl(
  __db: RoomDatabase,
) : WorkoutDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWorkoutEntity: EntityInsertAdapter<WorkoutEntity>

  private val __insertAdapterOfExerciseEntity: EntityInsertAdapter<ExerciseEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWorkoutEntity = object : EntityInsertAdapter<WorkoutEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `workout_table` (`id`,`date`,`totalXpGained`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.date)
        statement.bindLong(3, entity.totalXpGained.toLong())
      }
    }
    this.__insertAdapterOfExerciseEntity = object : EntityInsertAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `exercise_table` (`id`,`workoutId`,`name`,`reps`,`sets`,`duration`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.workoutId.toLong())
        statement.bindText(3, entity.name)
        val _tmpReps: Int? = entity.reps
        if (_tmpReps == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpReps.toLong())
        }
        statement.bindLong(5, entity.sets.toLong())
        val _tmpDuration: Int? = entity.duration
        if (_tmpDuration == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpDuration.toLong())
        }
      }
    }
  }

  public override suspend fun insertWorkout(workout: WorkoutEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfWorkoutEntity.insertAndReturnId(_connection, workout)
    _result
  }

  public override suspend fun insertExercises(exercises: List<ExerciseEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseEntity.insert(_connection, exercises)
  }

  public override suspend fun insertWorkoutWithExercises(workout: WorkoutEntity, exercises: List<ExerciseEntity>): Unit = performInTransactionSuspending(__db) {
    super@WorkoutDao_Impl.insertWorkoutWithExercises(workout, exercises)
  }

  public override fun getAllWorkouts(): Flow<List<WorkoutWithExercises>> {
    val _sql: String = "SELECT * FROM workout_table ORDER BY date DESC"
    return createFlow(__db, true, arrayOf("exercise_table", "workout_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTotalXpGained: Int = getColumnIndexOrThrow(_stmt, "totalXpGained")
        val _collectionExercises: LongSparseArray<MutableList<ExerciseEntity>> = LongSparseArray<MutableList<ExerciseEntity>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionExercises.containsKey(_tmpKey)) {
            _collectionExercises.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipexerciseTableAscomExampleMyapplicationModelExerciseEntity(_connection, _collectionExercises)
        val _result: MutableList<WorkoutWithExercises> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutWithExercises
          val _tmpWorkout: WorkoutEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpTotalXpGained: Int
          _tmpTotalXpGained = _stmt.getLong(_columnIndexOfTotalXpGained).toInt()
          _tmpWorkout = WorkoutEntity(_tmpId,_tmpDate,_tmpTotalXpGained)
          val _tmpExercisesCollection: MutableList<ExerciseEntity>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpExercisesCollection = checkNotNull(_collectionExercises.get(_tmpKey_1))
          _item = WorkoutWithExercises(_tmpWorkout,_tmpExercisesCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipexerciseTableAscomExampleMyapplicationModelExerciseEntity(_connection: SQLiteConnection, _map: LongSparseArray<MutableList<ExerciseEntity>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipexerciseTableAscomExampleMyapplicationModelExerciseEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`workoutId`,`name`,`reps`,`sets`,`duration` FROM `exercise_table` WHERE `workoutId` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "workoutId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfWorkoutId: Int = 1
      val _columnIndexOfName: Int = 2
      val _columnIndexOfReps: Int = 3
      val _columnIndexOfSets: Int = 4
      val _columnIndexOfDuration: Int = 5
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<ExerciseEntity>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: ExerciseEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpWorkoutId: Int
          _tmpWorkoutId = _stmt.getLong(_columnIndexOfWorkoutId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpReps: Int?
          if (_stmt.isNull(_columnIndexOfReps)) {
            _tmpReps = null
          } else {
            _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          }
          val _tmpSets: Int
          _tmpSets = _stmt.getLong(_columnIndexOfSets).toInt()
          val _tmpDuration: Int?
          if (_stmt.isNull(_columnIndexOfDuration)) {
            _tmpDuration = null
          } else {
            _tmpDuration = _stmt.getLong(_columnIndexOfDuration).toInt()
          }
          _item_1 = ExerciseEntity(_tmpId,_tmpWorkoutId,_tmpName,_tmpReps,_tmpSets,_tmpDuration)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
