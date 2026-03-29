package com.example.qtengo.pyme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.core.data.repositories.SupplierRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de proveedores en el módulo Pyme.
 */
class SupplierViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SupplierRepository
    private var currentProfile: String = "PYME"

    lateinit var suppliers: LiveData<List<Supplier>>

    init {
        val dao = AppDatabase.getDatabase(application).supplierDao()
        repository = SupplierRepository(dao)
        loadProfile("PYME")
    }

    /**
     * Filtra los proveedores por el perfil seleccionado.
     */
    fun loadProfile(profile: String) {
        currentProfile = profile
        suppliers = repository.getByProfile(profile)
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
            profile = currentProfile
        )
        repository.insert(supplier)
    }

    /**
     * Elimina un proveedor.
     */
    fun delete(supplier: Supplier) = viewModelScope.launch {
        repository.delete(supplier)
    }
}
