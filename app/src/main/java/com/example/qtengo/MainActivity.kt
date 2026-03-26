package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.User
import com.example.qtengo.data.repository.UserRepository
import com.example.qtengo.ui.auth.LoginScreen
import com.example.qtengo.ui.auth.RegisterScreen
import com.example.qtengo.ui.screens.*
import com.example.qtengo.ui.theme.QtengoTheme
import com.example.qtengo.ui.products.ProductScreen
import com.example.qtengo.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Actividad principal que gestiona la navegación de la aplicación Q-Tengo.
 * Organizada por perfiles: Familiar, Pyme y Restauración.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        val userRepository = UserRepository(AppDatabase.getDatabase(this).userDao())
        
        enableEdgeToEdge()
        setContent {
            QtengoTheme {
                // Estados para controlar la navegación
                var showSplash by remember { mutableStateOf(true) }
                var usuarioLogueado by remember { mutableStateOf<User?>(null) }
                var mostrarRegistro by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("") }
                val selectedShoppingList = remember { mutableStateOf<ShoppingList?>(null) }

                // Estados para diálogos o pantallas secundarias
                var showAddGasto by remember { mutableStateOf(false) }
                var showAddInventario by remember { mutableStateOf(false) }

                // Cargar sesión persistente
                LaunchedEffect(Unit) {
                    val savedEmail = sessionManager.getUserEmail()
                    if (savedEmail != null) {
                        val user = userRepository.getUserByEmail(savedEmail)
                        if (user != null) {
                            usuarioLogueado = user
                        }
                    }
                }

                when {
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                    usuarioLogueado == null && mostrarRegistro -> RegisterScreen(
                        onRegistroExitoso = { user ->
                            sessionManager.saveUserEmail(user.email)
                            usuarioLogueado = user
                            mostrarRegistro = false
                        },
                        onIrALogin = { mostrarRegistro = false }
                    )
                    usuarioLogueado == null -> LoginScreen(
                        onLoginExitoso = { user ->
                            sessionManager.saveUserEmail(user.email)
                            usuarioLogueado = user
                        },
                        onIrARegistro = { mostrarRegistro = true }
                    )
                    
                    // --- PERFIL FAMILIAR ---
                    usuarioLogueado!!.perfil == "Familiar" -> {
                        when (currentScreen) {
                            "" -> FamiliarHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = {
                                    sessionManager.clearSession()
                                    usuarioLogueado = null
                                }
                            )
                            "Lista de la compra" -> {
                                if (selectedShoppingList.value == null) {
                                    ShoppingListScreen(
                                        onListSelected = { selectedShoppingList.value = it },
                                        onBack = { currentScreen = "" }
                                    )
                                } else {
                                    ShoppingListDetailScreen(
                                        shoppingList = selectedShoppingList.value!!,
                                        onBack = { selectedShoppingList.value = null }
                                    )
                                }
                            }
                            "Control de gastos" -> {
                                if (!showAddGasto) {
                                    GastosScreen(
                                        profile = "FAMILIA",
                                        onAddGasto = { showAddGasto = true },
                                        onBack = { currentScreen = "" }
                                    )
                                } else {
                                    AddGastoScreen(
                                        profile = "FAMILIA",
                                        onGastoGuardado = { showAddGasto = false },
                                        onBack = { showAddGasto = false }
                                    )
                                }
                            }
                            "Inventario del hogar" -> {
                                if (!showAddInventario) {
                                    InventarioScreen(
                                        onAddItem = { showAddInventario = true },
                                        onBack = { currentScreen = "" }
                                    )
                                } else {
                                    AddInventarioScreen(
                                        profile = "FAMILIA",
                                        onItemGuardado = { showAddInventario = false },
                                        onBack = { showAddInventario = false }
                                    )
                                }
                            }
                            "Tareas y recordatorios" -> {
                                TaskScreen(
                                    onBack = { currentScreen = "" }
                                )
                            }
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL PYME ---
                    usuarioLogueado!!.perfil == "Pyme" -> {
                        when (currentScreen) {
                            "" -> PymeHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = {
                                    sessionManager.clearSession()
                                    usuarioLogueado = null
                                    currentScreen = ""
                                }
                            )
                            "Productos / Stock" -> {
                                ProductScreen(
                                    profile = "PYME",
                                    onBack = { currentScreen = "" }
                                )
                            }
                            "Gastos e ingresos" -> {
                                PymeFinanceScreen(
                                    onBack = { currentScreen = "" }
                                )
                            }
                            "Proveedores" -> {
                                SupplierScreen(
                                    profile = "PYME",
                                    onBack = { currentScreen = "" }
                                )
                            }
                            "Empleados" -> {
                                EmployeeScreen(
                                    profile = "PYME",
                                    onBack = { currentScreen = "" }
                                )
                            }
                            "Agenda de Tareas" -> {
                                TaskScreen(
                                    onBack = { currentScreen = "" }
                                )
                            }
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL RESTAURACIÓN ---
                    usuarioLogueado!!.perfil == "Restauración" -> {
                        when (currentScreen) {
                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = {
                                    sessionManager.clearSession()
                                    usuarioLogueado = null
                                    currentScreen = ""
                                }
                            )
                            "Stock de cocina" -> {
                                ProductScreen(
                                    profile = "Restauración",
                                    onBack = { currentScreen = "" }
                                )
                            }
                            "Proveedores" -> {
                                SupplierScreen(
                                    profile = "Restauración",
                                    onBack = { currentScreen = "" }
                                )
                            }
                            else -> currentScreen = ""
                        }
                    }
                }
            }
        }
    }
}
