package com.example.qtengo.ui.products

import android.app.Application
import androidx.lifecycle.*
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.Product
import com.example.qtengo.data.local.model.StockMovement
import com.example.qtengo.data.repository.ProductRepository
import com.example.qtengo.data.repository.StockMovementRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val stockMovementRepository: StockMovementRepository
    private val currentProfile = MutableLiveData<String>()

    val products: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getByProfile(profile)
    }

    val lowStockProducts: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getLowStock(profile)
    }

    val productCount: LiveData<Int> = currentProfile.switchMap { profile ->
        repository.countProducts(profile)
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProductRepository(db.productDao())
        stockMovementRepository = StockMovementRepository(db.stockMovementDao())
        currentProfile.value = "FAMILIA"
    }

    fun loadProfile(profile: String) {
        currentProfile.value = profile
    }

    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        // Opcional: registrar movimiento inicial
        recordMovement(product, product.quantity, product.quantity)
    }

    fun update(product: Product) = viewModelScope.launch {
        // Para registrar el cambio, necesitamos la cantidad anterior. 
        // Como Room no nos la da directamente en el update, lo ideal sería obtener el producto antes.
        // Pero para simplificar en este ejemplo, si el update viene de los botones +/-:
        repository.update(product)
    }

    /**
     * Actualiza la cantidad y registra el movimiento.
     */
    fun updateQuantity(product: Product, newQuantity: Double) = viewModelScope.launch {
        val diff = newQuantity - product.quantity
        if (diff != 0.0) {
            val updatedProduct = product.copy(quantity = newQuantity)
            repository.update(updatedProduct)
            recordMovement(updatedProduct, diff, newQuantity)
        }
    }

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

    fun delete(product: Product) = viewModelScope.launch { repository.delete(product) }
}
