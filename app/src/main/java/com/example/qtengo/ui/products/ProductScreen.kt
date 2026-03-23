package com.example.qtengo.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    profile: String = "FAMILIA",
    viewModel: ProductViewModel = viewModel()
) {

    val products by viewModel.products.observeAsState(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Productos") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { paddingValues ->

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay productos")
                    Text("Pulsa + para añadir el primero")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onDelete = { viewModel.delete(product) },
                        onEdit = { productToEdit = product }
                    )
                }
            }
        }
    }

    // ➕ Añadir
    if (showAddDialog) {
        AddProductDialog(
            profile = profile,
            onDismiss = { showAddDialog = false },
            onConfirm = {
                viewModel.insert(it)
                showAddDialog = false
            }
        )
    }

    // ✏️ Editar
    if (productToEdit != null) {
        AddProductDialog(
            profile = productToEdit!!.profile,
            product = productToEdit,
            onDismiss = { productToEdit = null },
            onConfirm = {
                viewModel.update(it)
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    val isLowStock = product.quantity <= product.minStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(product.name, fontWeight = FontWeight.Bold)
                    Text("Categoría: ${product.category}")
                    Text("Stock: ${product.quantity} ${product.unit}")
                }

                if (isLowStock) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    profile: String,
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {

    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var minStock by remember { mutableStateOf(product?.minStock?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "unidades") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (product == null) "Añadir Producto" else "Editar Producto")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") }
                )

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Stock mínimo") }
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") }
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    Product(
                        id = product?.id ?: 0,
                        name = name,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        minStock = minStock.toDoubleOrNull() ?: 0.0,
                        category = category,
                        profile = profile,
                        unit = unit
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}