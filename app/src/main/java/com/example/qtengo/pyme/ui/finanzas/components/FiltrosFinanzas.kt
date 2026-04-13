/**
 * Componente de filtrado para el módulo de Finanzas.
 * Permite la búsqueda de movimientos financieros por concepto utilizando la base unificada de filtros.
 */
package com.example.qtengo.pyme.ui.finanzas.components

import androidx.compose.runtime.Composable
import com.example.qtengo.pyme.ui.components.PymeFilterCard

@Composable
fun FiltrosFinanzas(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    PymeFilterCard(
        title = "Buscar Movimiento",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    )
}
