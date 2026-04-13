/**
 * ViewModel responsable de la gestión de inventario y catálogo de productos.
 * 
 * Centraliza la lógica de control de stock, alertas de reposición y el historial
 * automático de movimientos de mercancía (entradas y salidas).
 */
package com.example.qtengo.pyme.ui.productos

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.core.domain.models.StockMovement
import com.example.qtengo.core.data.repositories.ProductRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel que conecta la UI de Almacén con Firestore.
 * 
 * @param repository Repositorio para la gestión de la colección de productos.
 * @param stockMovementRepository Repositorio para auditar cambios de inventario.
 */
class ProductosViewModel(
    private val repository: ProductRepository = ProductRepository(),
    private val stockMovementRepository: StockMovementRepository = StockMovementRepository()
) : ViewModel() {

    private val _currentProfile = MutableStateFlow("PYME")

    /**
     * Lista reactiva de productos, sincronizada con Firebase y filtrada por perfil.
     */
    val products: LiveData<List<Product>> = _currentProfile.flatMapLatest { profile ->
        repository.getByProfileFlow(profile)
    }.asLiveData()

    /**
     * Filtra los productos cuya cantidad actual es menor o igual a su stock mínimo definido.
     */
    val lowStockProducts: LiveData<List<Product>> = products.map { list ->
        list.filter { it.quantity <= it.minStock }
    }

    /**
     * Cantidad total de referencias distintas en el almacén.
     */
    val productCount: LiveData<Int> = products.map { it.size }

    /**
     * Define el contexto de trabajo (PYME/Familia) para cargar el inventario correspondiente.
     */
    fun loadProfile(profile: String) {
        _currentProfile.value = profile
    }

    /**
     * Registra un nuevo producto y genera un movimiento de entrada inicial por la cantidad total.
     */
    fun insert(product: Product) = viewModelScope.launch {
        val currentList = products.value ?: emptyList()
        
        // Generar ID automático: 001 + Inicial de la unidad
        val nextNumber = if (currentList.isEmpty()) {
            1
        } else {
            currentList.mapNotNull { 
                it.customId.take(3).toIntOrNull() 
            }.maxOrNull()?.plus(1) ?: 1
        }
        
        val unitInitial = if (product.unit.isNotBlank()) product.unit.take(1).uppercase() else "U"
        val generatedCustomId = String.format("%03d%s", nextNumber, unitInitial)
        
        val productWithId = product.copy(customId = generatedCustomId)
        
        repository.insert(productWithId)
        recordMovement(productWithId, productWithId.quantity, productWithId.quantity)
    }

    /**
     * Actualiza metadatos del producto (nombre, descripción, categoría) sin alterar el stock.
     */
    fun update(product: Product) = viewModelScope.launch {
        repository.update(product, product.id)
    }

    /**
     * Realiza un ajuste de stock y registra automáticamente el cambio en el historial.
     * @param product Producto a modificar.
     * @param newQuantity Nueva cifra total de existencias.
     */
    fun updateQuantity(product: Product, newQuantity: Double) = viewModelScope.launch {
        val diff = newQuantity - product.quantity
        if (diff != 0.0) {
            val updatedProduct = product.copy(quantity = newQuantity)
            repository.update(updatedProduct, product.id)
            recordMovement(updatedProduct, diff, newQuantity)
        }
    }

    /**
     * Función interna para registrar auditorías de cambios en el inventario.
     */
    private suspend fun recordMovement(product: Product, diff: Double, total: Double) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val movement = StockMovement(
            productId = product.id.hashCode(),
            productName = product.name,
            quantityChanged = diff,
            newQuantity = total,
            date = date,
            profile = product.profile
        )
        stockMovementRepository.insert(movement)
    }

    /**
     * Elimina una referencia del catálogo permanentemente.
     */
    fun delete(product: Product) = viewModelScope.launch { 
        repository.delete(product.id)
    }
}
