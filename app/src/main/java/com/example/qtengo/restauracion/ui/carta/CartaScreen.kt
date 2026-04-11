package com.example.qtengo.restauracion.ui.carta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun CartaScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: CartaViewModel = viewModel()
) {
    val platos by viewModel.platos.collectAsState()
    val menuDia by viewModel.menuDia.collectAsState()

    var showAddPlatoDialog by remember { mutableStateOf(false) }
    var showMenuDiaDialog by remember { mutableStateOf(false) }
    var platoAEditar by remember { mutableStateOf<Plato?>(null) }
    var tabSeleccionado by remember { mutableStateOf(0) }

    val categorias = listOf("Entrantes", "Principales", "Postres", "Bebidas")

    LaunchedEffect(Unit) {
        viewModel.cargarPlatos()
        viewModel.cargarMenuDia()
    }

    if (showAddPlatoDialog) {
        AddPlatoDialog(
            onConfirm = { viewModel.añadirPlato(it); showAddPlatoDialog = false },
            onDismiss = { showAddPlatoDialog = false }
        )
    }

    platoAEditar?.let { plato ->
        EditarPlatoDialog(
            plato = plato,
            onConfirm = { viewModel.editarPlato(plato.id, it); platoAEditar = null },
            onDismiss = { platoAEditar = null }
        )
    }

    if (showMenuDiaDialog) {
        MenuDiaDialog(
            menuActual = menuDia,
            onConfirm = { viewModel.guardarMenuDia(it); showMenuDiaDialog = false },
            onDismiss = { showMenuDiaDialog = false }
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
            Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }) {
                Text("Carta", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }) {
                Text("Menú del día", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        when (tabSeleccionado) {
            0 -> {
                if (platos.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay platos en la carta. ¡Añade uno!", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            val platosCat = platos.filter { it.categoria == categoria }
                            if (platosCat.isNotEmpty()) {
                                item {
                                    Text(
                                        text = categoria,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A3A6B),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(platosCat) { plato ->
                                    PlatoCard(
                                        plato = plato,
                                        onToggleDisponible = { viewModel.toggleDisponible(plato.id, !plato.disponible) },
                                        onEdit = { platoAEditar = plato },
                                        onDelete = { viewModel.eliminarPlato(plato.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddPlatoDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir plato")
                }
            }

            1 -> {
                Column(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (menuDia == null) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay menú del día configurado.", color = Color.Gray)
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Menú del día", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                                if (menuDia!!.fecha.isNotBlank()) {
                                    Text(menuDia!!.fecha, fontSize = 12.sp, color = Color.Gray)
                                }
                                Divider()
                                MenuLineaItem("1er plato", menuDia!!.primerPlato)
                                MenuLineaItem("2º plato", menuDia!!.segundoPlato)
                                MenuLineaItem("Postre", menuDia!!.postre)
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
                    onClick = { showMenuDiaDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
                ) {
                    Text(if (menuDia == null) "Configurar menú del día" else "Editar menú del día")
                }
            }
        }
    }
}

@Composable
fun MenuLineaItem(label: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(80.dp))
        Text(text = valor.ifBlank { "—" }, fontSize = 13.sp, color = Color(0xFF1A3A6B))
    }
}