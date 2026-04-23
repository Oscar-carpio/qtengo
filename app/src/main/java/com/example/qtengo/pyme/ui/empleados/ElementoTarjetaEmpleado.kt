package com.example.qtengo.pyme.ui.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.Employee

/**
 * Representación visual de un empleado en la lista.
 */
@Composable
fun ElementoTarjetaEmpleado(
    empleado: Employee,
    onLlamar: () -> Unit,
    onCorreo: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con inicial
                Box(
                    modifier = Modifier.size(45.dp).background(Color(0xFF1565C0).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = empleado.name.take(1).uppercase(),
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = empleado.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = empleado.position, fontSize = 12.sp, color = Color.Gray)
                    Text(text = String.format("%.2f€", empleado.salary), fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                }

                Row {
                    IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (empleado.phone.isNotBlank()) {
                    Row(
                        modifier = Modifier.clickable { onLlamar() }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp), tint = Color(0xFF1565C0))
                        Text(" ${empleado.phone}", fontSize = 13.sp, color = Color(0xFF1565C0))
                    }
                }
                
                if (empleado.phone.isNotBlank() && empleado.email.isNotBlank()) {
                    Spacer(Modifier.width(20.dp))
                }

                if (empleado.email.isNotBlank()) {
                    Row(
                        modifier = Modifier.clickable { onCorreo() }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp), tint = Color(0xFF1565C0))
                        Text(" Email", fontSize = 13.sp, color = Color(0xFF1565C0))
                    }
                }
            }
        }
    }
}
