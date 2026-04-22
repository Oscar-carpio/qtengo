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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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



import com.example.qtengo.pyme.ui.inicio.PymeInicioPantalla
import com.example.qtengo.pyme.ui.finanzas.FinanzasPantalla
import com.example.qtengo.pyme.ui.empleados.EmpleadosPantalla
import com.example.qtengo.pyme.ui.proveedores.ProveedoresPantalla
import com.example.qtengo.pyme.ui.productos.ProductosPantalla
import com.example.qtengo.pyme.ui.tareas.TareasPantalla

import com.example.qtengo.core.ui.screens.SplashScreen
import com.example.qtengo.core.ui.theme.QtengoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.qtengo.restauracion.ui.carta.CartaScreen
import com.example.qtengo.restauracion.ui.stock.StockCocinaScreen
import com.example.qtengo.restauracion.ui.reservas.ReservasScreen
import com.example.qtengo.restauracion.ui.proveedores.ProveedoresRestauracionScreen
import com.example.qtengo.restauracion.ui.home.RestauracionHomeScreen
import com.example.qtengo.restauracion.ui.reservas.RestauracionReserva
import com.example.qtengo.restauracion.ui.proveedores.RestauracionProveedor




/**
 * Actividad principal que gestiona la navegación de la aplicación Q-Tengo.
 * Organizada por perfiles: Familiar, Pyme y Restauración.
 * Un usuario puede tener varios perfiles activos — se elige uno al iniciar sesión.
 */
class MainActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QtengoTheme {

                var uid by remember { mutableStateOf<String?>(null) }

                // Lista completa de perfiles del usuario
                var perfiles by remember { mutableStateOf<List<String>>(emptyList()) }

                // Perfil activo en esta sesión (el que el usuario seleccionó)
                var perfilActivo by remember { mutableStateOf<String?>(null) }

                var mostrarRegistro by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("") }
                val selectedShoppingList = remember { mutableStateOf<ShoppingList?>(null) }
                var showAddGasto by remember { mutableStateOf(false) }
                var showAddInventario by remember { mutableStateOf(false) }

                // --- Permiso de notificaciones (Android 13+) ---
                val permisoConcedido = remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { concedido -> permisoConcedido.value = concedido }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permisoConcedido.value) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // --- Fin permiso ---

                // Comprobar sesión activa al arrancar
                LaunchedEffect(Unit) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        try {
                            val doc = firestore.collection("usuarios")
                                .document(currentUser.uid)
                                .get()
                                .await()

                            // Compatibilidad: leer "perfiles" (nuevo) o "perfil" (antiguo)
                            @Suppress("UNCHECKED_CAST")
                            val perfilesRecuperados: List<String> = when {
                                doc.get("perfiles") != null ->
                                    (doc.get("perfiles") as? List<String>) ?: emptyList()
                                doc.getString("perfil") != null ->
                                    listOf(doc.getString("perfil")!!)
                                else -> emptyList()
                            }

                            if (perfilesRecuperados.isNotEmpty()) {
                                uid = currentUser.uid
                                perfiles = perfilesRecuperados
                                // Si solo tiene un perfil, lo activamos directamente
                                if (perfilesRecuperados.size == 1) {
                                    perfilActivo = perfilesRecuperados.first()
                                }
                            } else {
                                auth.signOut()
                            }
                        } catch (e: Exception) {
                            auth.signOut()
                        }
                    }

                }

                // Cierra sesión y resetea todo el estado
                fun cerrarSesion() {
                    auth.signOut()
                    uid = null
                    perfiles = emptyList()
                    perfilActivo = null
                    currentScreen = ""
                }

                // Vuelve al selector de perfil sin cerrar sesión
                fun cambiarPerfil() {
                    perfilActivo = null
                    currentScreen = ""
                }

                when {
                    uid == null && mostrarRegistro -> RegisterScreen(
                        onRegistroExitoso = { nuevoUid, nuevosPerfiles ->
                            uid = nuevoUid
                            perfiles = nuevosPerfiles
                            mostrarRegistro = false
                            // Si solo tiene un perfil, lo activamos directamente
                            if (nuevosPerfiles.size == 1) {
                                perfilActivo = nuevosPerfiles.first()
                            }
                        },
                        onIrALogin = { mostrarRegistro = false }
                    )

                    uid == null -> LoginScreen(
                        onLoginExitoso = { nuevoUid, nuevosPerfiles ->
                            uid = nuevoUid
                            perfiles = nuevosPerfiles
                            // Si solo tiene un perfil, lo activamos directamente
                            if (nuevosPerfiles.size == 1) {
                                perfilActivo = nuevosPerfiles.first()
                            }
                        },
                        onIrARegistro = { mostrarRegistro = true }
                    )

                    // Usuario autenticado pero aún sin elegir perfil activo → mostrar selector
                    uid != null && perfilActivo == null -> {
                        SelectorPerfilScreen(
                            perfiles = perfiles,
                            onPerfilSeleccionado = { perfilActivo = it },
                            onCerrarSesion = { cerrarSesion() }
                        )
                    }

                    // --- PERFIL FAMILIAR ---
                    perfilActivo == "Familiar" -> {
                        when (currentScreen) {
                            "" -> FamiliarHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
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
                    perfilActivo == "Pyme" -> {
                        when (currentScreen) {
                            "" -> PymeInicioPantalla(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Productos / Stock" -> ProductosPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Gastos e ingresos" -> FinanzasPantalla(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Proveedores" -> ProveedoresPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Empleados" -> EmpleadosPantalla(
                                profile = "PYME",
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Agenda de Tareas" -> TareasPantalla(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            else -> currentScreen = ""
                        }
                    }

                    // --- PERFIL RESTAURACIÓN ---
                    perfilActivo == "Restauración" -> {
                        when (currentScreen) {
                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Carta / Menú del día" -> CartaScreen(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Stock de cocina" -> StockCocinaScreen(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            "Reservas" -> ReservasScreen(
                                onBack = { currentScreen = "" }
                            )
                            "Proveedores" -> ProveedoresRestauracionScreen(
                                onBack = { currentScreen = "" },
                                onLogout = { cerrarSesion() },
                                onChangeProfile = { cambiarPerfil() }
                            )
                            else -> currentScreen = ""
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Perfil no reconocido: $perfilActivo")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pantalla de selección de perfil activo.
 * Se muestra cuando el usuario tiene más de un perfil registrado.
 * Si solo tiene uno, se salta automáticamente desde MainActivity.
 */
@Composable
fun SelectorPerfilScreen(
    perfiles: List<String>,
    onPerfilSeleccionado: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Con qué perfil quieres entrar?",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Puedes cambiar de perfil en cualquier momento desde el menú",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        perfiles.forEach { perfil ->
            Button(
                onClick = { onPerfilSeleccionado(perfil) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(perfil, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}
