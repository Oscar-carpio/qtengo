package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.qtengo.ui.auth.LoginScreen
import com.example.qtengo.ui.auth.RegisterScreen
import com.example.qtengo.ui.familiar.FamiliarHomeScreen
import com.example.qtengo.ui.familiar.compra.ShoppingListScreen
import com.example.qtengo.ui.familiar.compra.ShoppingListDetailScreen
import com.example.qtengo.ui.familiar.compra.ShoppingList
import com.example.qtengo.ui.familiar.gastos.GastosScreen
import com.example.qtengo.ui.familiar.gastos.AddGastoScreen
import com.example.qtengo.ui.familiar.inventario.InventarioScreen
import com.example.qtengo.ui.familiar.inventario.AddInventarioScreen
import com.example.qtengo.ui.familiar.tareas.TareasScreen

import com.example.qtengo.ui.screens.PymeHomeScreen
import com.example.qtengo.ui.screens.RestauracionHomeScreen
import com.example.qtengo.ui.screens.PymeFinanceScreen
import com.example.qtengo.ui.screens.EmployeeScreen
import com.example.qtengo.ui.screens.SupplierScreen
import com.example.qtengo.ui.screens.SplashScreen
import com.example.qtengo.ui.products.ProductScreen
import com.example.qtengo.ui.theme.QtengoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Actividad principal que gestiona la navegación de la aplicación Q-Tengo.
 * Organizada por perfiles: Familiar, Pyme y Restauración.
 * Autenticación y sesión gestionadas por Firebase Auth.
 */
class MainActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QtengoTheme {
                var showSplash by remember { mutableStateOf(true) }
                var uid by remember { mutableStateOf<String?>(null) }
                var perfil by remember { mutableStateOf<String?>(null) }
                var mostrarRegistro by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("") }
                val selectedShoppingList = remember { mutableStateOf<ShoppingList?>(null) }
                var showAddGasto by remember { mutableStateOf(false) }
                var showAddInventario by remember { mutableStateOf(false) }

                // Comprobar sesión activa en Firebase Auth al arrancar
                LaunchedEffect(Unit) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        try {
                            val doc = firestore.collection("usuarios")
                                .document(currentUser.uid)
                                .get()
                                .await()
                            val perfilRecuperado = doc.getString("perfil")

                            if (perfilRecuperado != null) {
                                // ✅ Perfil encontrado, continuar sesión
                                uid = currentUser.uid
                                perfil = perfilRecuperado
                            } else {
                                // ❌ Documento sin perfil, forzar login
                                auth.signOut()
                            }
                        } catch (e: Exception) {
                            // ❌ Error de red, forzar login
                            auth.signOut()
                        }
                    }
                    showSplash = false
                }

                /**
                 * Cierra la sesión del usuario y vuelve al login
                 */
                fun cerrarSesion() {
                    auth.signOut()
                    uid = null
                    perfil = null
                    currentScreen = ""
                }

                when {
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )

                    uid == null && mostrarRegistro -> RegisterScreen(
                        onRegistroExitoso = { nuevoUid, nuevoPerfil ->
                            uid = nuevoUid
                            perfil = nuevoPerfil
                            mostrarRegistro = false
                        },
                        onIrALogin = { mostrarRegistro = false }
                    )

                    uid == null -> LoginScreen(
                        onLoginExitoso = { nuevoUid, nuevoPerfil ->
                            uid = nuevoUid
                            perfil = nuevoPerfil
                        },
                        onIrARegistro = { mostrarRegistro = true }
                    )

                    // Mientras carga el perfil desde Firestore
                    perfil == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // --- PERFIL FAMILIAR ---
                    perfil == "Familiar" -> {
                        when (currentScreen) {
                            "" -> FamiliarHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { cerrarSesion() }
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
                                        onAddGasto = { showAddGasto = true },
                                        onBack = { currentScreen = "" }
                                    )
                                } else {
                                    AddGastoScreen(
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
                                        onItemGuardado = { showAddInventario = false },
                                        onBack = { showAddInventario = false }
                                    )
                                }
                            }
                            "Tareas y recordatorios" -> TareasScreen(
                                onBack = { currentScreen = "" }
                            )
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL PYME ---
                    perfil == "Pyme" -> {
                        when (currentScreen) {
                            "" -> PymeHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { cerrarSesion() }
                            )
                            "Productos / Stock" -> ProductScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Gastos e ingresos" -> PymeFinanceScreen(
                                onBack = { currentScreen = "" }
                            )
                            "Proveedores" -> SupplierScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Empleados" -> EmployeeScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Agenda de Tareas" -> TareasScreen(
                                onBack = { currentScreen = "" }
                            )
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL RESTAURACIÓN ---
                    perfil == "Restauración" -> {
                        when (currentScreen) {
                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { cerrarSesion() }
                            )
                            "Stock de cocina" -> ProductScreen(
                                profile = "Restauración",
                                onBack = { currentScreen = "" }
                            )
                            "Proveedores" -> SupplierScreen(
                                profile = "Restauración",
                                onBack = { currentScreen = "" }
                            )
                            else -> currentScreen = ""
                        }
                    }

                    // Perfil no reconocido
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Perfil no reconocido: $perfil")
                        }
                    }
                }
            }
        }
    }
}