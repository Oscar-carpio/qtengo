package com.example.qtengo.pyme.ui.tareas.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.qtengo.core.domain.models.Task
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TareaCardItem(
    task: Task,
    today: Date,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val scheduledDate = try { sdf.parse(task.date) } catch (_: Exception) { null }
    
    val cal = Calendar.getInstance().apply { 
        time = today
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val isExpired = !task.isCompleted && scheduledDate != null && scheduledDate.before(cal.time)

    val backgroundColor = when {
        task.isCompleted -> Color(0xFFF1F1F1)
        isExpired -> Color(0xFFFFEBEE)
        task.priority.uppercase() == "ALTA" -> Color(0xFFFFF3E0)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isExpired) BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color(0xFF2E7D32) else Color.Gray
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (task.isCompleted) Color.Gray else Color.Black
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (task.date.isEmpty()) "Sin fecha" else task.date,
                        fontSize = 12.sp,
                        color = if (isExpired) Color.Red else Color.Gray
                    )
                    
                    Spacer(Modifier.width(12.dp))
                    
                    val priorityColor = when(task.priority.uppercase()) {
                        "ALTA" -> Color(0xFFD32F2F)
                        "MEDIA" -> Color(0xFFF57C00)
                        else -> Color(0xFF388E3C)
                    }
                    Box(modifier = Modifier.background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = task.priority, color = priorityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}
