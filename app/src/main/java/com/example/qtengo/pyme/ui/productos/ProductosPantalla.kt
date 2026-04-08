package com.example.qtengo.pyme.ui.productos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.core.ui.components.QtengoTopBar

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
    var filtersExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
                (!filterByLowStock || it.quantity <= it.minStock)
    }.let { list ->
        if (sortBy == "Nombre") list.sortedBy { it.name }
        else list.sortedByDescending { it.quantity }
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

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF1A3A6B))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Búsqueda y Filtros" else "Buscando: $searchQuery",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A3A6B)
                        )
                    }
                    Icon(if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }

                AnimatedVisibility(visible = filtersExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar producto...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = sortBy == "Nombre", onClick = { sortBy = "Nombre" }, label = { Text("Nombre") })
                            FilterChip(selected = sortBy == "Cantidad", onClick = { sortBy = "Cantidad" }, label = { Text("Cantidad") })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = filterByLowStock, onCheckedChange = { filterByLowStock = it })
                            Text("Solo productos con poco stock", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResumenCard(title = "Total Productos", value = "${products.size}", color = Color(0xFF1A3A6B), modifier = Modifier.weight(1f))
            ResumenCard(title = "Poco Stock", value = "${lowStockProducts.size}", color = if (lowStockProducts.isNotEmpty()) Color(0xFFD32F2F) else Color(0xFF388E3C), modifier = Modifier.weight(1f))
        }

        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No se encontraron productos", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductoItemCard(product = product, onUpdateQuantity = { viewModel.updateQuantity(product, it) }, onDelete = { viewModel.delete(product) })
                }
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
        DialogoAnadirProducto(
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.insert(it.copy(profile = profile))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ResumenCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ProductoItemCard(product: Product, onUpdateQuantity: (Double) -> Unit, onDelete: () -> Unit) {
    val isLowStock = product.quantity <= product.minStock
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE3F2FD), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (isLowStock) "⚠️" else "📦", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    Text(text = product.category, fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${product.quantity.toInt()} ${product.unit}", 
                    fontWeight = FontWeight.Bold, 
                    color = if (isLowStock) Color.Red else Color.Black,
                    modifier = Modifier.clickable { showEditDialog = true }
                )
                Row {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQuantity(product.quantity - 1) }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                    IconButton(onClick = { onUpdateQuantity(product.quantity + 1) }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                }
            }
        }
    }

    if (showEditDialog) {
        var inputVal by remember { mutableStateOf(product.quantity.toString()) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ajustar Stock") },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) inputVal = filtered
                    },
                    label = { Text("Nueva cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = { 
                    onUpdateQuantity(inputVal.toDoubleOrNull() ?: product.quantity)
                    showEditDialog = false
                }) { Text("Actualizar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAnadirProducto(onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val unitOptions = listOf("Uds", "kg", "litros", "paquetes", "cajas")
    var selectedUnit by remember { mutableStateOf(unitOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                // Restricción: Solo positivos y decimales
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) quantity = filtered
                    },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) minStock = filtered
                    },
                    label = { Text("Stock Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && q >= 0) {
                    onConfirm(Product(
                        name = name,
                        quantity = q,
                        minStock = minStock.toDoubleOrNull() ?: 0.0,
                        category = category,
                        unit = selectedUnit
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
