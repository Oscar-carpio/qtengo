package com.example.qtengo.restauracion

import com.example.qtengo.restauracion.ui.menu.MenuViewModel
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
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MenuViewModel
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
    fun actualizarFiltro_cambiaFiltroSiExiste() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = MenuViewModel()

        // Act
        viewModel.actualizarFiltro("Postre")

        // Assert
        assertEquals("Postre", viewModel.filtro.value)
    }

    @Test
    fun agregarPlato_noGuardaSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = MenuViewModel()

        // Act
        viewModel.agregarPlato("Tarta", 3.5)

        // Assert
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun eliminarPlato_noBorraSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = MenuViewModel()

        // Act
        viewModel.eliminarPlato("plato-123")

        // Assert
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}
