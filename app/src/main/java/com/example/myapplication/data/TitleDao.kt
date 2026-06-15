package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.model.Title
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM title_table")
    fun getAllTitles(): Flow<List<Title>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTitles(titles: List<Title>)

    @Update
    suspend fun updateTitle(title: Title)
}
