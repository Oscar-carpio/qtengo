package com.example.qtengo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.qtengo.data.model.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertar(user: User)

    @Query("SELECT * FROM users WHERE id = :uid LIMIT 1")
    suspend fun buscarPorUid(uid: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): User?
}