package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.data.model.Supplier

@Composable
fun SupplierScreen(
    profile: String,
    onBack: () -> Unit
) {
    val suppliers = remember { mutableStateListOf<Supplier>() }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1A3A6B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir proveedor")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F7FB))
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A3A6B))
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(text = "←", fontSize = 24.sp, color = Color.White)
                }
                Text(
                    text = "Proveedores ($profile)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suppliers) { supplier ->
                    SupplierItem(supplier)
                }
            }
        }
    }

    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, contact, phone ->
                suppliers.add(Supplier(name = name, contactName = contact, phone = phone, email = "", category = "General", profile = profile))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SupplierItem(supplier: Supplier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = supplier.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Contacto: ${supplier.contactName}", color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = { /* Acción llamar */ }) {
                Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = Color(0xFF1A3A6B))
            }
        }
    }
}

@Composable
fun AddSupplierDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Proveedor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Empresa") })
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Persona de contacto") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, contact, phone) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
