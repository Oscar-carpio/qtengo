package com.example.qtengo.ui.restauracion

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.qtengo.ui.restauracion.inventario.InventarioRestauracionViewModel
import com.example.qtengo.ui.restauracion.menu.MenuViewModel
import com.example.qtengo.ui.restauracion.reservas.ReservasViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

// --- REGLA PARA MANEJAR CORRUTINAS ---
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            Dispatchers.setMain(testDispatcher)
            try { base.evaluate() } finally { Dispatchers.resetMain() }
        }
    }
}

class ViewModelsTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks comunes
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDoc: DocumentReference

    @Before
    fun setup() {
        mockFirestore = mockk(relaxed = true)
        mockAuth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        mockCollection = mockk(relaxed = true)
        mockDoc = mockk(relaxed = true)

        // Mocking estático para Firebase
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)

        every { FirebaseFirestore.getInstance() } returns mockFirestore
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_uid"

        // Simular ruta: usuarios/test_uid/subcoleccion
        every { mockFirestore.collection("usuarios") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDoc
        every { mockDoc.collection(any()) } returns mockCollection
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // --- TESTS PARA MENUVIEWMODEL ---
    @Test
    fun `MenuViewModel agregarPlato llama a add en firestore`() = runTest {
        val viewModel = MenuViewModel()
        viewModel.agregarPlato("Pizza", 15.0)

        verify { mockCollection.add(any()) }
    }

    @Test
    fun `MenuViewModel eliminarPlato llama a delete en firestore`() = runTest {
        val viewModel = MenuViewModel()
        val platoId = "id_123"
        every { mockCollection.document(platoId) } returns mockDoc

        viewModel.eliminarPlato(platoId)

        verify { mockDoc.delete() }
    }

    // --- TESTS PARA RESERVASVIEWMODEL ---
    @Test
    fun `ReservasViewModel agregarReserva intenta guardar datos`() = runTest {
        val viewModel = ReservasViewModel()

        viewModel.agregarReserva("Astrid", 4, "Mesa ventana")

        verify {
            mockCollection.add(match { data ->
                val map = data as? Map<String, Any>
                map?.get("nombreCliente") == "Astrid" && map?.get("comensales") == 4
            })
        }
    }
    @Test
    fun `ReservasViewModel eliminarReserva llama a delete`() = runTest {
        val viewModel = ReservasViewModel()
        val reservaId = "reserva_abc"
        every { mockCollection.document(reservaId) } returns mockDoc

        viewModel.eliminarReserva(reservaId)

        verify { mockDoc.delete() }
    }

    // --- TESTS PARA INVENTARIOVIEWMODEL ---
    @Test
    fun `InventarioViewModel agregarItem verifica que el usuario no sea null`() = runTest {
        val viewModel = InventarioRestauracionViewModel()
        viewModel.agregarItem("Harina", "Insumos", 10, 2, 5.0)

        verify { mockCollection.add(any()) }
    }

    @Test
    fun `InventarioViewModel actualizarStock llama a update con valor correcto`() = runTest {
        val viewModel = InventarioRestauracionViewModel()
        val prodId = "prod_99"
        every { mockCollection.document(prodId) } returns mockDoc

        viewModel.actualizarStock( prodId, 25)

        verify { mockDoc.update("stock", 25) }
    }

    @Test
    fun `InventarioViewModel disminuirStock nunca baja de cero`() = runTest {
        val viewModel = InventarioRestauracionViewModel()
        // Mock de un producto con stock 0
        val productoMock = mockk<RestauracionProducto>()
        every { productoMock.id_producto } returns "p1"
        every { productoMock.stock } returns 0

        viewModel.disminuirStock(productoMock)

        // Verificamos que se intente actualizar a 0, no a -1
        verify { mockDoc.update("stock", 0) }
    }
}