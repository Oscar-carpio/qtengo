package com.example.qtengo.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.Supplier

/**
 * Pantalla para visualizar y gestionar la lista de proveedores.
 * Incluye funcionalidades de comunicación (llamadas y emails).
 */
@Composable
fun SupplierScreen(
    profile: String = "PYME",
    viewModel: SupplierViewModel = viewModel(),
    onBack: () -> Unit
) {
    val suppliers by viewModel.suppliers.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Cabecera personalizada
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
                text = "Proveedores",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Lista de proveedores
        if (suppliers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay proveedores registrados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suppliers) { supplier ->
                    SupplierCard(
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
                        }
                    )
                }
            }
        }

        // Botón para añadir
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Nuevo Proveedor")
        }
    }

    // Diálogo para añadir nuevo proveedor
    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, contact, phone, email, cat ->
                viewModel.insert(name, contact, phone, email, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SupplierCard(supplier: Supplier, onCall: () -> Unit, onEmail: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = supplier.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = supplier.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
            Text(text = "Contacto: ${supplier.contactName}", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de llamada (Funcionalidad RA04_c)
                Row(
                    modifier = Modifier
                        .clickable { onCall() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Llamar",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1565C0)
                    )
                    Text(" ${supplier.phone}", fontSize = 13.sp, color = Color(0xFF1565C0))
                }
                
                Spacer(Modifier.width(20.dp))
                
                // Botón de email (Funcionalidad RA04_c)
                Row(
                    modifier = Modifier
                        .clickable { onEmail() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Enviar email",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1565C0)
                    )
                    Text(" Email", fontSize = 13.sp, color = Color(0xFF1565C0))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplierDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Proveedor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Empresa") })
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contacto") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
            }
        },
        confirmButton = {
            Button(
                onClick = { if(name.isNotEmpty()) onConfirm(name, contact, phone, email, category) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
