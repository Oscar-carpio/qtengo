package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE profile = :profile")
    fun getByProfile(profile: String): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :date AND profile = :profile")
    fun getByDate(date: String, profile: String): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}