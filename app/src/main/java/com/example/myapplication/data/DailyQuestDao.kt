package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.model.DailyQuest
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyQuestDao {
    @Query("SELECT * FROM daily_quest_table ORDER BY id ASC")
    fun getAllQuests(): Flow<List<DailyQuest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<DailyQuest>)

    @Update
    suspend fun updateQuest(quest: DailyQuest)

    @Query("DELETE FROM daily_quest_table")
    suspend fun deleteAllQuests()
}
