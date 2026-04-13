/**
 * Componente de filtrado para la sección de Empleados.
 * Permite buscar empleados por nombre y activar/desactivar el orden alfabético.
 */
package com.example.qtengo.pyme.ui.empleados.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.example.qtengo.pyme.ui.components.PymeFilterCard

@Composable
fun FiltrosEmpleados(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortByAlphabetical: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    PymeFilterCard(
        title = "Buscar y Ordenar Plantilla",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sortByAlphabetical, onCheckedChange = onSortChange)
            Text("Orden Alfabético (A-Z)", fontSize = 14.sp)
        }
    }
}
