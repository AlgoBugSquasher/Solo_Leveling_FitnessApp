package com.example.myapplication.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.myapplication.model.User
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserDao_Impl(
  __db: RoomDatabase,
) : UserDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUser: EntityInsertAdapter<User>

  private val __updateAdapterOfUser: EntityDeleteOrUpdateAdapter<User>
  init {
    this.__db = __db
    this.__insertAdapterOfUser = object : EntityInsertAdapter<User>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `user_table` (`id`,`xp`,`level`,`streak`,`rank`,`pushups`,`pullups`,`plankTime`,`totalPikePushups`,`totalPseudoPlanchePushups`,`totalHangingSeconds`,`totalExplosivePullups`,`totalXpEarned`,`totalWorkouts`,`highestStreak`,`lastWorkoutDate`,`activeTitle`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.xp.toLong())
        statement.bindLong(3, entity.level.toLong())
        statement.bindLong(4, entity.streak.toLong())
        statement.bindText(5, entity.rank)
        statement.bindLong(6, entity.pushups.toLong())
        statement.bindLong(7, entity.pullups.toLong())
        statement.bindLong(8, entity.plankTime.toLong())
        statement.bindLong(9, entity.totalPikePushups.toLong())
        statement.bindLong(10, entity.totalPseudoPlanchePushups.toLong())
        statement.bindLong(11, entity.totalHangingSeconds.toLong())
        statement.bindLong(12, entity.totalExplosivePullups.toLong())
        statement.bindLong(13, entity.totalXpEarned.toLong())
        statement.bindLong(14, entity.totalWorkouts.toLong())
        statement.bindLong(15, entity.highestStreak.toLong())
        statement.bindLong(16, entity.lastWorkoutDate)
        val _tmpActiveTitle: String? = entity.activeTitle
        if (_tmpActiveTitle == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpActiveTitle)
        }
      }
    }
    this.__updateAdapterOfUser = object : EntityDeleteOrUpdateAdapter<User>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `user_table` SET `id` = ?,`xp` = ?,`level` = ?,`streak` = ?,`rank` = ?,`pushups` = ?,`pullups` = ?,`plankTime` = ?,`totalPikePushups` = ?,`totalPseudoPlanchePushups` = ?,`totalHangingSeconds` = ?,`totalExplosivePullups` = ?,`totalXpEarned` = ?,`totalWorkouts` = ?,`highestStreak` = ?,`lastWorkoutDate` = ?,`activeTitle` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.xp.toLong())
        statement.bindLong(3, entity.level.toLong())
        statement.bindLong(4, entity.streak.toLong())
        statement.bindText(5, entity.rank)
        statement.bindLong(6, entity.pushups.toLong())
        statement.bindLong(7, entity.pullups.toLong())
        statement.bindLong(8, entity.plankTime.toLong())
        statement.bindLong(9, entity.totalPikePushups.toLong())
        statement.bindLong(10, entity.totalPseudoPlanchePushups.toLong())
        statement.bindLong(11, entity.totalHangingSeconds.toLong())
        statement.bindLong(12, entity.totalExplosivePullups.toLong())
        statement.bindLong(13, entity.totalXpEarned.toLong())
        statement.bindLong(14, entity.totalWorkouts.toLong())
        statement.bindLong(15, entity.highestStreak.toLong())
        statement.bindLong(16, entity.lastWorkoutDate)
        val _tmpActiveTitle: String? = entity.activeTitle
        if (_tmpActiveTitle == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpActiveTitle)
        }
        statement.bindLong(18, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertUser(user: User): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUser.insert(_connection, user)
  }

  public override suspend fun updateUser(user: User): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfUser.handle(_connection, user)
  }

  public override fun getUser(): Flow<User?> {
    val _sql: String = "SELECT * FROM user_table WHERE id = 0"
    return createFlow(__db, false, arrayOf("user_table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfXp: Int = getColumnIndexOrThrow(_stmt, "xp")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _columnIndexOfStreak: Int = getColumnIndexOrThrow(_stmt, "streak")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfPushups: Int = getColumnIndexOrThrow(_stmt, "pushups")
        val _columnIndexOfPullups: Int = getColumnIndexOrThrow(_stmt, "pullups")
        val _columnIndexOfPlankTime: Int = getColumnIndexOrThrow(_stmt, "plankTime")
        val _columnIndexOfTotalPikePushups: Int = getColumnIndexOrThrow(_stmt, "totalPikePushups")
        val _columnIndexOfTotalPseudoPlanchePushups: Int = getColumnIndexOrThrow(_stmt, "totalPseudoPlanchePushups")
        val _columnIndexOfTotalHangingSeconds: Int = getColumnIndexOrThrow(_stmt, "totalHangingSeconds")
        val _columnIndexOfTotalExplosivePullups: Int = getColumnIndexOrThrow(_stmt, "totalExplosivePullups")
        val _columnIndexOfTotalXpEarned: Int = getColumnIndexOrThrow(_stmt, "totalXpEarned")
        val _columnIndexOfTotalWorkouts: Int = getColumnIndexOrThrow(_stmt, "totalWorkouts")
        val _columnIndexOfHighestStreak: Int = getColumnIndexOrThrow(_stmt, "highestStreak")
        val _columnIndexOfLastWorkoutDate: Int = getColumnIndexOrThrow(_stmt, "lastWorkoutDate")
        val _columnIndexOfActiveTitle: Int = getColumnIndexOrThrow(_stmt, "activeTitle")
        val _result: User?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpXp: Int
          _tmpXp = _stmt.getLong(_columnIndexOfXp).toInt()
          val _tmpLevel: Int
          _tmpLevel = _stmt.getLong(_columnIndexOfLevel).toInt()
          val _tmpStreak: Int
          _tmpStreak = _stmt.getLong(_columnIndexOfStreak).toInt()
          val _tmpRank: String
          _tmpRank = _stmt.getText(_columnIndexOfRank)
          val _tmpPushups: Int
          _tmpPushups = _stmt.getLong(_columnIndexOfPushups).toInt()
          val _tmpPullups: Int
          _tmpPullups = _stmt.getLong(_columnIndexOfPullups).toInt()
          val _tmpPlankTime: Int
          _tmpPlankTime = _stmt.getLong(_columnIndexOfPlankTime).toInt()
          val _tmpTotalPikePushups: Int
          _tmpTotalPikePushups = _stmt.getLong(_columnIndexOfTotalPikePushups).toInt()
          val _tmpTotalPseudoPlanchePushups: Int
          _tmpTotalPseudoPlanchePushups = _stmt.getLong(_columnIndexOfTotalPseudoPlanchePushups).toInt()
          val _tmpTotalHangingSeconds: Int
          _tmpTotalHangingSeconds = _stmt.getLong(_columnIndexOfTotalHangingSeconds).toInt()
          val _tmpTotalExplosivePullups: Int
          _tmpTotalExplosivePullups = _stmt.getLong(_columnIndexOfTotalExplosivePullups).toInt()
          val _tmpTotalXpEarned: Int
          _tmpTotalXpEarned = _stmt.getLong(_columnIndexOfTotalXpEarned).toInt()
          val _tmpTotalWorkouts: Int
          _tmpTotalWorkouts = _stmt.getLong(_columnIndexOfTotalWorkouts).toInt()
          val _tmpHighestStreak: Int
          _tmpHighestStreak = _stmt.getLong(_columnIndexOfHighestStreak).toInt()
          val _tmpLastWorkoutDate: Long
          _tmpLastWorkoutDate = _stmt.getLong(_columnIndexOfLastWorkoutDate)
          val _tmpActiveTitle: String?
          if (_stmt.isNull(_columnIndexOfActiveTitle)) {
            _tmpActiveTitle = null
          } else {
            _tmpActiveTitle = _stmt.getText(_columnIndexOfActiveTitle)
          }
          _result = User(_tmpId,_tmpXp,_tmpLevel,_tmpStreak,_tmpRank,_tmpPushups,_tmpPullups,_tmpPlankTime,_tmpTotalPikePushups,_tmpTotalPseudoPlanchePushups,_tmpTotalHangingSeconds,_tmpTotalExplosivePullups,_tmpTotalXpEarned,_tmpTotalWorkouts,_tmpHighestStreak,_tmpLastWorkoutDate,_tmpActiveTitle)
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
