package com.example.qtengo.ui.screens

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
import com.example.qtengo.ui.products.ProductViewModel

data class MenuOption(val title: String, val icon: String, val color: Color)

@Composable
fun PymeHomeScreen(
    onMenuSelected: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProductViewModel = viewModel()
) {
    val productCount by viewModel.productCount.observeAsState(0)
    val lowStock by viewModel.lowStockProducts.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadProfile("PYME")
    }

    val menuOptions = listOf(
        MenuOption("Productos / Stock", "📦", Color(0xFF1565C0)),
        MenuOption("Gastos e ingresos", "💹", Color(0xFF1976D2)),
        MenuOption("Proveedores", "🚚", Color(0xFF1E88E5)),
        MenuOption("Empleados", "👥", Color(0xFF2196F3))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        PymeHeader(onBack)

        Spacer(modifier = Modifier.height(16.dp))

        DashboardSection(
            productCount = productCount,
            lowStockCount = lowStock.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gestión",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )

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
fun PymeHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A3A6B))
            .padding(24.dp)
    ) {
        IconButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(text = "←", fontSize = 24.sp, color = Color.White)
        }
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = "Q-Tengo",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "Panel Pyme",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun DashboardSection(
    productCount: Int,
    lowStockCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DashboardCard("Productos", productCount.toString(), Color(0xFF1565C0))
        DashboardCard("Stock bajo", lowStockCount.toString(), Color(0xFFD32F2F))
    }
}

@Composable
fun DashboardCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                color = Color.White
            )
        }
    }
}

@Composable
fun MenuCard(option: MenuOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = option.color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(option.icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = option.title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}