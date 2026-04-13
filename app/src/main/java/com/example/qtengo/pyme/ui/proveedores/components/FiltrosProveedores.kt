/**
 * Componente de filtrado para la sección de Proveedores.
 * Permite realizar búsquedas por nombre/categoría y alternar el orden alfabético.
 */
package com.example.qtengo.pyme.ui.proveedores.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.example.qtengo.pyme.ui.components.PymeFilterCard

@Composable
fun FiltrosProveedores(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortByAlphabetical: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    PymeFilterCard(
        title = "Buscador de Proveedores",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sortByAlphabetical, onCheckedChange = onSortChange)
            Text("Orden Alfabético (A-Z)", fontSize = 14.sp)
        }
    }
}
