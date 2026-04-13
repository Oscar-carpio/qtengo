package com.example.qtengo.pyme.ui.filtros

import androidx.compose.runtime.Composable

@Composable
fun FiltrosFinanzas(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit
) {
    PymeFilterCard(
        title = "Buscar Movimiento",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        OrderButtons(
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = onSortChange,
            showAmount = true,
            amountLabel = "Cantidad"
        )
    }
}
