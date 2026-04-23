package com.example.qtengo.pyme.ui.filtros

import androidx.compose.runtime.Composable

@Composable
fun FiltrosProveedores(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentOrder: OrderType,
    onOrderChange: (OrderType) -> Unit
) {
    val sortBy = when (currentOrder) {
        OrderType.NAME_ASC, OrderType.NAME_DESC -> "Nombre"
        else -> ""
    }
    val isAscending = currentOrder == OrderType.NAME_ASC
    val isFiltered = searchQuery.isNotBlank() || currentOrder != OrderType.NONE

    TarjetaFiltroPyme(
        title = "Buscador de Proveedores",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange,
        isFiltered = isFiltered
    ) {
        BotonesOrden(
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { name, asc ->
                val newOrder = when (name) {
                    "Nombre" -> if (asc) OrderType.NAME_ASC else OrderType.NAME_DESC
                    else -> OrderType.NONE
                }
                onOrderChange(newOrder)
            },
            showAmount = false
        )
    }
}
