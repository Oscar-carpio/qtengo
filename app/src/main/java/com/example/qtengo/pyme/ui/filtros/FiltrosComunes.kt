/**
 * Componentes de filtrado comunes para el módulo Pyme.
 * Contiene la estructura base de las tarjetas de filtro para mantener la consistencia visual.
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

/**
 * Enumeración para los tipos de ordenación disponibles en el módulo Pyme.
 */
enum class OrderType {
    NONE, NAME_ASC, NAME_DESC, AMOUNT_ASC, AMOUNT_DESC
}

/**
 * Tarjeta base expandible para filtros con un campo de búsqueda común.
 */
@Composable
fun PymeFilterCard(
    title: String = "Buscador y Filtros",
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, tint = Color(0xFF1A3A6B))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Contraer" else "Expandir"
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
                        shape = RoundedCornerShape(12.dp)
                    )
                    content()
                }
            }
        }
    }
}

/**
 * Componente común para mostrar botones de ordenación con el estilo del Inventario.
 */
@Composable
fun OrderButtons(
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    showAmount: Boolean = false,
    amountLabel: String = "Cantidad"
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ordenar por:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Botón de Nombre
            FilterChip(
                selected = sortBy == "Nombre",
                onClick = {
                    if (sortBy == "Nombre") {
                        if (isAscending) onSortChange("Nombre", false)
                        else onSortChange("Registro", true) // Al desmarcar vuelve a Registro
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

            // Botón de Cantidad (opcional)
            if (showAmount) {
                FilterChip(
                    selected = sortBy == amountLabel,
                    onClick = {
                        if (sortBy == amountLabel) {
                            if (isAscending) onSortChange(amountLabel, false)
                            else onSortChange("Registro", true) // Al desmarcar vuelve a Registro
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
