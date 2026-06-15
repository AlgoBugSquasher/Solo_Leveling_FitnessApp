package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.model.ExerciseEntity
import com.example.myapplication.model.WorkoutEntity
import com.example.myapplication.model.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Transaction
    suspend fun insertWorkoutWithExercises(workout: WorkoutEntity, exercises: List<ExerciseEntity>) {
        val workoutId = insertWorkout(workout).toInt()
        val exercisesWithId = exercises.map { it.copy(workoutId = workoutId) }
        insertExercises(exercisesWithId)
    }

    @Transaction
    @Query("SELECT * FROM workout_table ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutWithExercises>>
}
