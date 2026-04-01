package com.example.qtengo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.User
import com.example.qtengo.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val repository = UserRepository(
        AppDatabase.getDatabase(application).userDao()
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registrar(nombre: String, apellido1: String, apellido2: String, email: String, password: String, perfil: String) {
        viewModelScope.launch {
            try {
                // Validaciones
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El email no tiene un formato válido")
                    return@launch
                }
                if (password.length < 8) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 8 caracteres")
                    return@launch
                }
                if (!password.any { it.isUpperCase() }) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos una mayúscula")
                    return@launch
                }
                if (!password.any { it.isDigit() }) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos un número")
                    return@launch
                }

                // Registrar en Firebase Auth
                auth.createUserWithEmailAndPassword(email, password).await()
                val uid = auth.currentUser?.uid ?: throw Exception("Error al obtener UID")

                // Guardar datos extra en Room
                val user = User(
                    id = uid,
                    nombre = nombre,
                    apellido1 = apellido1,
                    apellido2 = apellido2,
                    email = email,
                    perfil = perfil
                )
                repository.registrar(user)
                _authState.value = AuthState.Success(user)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El email no tiene un formato válido")
                    return@launch
                }

                // Login con Firebase Auth
                auth.signInWithEmailAndPassword(email, password).await()
                val uid = auth.currentUser?.uid ?: throw Exception("Error al obtener UID")

                // Obtener datos del usuario desde Room
                val user = repository.buscarPorUid(uid) ?: throw Exception("Usuario no encontrado")
                _authState.value = AuthState.Success(user)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun reset() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val mensaje: String) : AuthState()
}