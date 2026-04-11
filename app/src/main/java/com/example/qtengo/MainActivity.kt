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

// --- AUTH ---
import com.example.qtengo.login.ui.LoginScreen
import com.example.qtengo.login.ui.RegisterScreen

// --- FAMILIAR ---
import com.example.qtengo.familiar.ui.FamiliarHomeScreen
import com.example.qtengo.familiar.ui.compra.*
import com.example.qtengo.familiar.ui.gastos.*
import com.example.qtengo.familiar.ui.inventario.*
import com.example.qtengo.familiar.ui.tareas.TareasScreen

// --- PYME ---
import com.example.qtengo.pyme.ui.inicio.PymeInicioPantalla
import com.example.qtengo.pyme.ui.finanzas.FinanzasPantalla
import com.example.qtengo.pyme.ui.empleados.EmpleadosPantalla
import com.example.qtengo.pyme.ui.proveedores.ProveedoresPantalla
import com.example.qtengo.pyme.ui.productos.ProductosPantalla
import com.example.qtengo.pyme.ui.tareas.TareasPantalla

// --- RESTAURACIÓN ---
import com.example.qtengo.restauracion.ui.home.RestauracionHomeScreen
import com.example.qtengo.restauracion.ui.menu.MenuDiaScreen
import com.example.qtengo.restauracion.ui.inventario.InventarioRestauracionScreen
import com.example.qtengo.restauracion.ui.reservas.ReservasScreen
import com.example.qtengo.restauracion.ui.proveedores.RestauracionProveedoresScreen

// --- CORE ---
import com.example.qtengo.core.ui.screens.SplashScreen
import com.example.qtengo.core.ui.theme.QtengoTheme

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
                                uid = currentUser.uid
                                perfil = perfilRecuperado
                            } else {
                                auth.signOut()
                            }
                        } catch (e: Exception) {
                            auth.signOut()
                        }
                    }
                    showSplash = false
                }

                fun cerrarSesion() {
                    auth.signOut()
                    uid = null
                    perfil = null
                    currentScreen = ""
                }

                when {
                    showSplash -> SplashScreen { showSplash = false }

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

                    perfil == null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    // ---------------- FAMILIAR ----------------
                    perfil == "Familiar" -> {
                        when (currentScreen) {
                            "" -> FamiliarHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
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

                    // ---------------- PYME ----------------
                    perfil == "Pyme" -> {
                        when (currentScreen) {

                            "" -> PymeInicioPantalla(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            "Productos / Stock" -> ProductosPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            "Gastos e ingresos" -> FinanzasPantalla(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            "Proveedores" -> ProveedoresPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            "Empleados" -> EmpleadosPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            "Agenda de Tareas" -> TareasPantalla(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = {}
                            )

                            else -> currentScreen = ""
                        }
                    }

                    // ---------------- RESTAURACIÓN ----------------
                    perfil == "Restauración" -> {
                        when (currentScreen) {

                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { cerrarSesion() }
                            )

                            "Carta / Menú del día" -> MenuDiaScreen(
                                onBack = { currentScreen = "" }
                            )

                            "Stock de cocina" -> InventarioRestauracionScreen(
                                onBack = { currentScreen = "" }
                            )

                            "Reservas" -> ReservasScreen(
                                onBack = { currentScreen = "" }
                            )

                            "Proveedores" -> RestauracionProveedoresScreen(
                                onBack = { currentScreen = "" }
                            )

                            else -> currentScreen = ""
                        }
                    }

                    else -> Box(
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