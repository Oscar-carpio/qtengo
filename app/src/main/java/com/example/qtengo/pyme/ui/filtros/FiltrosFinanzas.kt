package com.example.qtengo.pyme.ui.filtros

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun FiltrosFinanzas(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    periodoFilter: String,
    onPeriodoChange: (String) -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || sortBy.isNotEmpty() || periodoFilter.isNotEmpty()

    TarjetaFiltroPyme(
        title = "Buscar Movimiento",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange,
        isFiltered = isFiltered
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonesOrden(
                sortBy = sortBy,
                isAscending = isAscending,
                onSortChange = onSortChange,
                showAmount = true,
                amountLabel = "Cantidad"
            )

            Column {
                Text("Tipo de periodo:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", "Diarios", "Mensuales").forEach { periodo ->
                        FilterChip(
                            selected = periodoFilter == periodo,
                            onClick = { onPeriodoChange(periodo) },
                            label = { Text(periodo) }
                        )
                    }
                }
            }
        }
    }
}
