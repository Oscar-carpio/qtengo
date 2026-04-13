/**
 * Componente de filtrado exclusivo para la sección de Productos / Almacén.
 */
package com.example.qtengo.pyme.ui.filtros

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosProductos(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    selectedUnits: Set<String>,
    onUnitsChange: (Set<String>) -> Unit,
    filterByLowStock: Boolean,
    onLowStockChange: (Boolean) -> Unit
) {
    val unitOptions = listOf("Uds", "kg", "litros", "barriles", "paquetes", "cajas")

    PymeFilterCard(
        title = "Buscador y Filtros de Stock",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Ordenación común
            OrderButtons(
                sortBy = sortBy,
                isAscending = isAscending,
                onSortChange = onSortChange,
                showAmount = true,
                amountLabel = "Cantidad"
            )

            // Unidades de medida
            Column {
                Text("Filtrar por Unidades:", fontSize = 12.sp, style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    unitOptions.forEach { unit ->
                        val isSelected = selectedUnits.contains(unit)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onUnitsChange(if (isSelected) selectedUnits - unit else selectedUnits + unit)
                            },
                            label = { Text(unit) }
                        )
                    }
                }
            }

            // Alerta de stock
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = filterByLowStock, onCheckedChange = onLowStockChange)
                Text("Ver solo poco stock", fontSize = 14.sp)
            }
        }
    }
}
