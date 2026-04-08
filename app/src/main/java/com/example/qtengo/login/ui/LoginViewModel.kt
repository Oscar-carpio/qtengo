package com.example.qtengo.login.ui

import android.util.Log
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

    fun registrar(
        nombre: String,
        apellido1: String,
        apellido2: String,
        email: String,
        password: String,
        perfil: String
    ) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthDebug", "Registrando en Auth: $email")

                // 1. Crear usuario en Firebase Auth
                val resultado = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("Error al crear UID")

                // 2. Preparar datos para Firestore
                val userData = mapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "apellido1" to apellido1,
                    "apellido2" to apellido2,
                    "email" to email,
                    "perfil" to perfil
                )

                Log.d("AuthDebug", "Guardando perfil en 'usuarios' para UID: $uid")
                
                // 3. Guardar en Firestore. 
                // No usamos await() aquí para el registro inicial si queremos evitar bloqueos,
                // Firestore lo enviará en segundo plano y la sesión ya es válida.
                firestore.collection("usuarios").document(uid).set(userData)
                
                // 4. Éxito inmediato para navegar
                _authState.value = AuthState.Success(uid, nombre, email, perfil)
                Log.d("AuthDebug", "Registro exitoso, navegando...")

            } catch (e: Exception) {
                Log.e("AuthDebug", "Error en registro: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthDebug", "Iniciando sesión: $email")
                
                val resultado = auth.signInWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("UID no encontrado")

                val doc = firestore.collection("usuarios").document(uid).get().await()
                val nombre = doc.getString("nombre") ?: ""
                val perfil = doc.getString("perfil") ?: ""

                if (perfil.isEmpty()) {
                    _authState.value = AuthState.Error("El usuario no tiene un perfil asignado.")
                } else {
                    _authState.value = AuthState.Success(uid, nombre, email, perfil)
                }
            } catch (e: Exception) {
                Log.e("AuthDebug", "Error en login: ${e.message}")
                _authState.value = AuthState.Error("Email o contraseña incorrectos")
            }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun reset() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String, val nombre: String, val email: String, val perfil: String) : AuthState()
    data class Error(val mensaje: String) : AuthState()
}
