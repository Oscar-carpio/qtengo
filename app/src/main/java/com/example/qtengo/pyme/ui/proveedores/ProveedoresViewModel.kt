/**
 * ViewModel responsable de gestionar la lógica de negocio de los proveedores.
 * 
 * Facilita las operaciones CRUD sobre la colección de proveedores en Firestore,
 * permitiendo filtrar la información por el perfil de usuario activo.
 */
package com.example.qtengo.pyme.ui.proveedores

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Supplier
import com.example.qtengo.core.data.repositories.SupplierRepository
import kotlinx.coroutines.launch

/**
 * ViewModel que conecta la interfaz de Proveedores con su repositorio.
 * 
 * @param repository Repositorio encargado del acceso a datos de proveedores.
 */
class ProveedoresViewModel(
    private val repository: SupplierRepository = SupplierRepository()
) : ViewModel() {

    private val _profileFilter = MutableLiveData<String>()

    /**
     * Lista reactiva de proveedores filtrada según el perfil configurado.
     */
    val suppliers: LiveData<List<Supplier>> = _profileFilter.switchMap { profile ->
        repository.getByProfileFlow(profile).asLiveData()
    }

    init {
        // Inicialización predeterminada para el entorno PYME
        loadProfile("PYME")
    }

    /**
     * Define el perfil actual para la consulta de proveedores.
     * @param profile Identificador del perfil (ej: "PYME", "FAMILIAR").
     */
    fun loadProfile(profile: String) {
        _profileFilter.value = profile
    }

    /**
     * Crea un nuevo registro de proveedor en Firestore.
     * @param name Nombre de la empresa proveedora.
     * @param contact Nombre del contacto principal.
     * @param phone Teléfono de contacto.
     * @param email Correo electrónico.
     * @param category Categoría o descripción del servicio/producto que provee.
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
     * Actualiza la información de un proveedor existente.
     * @param supplier Objeto proveedor con los datos actualizados.
     */
    fun update(supplier: Supplier) = viewModelScope.launch {
        repository.update(supplier)
    }

    /**
     * Elimina un proveedor del sistema.
     * @param supplierId Identificador único del proveedor a borrar.
     */
    fun delete(supplierId: String) = viewModelScope.launch {
        repository.delete(supplierId)
    }
}
