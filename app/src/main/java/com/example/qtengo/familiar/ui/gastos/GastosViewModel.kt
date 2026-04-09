package com.example.qtengo.familiar.ui.gastos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
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
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos

    private val _presupuesto = MutableStateFlow<Double?>(null)
    val presupuesto: StateFlow<Double?> = _presupuesto

    private val _gastosRecurrentes = MutableStateFlow<List<GastoRecurrente>>(emptyList())
    val gastosRecurrentes: StateFlow<List<GastoRecurrente>> = _gastosRecurrentes

    /** Fecha inicio del filtro (null = sin filtro) */
    private val _fechaInicio = MutableStateFlow<Date?>(null)
    val fechaInicio: StateFlow<Date?> = _fechaInicio

    /** Fecha fin del filtro (null = sin filtro) */
    private val _fechaFin = MutableStateFlow<Date?>(null)
    val fechaFin: StateFlow<Date?> = _fechaFin

    /** Gastos filtrados por rango de fechas */
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

    /** Aplica el filtro de rango de fechas */
    fun filtrarPorFechas(inicio: Date?, fin: Date?) {
        _fechaInicio.value = inicio
        _fechaFin.value = fin
    }

    /** Limpia el filtro de fechas */
    fun limpiarFiltro() {
        _fechaInicio.value = null
        _fechaFin.value = null
    }

    /** Referencia base de gastos en Firestore */
    private fun gastosRef() = db.collection("usuarios").document(uid).collection("gastos")

    /** Referencia base de gastos recurrentes en Firestore */
    private fun recurrentesRef() = db.collection("usuarios").document(uid).collection("gastosRecurrentes")

    /** Referencia al documento de configuración del usuario */
    private fun configRef() = db.collection("usuarios").document(uid)

    /** Carga todos los gastos del usuario en tiempo real */
    fun cargarGastos() {
        gastosRef()
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val result = snapshot?.documents?.map { doc ->
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
                _gastos.value = result
            }
    }

    /** Carga todos los gastos recurrentes en tiempo real */
    fun cargarGastosRecurrentes() {
        recurrentesRef().addSnapshotListener { snapshot, _ ->
            val result = snapshot?.documents?.map { doc ->
                GastoRecurrente(
                    id = doc.id,
                    descripcion = doc.getString("descripcion") ?: "",
                    cantidad = doc.getDouble("cantidad") ?: 0.0,
                    categoria = doc.getString("categoria") ?: "",
                    fechaCobro = doc.getString("fechaCobro") ?: ""
                )
            } ?: emptyList()
            _gastosRecurrentes.value = result.sortedBy { it.fechaCobro }
        }
    }

    /** Carga el presupuesto mensual del usuario */
    fun cargarPresupuesto() {
        configRef().addSnapshotListener { snapshot, _ ->
            _presupuesto.value = snapshot?.getDouble("presupuestoMensual")
        }
    }

    /** Guarda o actualiza el presupuesto mensual */
    fun guardarPresupuesto(cantidad: Double) {
        viewModelScope.launch {
            configRef().update("presupuestoMensual", cantidad).await()
        }
    }

    /** Añade un gasto manualmente */
    fun añadirGasto(descripcion: String, cantidad: Double, categoria: String, tipo: String) {
        viewModelScope.launch {
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
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
        }
    }

    /** Añade un gasto recurrente */
    fun añadirGastoRecurrente(descripcion: String, cantidad: Double, categoria: String, fechaCobro: String) {
        viewModelScope.launch {
            val data = mapOf(
                "descripcion" to descripcion,
                "cantidad" to cantidad,
                "categoria" to categoria,
                "fechaCobro" to fechaCobro
            )
            recurrentesRef().add(data).await()
        }
    }

    /** Edita un gasto recurrente */
    fun editarGastoRecurrente(gastoId: String, descripcion: String, cantidad: Double, categoria: String, fechaCobro: String) {
        viewModelScope.launch {
            recurrentesRef().document(gastoId).update(
                mapOf(
                    "descripcion" to descripcion,
                    "cantidad" to cantidad,
                    "categoria" to categoria,
                    "fechaCobro" to fechaCobro
                )
            ).await()
        }
    }

    /** Elimina un gasto recurrente */
    fun eliminarGastoRecurrente(gastoId: String) {
        viewModelScope.launch {
            recurrentesRef().document(gastoId).delete().await()
        }
    }

    /** Edita un gasto existente */
    fun editarGasto(gastoId: String, descripcion: String, cantidad: Double, categoria: String) {
        viewModelScope.launch {
            gastosRef().document(gastoId).update(
                mapOf(
                    "descripcion" to descripcion,
                    "cantidad" to cantidad,
                    "categoria" to categoria
                )
            ).await()
        }
    }

    /** Registra un gasto desde una lista de la compra */
    fun registrarGastoDesdeLista(listaId: String, nombreLista: String, cantidad: Double) {
        viewModelScope.launch {
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
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
        }
    }

    /** Elimina un gasto */
    fun eliminarGasto(gastoId: String) {
        viewModelScope.launch {
            gastosRef().document(gastoId).delete().await()
        }
    }

    /** Total de gastos del mes actual */
    fun totalGastos(): Double {
        val mesActual = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        return _gastos.value
            .filter { it.tipo == "GASTO" && it.fecha.endsWith(mesActual) }
            .sumOf { it.cantidad }
    }

    /** Total de gastos recurrentes mensuales */
    fun totalRecurrentes(): Double = _gastosRecurrentes.value.sumOf { it.cantidad }

    /** Total de ingresos */
    fun totalIngresos(): Double = _gastos.value
        .filter { it.tipo == "INGRESO" }
        .sumOf { it.cantidad }
}