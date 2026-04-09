package com.example.qtengo.pyme.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.productos.ProductosViewModel

data class PymeMenuOption(val title: String, val icon: String, val color: Color)

@Composable
fun PymeInicioPantalla(
    onMenuSelected: (String) -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    productosViewModel: ProductosViewModel = viewModel()
) {
    val productCount by productosViewModel.productCount.observeAsState(0)
    val lowStockProducts by productosViewModel.lowStockProducts.observeAsState(emptyList())

    val menuOptions = listOf(
        PymeMenuOption("Productos / Stock", "📦", Color(0xFF1565C0)),
        PymeMenuOption("Gastos e ingresos", "💹", Color(0xFF1976D2)),
        PymeMenuOption("Proveedores", "🚚", Color(0xFF1E88E5)),
        PymeMenuOption("Empleados", "👥", Color(0xFF2196F3)),
        PymeMenuOption("Agenda de Tareas", "📝", Color(0xFF0288D1))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Panel Pyme",
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard(title = "Productos", value = productCount.toString(), color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
            DashboardCard(title = "Stock bajo", value = lowStockProducts.size.toString(), color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Gestión", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuOptions) { option ->
                MenuCard(option = option, onClick = { onMenuSelected(option.title) })
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, color = Color.White)
        }
    }
}

@Composable
fun MenuCard(option: PymeMenuOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = option.color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(option.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = option.title, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
