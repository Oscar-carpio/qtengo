package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.qtengo.core.data.repositories.EmployeeRepository
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.pyme.ui.empleados.EmpleadosViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

@ExperimentalCoroutinesApi
class EmpleadosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val employeeRepository = mockk<EmployeeRepository>(relaxed = true)
    private val financeRepository = mockk<FinanceRepository>(relaxed = true)
    private lateinit var viewModel: EmpleadosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { employeeRepository.getByProfileFlow(any()) } returns flowOf(emptyList())
        viewModel = EmpleadosViewModel(employeeRepository, financeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile actualiza la lista de empleados`() = runTest {
        val listaMock = listOf(Employee(id = "1", name = "Test", profile = "PYME"))
        every { employeeRepository.getByProfileFlow("PYME") } returns flowOf(listaMock)

        val observer = mockk<Observer<List<Employee>>>(relaxed = true)
        viewModel.employees.observeForever(observer)

        viewModel.loadProfile("PYME")
        advanceUntilIdle()

        verify { observer.onChanged(listaMock) }
        Assert.assertEquals(listaMock, viewModel.employees.value)
    }

    @Test
    fun `insert registra empleado y genera un movimiento de gasto en finanzas`() = runTest {
        // Given
        val nombre = "Carlos"
        val salario = 1500.0

        // When
        viewModel.insert(nombre, "Gerente", salario, "123", "test@test.com", "Notas")
        advanceUntilIdle()

        // Then
        // Verificar inserción de empleado
        coVerify { employeeRepository.insert(match { it.name == nombre && it.salary == salario }) }

        // Verificar inserción automática de nómina en finanzas
        coVerify {
            financeRepository.insert(match {
                it.concept == "Nómina de $nombre" &&
                        it.amount == salario &&
                        it.type == "GASTO"
            })
        }
    }

    @Test
    fun `delete llama al repositorio de empleados`() = runTest {
        val id = "emp_123"
        viewModel.delete(id)
        advanceUntilIdle()
        coVerify { employeeRepository.delete(id) }
    }

    @Test
    fun `update llama al repositorio de empleados`() = runTest {
        val emp = Employee(id = "1", name = "Editado")
        viewModel.update(emp)
        advanceUntilIdle()
        coVerify { employeeRepository.update(emp) }
    }
}