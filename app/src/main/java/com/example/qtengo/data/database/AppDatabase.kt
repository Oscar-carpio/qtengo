package com.example.qtengo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qtengo.data.dao.*
import com.example.qtengo.data.model.*

/**
 * Base de datos principal de la aplicación que utiliza Room.
 * Define las tablas (entities) y la versión de la base de datos.
 */
@Database(
    entities = [
        Product::class, 
        Expense::class, 
        Supplier::class, 
        Employee::class,
        Task::class
    ],
    version = 4, // Incrementamos la versión para la tabla de tareas
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs (Objetos de Acceso a Datos)
    abstract fun productDao(): ProductDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Método para obtener la instancia única de la base de datos (Singleton).
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockapp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
