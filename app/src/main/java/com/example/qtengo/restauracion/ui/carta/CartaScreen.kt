package com.example.qtengo.restauracion.ui.carta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.ui.components.QtengoTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CartaScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: CartaViewModel = viewModel()
) {
    val platos by viewModel.platos.collectAsState()
    val menuDia by viewModel.menuDia.collectAsState()

    var mostrarAgregar by remember { mutableStateOf(false) }
    var mostrarMenuDia by remember { mutableStateOf(false) }
    var platoEditar by remember { mutableStateOf<Plato?>(null) }
    var tabSeleccionado by remember { mutableStateOf(0) }

    val categorias = listOf("Entrantes", "Principales", "Postres", "Bebidas")

    LaunchedEffect(Unit) {
        viewModel.cargarPlatos()
        viewModel.cargarMenuDia()
    }

    if (mostrarAgregar) {
        AddPlatoDialog(
            onConfirm = { plato ->
                viewModel.añadirPlato(plato)
                mostrarAgregar = false
            },
            onDismiss = {
                mostrarAgregar = false
            }
        )
    }

    if (platoEditar != null) {
        EditarPlatoDialog(
            plato = platoEditar!!,
            onConfirm = { plato ->
                viewModel.editarPlato(platoEditar!!.id, plato)
                platoEditar = null
            },
            onDismiss = {
                platoEditar = null
            }
        )
    }

    if (mostrarMenuDia) {
        MenuDiaDialog(
            menuActual = menuDia,
            onConfirm = { menu ->
                viewModel.guardarMenuDia(menu)
                mostrarMenuDia = false
            },
            onDismiss = {
                mostrarMenuDia = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Carta / Menú del día",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        TabRow(
            selectedTabIndex = tabSeleccionado,
            containerColor = Color.White,
            contentColor = Color(0xFF1A3A6B)
        ) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 }
            ) {
                Text(
                    text = "Carta",
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 }
            ) {
                Text(
                    text = "Menú del día",
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (tabSeleccionado == 0) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (platos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay platos en la carta. Añade uno.",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            val platosCategoria = platos.filter { plato ->
                                plato.categoria == categoria
                            }

                            if (platosCategoria.isNotEmpty()) {
                                item {
                                    Text(
                                        text = categoria,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A3A6B),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(platosCategoria) { plato ->
                                    PlatoCard(
                                        plato = plato,
                                        onToggleDisponible = {
                                            viewModel.toggleDisponible(
                                                plato.id,
                                                !plato.disponible
                                            )
                                        },
                                        onEdit = {
                                            platoEditar = plato
                                        },
                                        onDelete = {
                                            viewModel.eliminarPlato(plato.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        mostrarAgregar = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A3A6B)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Añadir plato"
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (menuDia == null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay menú del día configurado.",
                                color = Color.Gray
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Menú del día",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A3A6B)
                                )

                                if (menuDia!!.fecha.isNotBlank()) {
                                    Text(
                                        text = menuDia!!.fecha,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Divider()

                                MenuLineaItem(
                                    label = "1er plato",
                                    valor = menuDia!!.primerPlato
                                )

                                MenuLineaItem(
                                    label = "2º plato",
                                    valor = menuDia!!.segundoPlato
                                )

                                MenuLineaItem(
                                    label = "Postre",
                                    valor = menuDia!!.postre
                                )

                                Divider()

                                Text(
                                    text = "${"%.2f".format(menuDia!!.precio)} €",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A3A6B)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        mostrarMenuDia = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A3A6B)
                    )
                ) {
                    Text(
                        text = if (menuDia == null) {
                            "Configurar menú del día"
                        } else {
                            "Editar menú del día"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatoCard(
    plato: Plato,
    onToggleDisponible: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plato.disponible) {
                Color.White
            } else {
                Color(0xFFF5F5F5)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = plato.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (plato.disponible) {
                        Color(0xFF1A3A6B)
                    } else {
                        Color.Gray
                    }
                )

                if (plato.descripcion.isNotBlank()) {
                    Text(
                        text = plato.descripcion,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${"%.2f".format(plato.precio)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
            }

            Switch(
                checked = plato.disponible,
                onCheckedChange = {
                    onToggleDisponible()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF1A3A6B)
                )
            )

            IconButton(
                onClick = onEdit
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar plato",
                    tint = Color(0xFF1A3A6B)
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar plato",
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun AddPlatoDialog(
    onConfirm: (Plato) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Principales") }
    var errorNombre by remember { mutableStateOf("") }
    var errorPrecio by remember { mutableStateOf("") }

    val categorias = listOf("Entrantes", "Principales", "Postres", "Bebidas")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Añadir plato"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorNombre = ""
                    },
                    label = {
                        Text(
                            text = "Nombre del plato"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty()) {
                            Text(
                                text = errorNombre,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    label = {
                        Text(
                            text = "Descripción"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                        errorPrecio = ""
                    },
                    label = {
                        Text(
                            text = "Precio"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorPrecio.isNotEmpty(),
                    supportingText = {
                        if (errorPrecio.isNotEmpty()) {
                            Text(
                                text = errorPrecio,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Text(
                    text = "Categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )

                categorias.chunked(2).forEach { fila ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { categoria ->
                            FilterChip(
                                selected = categoriaSeleccionada == categoria,
                                onClick = {
                                    categoriaSeleccionada = categoria
                                },
                                label = {
                                    Text(
                                        text = categoria,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A3A6B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var valido = true

                    if (nombre.isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        valido = false
                    }

                    val precioDouble = precio
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (precioDouble == null || precioDouble < 0) {
                        errorPrecio = "Introduce un precio válido"
                        valido = false
                    }

                    if (valido && precioDouble != null) {
                        val plato = Plato(
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            precio = precioDouble,
                            categoria = categoriaSeleccionada,
                            disponible = true
                        )

                        onConfirm(plato)
                    }
                }
            ) {
                Text(
                    text = "Añadir"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar"
                )
            }
        }
    )
}

@Composable
private fun EditarPlatoDialog(
    plato: Plato,
    onConfirm: (Plato) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(plato.nombre) }
    var descripcion by remember { mutableStateOf(plato.descripcion) }
    var precio by remember { mutableStateOf("%.2f".format(plato.precio)) }
    var categoriaSeleccionada by remember { mutableStateOf(plato.categoria) }
    var errorNombre by remember { mutableStateOf("") }
    var errorPrecio by remember { mutableStateOf("") }

    val categorias = listOf("Entrantes", "Principales", "Postres", "Bebidas")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar plato"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorNombre = ""
                    },
                    label = {
                        Text(
                            text = "Nombre del plato"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty()) {
                            Text(
                                text = errorNombre,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    label = {
                        Text(
                            text = "Descripción"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                        errorPrecio = ""
                    },
                    label = {
                        Text(
                            text = "Precio"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorPrecio.isNotEmpty(),
                    supportingText = {
                        if (errorPrecio.isNotEmpty()) {
                            Text(
                                text = errorPrecio,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Text(
                    text = "Categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )

                categorias.chunked(2).forEach { fila ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { categoria ->
                            FilterChip(
                                selected = categoriaSeleccionada == categoria,
                                onClick = {
                                    categoriaSeleccionada = categoria
                                },
                                label = {
                                    Text(
                                        text = categoria,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A3A6B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var valido = true

                    if (nombre.isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        valido = false
                    }

                    val precioDouble = precio
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (precioDouble == null || precioDouble < 0) {
                        errorPrecio = "Introduce un precio válido"
                        valido = false
                    }

                    if (valido && precioDouble != null) {
                        val platoEditado = Plato(
                            id = plato.id,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            precio = precioDouble,
                            categoria = categoriaSeleccionada,
                            disponible = plato.disponible
                        )

                        onConfirm(platoEditado)
                    }
                }
            ) {
                Text(
                    text = "Guardar"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar"
                )
            }
        }
    )
}

@Composable
private fun MenuDiaDialog(
    menuActual: MenuDia?,
    onConfirm: (MenuDia) -> Unit,
    onDismiss: () -> Unit
) {
    var primerPlato by remember {
        mutableStateOf(menuActual?.primerPlato ?: "")
    }

    var segundoPlato by remember {
        mutableStateOf(menuActual?.segundoPlato ?: "")
    }

    var postre by remember {
        mutableStateOf(menuActual?.postre ?: "")
    }

    var precio by remember {
        mutableStateOf(
            if ((menuActual?.precio ?: 0.0) > 0.0) {
                "%.2f".format(menuActual?.precio ?: 0.0)
            } else {
                ""
            }
        )
    }

    val fecha = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "ES")
    ).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Menú del día — $fecha"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = primerPlato,
                    onValueChange = {
                        primerPlato = it
                    },
                    label = {
                        Text(
                            text = "1er plato"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = segundoPlato,
                    onValueChange = {
                        segundoPlato = it
                    },
                    label = {
                        Text(
                            text = "2º plato"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = postre,
                    onValueChange = {
                        postre = it
                    },
                    label = {
                        Text(
                            text = "Postre"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                    },
                    label = {
                        Text(
                            text = "Precio"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val precioDouble = precio
                        .replace(",", ".")
                        .toDoubleOrNull() ?: 0.0

                    val menu = MenuDia(
                        primerPlato = primerPlato,
                        segundoPlato = segundoPlato,
                        postre = postre,
                        precio = precioDouble,
                        fecha = fecha
                    )

                    onConfirm(menu)
                }
            ) {
                Text(
                    text = "Guardar"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar"
                )
            }
        }
    )
}

@Composable
private fun MenuLineaItem(
    label: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = valor.ifBlank { "—" },
            fontSize = 13.sp,
            color = Color(0xFF1A3A6B)
        )
    }
}