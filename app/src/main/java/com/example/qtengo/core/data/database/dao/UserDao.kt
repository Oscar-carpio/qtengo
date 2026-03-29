package com.example.qtengo.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.qtengo.core.domain.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertar(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): User?
}