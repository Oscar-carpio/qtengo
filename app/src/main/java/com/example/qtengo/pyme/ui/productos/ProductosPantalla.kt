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
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.components.FiltrosProductosPyme
import com.example.qtengo.pyme.ui.productos.components.DialogoEdicionProducto
import com.example.qtengo.pyme.ui.productos.components.ProductoItemCard
import com.example.qtengo.pyme.ui.productos.components.SummaryCard

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

    var searchQuery by remember { mutableStateOf("") }
    var filterByLowStock by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nombre") }
    var isAscending by remember { mutableStateOf(true) }
    var selectedUnits by remember { mutableStateOf(setOf<String>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
                (!filterByLowStock || it.quantity <= it.minStock) &&
                (selectedUnits.isEmpty() || selectedUnits.contains(it.unit))
    }.sortedWith { p1, p2 ->
        val low1 = p1.quantity <= p1.minStock
        val low2 = p2.quantity <= p2.minStock
        if (low1 != low2) {
            if (low1) -1 else 1
        } else {
            val res = when (sortBy) {
                "Nombre" -> p1.name.compareTo(p2.name, ignoreCase = true)
                "Cantidad" -> p1.quantity.compareTo(p2.quantity)
                else -> 0
            }
            if (isAscending) res else -res
        }
    }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
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

        // Filtro Unificado de Productos
        FiltrosProductosPyme(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { s, a -> sortBy = s; isAscending = a },
            selectedUnits = selectedUnits,
            onUnitsChange = { selectedUnits = it },
            filterByLowStock = filterByLowStock,
            onLowStockChange = { filterByLowStock = it }
        )

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(title = "Total Items", value = "${products.size}", color = Color(0xFF1A3A6B), modifier = Modifier.weight(1f))
            SummaryCard(title = "Stock Bajo", value = "${lowStockProducts.size}", color = if (lowStockProducts.isNotEmpty()) Color(0xFFD32F2F) else Color(0xFF388E3C), modifier = Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProducts) { product ->
                ProductoItemCard(
                    product = product, 
                    onUpdateQuantity = { viewModel.updateQuantity(product, it) }, 
                    onDelete = { viewModel.delete(product) },
                    onEdit = { productToEdit = it }
                )
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir al Almacén")
        }
    }

    if (showAddDialog) {
        DialogoEdicionProducto(
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.insert(it.copy(profile = profile))
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        DialogoEdicionProducto(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { 
                viewModel.update(it)
                productToEdit = null
            }
        )
    }
}
