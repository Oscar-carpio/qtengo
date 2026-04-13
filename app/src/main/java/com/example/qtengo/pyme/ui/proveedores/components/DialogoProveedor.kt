package com.example.qtengo.pyme.ui.proveedores.components

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid = phone.isEmpty() || phone.startsWith("+") || phone.all { it.isDigit() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Empresa") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Nombre de contacto") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() || it == '+' }) phone = input 
                    }, 
                    label = { Text("Teléfono (ej: +34...)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = !isPhoneValid
                )
                
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = !isEmailValid,
                    supportingText = { if (!isEmailValid) Text("Formato de email inválido", color = Color.Red) }
                )
                
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción / Detalles") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if(name.isNotEmpty() && isEmailValid && phone.isNotEmpty()) {
                        onConfirm(name, contact, phone, email, description) 
                    }
                },
                enabled = name.isNotEmpty() && isEmailValid && isPhoneValid && phone.isNotEmpty()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
