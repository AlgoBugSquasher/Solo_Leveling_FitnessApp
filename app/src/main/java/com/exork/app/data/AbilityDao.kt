package com.exork.app.data

import androidx.room.*
import com.exork.app.model.Ability
import kotlinx.coroutines.flow.Flow

@Dao
interface AbilityDao {
    @Query("SELECT * FROM ability_table")
    fun getAllAbilities(): Flow<List<Ability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilities(abilities: List<Ability>)

    @Update
    suspend fun updateAbility(ability: Ability)

    @Query("DELETE FROM ability_table")
    suspend fun deleteAllAbilities()
}
