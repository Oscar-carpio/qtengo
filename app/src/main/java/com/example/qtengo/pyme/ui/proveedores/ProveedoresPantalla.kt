/**
 * Pantalla de Gestión de Proveedores.
 * 
 * Permite administrar la red de contactos comerciales, realizar búsquedas rápidas,
 * organizar el catálogo de proveedores alfabéticamente y facilitar la comunicación
 * directa mediante llamadas o correos electrónicos.
 */
package com.example.qtengo.pyme.ui.proveedores

import android.content.Intent
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
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.proveedores.components.DialogoProveedor
import com.example.qtengo.pyme.ui.filtros.FiltrosProveedores
import com.example.qtengo.pyme.ui.filtros.OrderType
import com.example.qtengo.pyme.ui.proveedores.components.SupplierCardItem

/**
 * Composable que define la estructura visual del módulo de Proveedores.
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
    // Datos reactivos del repositorio
    val suppliers by viewModel.suppliers.observeAsState(emptyList())
    
    // Estados locales de control UI
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var currentOrder by remember { mutableStateOf(OrderType.NONE) }

    // Lógica de filtrado y ordenación dinámica
    val filteredSuppliers = remember(suppliers, searchQuery, currentOrder) {
        suppliers.filter {
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.category.contains(searchQuery, ignoreCase = true)
        }.let { list ->
            when (currentOrder) {
                OrderType.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                OrderType.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                else -> list
            }
        }
    }

    // Efecto para recargar datos si cambia el perfil
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

        // Buscador y filtros de ordenación
        FiltrosProveedores(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            currentOrder = currentOrder,
            onOrderChange = { currentOrder = it }
        )

        // Lista de proveedores o mensaje informativo
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
                                data = "tel:${supplier.phone}".toUri()
                            }
                            context.startActivity(intent)
                        },
                        onEmail = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${supplier.email}".toUri()
                            }
                            context.startActivity(intent)
                        },
                        onEdit = { supplierToEdit = supplier },
                        onDelete = { viewModel.delete(supplier.id) }
                    )
                }
            }
        }

        // Botón flotante de acción rápida
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

    // Diálogos de creación y edición
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
