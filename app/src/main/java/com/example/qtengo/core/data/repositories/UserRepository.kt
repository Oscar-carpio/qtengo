package com.example.qtengo.core.data.repositories

import com.example.qtengo.core.data.database.dao.UserDao
import com.example.qtengo.core.domain.models.User

/**
 * Repositorio para gestionar la persistencia y recuperación de usuarios.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Registra un nuevo usuario en la base de datos si el email no existe.
     * @return true si el registro fue exitoso, false si el email ya existe.
     */
    suspend fun registrar(user: User): Boolean {
        val existe = userDao.buscarPorEmail(user.email)
        if (existe != null) return false
        userDao.insertar(user)
        return true
    }

    /**
     * Realiza la validación de credenciales para el inicio de sesión.
     */
    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    /**
     * Recupera un usuario completo a partir de su dirección de correo electrónico.
     * Útil para restaurar la sesión persistente.
     */
    suspend fun getUserByEmail(email: String): User? {
        return userDao.buscarPorEmail(email)
    }
}
