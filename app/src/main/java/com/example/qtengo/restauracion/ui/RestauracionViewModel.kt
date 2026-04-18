package com.example.qtengo.restauracion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.model.restauracion.*
import com.example.qtengo.data.repository.restauracion.RestauracionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestauracionViewModel : ViewModel() {

    private val repository = RestauracionRepository()

    private val _productos = MutableStateFlow<List<RestauracionProducto>>(emptyList())
    //val producto.s: StateFlow<List<RestauracionProducto>> = _productos.asStateFlow()

    private val _proveedores = MutableStateFlow<List<RestauracionProveedor>>(emptyList())
    val proveedores: StateFlow<List<RestauracionProveedor>> = _proveedores.asStateFlow()

    private val _pedidos = MutableStateFlow<List<RestauracionPedido>>(emptyList())
    val pedidos: StateFlow<List<RestauracionPedido>> = _pedidos.asStateFlow()

    private val _movimientos = MutableStateFlow<List<RestauracionMovimiento>>(emptyList())
    val movimientos: StateFlow<List<RestauracionMovimiento>> = _movimientos.asStateFlow()

    fun cargarDatos() {
        viewModelScope.launch {
            _productos.value = repository.getProductos()
            _proveedores.value = repository.getProveedores()
            _pedidos.value = repository.getPedidos()
            _movimientos.value = repository.getMovimientos()
        }
    }

    fun agregarProductoPrueba() {
        viewModelScope.launch {
            val p = RestauracionProducto(
                nombre = "Producto Prueba",
                categoria = "Pruebas",
                stock = 10,
                stock_minimo = 2,
                precio = 15.5
            )
            repository.addProducto(p)
            _productos.value = repository.getProductos()
        }
    }

    fun agregarProveedorPrueba() {
        viewModelScope.launch {
            val p = RestauracionProveedor(
                nombre = "Proveedor Prueba",
                direccion = "Calle 123",
                email = "test@proveedor.com",
                telefono = "123456789"
            )
            repository.addProveedor(p)
            _proveedores.value = repository.getProveedores()
        }
    }

    fun agregarProveedor(proveedor: RestauracionProveedor) {
        viewModelScope.launch {
            repository.addProveedor(proveedor)
            _proveedores.value = repository.getProveedores()
        }
    }

    fun agregarPedidoPrueba() {
        viewModelScope.launch {
            val p = RestauracionPedido(
                fecha = System.currentTimeMillis(),
                id_proveedor = "proveedor_prueba_id"
            )
            repository.addPedido(p)
            _pedidos.value = repository.getPedidos()
        }
    }

    fun agregarMovimientoPrueba() {
        viewModelScope.launch {
            val m = RestauracionMovimiento(
                tipo = "ENTRADA",
                fecha = System.currentTimeMillis(),
                cantidad = 5
            )
            repository.addMovimiento(m)
            _movimientos.value = repository.getMovimientos()
        }
    }
}
