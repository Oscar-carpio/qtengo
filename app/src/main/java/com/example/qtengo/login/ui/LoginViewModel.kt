package com.example.qtengo.login.ui

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
     * Registra un nuevo usuario en Firebase Auth y guarda sus datos en Firestore.
     * Los perfiles se guardan como array en Firestore para soportar multiselección.
     */
    fun registrar(
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        perfiles: List<String>
    ) {
        viewModelScope.launch {
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
            if (perfiles.isEmpty()) {
                _authState.value = AuthState.Error("Debes seleccionar al menos un perfil")
                return@launch
            }

            try {
                _authState.value = AuthState.Loading

                val resultado = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("Error al obtener UID")

                val userData = mapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "email" to email,
                    "perfiles" to perfiles
                )
                firestore.collection("usuarios").document(uid).set(userData).await()

                _authState.value = AuthState.Success(
                    uid = uid,
                    nombre = nombre,
                    email = email,
                    perfiles = perfiles
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrar")
            }
        }
    }

    /**
     * Inicia sesión y recupera los perfiles del usuario desde Firestore.
     * Soporta tanto el campo nuevo "perfiles" (List) como el antiguo "perfil" (String)
     * para no romper cuentas ya registradas.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("El email no tiene un formato válido")
                return@launch
            }

            try {
                _authState.value = AuthState.Loading

                val resultado = auth.signInWithEmailAndPassword(email, password).await()
                val uid = resultado.user?.uid ?: throw Exception("Error al obtener UID")

                val doc = firestore.collection("usuarios").document(uid).get().await()
                val nombre = doc.getString("nombre") ?: ""

                @Suppress("UNCHECKED_CAST")
                val perfiles: List<String> = when {
                    doc.get("perfiles") != null ->
                        (doc.get("perfiles") as? List<String>) ?: emptyList()
                    doc.getString("perfil") != null ->
                        listOf(doc.getString("perfil")!!)
                    else -> emptyList()
                }

                _authState.value = AuthState.Success(
                    uid = uid,
                    nombre = nombre,
                    email = email,
                    perfiles = perfiles
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Email o contraseña incorrectos")
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun cerrarSesion() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    /**
     * Resetea el estado a Idle (llamar desde la UI tras navegar).
     */
    fun reset() {
        _authState.value = AuthState.Idle
    }

    /* fun recuperarPassword(email: String) {
        viewModelScope.launch {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("Introduce un email válido primero")
                return@launch
            }
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.RecuperacionEnviada
            } catch (e: Exception) {
                _authState.value = AuthState.Error("No se pudo enviar el correo")
            }
        }
    }*/
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    // object RecuperacionEnviada : AuthState()
    data class Success(
        val uid: String,
        val nombre: String,
        val email: String,
        val perfiles: List<String>
    ) : AuthState()
    data class Error(val mensaje: String) : AuthState()
}