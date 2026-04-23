/**
 * Pantalla de Control de Stock e Inventario.
 */
package com.example.qtengo.pyme.ui.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.pyme.ui.DialogoConfirmarEliminar
import com.example.qtengo.pyme.ui.TarjetaEstadisticaPyme
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.filtros.FiltrosProductos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosPantalla(
    profile: String = "PYME",
    viewModel: ProductosViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val products by viewModel.products.observeAsState(emptyList())
    val lowStockProducts by viewModel.lowStockProducts.observeAsState(emptyList())

    val searchQuery = remember { mutableStateOf("") }
    val filterByLowStock = remember { mutableStateOf(false) }
    val sortBy = remember { mutableStateOf("") }
    val isAscending = remember { mutableStateOf(true) }
    val selectedUnits = remember { mutableStateOf(setOf<String>()) }
    val showAddDialog = remember { mutableStateOf(false) }
    val productToEdit = remember { mutableStateOf<Product?>(null) }
    val productToDelete = remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.name.contains(searchQuery.value, ignoreCase = true)
        val matchesLowStock = !filterByLowStock.value || product.quantity <= product.minStock
        val matchesUnits = selectedUnits.value.isEmpty() || selectedUnits.value.contains(product.unit)
        matchesSearch && matchesLowStock && matchesUnits
    }.let { list ->
        if (sortBy.value == "") {
            // Orden por defecto: el último creado arriba
            list.sortedByDescending { it.timestamp }
        } else {
            list.sortedWith { p1, p2 ->
                val low1 = p1.quantity <= p1.minStock
                val low2 = p2.quantity <= p2.minStock
                if (low1 != low2) {
                    if (low1) -1 else 1
                } else {
                    val res = when (sortBy.value) {
                        "Nombre" -> p1.name.compareTo(p2.name, ignoreCase = true)
                        "Cantidad" -> p1.quantity.compareTo(p2.quantity)
                        else -> 0
                    }
                    if (isAscending.value) res else -res
                }
            }
        }
    }

    LaunchedEffect(profile) {
        viewModel.cargarPerfil(profile)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Control de Stock",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        FiltrosProductos(
            searchQuery = searchQuery.value,
            onSearchChange = { searchQuery.value = it },
            sortBy = sortBy.value,
            isAscending = isAscending.value,
            onSortChange = { s, a -> sortBy.value = s; isAscending.value = a },
            selectedUnits = selectedUnits.value,
            onUnitsChange = { selectedUnits.value = it },
            filterByLowStock = filterByLowStock.value,
            onLowStockChange = { filterByLowStock.value = it }
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaEstadisticaPyme(titulo = "Total Items", valor = "${products.size}", color = Color(0xFF1A3A6B), modifier = Modifier.weight(1f))
            TarjetaEstadisticaPyme(titulo = "Stock Bajo", valor = "${lowStockProducts.size}", color = if (lowStockProducts.isNotEmpty()) Color(0xFFD32F2F) else Color(0xFF388E3C), modifier = Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProducts) { product ->
                ElementoTarjetaProducto(
                    producto = product, 
                    onActualizarCantidad = { viewModel.actualizarCantidad(product, it) },
                    onEliminar = { productToDelete.value = product },
                    onEditar = { productToEdit.value = it }
                )
            }
        }

        Button(
            onClick = { showAddDialog.value = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B), contentColor = Color.White)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Producto", color = Color.White)
        }
    }

    if (showAddDialog.value) {
        DialogoEdicionProducto(
            onDismiss = { showAddDialog.value = false },
            onConfirm = {
                viewModel.insertar(it.copy(profile = profile))
                showAddDialog.value = false
            }
        )
    }

    val pToEdit = productToEdit.value
    if (pToEdit != null) {
        DialogoEdicionProducto(
            product = pToEdit,
            onDismiss = { productToEdit.value = null },
            onConfirm = {
                viewModel.actualizar(it)
                productToEdit.value = null
            }
        )
    }

    val pToDelete = productToDelete.value
    if (pToDelete != null) {
        DialogoConfirmarEliminar(
            titulo = "Confirmar eliminación",
            mensaje = "¿Estás seguro de que deseas eliminar el producto ${pToDelete.name}?",
            onConfirmar = {
                viewModel.eliminar(pToDelete)
                productToDelete.value = null
            },
            onDescartar = { productToDelete.value = null }
        )
    }
}
