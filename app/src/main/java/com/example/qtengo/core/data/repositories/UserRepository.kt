package com.example.qtengo.core.data.repositories

import com.example.qtengo.core.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar la persistencia y recuperación de usuarios usando Firebase Firestore.
 */
class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("users")

    /**
     * Registra un nuevo usuario en la base de datos si el email no existe.
     * @return true si el registro fue exitoso, false si el email ya existe.
     */
    suspend fun registrar(user: User): Boolean {
        val snapshot = collection.whereEqualTo("email", user.email).get().await()
        if (!snapshot.isEmpty) return false
        
        val docRef = collection.document()
        val userWithId = user.copy(id = docRef.id)
        docRef.set(userWithId).await()
        return true
    }

    /**
     * Realiza la validación de credenciales para el inicio de sesión.
     */
    suspend fun login(email: String, password: String): User? {
        val snapshot = collection
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .limit(1)
            .get()
            .await()
        
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    /**
     * Recupera un usuario completo a partir de su dirección de correo electrónico.
     */
    suspend fun getUserByEmail(email: String): User? {
        val snapshot = collection
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }
}
