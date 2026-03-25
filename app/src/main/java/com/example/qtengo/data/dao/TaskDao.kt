package com.example.qtengo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.model.Task

/**
 * Operaciones para la lista de tareas pendientes de la Pyme.
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE profile = :profile ORDER BY isCompleted ASC")
    fun getByProfile(profile: String): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
