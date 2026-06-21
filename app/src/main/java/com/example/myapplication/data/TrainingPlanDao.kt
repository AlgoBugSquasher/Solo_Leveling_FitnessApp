package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.model.TrainingDay
import com.example.myapplication.model.WeeklyBonusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPlanDao {
    @Query("SELECT * FROM training_plan_table ORDER BY dayOfWeek ASC")
    fun getTrainingPlan(): Flow<List<TrainingDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingDays(days: List<TrainingDay>)

    @Update
    suspend fun updateTrainingDay(day: TrainingDay)

    @Query("SELECT * FROM weekly_bonus_table WHERE id = 0")
    fun getWeeklyBonus(): Flow<WeeklyBonusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyBonus(bonus: WeeklyBonusEntity)
}
