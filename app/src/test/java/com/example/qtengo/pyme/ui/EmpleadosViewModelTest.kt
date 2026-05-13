package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.qtengo.core.data.repositories.EmployeeRepository
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

/**
 * Tests unitarios para [EmpleadosViewModel].
 * 
 * Verifica la gestión de la plantilla, reactividad ante cambios de perfil y
 * cálculos financieros derivados (nóminas).
 */
@ExperimentalCoroutinesApi
class EmpleadosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val employeeRepository = mockk<EmployeeRepository>(relaxed = true)
    private lateinit var viewModel: EmpleadosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mock inicial para evitar que el switchMap falle al iniciar el ViewModel
        every { employeeRepository.getByProfileFlow(any()) } returns flowOf(emptyList())
        viewModel = EmpleadosViewModel(employeeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Valida que al cambiar el perfil de trabajo, se actualice reactivamente
     * la lista de empleados mostrada en la UI.
     */
    @Test
    fun cargarPerfil_actualizaLaListaDeEmpleados() = runTest {
        // Given
        val listaMock = listOf(Employee(id = "1", name = "Test", profile = "PYME"))
        every { employeeRepository.getByProfileFlow("PYME") } returns flowOf(listaMock)

        val observer = mockk<Observer<List<Employee>>>(relaxed = true)
        viewModel.employees.observeForever(observer)

        // When
        viewModel.cargarPerfil("PYME")
        advanceUntilIdle()

        // Then
        verify { observer.onChanged(listaMock) }
        Assert.assertEquals("La lista de empleados debe coincidir con el mock", listaMock, viewModel.employees.value)
        
        viewModel.employees.removeObserver(observer)
    }

    /**
     * Verifica que al insertar un empleado se guarde correctamente en el repositorio.
     */
    @Test
    fun insertar_registraEmpleadoCorrectamente() = runTest {
        // Given
        val nombre = "Carlos"
        val salario = 1500.0

        // When
        viewModel.insertar(nombre, "Gerente", salario, "123", "test@test.com", "Notas")
        advanceUntilIdle()

        // Then: Verificar persistencia del empleado
        coVerify { employeeRepository.insert(match { it.name == nombre && it.salary == salario }) }
    }

    /**
     * Asegura que la orden de eliminación se propague correctamente al repositorio.
     */
    @Test
    fun eliminar_llamaAlRepositorioDeEmpleados() = runTest {
        val id = "emp_123"
        viewModel.eliminar(id)
        advanceUntilIdle()
        coVerify { employeeRepository.delete(id) }
    }

    /**
     * Asegura que la actualización de datos del empleado se propague correctamente al repositorio.
     */
    @Test
    fun actualizar_llamaAlRepositorioDeEmpleados() = runTest {
        val emp = Employee(id = "1", name = "Editado")
        viewModel.actualizar(emp)
        advanceUntilIdle()
        coVerify { employeeRepository.update(emp) }
    }
    /**
     * Valida que el cálculo del sumatorio de salarios sea correcto para reflejar
     * el gasto total en personal en los informes financieros.
     */
    @Test
    fun `totalSalarios calcula la suma correcta de todos los empleados`() = runTest {
        // Given
        val empleados = listOf(
            Employee(salary = 1000.0),
            Employee(salary = 500.0)
        )
        every { employeeRepository.getByProfileFlow("PYME") } returns flowOf(empleados)

        // Activamos la observación para que el switchMap recolecte el flow
        viewModel.employees.observeForever {}

        // When
        viewModel.cargarPerfil("PYME")
        advanceUntilIdle()

        // Then
        val total = viewModel.employees.value?.sumOf { it.salary } ?: 0.0
        Assert.assertEquals(1500.0, total, 0.1)
    }

    /**
     * Verifica que el empleado herede automáticamente el perfil (PYME, etc.)
     * que esté seleccionado actualmente en el ViewModel al ser insertado.
     */
    @Test
    fun `insertar asigna el perfil correcto del ViewModel al nuevo empleado`() = runTest {
        viewModel.cargarPerfil("ESPECIAL")
        advanceUntilIdle()

        viewModel.insertar("Ana", "Dev", 2000.0, "1", "a@a.com", "")
        advanceUntilIdle()

        coVerify {
            employeeRepository.insert(match { it.profile == "ESPECIAL" })
        }
    }
}
