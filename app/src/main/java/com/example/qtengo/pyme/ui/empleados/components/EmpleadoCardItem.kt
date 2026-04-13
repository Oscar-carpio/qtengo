package com.example.qtengo.pyme.ui.empleados.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.Employee

@Composable
fun EmpleadoCardItem(
    employee: Employee, 
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onEdit: () -> Unit, 
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color(0xFFE3F2FD)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AssignmentInd, null, tint = Color(0xFF1565C0))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.name, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF1A3A6B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = employee.position, 
                        fontSize = 13.sp, 
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "%.2f €".format(employee.salary), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (employee.phone.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable { onCall() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(6.dp))
                        Text(text = employee.phone, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1565C0))
                    }
                }
                if (employee.email.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable { onEmail() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(6.dp))
                        Text(text = employee.email, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1565C0))
                    }
                }
                if (employee.details.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = employee.details, 
                            fontSize = 12.sp, 
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
