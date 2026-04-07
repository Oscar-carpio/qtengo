package com.example.qtengo.pyme.ui

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.core.data.repositories.SupplierRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de proveedores en el módulo Pyme usando Firebase.
 */
class SupplierViewModel : ViewModel() {

    private val repository = SupplierRepository()
    private val _profileFilter = MutableLiveData<String>()

    val suppliers: LiveData<List<Supplier>> = _profileFilter.switchMap { profile ->
        repository.getByProfileFlow(profile).asLiveData()
    }

    init {
        loadProfile("PYME")
    }

    /**
     * Filtra los proveedores por el perfil seleccionado.
     */
    fun loadProfile(profile: String) {
        _profileFilter.value = profile
    }

    /**
     * Añade un nuevo proveedor a la base de datos.
     */
    fun insert(name: String, contact: String, phone: String, email: String, category: String) = viewModelScope.launch {
        val supplier = Supplier(
            name = name,
            contactName = contact,
            phone = phone,
            email = email,
            category = category,
            profile = _profileFilter.value ?: "PYME"
        )
        repository.insert(supplier)
    }

    /**
     * Elimina un proveedor.
     */
    fun delete(supplier: Supplier) = viewModelScope.launch {
        repository.delete(supplier.id)
    }
}
