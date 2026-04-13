package com.example.qtengo.pyme.ui.proveedores

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.components.FiltrosProveedoresPyme
import com.example.qtengo.pyme.ui.proveedores.components.DialogoProveedor
import com.example.qtengo.pyme.ui.proveedores.components.SupplierCardItem

/**
 * Pantalla principal para la gestión de proveedores en el perfil PYME.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresPantalla(
    profile: String = "PYME",
    viewModel: ProveedoresViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val suppliers by viewModel.suppliers.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var sortByAlphabetical by remember { mutableStateOf(true) }

    // Filtra los proveedores por nombre o descripción basándose en la búsqueda
    val filteredSuppliers = suppliers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.category.contains(searchQuery, ignoreCase = true)
    }.let { list ->
        if (sortByAlphabetical) list.sortedBy { it.name }
        else list
    }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Proveedores",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Filtro Unificado
        FiltrosProveedoresPyme(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            sortByAlphabetical = sortByAlphabetical,
            onSortChange = { sortByAlphabetical = it }
        )

        if (filteredSuppliers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(if (searchQuery.isEmpty()) "No hay proveedores registrados" else "Sin resultados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSuppliers) { supplier ->
                    SupplierCardItem(
                        supplier = supplier,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${supplier.phone}")
                            }
                            context.startActivity(intent)
                        },
                        onEmail = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${supplier.email}")
                            }
                            context.startActivity(intent)
                        },
                        onEdit = { supplierToEdit = supplier },
                        onDelete = { viewModel.delete(supplier.id) }
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
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Nuevo Proveedor")
        }
    }

    if (showAddDialog) {
        DialogoProveedor(
            titulo = "Añadir Proveedor",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, contact, phone, email, desc ->
                viewModel.insert(name, contact, phone, email, desc)
                showAddDialog = false
            }
        )
    }

    supplierToEdit?.let { supplier ->
        DialogoProveedor(
            titulo = "Editar Proveedor",
            supplier = supplier,
            onDismiss = { supplierToEdit = null },
            onConfirm = { name, contact, phone, email, desc ->
                viewModel.update(supplier.copy(name = name, contactName = contact, phone = phone, email = email, category = desc))
                supplierToEdit = null
            }
        )
    }
}
