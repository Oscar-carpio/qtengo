package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.qtengo.core.data.repositories.ProductRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.pyme.ui.productos.ProductosViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitarios para [ProductosViewModel].
 * 
 * Verifica la lógica de inventario, incluyendo el filtrado de stock bajo
 * y la generación automática de movimientos de auditoría.
 */
@ExperimentalCoroutinesApi
class ProductosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository = mockk<ProductRepository>(relaxed = true)
    private val stockRepository = mockk<StockMovementRepository>(relaxed = true)
    private lateinit var viewModel: ProductosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { productRepository.getByProfileFlow(any()) } returns flowOf(emptyList())
        viewModel = ProductosViewModel(productRepository, stockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Valida que el filtro de stock bajo identifique correctamente los productos
     * que están por debajo de su margen mínimo de seguridad.
     */
    @Test
    fun lowStockProducts_filtraCorrectamenteProductosConPocoStock() = runTest {
        val productos = listOf(
            Product(id = "1", name = "Mucho Stock", quantity = 10.0, minStock = 5.0),
            Product(id = "2", name = "Poco Stock", quantity = 2.0, minStock = 5.0)
        )
        every { productRepository.getByProfileFlow("PYME") } returns flowOf(productos)

        val observer = mockk<Observer<List<Product>>>(relaxed = true)
        viewModel.lowStockProducts.observeForever(observer)

        advanceUntilIdle()

        val result = viewModel.lowStockProducts.value
        Assert.assertEquals(1, result?.size)
        Assert.assertEquals("Poco Stock", result?.get(0)?.name)
    }

    /**
     * Comprueba que cualquier cambio en la cantidad de un producto genere
     * automáticamente un registro en el historial de movimientos de stock.
     */
    @Test
    fun actualizarCantidad_registraUnMovimientoDeStockSiLaCantidadCambia() = runTest {
        val product = Product(id = "prod1", name = "Martillo", quantity = 10.0, profile = "PYME")

        viewModel.actualizarCantidad(product, 15.0)
        advanceUntilIdle()

        // Verifica que se actualiza el producto
        coVerify { productRepository.update(match { it.quantity == 15.0 }, "prod1") }

        // Verifica que se registra el movimiento (+5.0)
        coVerify {
            stockRepository.insert(match {
                it.productName == "Martillo" && it.quantityChanged == 5.0 && it.newQuantity == 15.0
            })
        }
    }

    /**
     * Valida que si no hay cambio real en la cantidad, no se realicen llamadas
     * innecesarias a la base de datos ni se registren movimientos de stock.
     */
    @Test
    fun actualizarCantidad_noHaceNadaSiLaCantidadEsLaMisma() = runTest {
        val product = Product(id = "prod1", name = "Martillo", quantity = 10.0, profile = "PYME")

        viewModel.actualizarCantidad(product, 10.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { productRepository.update(any(), any()) }
        coVerify(exactly = 0) { stockRepository.insert(any()) }
    }

    /**
     * Asegura que al dar de alta un producto se registre también su entrada inicial.
     */
    @Test
    fun insertar_registraProductoYMovimientoInicial() = runTest {
        // Given
        val product = Product(id = "new", name = "Nuevo", quantity = 20.0, profile = "PYME", unit = "uds")

        // When
        viewModel.insertar(product)
        advanceUntilIdle()

        // Then
        coVerify { 
            productRepository.insert(match { 
                it.name == "Nuevo" && it.quantity == 20.0 && it.customId.isNotEmpty() 
            }) 
        }
        coVerify { 
            stockRepository.insert(match { 
                it.productName == "Nuevo" && it.quantityChanged == 20.0 
            }) 
        }
    }

    /**
     * Verifica la lógica de generación de IDs personalizados según la unidad.
     * Ejemplo: Un producto de tipo 'KG' debe terminar en 'K'.
     */
    @Test
    fun insertar_generaIdPersonalizadoCorrectoSegunUnidad() = runTest {
        // Mock de lista vacía para que el ID sea 001
        every { productRepository.getByProfileFlow("PYME") } returns flowOf(emptyList())
        viewModel.products.observeForever {  }

        val product = Product(name = "Harina", unit = "KG", quantity = 5.0, profile = "PYME")
        viewModel.insertar(product)
        advanceUntilIdle()

        coVerify {
            productRepository.insert(match { it.customId == "001K" })
        }
    }

    /**
     * Valida que el contador de productos refleje el tamaño real de la lista de inventario.
     */
    @Test
    fun productCount_devuelveElTamanoDeLaLista() = runTest {
        val productos = listOf(
            Product(id = "1"),
            Product(id = "2"),
            Product(id = "3")
        )
        every { productRepository.getByProfileFlow("PYME") } returns flowOf(productos)
        
        viewModel.productCount.observeForever { }
        advanceUntilIdle()

        Assert.assertEquals(3, viewModel.productCount.value)
    }
}
