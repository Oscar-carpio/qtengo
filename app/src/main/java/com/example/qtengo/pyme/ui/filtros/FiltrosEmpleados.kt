package com.example.qtengo.pyme.ui.filtros

import androidx.compose.runtime.Composable

@Composable
fun FiltrosEmpleados(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || sortBy.isNotEmpty()

    TarjetaFiltroPyme(
        title = "Buscar y Ordenar Plantilla",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange,
        isFiltered = isFiltered
    ) {
        BotonesOrden(
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = onSortChange,
            showAmount = false
        )
    }
}
