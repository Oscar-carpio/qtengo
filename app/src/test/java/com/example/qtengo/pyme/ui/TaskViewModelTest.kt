package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import com.example.qtengo.core.data.repositories.TaskRepository
import com.example.qtengo.core.domain.models.Task
import com.example.qtengo.core.ui.screens.TaskViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tests unitarios para [TaskViewModel].
 * 
 * Valida la integración de la agenda diaria, asegurando que el cambio de fecha
 * sincronice correctamente tareas, movimientos financieros e inventario.
 */
@ExperimentalCoroutinesApi
class TaskViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val financeRepository = mockk<FinanceRepository>(relaxed = true)
    private val stockRepository = mockk<StockMovementRepository>(relaxed = true)
    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mocks iniciales
        every { taskRepository.getByProfileFlow("PYME") } returns flowOf(emptyList())
        every { taskRepository.getByDate(any(), "PYME") } returns flowOf(emptyList())
        every { financeRepository.getByDate(any(), "PYME") } returns flowOf(emptyList())
        every { stockRepository.getMovementsByDate(any(), "PYME") } returns flowOf(emptyList())

        viewModel = TaskViewModel(taskRepository, financeRepository, stockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifica que al iniciar el ViewModel, la fecha seleccionada por defecto sea la de hoy.
     */
    @Test
    fun init_estableceLaFechaDeHoyComoPredeterminada() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        Assert.assertEquals(today, viewModel.selectedDate.value)
    }

    /**
     * Valida que el flujo de todas las tareas (independiente de la fecha) se cargue
     * correctamente al iniciar el ViewModel.
     */
    @Test
    fun allTasks_cargaCorrectamenteTodasLasTareasDelPerfil() = runTest {
        val tareas = listOf(Task(id = "1", title = "T1"), Task(id = "2", title = "T2"))
        every { taskRepository.getByProfileFlow("PYME") } returns flowOf(tareas)

        // Reiniciar ViewModel para que recoja el nuevo mock
        viewModel = TaskViewModel(taskRepository, financeRepository, stockRepository)
        
        val observer = mockk<Observer<List<Task>>>(relaxed = true)
        viewModel.allTasks.observeForever(observer)
        advanceUntilIdle()

        verify { observer.onChanged(tareas) }
        Assert.assertEquals(2, viewModel.allTasks.value?.size)
    }

    /**
     * Valida que al seleccionar una fecha en el calendario, el ViewModel
     * actualice su estado y dispare las consultas correspondientes a los repositorios.
     */
    @Test
    fun seleccionarFecha_actualizaLaFechaSeleccionadaYDisparaNuevasConsultas() = runTest {
        val nuevaFecha = "25/12/2024"
        val tareasMock = listOf(Task(id = "1", title = "Tarea Navidad", date = nuevaFecha))

        every { taskRepository.getByDate(nuevaFecha, "PYME") } returns flowOf(tareasMock)

        val observer = mockk<Observer<List<Task>>>(relaxed = true)
        viewModel.tasksByDate.observeForever(observer)

        viewModel.seleccionarFecha(nuevaFecha)
        advanceUntilIdle()

        Assert.assertEquals(nuevaFecha, viewModel.selectedDate.value)
        verify { observer.onChanged(tareasMock) }
        
        viewModel.tasksByDate.removeObserver(observer)
    }

    /**
     * Verifica que al crear una tarea, se le asigne automáticamente la fecha de hoy
     * como fecha de creación para auditoría.
     */
    @Test
    fun insertarTarea_llamaAlRepositorioConLaFechaDeCreacionDeHoy() = runTest {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        viewModel.insertarTarea("Comprar pan", "Descripción", "ALTA", "30/12/2024")
        advanceUntilIdle()

        coVerify {
            taskRepository.insert(match {
                it.title == "Comprar pan" &&
                        it.date == "30/12/2024" &&
                        it.createdAt == today
            })
        }
    }

    /**
     * Asegura que las operaciones CRUD de tareas se deleguen al repositorio.
     */
    @Test
    fun actualizarTareaYEliminarTarea_llamanCorrectamenteAlRepositorio() = runTest {
        val task = Task(id = "task_1", title = "Test")

        viewModel.actualizarTarea(task)
        advanceUntilIdle()
        coVerify { taskRepository.update(task) }

        viewModel.eliminarTarea(task)
        advanceUntilIdle()
        coVerify { taskRepository.delete("task_1") }
    }

    /**
     * Comprueba que los flujos de finanzas y stock reaccionen automáticamente
     * cuando cambia la fecha seleccionada (patrón switchMap).
     */
    @Test
    fun financeByDateYStockByDate_reaccionanAlCambioDeFecha() = runTest {
        val fecha = "01/01/2024"
        
        // ACTIVAR LOS LIVEDATA para que el switchMap se ejecute
        viewModel.financeByDate.observeForever {}
        viewModel.stockByDate.observeForever {}
        
        viewModel.seleccionarFecha(fecha)
        advanceUntilIdle()

        // Verificar que se llamó a los repositorios correspondientes con esa fecha
        verify { financeRepository.getByDate(fecha, "PYME") }
        verify { stockRepository.getMovementsByDate(fecha, "PYME") }
    }
}
