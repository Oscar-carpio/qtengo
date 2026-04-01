package com.example.qtengo.data.repository

import com.example.qtengo.data.dao.UserDao
import com.example.qtengo.data.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registrar(user: User) {
        userDao.insertar(user)
    }

    suspend fun buscarPorUid(uid: String): User? {
        return userDao.buscarPorUid(uid)
    }

    suspend fun buscarPorEmail(email: String): User? {
        return userDao.buscarPorEmail(email)
    }
}