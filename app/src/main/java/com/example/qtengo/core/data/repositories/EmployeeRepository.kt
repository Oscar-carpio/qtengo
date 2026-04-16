package com.example.qtengo.core.data.repositories

import android.util.Log
import com.example.qtengo.core.domain.models.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmployeeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun collection() = firestore
        .collection("usuarios")
        .document(auth.currentUser?.uid ?: "")
        .collection("employees")

    fun getByProfileFlow(profile: String): Flow<List<Employee>> = callbackFlow {
        val listener = collection()
            .whereEqualTo("profile", profile)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EmployeeRepository", "Error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val employees = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Employee::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(employees)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(employee: Employee) {
        try { collection().add(employee).await() }
        catch (e: Exception) { Log.e("EmployeeRepository", "Error insertando: ${e.message}") }
    }

    suspend fun update(employee: Employee) {
        try { collection().document(employee.id).set(employee).await() }
        catch (e: Exception) { Log.e("EmployeeRepository", "Error actualizando: ${e.message}") }
    }

    suspend fun delete(employeeId: String) {
        try { collection().document(employeeId).delete().await() }
        catch (e: Exception) { Log.e("EmployeeRepository", "Error eliminando: ${e.message}") }
    }
}