package com.example.qtengo.familiar.ui.gastos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import kotlinx.coroutines.flow.map
import java.util.*

data class Gasto(
    val id: String = "",
    val descripcion: String = "",
    val cantidad: Double = 0.0,
    val categoria: String = "",
    val tipo: String = "GASTO",
    val fecha: String = "",
    val origen: String = "manual",
    val listaId: String = ""
)

data class GastoRecurrente(
    val id: String = "",
    val descripcion: String = "",
    val cantidad: Double = 0.0,
    val categoria: String = "",
    val fechaCobro: String = ""
)

class GastosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos

    private val _presupuesto = MutableStateFlow<Double?>(null)
    val presupuesto: StateFlow<Double?> = _presupuesto

    private val _gastosRecurrentes = MutableStateFlow<List<GastoRecurrente>>(emptyList())
    val gastosRecurrentes: StateFlow<List<GastoRecurrente>> = _gastosRecurrentes

    val gastosPorCategoria: StateFlow<Map<String, Double>> = _gastos
        .map { lista ->
            lista.filter { it.tipo == "GASTO" }
                .groupBy { it.categoria.ifBlank { "Sin categoría" } }
                .mapValues { (_, items) -> items.sumOf { it.cantidad } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // FIX CRIT #4 — Canal de errores para que la UI los muestre al usuario
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // FIX CRIT #2 — Guardamos los listeners para cancelarlos en onCleared()
    private var gastosListener: ListenerRegistration? = null
    private var recurrentesListener: ListenerRegistration? = null
    private var presupuestoListener: ListenerRegistration? = null

    fun clearError() { _error.value = null }

    // FIX CRIT #3 — Guard centralizado contra uid vacío
    private fun requireUid(): String? {
        if (uid.isBlank()) {
            _error.value = "Usuario no autenticado. Por favor, inicia sesión de nuevo."
            return null
        }
        return uid
    }

    private val _fechaInicio = MutableStateFlow<Date?>(null)
    val fechaInicio: StateFlow<Date?> = _fechaInicio

    private val _fechaFin = MutableStateFlow<Date?>(null)
    val fechaFin: StateFlow<Date?> = _fechaFin

    val gastosFiltrados: StateFlow<List<Gasto>> = combine(_gastos, _fechaInicio, _fechaFin) { gastos, inicio, fin ->
        if (inicio == null && fin == null) {
            gastos
        } else {
            gastos.filter { gasto ->
                val fechaGasto = runCatching { sdf.parse(gasto.fecha) }.getOrNull() ?: return@filter false
                val despuesDeInicio = inicio == null || !fechaGasto.before(inicio)
                val antesDeEnd = fin == null || !fechaGasto.after(fin)
                despuesDeInicio && antesDeEnd
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filtrarPorFechas(inicio: Date?, fin: Date?) {
        _fechaInicio.value = inicio
        _fechaFin.value = fin
    }

    fun limpiarFiltro() {
        _fechaInicio.value = null
        _fechaFin.value = null
    }

    private fun gastosRef() = db.collection("usuarios").document(uid).collection("gastos")
    private fun recurrentesRef() = db.collection("usuarios").document(uid).collection("gastosRecurrentes")
    private fun configRef() = db.collection("usuarios").document(uid)

    // ─── Carga de datos ──────────────────────────────────────────────────────

    fun cargarGastos() {
        val uid = requireUid() ?: return

        // FIX CRIT #2 — cancelamos listener anterior
        gastosListener?.remove()
        gastosListener = db.collection("usuarios").document(uid).collection("gastos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                // FIX CRIT #4 — capturamos error del listener
                if (e != null) {
                    _error.value = "Error al cargar gastos: ${e.message}"
                    return@addSnapshotListener
                }
                _gastos.value = snapshot?.documents?.map { doc ->
                    Gasto(
                        id = doc.id,
                        descripcion = doc.getString("descripcion") ?: "",
                        cantidad = doc.getDouble("cantidad") ?: 0.0,
                        categoria = doc.getString("categoria") ?: "",
                        tipo = doc.getString("tipo") ?: "GASTO",
                        fecha = doc.getString("fecha") ?: "",
                        origen = doc.getString("origen") ?: "manual",
                        listaId = doc.getString("listaId") ?: ""
                    )
                } ?: emptyList()
            }
    }

    fun cargarGastosRecurrentes() {
        val uid = requireUid() ?: return

        // FIX CRIT #2 — cancelamos listener anterior
        recurrentesListener?.remove()
        recurrentesListener = db.collection("usuarios").document(uid).collection("gastosRecurrentes")
            .addSnapshotListener { snapshot, e ->
                // FIX CRIT #4 — capturamos error del listener
                if (e != null) {
                    _error.value = "Error al cargar gastos recurrentes: ${e.message}"
                    return@addSnapshotListener
                }
                _gastosRecurrentes.value = snapshot?.documents?.map { doc ->
                    GastoRecurrente(
                        id = doc.id,
                        descripcion = doc.getString("descripcion") ?: "",
                        cantidad = doc.getDouble("cantidad") ?: 0.0,
                        categoria = doc.getString("categoria") ?: "",
                        fechaCobro = doc.getString("fechaCobro") ?: ""
                    )
                }?.sortedBy { it.fechaCobro } ?: emptyList()
            }
    }

    fun cargarPresupuesto() {
        val uid = requireUid() ?: return

        // FIX CRIT #2 — cancelamos listener anterior
        presupuestoListener?.remove()
        presupuestoListener = db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, e ->
                // FIX CRIT #4 — capturamos error del listener
                if (e != null) {
                    _error.value = "Error al cargar presupuesto: ${e.message}"
                    return@addSnapshotListener
                }
                _presupuesto.value = snapshot?.getDouble("presupuestoMensual")
            }
    }

    // ─── Escritura ───────────────────────────────────────────────────────────

    /**
     * Guarda o actualiza el presupuesto mensual.
     * FIX WARN — usa set/merge en lugar de update para no fallar si el campo no existe.
     */
    fun guardarPresupuesto(cantidad: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            try {
                configRef().set(
                    mapOf("presupuestoMensual" to cantidad),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                _error.value = "Error al guardar presupuesto: ${e.message}"
            }
        }
    }

    fun añadirGasto(descripcion: String, cantidad: Double, categoria: String, tipo: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date())
                val data = mapOf(
                    "descripcion" to descripcion,
                    "cantidad" to cantidad,
                    "categoria" to categoria,
                    "tipo" to tipo,
                    "fecha" to fecha,
                    "origen" to "manual",
                    "listaId" to ""
                )
                gastosRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al añadir gasto: ${e.message}"
            }
        }
    }

    fun añadirGastoRecurrente(descripcion: String, cantidad: Double, categoria: String, fechaCobro: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                val data = mapOf(
                    "descripcion" to descripcion,
                    "cantidad" to cantidad,
                    "categoria" to categoria,
                    "fechaCobro" to fechaCobro
                )
                recurrentesRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al añadir gasto recurrente: ${e.message}"
            }
        }
    }

    fun editarGastoRecurrente(gastoId: String, descripcion: String, cantidad: Double, categoria: String, fechaCobro: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                recurrentesRef().document(gastoId).update(
                    mapOf(
                        "descripcion" to descripcion,
                        "cantidad" to cantidad,
                        "categoria" to categoria,
                        "fechaCobro" to fechaCobro
                    )
                ).await()
            } catch (e: Exception) {
                _error.value = "Error al editar gasto recurrente: ${e.message}"
            }
        }
    }

    fun eliminarGastoRecurrente(gastoId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                recurrentesRef().document(gastoId).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar gasto recurrente: ${e.message}"
            }
        }
    }

    fun editarGasto(gastoId: String, descripcion: String, cantidad: Double, categoria: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                gastosRef().document(gastoId).update(
                    mapOf(
                        "descripcion" to descripcion,
                        "cantidad" to cantidad,
                        "categoria" to categoria
                    )
                ).await()
            } catch (e: Exception) {
                _error.value = "Error al editar gasto: ${e.message}"
            }
        }
    }

    fun registrarGastoDesdeLista(listaId: String, nombreLista: String, cantidad: Double) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date())
                val data = mapOf(
                    "descripcion" to "Compra: $nombreLista",
                    "cantidad" to cantidad,
                    "categoria" to "Alimentación",
                    "tipo" to "GASTO",
                    "fecha" to fecha,
                    "origen" to "lista_compra",
                    "listaId" to listaId
                )
                gastosRef().add(data).await()
            } catch (e: Exception) {
                _error.value = "Error al registrar gasto desde lista: ${e.message}"
            }
        }
    }

    fun eliminarGasto(gastoId: String) {
        requireUid() ?: return
        viewModelScope.launch {
            // FIX CRIT #4
            try {
                gastosRef().document(gastoId).delete().await()
            } catch (e: Exception) {
                _error.value = "Error al eliminar gasto: ${e.message}"
            }
        }
    }

    // ─── Totales ─────────────────────────────────────────────────────────────

    fun totalGastos(): Double {
        val mesActual = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        return _gastos.value
            .filter { it.tipo == "GASTO" && it.fecha.endsWith(mesActual) }
            .sumOf { it.cantidad }
    }

    fun totalRecurrentes(): Double = _gastosRecurrentes.value.sumOf { it.cantidad }

    fun totalIngresos(): Double = _gastos.value
        .filter { it.tipo == "INGRESO" }
        .sumOf { it.cantidad }

    // FIX CRIT #2 — cancelamos todos los listeners al destruirse el ViewModel
    override fun onCleared() {
        super.onCleared()
        gastosListener?.remove()
        recurrentesListener?.remove()
        presupuestoListener?.remove()
    }
}