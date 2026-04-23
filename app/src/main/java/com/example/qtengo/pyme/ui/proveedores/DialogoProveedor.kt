package com.example.qtengo.pyme.ui.proveedores

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.Supplier

@Composable
fun DialogoProveedor(
    titulo: String,
    supplier: Supplier? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var contact by remember { mutableStateOf(supplier?.contactName ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var description by remember { mutableStateOf(supplier?.category ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { }, // Evita el cierre al pulsar fuera
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) nameError = null
                    }, 
                    label = { Text("Empresa *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } }
                )
                
                OutlinedTextField(
                    value = contact, 
                    onValueChange = { contact = it }, 
                    label = { Text("Nombre de contacto") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() || it == '+' }) {
                            phone = input
                            contactError = null
                        }
                    }, 
                    label = { Text("Teléfono") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = contactError != null
                )
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { 
                        email = it
                        emailError = null
                        contactError = null
                    }, 
                    label = { Text("Email") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null || contactError != null,
                    supportingText = { 
                        if (emailError != null) Text(emailError!!, color = Color.Red, fontSize = 12.sp)
                        else if (contactError != null) Text(contactError!!, color = Color.Red, fontSize = 12.sp)
                    }
                )
                
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Descripción / Detalles") }, 
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    var hasError = false
                    
                    if (name.isBlank()) {
                        nameError = "El nombre de la empresa es obligatorio"
                        hasError = true
                    }
                    
                    if (phone.isBlank() && email.isBlank()) {
                        contactError = "Introduce al menos un teléfono o un email"
                        hasError = true
                    }

                    if (phone.isNotEmpty() && !(phone.startsWith("+") || phone.all { it.isDigit() })) {
                        contactError = "Formato de teléfono inválido"
                        hasError = true
                    }
                    
                    if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Formato de email inválido"
                        hasError = true
                    }

                    if (!hasError) {
                        onConfirm(name, contact, phone, email, description) 
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
