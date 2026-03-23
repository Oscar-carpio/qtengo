package com.example.qtengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import com.example.qtengo.ui.screens.FamiliarHomeScreen
import com.example.qtengo.ui.screens.ProfileScreen
import com.example.qtengo.ui.screens.PymeHomeScreen
import com.example.qtengo.ui.screens.RestauracionHomeScreen
import com.example.qtengo.ui.screens.ShoppingList
import com.example.qtengo.ui.screens.ShoppingListScreen
import com.example.qtengo.ui.screens.SplashScreen
import com.example.qtengo.ui.theme.QtengoTheme
import com.example.qtengo.ui.screens.ShoppingListDetailScreen
import com.example.qtengo.ui.screens.AddGastoScreen
import com.example.qtengo.ui.screens.GastosScreen
import com.example.qtengo.ui.screens.InventarioScreen
import com.example.qtengo.ui.products.ProductScreen
import com.example.qtengo.ui.screens.AddInventarioScreen
import com.example.qtengo.ui.screens.PymeFinanceScreen


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
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
                var showAddInventario by remember {mutableStateOf(false)}


                when {
                    showSplash -> SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                    selectedProfile.isEmpty() -> ProfileScreen(
                        onProfileSelected = { selectedProfile = it }
                    )
                    selectedProfile == "Familiar" && currentScreen.isEmpty() -> FamiliarHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = { selectedProfile = "" }
                    )
                    selectedProfile == "Familiar" && currentScreen == "Lista de la compra" && selectedShoppingList.value == null -> ShoppingListScreen(
                        onListSelected = { selectedShoppingList.value = it },
                        onBack = { currentScreen = "" }
                    )
                    selectedProfile == "Familiar" && currentScreen == "Lista de la compra" && selectedShoppingList.value != null -> ShoppingListDetailScreen(
                        shoppingList = selectedShoppingList.value!!,
                        onBack = { selectedShoppingList.value = null }
                    )
                    selectedProfile == "Familiar" && currentScreen == "Control de gastos" && !showAddGasto -> GastosScreen(
                        onAddGasto = { showAddGasto = true },
                        onBack = { currentScreen = "" }
                    )
                    selectedProfile == "Familiar" && currentScreen == "Control de gastos" && showAddGasto -> AddGastoScreen(
                        onGastoGuardado = { showAddGasto = false },
                        onBack = { showAddGasto = false }
                    )
                    selectedProfile == "Familiar" && currentScreen == "Inventario del hogar" && !showAddInventario -> InventarioScreen(
                        onAddItem = {showAddInventario = true},
                        onBack = {currentScreen=""}
                    )


                    selectedProfile == "Pyme" && currentScreen.isEmpty() -> PymeHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = {
                            selectedProfile = ""
                            currentScreen = ""
                        }
                    )
                    selectedProfile == "Pyme" && currentScreen == "Productos / Stock" -> ProductScreen(
                        profile = "PYME"
                    )
                    selectedProfile == "Pyme" && currentScreen == "Gastos e ingresos" -> PymeFinanceScreen(
                        onBack = { currentScreen = "" }
                    )

                    selectedProfile == "Restauración" && currentScreen.isEmpty() -> RestauracionHomeScreen(
                        onMenuSelected = { currentScreen = it },
                        onBack = {
                            selectedProfile = ""
                            currentScreen = ""
                        }
                    )
                }
            }
        }
    }
}