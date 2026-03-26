package com.example.qtengo.data.repository

import com.example.qtengo.data.dao.UserDao
import com.example.qtengo.data.model.User

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
}