package com.example.qtengo.pyme.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class PymeHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verificarQueSeMuestranTodosLosModulosDeGestionEnElMenuPrincipal() {
        composeTestRule.setContent {
            PymeHomeScreen(
                onMenuSelected = {},
                onLogout = {},
                onChangeProfile = {},
                productViewModel = mockk(relaxed = true)
            )
        }

        // Verificar que las opciones del menú están presentes
        composeTestRule.onNodeWithText("Productos / Stock").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gastos e ingresos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Proveedores").assertIsDisplayed()
        composeTestRule.onNodeWithText("Empleados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agenda de Tareas").assertIsDisplayed()
    }

    @Test
    fun verificarQueAlSeleccionarUnModuloSeDisparaLaNavegacionCorrespondiente() {
        var moduloSeleccionado = ""
        composeTestRule.setContent {
            PymeHomeScreen(
                onMenuSelected = { moduloSeleccionado = it },
                onLogout = {},
                onChangeProfile = {},
                productViewModel = mockk(relaxed = true)
            )
        }

        // Simular clic en el módulo de "Empleados"
        composeTestRule.onNodeWithText("Empleados").performClick()

        // Comprobar que el callback de navegación recibió el nombre del módulo
        assert(moduloSeleccionado == "Empleados")
    }

    @Test
    fun verificarQueElDashboardMuestraLosContadoresCorrectosDeProductosYStockBajo() {
        composeTestRule.setContent {
            DashboardSection(
                productCount = 15,
                lowStockCount = 3
            )
        }

        // Verificar que las cifras y etiquetas del dashboard son visibles
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("Productos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock bajo").assertIsDisplayed()
    }
}
