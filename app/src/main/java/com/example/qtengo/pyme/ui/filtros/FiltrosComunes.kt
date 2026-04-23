/**
 * Componentes de filtrado comunes para el módulo Pyme.
 */
package com.example.qtengo.pyme.ui.filtros

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OrderType {
    NONE, NAME_ASC, NAME_DESC
}

@Composable
fun TarjetaFiltroPyme(
    title: String = "Buscador y Filtros",
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isFiltered: Boolean = false, // Nuevo parámetro para indicar estado activo
    content: @Composable ColumnScope.() -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Color de fondo: Azul claro si hay filtros, Blanco si no.
    val backgroundColor = if (isFiltered) Color(0xFFE3F2FD) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterList, 
                        null, 
                        tint = if (isFiltered) Color(0xFF1565C0) else Color(0xFF1A3A6B)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isFiltered) "$title (Activos)" else title,
                        fontWeight = FontWeight.Bold, 
                        color = if (isFiltered) Color(0xFF1565C0) else Color(0xFF1A3A6B)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = if (isFiltered) Color(0xFF1565C0) else Color.Black
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, "Limpiar búsqueda", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isFiltered) Color.White else Color.Transparent
                        )
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun BotonesOrden(
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    showAmount: Boolean = false,
    amountLabel: String = "Cantidad"
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ordenar por:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            if (sortBy != "" && sortBy != "Registro") {
                TextButton(
                    onClick = { onSortChange("", false) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text("Limpiar", fontSize = 11.sp, color = Color(0xFF1565C0))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = sortBy == "Nombre",
                onClick = {
                    if (sortBy == "Nombre") {
                        if (isAscending) onSortChange("Nombre", false)
                        else onSortChange("", false)
                    } else {
                        onSortChange("Nombre", true)
                    }
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Nombre")
                        if (sortBy == "Nombre") {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            )

            if (showAmount) {
                FilterChip(
                    selected = sortBy == amountLabel,
                    onClick = {
                        if (sortBy == amountLabel) {
                            if (isAscending) onSortChange(amountLabel, false)
                            else onSortChange("", false)
                        } else {
                            onSortChange(amountLabel, true)
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(amountLabel)
                            if (sortBy == amountLabel) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
