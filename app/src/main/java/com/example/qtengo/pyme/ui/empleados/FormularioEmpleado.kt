/**
 * Diálogo para la gestión de datos de empleados.
 * 
 * Este componente centraliza la lógica de entrada de datos para altas y ediciones.
 * Implementa validaciones en tiempo real y mensajes de error específicos.
 */
package com.example.qtengo.pyme.ui.empleados

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
import com.example.qtengo.core.domain.models.Employee

/**
 * Composable que muestra el formulario de empleado con validaciones.
 */
@Composable
fun DialogoEmpleado(
    titulo: String,
    employee: Employee? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Double, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var pos by remember { mutableStateOf(employee?.position ?: "") }
    var sal by remember { mutableStateOf(employee?.salary?.toString() ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var notes by remember { mutableStateOf(employee?.details ?: "") }

    // Estados de error
    var nameError by remember { mutableStateOf<String?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var salaryError by remember { mutableStateOf<String?>(null) }

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
                    label = { Text("Nombre Completo *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } }
                )
                
                OutlinedTextField(
                    value = pos, 
                    onValueChange = { pos = it }, 
                    label = { Text("Cargo") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = sal, 
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            if (input.count { it == '.' } <= 1) {
                                sal = input
                                salaryError = null
                            }
                        }
                    }, 
                    label = { Text("Salario Mensual") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = salaryError != null,
                    supportingText = { salaryError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } }
                )
                
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() || char == '+' }) {
                            phone = it
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
                    label = { Text("Email de contacto") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null || contactError != null,
                    supportingText = { 
                        if (emailError != null) Text(emailError!!, color = Color.Red, fontSize = 12.sp)
                        else if (contactError != null) Text(contactError!!, color = Color.Red, fontSize = 12.sp)
                    }
                )

                OutlinedTextField(
                    value = notes, 
                    onValueChange = { notes = it }, 
                    label = { Text("Notas / Detalles") }, 
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    var hasError = false
                    
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
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
                    
                    val salarioValor = sal.toDoubleOrNull()
                    if (sal.isNotEmpty() && salarioValor == null) {
                        salaryError = "Salario inválido"
                        hasError = true
                    }

                    if (!hasError) {
                        onConfirm(name, pos, salarioValor ?: 0.0, phone, email, notes)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancelar") } 
        }
    )
}
