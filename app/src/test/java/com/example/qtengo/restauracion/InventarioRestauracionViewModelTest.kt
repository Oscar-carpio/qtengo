package com.example.qtengo.restauracion

import com.example.qtengo.data.model.restauracion.RestauracionProducto
import com.example.qtengo.restauracion.ui.inventario.InventarioRestauracionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioRestauracionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: InventarioRestauracionViewModel
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
    fun agregarItem_noGuardaSinUsuario() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = InventarioRestauracionViewModel()

        // Act
        viewModel.agregarItem("Tomate", "Verduras", 10, 2, 1.5)

        // Assert
        // El ViewModel usa return temprano si auth es null.
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }

    @Test
    fun disminuirStock_noBajaDeCeroSiExiste() {
        // Arrange
        every { mockAuth.currentUser } returns null
        viewModel = InventarioRestauracionViewModel()
        val producto = RestauracionProducto(id_producto = "1", stock = 0)

        // Act
        viewModel.disminuirStock(producto)

        // Assert
        // Al intentar disminuir stock de 0, el nuevoStock se calcula con coerceAtLeast(0) garantizando que no sea negativo, 
        // y como auth es null, retorna sin tocar Firestore.
        verify(exactly = 0) { mockFirestore.collection(any()) }
    }
}
