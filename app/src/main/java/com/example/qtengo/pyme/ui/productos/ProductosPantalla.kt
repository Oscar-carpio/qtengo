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
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.core.ui.components.QtengoTopBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var filtersExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    val unitOptions = listOf("Uds", "kg", "litros", "barriles", "paquetes", "cajas")

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
                        Icon(Icons.Default.Search, null, tint = Color(0xFF1A3A6B))
                        Spacer(Modifier.width(8.dp))
                        Text("Buscador y Filtros", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
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
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { if(searchQuery.isNotEmpty()) IconButton(onClick = {searchQuery = ""}) { Icon(Icons.Default.Close, null) } }
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        Text("Ordenar por:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Nombre", "Cantidad").forEach { option ->
                                FilterChip(
                                    selected = sortBy == option,
                                    onClick = {
                                        if (sortBy == option) isAscending = !isAscending
                                        else { sortBy = option; isAscending = true }
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(option)
                                            if (sortBy == option) {
                                                Spacer(Modifier.width(4.dp))
                                                Icon(
                                                    if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                    null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("Filtrar por Unidades:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            unitOptions.forEach { unit ->
                                val isSelected = selectedUnits.contains(unit)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUnits = if (isSelected) {
                                            selectedUnits - unit
                                        } else {
                                            selectedUnits + unit
                                        }
                                    },
                                    label = { Text(unit) }
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = filterByLowStock, onCheckedChange = { filterByLowStock = it })
                            Text("Ver solo poco stock", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

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

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(70.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ProductoItemCard(product: Product, onUpdateQuantity: (Double) -> Unit, onDelete: () -> Unit, onEdit: (Product) -> Unit) {
    val isLowStock = product.quantity <= product.minStock
    var showQuickQuantityDialog by remember { mutableStateOf(false) }

    val icon = when(product.unit.lowercase()) {
        "uds" -> "🏷️"
        "kg" -> "⚖️"
        "litros" -> "🥤"
        "barriles" -> "🛢️"
        "paquetes" -> "🛍️" // Bolsas
        "cajas" -> "📦" // Caja de cartón
        else -> "🎁"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLowStock) Color(0xFFFFFBFA) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFF1F8E9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (isLowStock) "⚠️" else icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    if (product.category.isNotBlank()) {
                        Text(
                            text = product.category, 
                            fontSize = 12.sp, 
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    IconButton(onClick = { onEdit(product) }) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFF4F7FB), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "Mín: ${product.minStock.toInt()}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color.Gray)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQuantity(product.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.RemoveCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                    
                    Surface(
                        modifier = Modifier.clickable { showQuickQuantityDialog = true },
                        color = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF1A3A6B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${product.quantity.toInt()} ${product.unit}", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(onClick = { onUpdateQuantity(product.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                }
            }
        }
    }

    if (showQuickQuantityDialog) {
        var inputVal by remember { mutableStateOf(product.quantity.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showQuickQuantityDialog = false },
            title = { Text("Ajustar Cantidad") },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { input -> if (input.all { it.isDigit() }) inputVal = input },
                    label = { Text("Nueva cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = { 
                    onUpdateQuantity(inputVal.toDoubleOrNull() ?: product.quantity)
                    showQuickQuantityDialog = false
                }) { Text("Actualizar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEdicionProducto(product: Product? = null, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toInt()?.toString() ?: "") }
    var minStock by remember { mutableStateOf(product?.minStock?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    val unitOptions = listOf("Uds", "kg", "litros", "barriles", "paquetes", "cajas")
    var selectedUnit by remember { mutableStateOf(product?.unit ?: unitOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del item") })
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad de medida") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) }, 
                                onClick = { selectedUnit = option; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input -> if (input.all { it.isDigit() }) quantity = input },
                    label = { Text("Stock Actual") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { input -> if (input.all { it.isDigit() }) minStock = input },
                    label = { Text("Aviso de Stock Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && q >= 0) {
                    onConfirm(Product(
                        id = product?.id ?: "",
                        name = name,
                        quantity = q,
                        minStock = minStock.toDoubleOrNull() ?: 0.0,
                        category = category,
                        unit = selectedUnit,
                        profile = product?.profile ?: ""
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
