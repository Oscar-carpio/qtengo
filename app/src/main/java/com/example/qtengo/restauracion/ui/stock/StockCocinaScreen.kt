package com.example.qtengo.restauracion.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── DATA CLASS ──────────────────────────────────────────────────────────────

data class StockCocina(
    val id: String = "",
    val nombre: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = "kg",
    val categoria: String = "Carnes",
    val minStock: Double = 1.0
)

// ─── VIEWMODEL ───────────────────────────────────────────────────────────────

class StockCocinaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _stock = MutableStateFlow<List<StockCocina>>(emptyList())
    val stock: StateFlow<List<StockCocina>> = _stock

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var stockListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    private fun requireUid(): String? {
        if (uid.isBlank()) { _error.value = "Usuario no autenticado"; return null }
        return uid
    }

    private fun stockRef() = db.collection("usuarios").document(uid).collection("restauracion_stock")

    fun cargarStock() {
        val uid = requireUid() ?: return
        stockListener?.remove()
        stockListener = db.collection("usuarios").document(uid).collection("restauracion_stock")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { _error.value = "Error al cargar stock: ${e.message}"; return@addSnapshotListener }
                _stock.value = snapshot?.documents?.map { doc ->
                    StockCocina(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        cantidad = doc.getDouble("cantidad") ?: 0.0,
                        unidad = doc.getString("unidad") ?: "kg",
                        categoria = doc.getString("categoria") ?: "Carnes",
                        minStock = doc.getDouble("minStock") ?: 1.0
                    )
                } ?: emptyList()
            }
    }

    fun añadirProducto(producto: StockCocina) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to producto.nombre,
                    "cantidad" to producto.cantidad,
                    "unidad" to producto.unidad,
                    "categoria" to producto.categoria,
                    "minStock" to producto.minStock
                )
                stockRef().add(data).await()
            } catch (e: Exception) { _error.value = "Error al añadir producto: ${e.message}" }
        }
    }

    fun actualizarCantidad(productoId: String, nuevaCantidad: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            try { stockRef().document(productoId).update("cantidad", nuevaCantidad).await() }
            catch (e: Exception) { _error.value = "Error al actualizar cantidad: ${e.message}" }
        }
    }

    fun eliminarProducto(productoId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            try { stockRef().document(productoId).delete().await() }
            catch (e: Exception) { _error.value = "Error al eliminar producto: ${e.message}" }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stockListener?.remove()
    }
}

// ─── SCREEN ──────────────────────────────────────────────────────────────────

@Composable
fun StockCocinaScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: StockCocinaViewModel = viewModel()
) {
    val stock by viewModel.stock.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoriaFiltro by remember { mutableStateOf("Todos") }

    val categorias = listOf("Todos", "Carnes", "Pescados", "Verduras", "Bebidas", "Otros")
    val stockFiltrado = if (categoriaFiltro == "Todos") stock else stock.filter { it.categoria == categoriaFiltro }
    val bajosDeStock = stock.count { it.cantidad <= it.minStock }

    LaunchedEffect(Unit) { viewModel.cargarStock() }

    if (showAddDialog) {
        AddStockDialog(
            onConfirm = { viewModel.añadirProducto(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Stock de cocina",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A6B))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stock.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Productos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (bajosDeStock > 0) Color(0xFFD32F2F) else Color(0xFF388E3C))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$bajosDeStock", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Bajo mínimos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = categorias.indexOf(categoriaFiltro),
            containerColor = Color.White,
            contentColor = Color(0xFF1A3A6B),
            edgePadding = 16.dp
        ) {
            categorias.forEach { cat ->
                Tab(
                    selected = categoriaFiltro == cat,
                    onClick = { categoriaFiltro = cat },
                    text = { Text(cat, fontSize = 13.sp) }
                )
            }
        }

        if (stockFiltrado.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay productos en esta categoría", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stockFiltrado) { producto ->
                    StockProductoCard(
                        producto = producto,
                        onIncrementar = { viewModel.actualizarCantidad(producto.id, producto.cantidad + 1) },
                        onDecrementar = { if (producto.cantidad > 0) viewModel.actualizarCantidad(producto.id, producto.cantidad - 1) },
                        onDelete = { viewModel.eliminarProducto(producto.id) }
                    )
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir producto")
        }
    }
}

@Composable
fun StockProductoCard(
    producto: StockCocina,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onDelete: () -> Unit
) {
    val bajoDeMínimos = producto.cantidad <= producto.minStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (bajoDeMínimos) Color(0xFFFFF3E0) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Text(producto.categoria, fontSize = 12.sp, color = Color.Gray)
                if (bajoDeMínimos) {
                    Text(
                        "⚠️ Stock bajo (mín. ${producto.minStock.toInt()} ${producto.unidad})",
                        fontSize = 11.sp,
                        color = Color(0xFFF57C00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDecrementar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF1A3A6B))
                }
                Surface(
                    color = if (bajoDeMínimos) Color(0xFFD32F2F) else Color(0xFF1A3A6B),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${producto.cantidad.toInt()} ${producto.unidad}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onIncrementar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF1A3A6B))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun AddStockDialog(onConfirm: (StockCocina) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("1") }
    var unidadSeleccionada by remember { mutableStateOf("kg") }
    var categoriaSeleccionada by remember { mutableStateOf("Carnes") }
    var errorNombre by remember { mutableStateOf("") }

    val unidades = listOf("kg", "litros", "uds", "raciones", "cajas")
    val categorias = listOf("Carnes", "Pescados", "Verduras", "Bebidas", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty())
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Mínimo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Text("Unidad", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    unidades.forEach { unidad ->
                        FilterChip(
                            selected = unidadSeleccionada == unidad,
                            onClick = { unidadSeleccionada = unidad },
                            label = { Text(unidad, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1A3A6B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text("Categoría", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                categorias.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { categoriaSeleccionada = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A3A6B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isBlank()) { errorNombre = "El nombre es obligatorio"; return@TextButton }
                onConfirm(StockCocina(
                    nombre = nombre.trim(),
                    cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                    unidad = unidadSeleccionada,
                    categoria = categoriaSeleccionada,
                    minStock = minStock.toDoubleOrNull() ?: 1.0
                ))
            }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}