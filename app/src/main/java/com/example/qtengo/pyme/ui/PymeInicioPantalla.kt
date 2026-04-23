/**
 * Pantalla principal del Panel Pyme.
 * Actúa como hub central de navegación proporcionando acceso a las secciones de:
 * Stock, Finanzas, Proveedores, Empleados y Tareas.
 * También muestra indicadores clave (KPIs) de inventario.
 */
package com.example.qtengo.pyme.ui

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

/**
 * Define una opción del menú principal con su estética y destino.
 */
data class OpcionMenuPyme(val title: String, val icon: String, val color: Color)

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
        OpcionMenuPyme("Productos / Stock", "📦", Color(0xFF1565C0)),
        OpcionMenuPyme("Gastos e ingresos", "💹", Color(0xFF1565C0)),
        OpcionMenuPyme("Proveedores", "🚚", Color(0xFF1565C0)),
        OpcionMenuPyme("Empleados", "👥", Color(0xFF1565C0)),
        OpcionMenuPyme("Agenda de Tareas", "📝", Color(0xFF1565C0))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Perfil Pyme",
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Marcadores de indicadores clave (Dashboard) usando el componente unificado
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IndicadorEstadisticoPyme(
                title = "Productos", 
                value = productCount.toString(), 
                color = Color(0xFF1565C0), 
                modifier = Modifier.weight(1f).clickable { onMenuSelected("Productos / Stock") }
            )
            IndicadorEstadisticoPyme(
                title = "Stock bajo", 
                value = lowStockProducts.size.toString(), 
                color = Color(0xFFD32F2F), 
                modifier = Modifier.weight(1f).clickable { onMenuSelected("Productos / Stock") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Gestión", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))

        // Cuadrícula de navegación a sub-módulos
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuOptions) { option ->
                TarjetaMenu(option = option, onClick = { onMenuSelected(option.title) })
            }
        }
    }
}

/**
 * Tarjeta interactiva del menú de navegación.
 */
@Composable
fun TarjetaMenu(option: OpcionMenuPyme, onClick: () -> Unit) {
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

/**
 * Tarjeta para mostrar indicadores (KPIs) en el dashboard.
 * Utiliza el componente TarjetaEstadisticaPyme para mantener la consistencia visual.
 */
@Composable
fun IndicadorEstadisticoPyme(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    TarjetaEstadisticaPyme(
        titulo = title,
        valor = value,
        color = color,
        modifier = modifier
    )
}
