package com.exork.app.data

import androidx.room.*
import com.exork.app.model.JourneyEvent
import com.exork.app.model.JourneyEventType
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyEventDao {
    @Query("SELECT * FROM journey_event_table ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<JourneyEvent>>

    @Insert
    suspend fun insertEvent(event: JourneyEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<JourneyEvent>)

    @Query("SELECT COUNT(*) FROM journey_event_table WHERE eventType = :eventType")
    suspend fun getEventCountByType(eventType: JourneyEventType): Int

    @Query("DELETE FROM journey_event_table")
    suspend fun deleteAllEvents()
}
