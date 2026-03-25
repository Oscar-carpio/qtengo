package com.example.qtengo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.model.Employee

/**
 * Interfaz para definir las operaciones de base de datos para los empleados.
 */
@Dao
interface EmployeeDao {

    // Obtener todos los empleados de un perfil específico (Pyme o Hostelería)
    @Query("SELECT * FROM employees WHERE profile = :profile ORDER BY name ASC")
    fun getByProfile(profile: String): LiveData<List<Employee>>

    // Insertar un nuevo empleado (si ya existe, lo reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee)

    // Actualizar los datos de un empleado existente
    @Update
    suspend fun update(employee: Employee)

    // Eliminar un empleado de la base de datos
    @Delete
    suspend fun delete(employee: Employee)
}
