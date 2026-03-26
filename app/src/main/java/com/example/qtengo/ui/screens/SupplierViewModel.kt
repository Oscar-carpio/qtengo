package com.example.qtengo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.Supplier
import com.example.qtengo.data.repository.SupplierRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de la interfaz de proveedores.
 */
class SupplierViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SupplierRepository
    private var currentProfile: String = "PYME"

    // Lista de proveedores expuesta como LiveData para la UI
    lateinit var suppliers: LiveData<List<Supplier>>

    init {
        val dao = AppDatabase.getDatabase(application).supplierDao()
        repository = SupplierRepository(dao)
        loadProfile("PYME")
    }

    /**
     * Carga los proveedores correspondientes al perfil seleccionado.
     */
    fun loadProfile(profile: String) {
        currentProfile = profile
        suppliers = repository.getByProfile(profile)
    }

    /**
     * Inserta un nuevo proveedor.
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
