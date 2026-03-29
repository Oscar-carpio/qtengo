package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.User
import com.example.qtengo.core.data.repositories.UserRepository
import com.example.qtengo.core.ui.screens.SplashScreen
import com.example.qtengo.core.ui.screens.ProfileScreen
import com.example.qtengo.core.ui.theme.QtengoTheme
import com.example.qtengo.login.ui.LoginScreen
import com.example.qtengo.login.ui.RegisterScreen
import com.example.qtengo.familiar.ui.FamiliarHomeScreen
import com.example.qtengo.familiar.ui.GastosScreen
import com.example.qtengo.pyme.ui.*
import com.example.qtengo.restauracion.ui.RestauracionHomeScreen
import com.example.qtengo.utils.SessionManager

/**
 * Actividad principal que sirve como punto de entrada a la aplicación.
 * Inicializa la persistencia de sesión y gestiona la navegación global.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
        
        enableEdgeToEdge()
        setContent {
            QtengoTheme {
                AppNavigation(sessionManager, userRepository)
            }
        }
    }
}

/**
 * Orquestador principal de la navegación.
 * Gestiona el flujo entre Splash, Autenticación, Selección de Perfil y las áreas de usuario.
 */
@Composable
fun AppNavigation(sessionManager: SessionManager, userRepository: UserRepository) {
    var showSplash by remember { mutableStateOf(true) }
    var usuarioLogueado by remember { mutableStateOf<User?>(null) }
    var mostrarRegistro by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("") }
    var showProfileSelection by remember { mutableStateOf(false) }

    // Restaurar sesión si existe un token/email guardado
    LaunchedEffect(Unit) {
        val savedEmail = sessionManager.getUserEmail()
        if (savedEmail != null) {
            val user = userRepository.getUserByEmail(savedEmail)
            if (user != null) {
                usuarioLogueado = user
            }
        }
    }

    /**
     * Cierra la sesión activa y limpia las preferencias del sistema.
     */
    val onLogout = {
        sessionManager.clearSession()
        usuarioLogueado = null
        currentScreen = ""
        showProfileSelection = false
    }

    /**
     * Permite al usuario volver a la pantalla de selección de rol.
     */
    val onChangeProfile = {
        showProfileSelection = true
        currentScreen = ""
    }

    when {
        showSplash -> SplashScreen(onSplashFinished = { showSplash = false })
        
        usuarioLogueado == null -> {
            if (mostrarRegistro) {
                RegisterScreen(
                    onRegistroExitoso = { user ->
                        sessionManager.saveUserEmail(user.email)
                        usuarioLogueado = user
                        mostrarRegistro = false
                    },
                    onIrALogin = { mostrarRegistro = false }
                )
            } else {
                LoginScreen(
                    onLoginExitoso = { user ->
                        sessionManager.saveUserEmail(user.email)
                        usuarioLogueado = user
                    },
                    onIrARegistro = { mostrarRegistro = true }
                )
            }
        }

        showProfileSelection -> ProfileScreen(onProfileSelected = { newProfile ->
            usuarioLogueado = usuarioLogueado?.copy(perfil = newProfile)
            showProfileSelection = false
        })

        else -> {
            when (usuarioLogueado?.perfil) {
                "Familiar" -> FamiliarNavigation(currentScreen, { currentScreen = it }, onLogout, onChangeProfile)
                "Pyme" -> PymeNavigation(currentScreen, { currentScreen = it }, onLogout, onChangeProfile)
                "Restauración" -> RestauracionNavigation(currentScreen, { currentScreen = it }, onLogout, onChangeProfile)
            }
        }
    }
}

/**
 * Gestiona la navegación específica del perfil Familiar.
 */
@Composable
fun FamiliarNavigation(currentScreen: String, onScreenChange: (String) -> Unit, onLogout: () -> Unit, onChangeProfile: () -> Unit) {
    when (currentScreen) {
        "" -> FamiliarHomeScreen(onMenuSelected = onScreenChange, onLogout = onLogout, onChangeProfile = onChangeProfile)
        "Control de gastos" -> GastosScreen(
            onLogout = onLogout, 
            onChangeProfile = onChangeProfile, 
            onBack = { onScreenChange("") }
        )
        "Tareas y recordatorios" -> TaskScreen(
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        else -> onScreenChange("")
    }
}

/**
 * Gestiona la navegación específica del perfil Pyme.
 */
@Composable
fun PymeNavigation(currentScreen: String, onScreenChange: (String) -> Unit, onLogout: () -> Unit, onChangeProfile: () -> Unit) {
    when (currentScreen) {
        "" -> PymeHomeScreen(onMenuSelected = onScreenChange, onLogout = onLogout, onChangeProfile = onChangeProfile)
        "Productos / Stock" -> ProductScreen(
            onBack = { onScreenChange("") }, 
            onLogout = onLogout, 
            onChangeProfile = onChangeProfile
        )
        "Gastos e ingresos" -> PymeFinanceScreen(
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        "Proveedores" -> SupplierScreen(
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        "Empleados" -> EmployeeScreen(
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        "Agenda de Tareas" -> TaskScreen(
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        else -> onScreenChange("")
    }
}

/**
 * Gestiona la navegación específica del perfil Restauración.
 */
@Composable
fun RestauracionNavigation(currentScreen: String, onScreenChange: (String) -> Unit, onLogout: () -> Unit, onChangeProfile: () -> Unit) {
    when (currentScreen) {
        "" -> RestauracionHomeScreen(onMenuSelected = onScreenChange, onLogout = onLogout, onChangeProfile = onChangeProfile)
        "Stock de cocina" -> ProductScreen(
            profile = "Restauración", 
            onBack = { onScreenChange("") }, 
            onLogout = onLogout, 
            onChangeProfile = onChangeProfile
        )
        "Proveedores" -> SupplierScreen(
            profile = "Restauración", 
            onBack = { onScreenChange("") },
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )
        else -> onScreenChange("")
    }
}
