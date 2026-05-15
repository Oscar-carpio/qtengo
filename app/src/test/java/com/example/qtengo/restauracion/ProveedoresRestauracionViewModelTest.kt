package com.example.qtengo.restauracion

import com.example.qtengo.restauracion.ui.proveedores.Proveedor
import com.example.qtengo.restauracion.ui.proveedores.ProveedoresRestauracionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProveedoresRestauracionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ProveedoresRestauracionViewModel
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)

        mockAuth = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { FirebaseFirestore.getInstance() } returns mockFirestore
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun actualizarFiltro_cambiaFiltro() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = ProveedoresRestauracionViewModel()

        // Act
        viewModel.actualizarFiltro("Bebidas")

        // Assert
        assertEquals("Bebidas", viewModel.filtro.value)
    }

    @Test
    fun agregarProveedor_noGuardaSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = ProveedoresRestauracionViewModel()

        // Act
        viewModel.agregarProveedor(Proveedor(nombre = "Coca Cola"))

        // Assert
        assertEquals("Usuario no autenticado. Por favor, inicia sesión de nuevo.", viewModel.error.value)
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun eliminarProveedor_noBorraSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = ProveedoresRestauracionViewModel()

        // Act
        viewModel.eliminarProveedor("prov-123")

        // Assert
        assertEquals("Usuario no autenticado. Por favor, inicia sesión de nuevo.", viewModel.error.value)
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}
