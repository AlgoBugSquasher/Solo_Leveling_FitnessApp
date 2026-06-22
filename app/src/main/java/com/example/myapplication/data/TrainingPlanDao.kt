package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.model.PlannedExercise
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

    @Query("SELECT * FROM planned_exercise_table ORDER BY dayOfWeek ASC, id ASC")
    fun getAllPlannedExercises(): Flow<List<PlannedExercise>>

    @Query("SELECT * FROM planned_exercise_table WHERE dayOfWeek = :dayOfWeek ORDER BY id ASC")
    fun getPlannedExercisesForDay(dayOfWeek: Int): Flow<List<PlannedExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedExercise(exercise: PlannedExercise)

    @Update
    suspend fun updatePlannedExercise(exercise: PlannedExercise)

    @Delete
    suspend fun deletePlannedExercise(exercise: PlannedExercise)

    @Query("SELECT * FROM weekly_bonus_table WHERE id = 0")
    fun getWeeklyBonus(): Flow<WeeklyBonusEntity?>

    @Query("SELECT * FROM weekly_bonus_table WHERE id = 0")
    suspend fun getWeeklyBonusSync(): WeeklyBonusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyBonus(bonus: WeeklyBonusEntity)
}

