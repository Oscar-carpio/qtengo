package com.example.qtengo.restauracion

import com.example.qtengo.restauracion.ui.carta.CartaViewModel
import com.example.qtengo.restauracion.ui.carta.MenuDia
import com.example.qtengo.restauracion.ui.carta.Plato
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
class CartaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CartaViewModel
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
    fun seCreaCorrectamente_conUsuario() {
        // Arrange
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid"

        // Act
        viewModel = CartaViewModel()

        // Assert
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun añadirPlato_noGuardaSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = CartaViewModel()

        // Act
        viewModel.añadirPlato(Plato(nombre = "Sopa", precio = 5.0))

        // Assert
        assertEquals("Usuario no autenticado", viewModel.error.value)
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun guardarMenuDia_noGuardaSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = CartaViewModel()

        // Act
        viewModel.guardarMenuDia(MenuDia(primerPlato = "Sopa"))

        // Assert
        assertEquals("Usuario no autenticado", viewModel.error.value)
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}
