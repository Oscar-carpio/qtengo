package com.example.qtengo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.model.Expense

/**
 * Interfaz para definir las operaciones de base de datos para los gastos e ingresos.
 */
@Dao
interface ExpenseDao {

    // Obtener todos los movimientos de un perfil (familia o pyme)
    @Query("SELECT * FROM expenses WHERE profile = :profile ORDER BY date DESC")
    fun getByProfile(profile: String): LiveData<List<Expense>>

    // Obtener la suma total de gastos (devuelve null si no hay registros, por eso usamos Double?)
    @Query("SELECT SUM(amount) FROM expenses WHERE profile = :profile AND type = 'GASTO'")
    fun getTotalExpenses(profile: String): LiveData<Double?>

    // Obtener la suma total de ingresos
    @Query("SELECT SUM(amount) FROM expenses WHERE profile = :profile AND type = 'INGRESO'")
    fun getTotalIncomes(profile: String): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
