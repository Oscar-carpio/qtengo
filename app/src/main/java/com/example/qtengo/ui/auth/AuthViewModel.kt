package com.example.qtengo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.User
import com.example.qtengo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(
        AppDatabase.getDatabase(application).userDao()
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registrar(nombre: String, apellido1: String, apellido2: String, email: String, password: String, perfil: String) {
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
            // Registrar
            val user = User(
                nombre = nombre,
                apellido1 = apellido1,
                apellido2 = apellido2,
                email = email,
                password = password,
                perfil = perfil
            )
            val exito = repository.registrar(user)
            _authState.value = if (exito) AuthState.Success(user) else AuthState.Error("El email ya está registrado")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("El email no tiene un formato válido")
                return@launch
            }
            val user = repository.login(email, password)
            _authState.value = if (user != null) AuthState.Success(user) else AuthState.Error("Email o contraseña incorrectos")
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