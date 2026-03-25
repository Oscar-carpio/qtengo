package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
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
                var selectedProfile by remember { mutableStateOf("") }
                var currentScreen by remember { mutableStateOf("") }
                val selectedShoppingList = remember { mutableStateOf<ShoppingList?>(null) }
                
                // Estados para diálogos o pantallas secundarias
                var showAddGasto by remember { mutableStateOf(false) }
                var showAddInventario by remember { mutableStateOf(false) }

                when {
                    // 1. Pantalla de Bienvenida
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                    
                    // 2. Selección de Perfil
                    selectedProfile.isEmpty() -> ProfileScreen(
                        onProfileSelected = { selectedProfile = it }
                    )

                    // 3. Flujo PERFIL FAMILIAR
                    selectedProfile == "Familiar" -> {
                        when (currentScreen) {
                            "" -> FamiliarHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { selectedProfile = "" }
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
                                        onItemGuardado = { showAddInventario = false },
                                        onBack = { showAddInventario = false }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Flujo PERFIL PYME (Completado con persistencia Room)
                    selectedProfile == "Pyme" -> {
                        when (currentScreen) {
                            "" -> PymeHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { selectedProfile = "" }
                            )
                            "Productos / Stock" -> ProductScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Gastos e ingresos" -> {
                                if (!showAddGasto) {
                                    GastosScreen(
                                        profile = "PYME",
                                        onAddGasto = { showAddGasto = true },
                                        onBack = { currentScreen = "" }
                                    )
                                } else {
                                    AddGastoScreen(
                                        profile = "PYME",
                                        onGastoGuardado = { showAddGasto = false },
                                        onBack = { showAddGasto = false }
                                    )
                                }
                            }
                            "Proveedores" -> SupplierScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Empleados" -> EmployeeScreen(
                                profile = "PYME",
                                onBack = { currentScreen = "" }
                            )
                            "Agenda de Tareas" -> TaskScreen(
                                onBack = { currentScreen = "" }
                            )
                        }
                    }

                    // 5. Perfil Restauración
                    selectedProfile == "Restauración" -> {
                        RestauracionHomeScreen(
                            onMenuSelected = { currentScreen = it },
                            onBack = { selectedProfile = "" }
                        )
                    }
                }
            }
        }
    }
}
