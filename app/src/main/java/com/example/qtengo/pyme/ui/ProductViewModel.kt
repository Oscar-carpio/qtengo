package com.example.qtengo.pyme.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.core.domain.models.StockMovement
import com.example.qtengo.core.data.repositories.ProductRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel encargado de la lógica de negocio para la gestión de productos y stock en el módulo Pyme.
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val stockMovementRepository: StockMovementRepository
    private val currentProfile = MutableLiveData<String>()

    /**
     * Lista reactiva de todos los productos del perfil actual.
     */
    val products: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getByProfile(profile)
    }

    /**
     * Lista reactiva de productos con stock por debajo del mínimo establecido.
     */
    val lowStockProducts: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getLowStock(profile)
    }

    /**
     * Contador total de productos registrados para el perfil.
     */
    val productCount: LiveData<Int> = currentProfile.switchMap { profile ->
        repository.countProducts(profile)
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProductRepository(db.productDao())
        stockMovementRepository = StockMovementRepository(db.stockMovementDao())
        currentProfile.value = "FAMILIA"
    }

    /**
     * Establece el perfil activo para filtrar los productos en la interfaz.
     */
    fun loadProfile(profile: String) {
        currentProfile.value = profile
    }

    /**
     * Inserta un nuevo producto en la base de datos y registra el movimiento inicial de stock.
     */
    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        recordMovement(product, product.quantity, product.quantity)
    }

    /**
     * Actualiza la información general de un producto.
     * Metodo seleccionado para borrar, falta confirmar (se prefiere usar updateQuantity para cambios de stock).
     */
    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
    }

    /**
     * Actualiza la cantidad de stock de un producto y genera automáticamente un registro de movimiento.
     */
    fun updateQuantity(product: Product, newQuantity: Double) = viewModelScope.launch {
        val diff = newQuantity - product.quantity
        if (diff != 0.0) {
            val updatedProduct = product.copy(quantity = newQuantity)
            repository.update(updatedProduct)
            recordMovement(updatedProduct, diff, newQuantity)
        }
    }

    /**
     * Crea un registro histórico de la entrada o salida de stock.
     */
    private suspend fun recordMovement(product: Product, diff: Double, total: Double) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val movement = StockMovement(
            productId = product.id,
            productName = product.name,
            quantityChanged = diff,
            newQuantity = total,
            date = date,
            profile = product.profile
        )
        stockMovementRepository.insert(movement)
    }

    /**
     * Elimina permanentemente un producto de la base de datos.
     */
    fun delete(product: Product) = viewModelScope.launch { 
        repository.delete(product) 
    }
}
