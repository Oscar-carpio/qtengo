package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.qtengo.core.data.repositories.EmployeeRepository
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.pyme.ui.finanzas.FinanzasViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FinanzasViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val financeRepository = mockk<FinanceRepository>(relaxed = true)
    private val employeeRepository = mockk<EmployeeRepository>(relaxed = true)

    // Usamos MutableStateFlow para tener control total sobre las emisiones en el test
    private val financeFlow = MutableStateFlow<List<FinanceMovement>>(emptyList())
    private val employeeFlow = MutableStateFlow<List<Employee>>(emptyList())

    private lateinit var viewModel: FinanzasViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { financeRepository.getAllFlow("PYME") } returns financeFlow
        every { employeeRepository.getByProfileFlow("PYME") } returns employeeFlow

        viewModel = FinanzasViewModel(financeRepository, employeeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `movements combina correctamente movimientos de caja y nominas de empleados`() = runTest {
        // Given
        val movimientosCaja = listOf(
            FinanceMovement(
                id = "1",
                concept = "Venta",
                amount = 100.0,
                type = "INGRESO",
                profile = "PYME"
            )
        )
        val empleados = listOf(
            Employee(id = "emp1", name = "Juan", salary = 1200.0, profile = "PYME")
        )

        // Activamos observación
        viewModel.movements.observeForever {}

        // When: Emitimos nuevos valores
        financeFlow.value = movimientosCaja
        employeeFlow.value = empleados

        // Then
        val result = viewModel.movements.value
        Assert.assertEquals(2, result?.size)
        Assert.assertEquals("Venta", result?.get(0)?.concept)
        Assert.assertEquals("Nómina de Juan", result?.get(1)?.concept)
    }

    @Test
    fun `calculo de totalIngresos y totalGastos es correcto`() = runTest {
        // Given
        val movimientos = listOf(
            FinanceMovement(amount = 500.0, type = "INGRESO"),
            FinanceMovement(amount = 200.0, type = "GASTO")
        )
        val empleados = listOf(
            Employee(salary = 1000.0)
        )

        // Observar para activar transformaciones
        viewModel.totalIngresos.observeForever {}
        viewModel.totalGastos.observeForever {}

        // When
        financeFlow.value = movimientos
        employeeFlow.value = empleados

        // Then
        Assert.assertEquals(500.0, viewModel.totalIngresos.value ?: 0.0, 0.1)
        Assert.assertEquals(1200.0, viewModel.totalGastos.value ?: 0.0, 0.1)
    }

    @Test
    fun `insert llama al repositorio de finanzas`() = runTest {
        val movement = FinanceMovement(concept = "Test", amount = 10.0, type = "INGRESO")
        viewModel.insert(movement)
        coVerify { financeRepository.insert(movement) }
    }

    @Test
    fun `delete no elimina si el ID empieza por nomina_`() = runTest {
        viewModel.delete("nomina_123")
        coVerify(exactly = 0) { financeRepository.delete(any()) }
    }

    @Test
    fun `delete elimina si el ID NO empieza por nomina_`() = runTest {
        viewModel.delete("mov_123")
        coVerify { financeRepository.delete("mov_123") }
    }
}