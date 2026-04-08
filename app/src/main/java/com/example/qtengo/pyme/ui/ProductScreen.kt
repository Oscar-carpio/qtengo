package com.example.qtengo.pyme.ui

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
fun ProductScreen(
    profile: String = "PYME",
    viewModel: ProductViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val products by viewModel.products.observeAsState(emptyList())
    val lowStockProducts by viewModel.lowStockProducts.observeAsState(emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var filterByLowStock by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Nombre") } // "Nombre" o "Cantidad"
    var filtersExpanded by remember { mutableStateOf(false) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) &&
                (!filterByLowStock || it.quantity <= it.minStock)
    }.let { list ->
        if (sortBy == "Nombre") list.sortedBy { it.name }
        else list.sortedByDescending { it.quantity }
    }

    // Indicadores solicitados: Cantidad de objetos distintos
    val totalDistinctProducts = products.size
    val totalLowStockDistinct = lowStockProducts.size
    
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Control de Stock",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Filtros Desplegables para ahorrar espacio
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF1A3A6B))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Búsqueda y Filtros" else "Buscando: $searchQuery",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A3A6B)
                        )
                    }
                    Icon(
                        imageVector = if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = filtersExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar producto...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        )
                        
                        Spacer(Modifier.height(12.dp))

                        Text("Ordenar por:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = sortBy == "Nombre",
                                onClick = { sortBy = "Nombre" },
                                label = { Text("Nombre") }
                            )
                            FilterChip(
                                selected = sortBy == "Cantidad",
                                onClick = { sortBy = "Cantidad" },
                                label = { Text("Cantidad") }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = filterByLowStock,
                                onCheckedChange = { filterByLowStock = it }
                            )
                            Text("Solo productos con poco stock", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Fila de resumen con nombres fáciles de entender
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Total Productos",
                value = "$totalDistinctProducts",
                color = Color(0xFF1A3A6B),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Poco Stock",
                value = "$totalLowStockDistinct",
                color = if (totalLowStockDistinct > 0) Color(0xFFD32F2F) else Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }

        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No se encontraron productos", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductItemCard(
                        product = product,
                        onUpdateQuantity = { viewModel.updateQuantity(product, it) },
                        onDelete = { viewModel.delete(product) }
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
            Text(text = "Añadir al Almacén")
        }

        if (showAddDialog) {
            AddProductDialog(
                profile = profile,
                onDismiss = { showAddDialog = false },
                onConfirm = { viewModel.insert(it) }
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ProductItemCard(product: Product, onUpdateQuantity: (Double) -> Unit, onDelete: () -> Unit) {
    val isLowStock = product.quantity <= product.minStock
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE3F2FD), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isLowStock) "⚠️" else "📦", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    Text(text = product.category, fontSize = 12.sp, color = Color.Gray)
                }

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Mínimo: ${product.minStock.toInt()} ${product.unit}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQuantity(product.quantity - 1) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF1A3A6B))
                    }

                    Surface(
                        modifier = Modifier.clickable { showEditDialog = true },
                        color = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF1A3A6B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${product.quantity.toInt()} ${product.unit}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { onUpdateQuantity(product.quantity + 1) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF1A3A6B))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var inputQuantity by remember { mutableStateOf(product.quantity.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ajustar Stock: ${product.name}") },
            text = {
                OutlinedTextField(
                    value = inputQuantity,
                    onValueChange = { if (it.all { c -> c.isDigit() }) inputQuantity = it },
                    label = { Text("Cantidad actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateQuantity(inputQuantity.toDoubleOrNull() ?: product.quantity)
                    showEditDialog = false
                }) { Text("Actualizar") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Se borrará '${product.name}' permanentemente del inventario.") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(profile: String, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val unitOptions = listOf("Uds", "kg", "litros", "paquetes", "cajas")
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(unitOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (Obligatorio)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) quantity = it },
                        label = { Text("Cant.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            unitOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedUnit = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) minStock = it },
                    label = { Text("Stock Mínimo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría / Ubicación") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && quantity.isNotBlank()) {
                        onConfirm(Product(
                            name = name,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            minStock = minStock.toDoubleOrNull() ?: 0.0,
                            category = category,
                            profile = profile,
                            unit = selectedUnit,
                            notes = notes
                        ))
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
