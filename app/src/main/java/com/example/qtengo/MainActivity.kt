package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.qtengo.data.model.User
import com.example.qtengo.ui.auth.LoginScreen
import com.example.qtengo.ui.auth.RegisterScreen
import com.example.qtengo.ui.screens.FamiliarHomeScreen
import com.example.qtengo.ui.screens.PymeHomeScreen
import com.example.qtengo.ui.screens.RestauracionHomeScreen
import com.example.qtengo.ui.screens.ShoppingList
import com.example.qtengo.ui.screens.ShoppingListScreen
import com.example.qtengo.ui.screens.SplashScreen
import com.example.qtengo.ui.screens.*
import com.example.qtengo.ui.theme.QtengoTheme
import com.example.qtengo.ui.products.ProductScreen

/**
 * Actividad principal que gestiona la navegación de la aplicación Q-Tengo.
 * Organizada por perfiles: Familiar, Pyme y Restauración.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                when {
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                    usuarioLogueado == null && mostrarRegistro -> RegisterScreen(
                        onRegistroExitoso = {
                            usuarioLogueado = it
                            mostrarRegistro = false
                        },
                        onIrALogin = { mostrarRegistro = false }
                    )
                    usuarioLogueado == null -> LoginScreen(
                        onLoginExitoso = { usuarioLogueado = it },
                        onIrARegistro = { mostrarRegistro = true }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen.isEmpty() -> FamiliarHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = { usuarioLogueado = null }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen == "Lista de la compra" && selectedShoppingList.value == null -> ShoppingListScreen(
                        onListSelected = { selectedShoppingList.value = it },
                        onBack = { currentScreen = "" }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen == "Lista de la compra" && selectedShoppingList.value != null -> ShoppingListDetailScreen(
                        shoppingList = selectedShoppingList.value!!,
                        onBack = { selectedShoppingList.value = null }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen == "Control de gastos" && !showAddGasto -> GastosScreen(
                        onAddGasto = { showAddGasto = true },
                        onBack = { currentScreen = "" }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen == "Control de gastos" && showAddGasto -> AddGastoScreen(
                        onGastoGuardado = { showAddGasto = false },
                        onBack = { showAddGasto = false }
                    )
                    usuarioLogueado!!.perfil == "Familiar" && currentScreen == "Inventario del hogar" && !showAddInventario -> InventarioScreen(
                        onAddItem = { showAddInventario = true },
                        onBack = { currentScreen = "" }
                    )
                    usuarioLogueado!!.perfil == "Pyme" && currentScreen.isEmpty() -> PymeHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = {
                            usuarioLogueado = null
                            currentScreen = ""
                        }
                    )
                    usuarioLogueado!!.perfil == "Restauración" && currentScreen.isEmpty() -> RestauracionHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = {
                            usuarioLogueado = null
                            currentScreen = ""
                        }
                    )
                }
            }
        }
    }
}
