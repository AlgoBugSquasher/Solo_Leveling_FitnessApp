package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.model.JourneyEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyEventDao {
    @Query("SELECT * FROM journey_event_table ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<JourneyEvent>>

    @Insert
    suspend fun insertEvent(event: JourneyEvent)

    @Query("SELECT COUNT(*) FROM journey_event_table WHERE type = :type")
    suspend fun getEventCountByType(type: String): Int

    @Query("DELETE FROM journey_event_table")
    suspend fun deleteAllEvents()
}
