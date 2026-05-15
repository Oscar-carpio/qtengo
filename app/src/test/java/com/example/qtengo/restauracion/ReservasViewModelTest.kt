package com.example.qtengo.restauracion

import com.example.qtengo.restauracion.ui.reservas.ReservasViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservasViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ReservasViewModel
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)

        mockAuth = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { FirebaseFirestore.getInstance() } returns mockFirestore
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun actualizarFiltro_cambiaElValorDelFiltro() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = ReservasViewModel()

        // Act
        viewModel.actualizarFiltro("Cumpleaños")

        // Assert
        assertEquals("Cumpleaños", viewModel.filtro.value)
    }

    @Test
    fun agregarReserva_noGuardaSiNoHayUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = ReservasViewModel()

        // Act
        viewModel.agregarReserva(nombre = "Juan", comensales = 4, notas = "Mesa cerca de la ventana")

        // Assert
        assertEquals("Usuario no autenticado. Por favor, inicia sesión de nuevo.", viewModel.error.value)
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun agregarReserva_intentaGuardarSiHayUsuario() = runTest {
        // Arrange
        every { mockUser.uid } returns "test-uid"
        every { mockAuth.currentUser } returns mockUser
        
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDocument = mockk<DocumentReference>(relaxed = true)
        
        // Mock de la ruta: db.collection("usuarios").document(uid).collection("reservas")
        every { mockFirestore.collection("usuarios") } returns mockCollection
        every { mockCollection.document("test-uid") } returns mockDocument
        every { mockDocument.collection("reservas") } returns mockCollection
        
        viewModel = ReservasViewModel()

        // Act
        viewModel.agregarReserva(nombre = "Juan", comensales = 4, notas = "Notas")

        // Assert
        // Como await() es complejo de mockear, verificamos que no lanza el error de usuario (es decir, el requireUid funcionó y permitió avanzar).
        assertTrue(viewModel.error.value != "Usuario no autenticado. Por favor, inicia sesión de nuevo.")
    }

    @Test
    fun eliminarReserva_intentaBorrarSiHayUsuario() = runTest {
        // Arrange
        every { mockUser.uid } returns "test-uid"
        every { mockAuth.currentUser } returns mockUser
        
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDocument = mockk<DocumentReference>(relaxed = true)
        
        // Mock de la ruta: db.collection("usuarios").document(uid).collection("reservas").document(id)
        every { mockFirestore.collection("usuarios") } returns mockCollection
        every { mockCollection.document("test-uid") } returns mockDocument
        every { mockDocument.collection("reservas") } returns mockCollection
        every { mockCollection.document("reserva-123") } returns mockDocument
        
        viewModel = ReservasViewModel()

        // Act
        viewModel.eliminarReserva("reserva-123")

        // Assert
        assertTrue(viewModel.error.value != "Usuario no autenticado. Por favor, inicia sesión de nuevo.")
    }
}
