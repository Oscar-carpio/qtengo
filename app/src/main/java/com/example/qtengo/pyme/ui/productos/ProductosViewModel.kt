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
 * ViewModel que gestiona la lógica de productos y control de inventario en tiempo real.
 */
class ProductosViewModel(
    private val repository: ProductRepository = ProductRepository(),
    private val stockMovementRepository: StockMovementRepository = StockMovementRepository()
) : ViewModel() {

    private val _currentProfile = MutableStateFlow("PYME")

    /**
     * Lista de productos observada desde Firebase, filtrada por el perfil actual.
     */
    val products: LiveData<List<Product>> = _currentProfile.flatMapLatest { profile ->
        repository.getByProfileFlow(profile)
    }.asLiveData()

    /**
     * Filtra y expone los productos que están por debajo de su stock de seguridad.
     */
    val lowStockProducts: LiveData<List<Product>> = products.map { list ->
        list.filter { it.quantity <= it.minStock }
    }

    /**
     * Expone la cantidad total de productos en el catálogo.
     */
    val productCount: LiveData<Int> = products.map { it.size }

    /**
     * Cambia el perfil de trabajo (ej: PYME, Familia) para cargar sus respectivos productos.
     */
    fun loadProfile(profile: String) {
        _currentProfile.value = profile
    }

    /**
     * Registra un nuevo producto y genera un movimiento de entrada inicial.
     */
    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        recordMovement(product, product.quantity, product.quantity)
    }

    /**
     * Actualiza los datos descriptivos de un producto existente.
     */
    fun update(product: Product) = viewModelScope.launch {
        repository.update(product, product.id)
    }

    /**
     * Ajusta la cantidad de stock y registra la diferencia en el historial de movimientos.
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
     * Registra internamente un cambio en el stock en la base de datos de movimientos.
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
     * Elimina el producto del catálogo.
     */
    fun delete(product: Product) = viewModelScope.launch { 
        repository.delete(product.id)
    }
}
