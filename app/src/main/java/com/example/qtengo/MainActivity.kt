package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.qtengo.ui.screens.*
import com.example.qtengo.ui.theme.QtengoTheme
import com.example.qtengo.ui.products.ProductScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QtengoTheme {
                var showSplash by remember { mutableStateOf(true) }
                var selectedProfile by remember { mutableStateOf("") }
                var currentScreen by remember { mutableStateOf("") }
                val selectedShoppingList = remember { mutableStateOf<ShoppingList?>(null) }
                var showAddGasto by remember { mutableStateOf(false) }
                var showAddInventario by remember { mutableStateOf(false) }

                when {
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                    selectedProfile.isEmpty() -> ProfileScreen(
                        onProfileSelected = { selectedProfile = it }
                    )
                    
                    // PERFIL FAMILIAR
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

                    // PERFIL PYME
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
                            else -> PymeHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { selectedProfile = "" }
                            )
                        }
                    }

                    // PERFIL RESTAURACIÓN
                    selectedProfile == "Restauración" -> {
                        when (currentScreen) {
                            "" -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { selectedProfile = "" }
                            )
                            "Stock de cocina" -> ProductScreen(
                                profile = "HOSTELERIA",
                                onBack = { currentScreen = "" }
                            )
                            "Carta / Menú del día" -> DishScreen(
                                profile = "HOSTELERIA",
                                onBack = { currentScreen = "" }
                            )
                            "Proveedores" -> SupplierScreen(
                                profile = "HOSTELERIA",
                                onBack = { currentScreen = "" }
                            )
                            else -> RestauracionHomeScreen(
                                onMenuSelected = { currentScreen = it },
                                onBack = { selectedProfile = "" }
                            )
                        }
                    }
                }
            }
        }
    }
}
