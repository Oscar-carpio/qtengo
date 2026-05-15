package com.example.qtengo.pyme.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.qtengo.core.data.repositories.SupplierRepository
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.pyme.ui.proveedores.ProveedoresViewModel
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
 * Tests unitarios para [ProveedoresViewModel].
 * 
 * Valida la gestión del catálogo de proveedores y el filtrado por perfil.
 */
@ExperimentalCoroutinesApi
class ProveedoresViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<SupplierRepository>(relaxed = true)
    private lateinit var viewModel: ProveedoresViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getByProfileFlow(any()) } returns flowOf(emptyList())
        viewModel = ProveedoresViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifica que al cambiar el perfil se carguen solo los proveedores asociados.
     */
    @Test
    fun cargarPerfil_cargaLosProveedoresDelPerfilIndicado() = runTest {
        val proveedoresMock = listOf(Supplier(id = "1", name = "Proveedor A", profile = "PYME"))
        every { repository.getByProfileFlow("PYME") } returns flowOf(proveedoresMock)

        val observer = mockk<Observer<List<Supplier>>>(relaxed = true)
        viewModel.suppliers.observeForever(observer)

        viewModel.cargarPerfil("PYME")
        advanceUntilIdle()

        verify { observer.onChanged(proveedoresMock) }
        Assert.assertEquals(proveedoresMock, viewModel.suppliers.value)
    }

    /**
     * Valida que la inserción de un proveedor asocie correctamente el perfil activo.
     */
    @Test
    fun insertar_llamaAlRepositorioConLosDatosCorrectos() = runTest {
        viewModel.cargarPerfil("PYME")
        advanceUntilIdle()

        viewModel.insertar("Empresa X", "Juan", "600000000", "test@test.com", "Alimentación")
        advanceUntilIdle()

        coVerify {
            repository.insert(match {
                it.name == "Empresa X" &&
                        it.contactName == "Juan" &&
                        it.profile == "PYME"
            })
        }
    }

    /**
     * Asegura que las actualizaciones y eliminaciones se propaguen al repositorio.
     */
    @Test
    fun actualizar_llamaAlRepositorio() = runTest {
        val supplier = Supplier(id = "123", name = "Update Test")
        viewModel.actualizar(supplier)
        advanceUntilIdle()
        coVerify { repository.update(supplier) }
    }

    @Test
    fun eliminar_llamaAlRepositorio() = runTest {
        val id = "id_delete"
        viewModel.eliminar(id)
        advanceUntilIdle()
        coVerify { repository.delete(id) }
    }
}
