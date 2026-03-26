package com.example.qtengo.data.repository

import com.example.qtengo.data.local.dao.UserDao
import com.example.qtengo.data.local.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registrar(user: User): Boolean {
        val existe = userDao.buscarPorEmail(user.email)
        if (existe != null) return false
        userDao.insertar(user)
        return true
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.buscarPorEmail(email)
    }
}