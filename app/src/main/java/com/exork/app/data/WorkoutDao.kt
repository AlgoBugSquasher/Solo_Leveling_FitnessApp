package com.exork.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.exork.app.model.ExerciseEntity
import com.exork.app.model.WorkoutEntity
import com.exork.app.model.WorkoutWithExercises
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

    @Query("SELECT * FROM workout_table WHERE date = :timestamp LIMIT 1")
    suspend fun getWorkoutByTimestamp(timestamp: Long): WorkoutEntity?

    @Query("SELECT * FROM workout_table WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getWorkoutByRemoteId(remoteId: String): WorkoutEntity?

    @Query("DELETE FROM workout_table")
    suspend fun deleteAllWorkouts()

    @Query("DELETE FROM exercise_table")
    suspend fun deleteAllExercises()
}
