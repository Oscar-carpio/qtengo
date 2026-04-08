package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.domain.models.FinanceMovement
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FinanceViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<FinanceRepository>(relaxed = true)
    private lateinit var viewModel: FinanceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Mocking the initial flow to avoid null errors on init
        every { repository.getAllFlow(any()) } returns flowOf(emptyList())
        // Now passing the mock repository directly via constructor
        viewModel = FinanceViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verificar que la insercion de un movimiento llama al repositorio correctamente`() = runTest {
        // Given
        val movement = FinanceMovement(
            concept = "Test Gasto",
            amount = 100.0,
            type = "GASTO",
            date = "01/01/2024",
            profile = "PYME"
        )
        coEvery { repository.insert(movement) } returns Unit

        // When
        viewModel.insert(movement)
        advanceUntilIdle()

        // Then
        coVerify { repository.insert(movement) }
    }

    @Test
    fun `verificar que la eliminacion de un movimiento llama al repositorio correctamente`() = runTest {
        // Given
        val movement = FinanceMovement(
            id = "doc123",
            concept = "Test Gasto",
            amount = 100.0,
            type = "GASTO",
            date = "01/01/2024",
            profile = "PYME"
        )
        coEvery { repository.delete(movement.id) } returns Unit

        // When
        viewModel.delete(movement)
        advanceUntilIdle()

        // Then
        coVerify { repository.delete(movement.id) }
    }
}
