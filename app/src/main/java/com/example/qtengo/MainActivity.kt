package com.example.qtengo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.qtengo.login.ui.LoginScreen
import com.example.qtengo.login.ui.RegisterScreen
import com.example.qtengo.familiar.ui.FamiliarHomeScreen
import com.example.qtengo.familiar.ui.compra.ShoppingListScreen
import com.example.qtengo.familiar.ui.compra.ShoppingListDetailScreen
import com.example.qtengo.familiar.ui.compra.ShoppingList
import com.example.qtengo.familiar.ui.gastos.GastosScreen
import com.example.qtengo.familiar.ui.gastos.AddGastoScreen
import com.example.qtengo.familiar.ui.inventario.InventarioScreen
import com.example.qtengo.familiar.ui.inventario.AddInventarioScreen
import com.example.qtengo.familiar.ui.tareas.TareasScreen
import com.example.qtengo.pyme.ui.PymeHomeScreen
import com.example.qtengo.restauracion.ui.RestauracionHomeScreen
import com.example.qtengo.pyme.ui.PymeFinanceScreen
import com.example.qtengo.pyme.ui.EmployeeScreen
import com.example.qtengo.pyme.ui.SupplierScreen
import com.example.qtengo.pyme.ui.ProductScreen
import com.example.qtengo.pyme.ui.TaskScreen
import com.example.qtengo.core.ui.screens.SplashScreen
import com.example.qtengo.core.ui.theme.QtengoTheme
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

                // --- Solicitud de permiso de notificaciones (Android 13+) ---
                val permisoConcedido = remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true // En versiones anteriores no hace falta pedir permiso
                        }
                    )
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { concedido ->
                    permisoConcedido.value = concedido
                }

                // Pedir permiso al arrancar si es Android 13+ y no está concedido
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !permisoConcedido.value
                    ) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // --- Fin solicitud permiso ---

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
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
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
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Productos / Stock" -> ProductScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Gastos e ingresos" -> PymeFinanceScreen(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Proveedores" -> SupplierScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Empleados" -> EmployeeScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Agenda de Tareas" -> TaskScreen(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL RESTAURACIÓN ---
                    perfil == "Restauración" -> {
                        when (currentScreen) {
                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Stock de cocina" -> ProductScreen(
                                profile = "Restauración",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
                            )
                            "Proveedores" -> SupplierScreen(
                                profile = "Restauración",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { /* TODO */ }
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