package com.example.qtengo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Registra un nuevo usuario en Firebase Auth y guarda sus datos en Firestore
     */
    fun registrar(
        nombre: String,
        apellido1: String,
        apellido2: String,
        email: String,
        password: String,
        perfil: String
    ) {
        viewModelScope.launch {
            // Validar email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("El email no tiene un formato válido")
                return@launch
            }
            // Validar contraseña
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

            try {
                _authState.value = AuthState.Loading

                // Crear usuario en Firebase Auth
                val resultado = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("Error al obtener UID")

                // Guardar datos extra en Firestore
                val userData = mapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "apellido1" to apellido1,
                    "apellido2" to apellido2,
                    "email" to email,
                    "perfil" to perfil
                )
                firestore.collection("usuarios").document(uid).set(userData).await()

                _authState.value = AuthState.Success(
                    uid = uid,
                    nombre = nombre,
                    email = email,
                    perfil = perfil
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    /**
     * Inicia sesión con Firebase Auth y recupera los datos del usuario desde Firestore
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("El email no tiene un formato válido")
                return@launch
            }

            try {
                _authState.value = AuthState.Loading

                // Login en Firebase Auth
                val resultado = auth.signInWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("Error al obtener UID")

                // Recuperar datos del usuario desde Firestore
                val doc = firestore.collection("usuarios").document(uid).get().await()
                val nombre = doc.getString("nombre") ?: ""
                val perfil = doc.getString("perfil") ?: ""

                _authState.value = AuthState.Success(
                    uid = uid,
                    nombre = nombre,
                    email = email,
                    perfil = perfil
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Email o contraseña incorrectos")
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * Resetea el estado a Idle
     */
    fun reset() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(
        val uid: String,
        val nombre: String,
        val email: String,
        val perfil: String
    ) : AuthState()
    data class Error(val mensaje: String) : AuthState()
}