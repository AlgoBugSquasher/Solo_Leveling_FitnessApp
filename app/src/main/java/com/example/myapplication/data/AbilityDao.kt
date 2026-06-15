package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.model.Ability
import kotlinx.coroutines.flow.Flow

@Dao
interface AbilityDao {
    @Query("SELECT * FROM ability_table")
    fun getAllAbilities(): Flow<List<Ability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilities(abilities: List<Ability>)

    @Update
    suspend fun updateAbility(ability: Ability)
}
