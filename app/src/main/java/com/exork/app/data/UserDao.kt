package com.exork.app.data

import androidx.room.*
import com.exork.app.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE id = 0")
    fun getUser(): Flow<User?>

    @Query("SELECT * FROM user_table WHERE id = 0")
    suspend fun getUserDirect(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM user_table")
    suspend fun deleteAllUsers()
}
