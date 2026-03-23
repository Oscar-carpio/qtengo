package com.example.qtengo.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qtengo.data.local.dao.ProductDao
import com.example.qtengo.data.local.dao.FinanceDao
import com.example.qtengo.data.local.model.Product
import com.example.qtengo.data.local.model.FinanceMovement

@Database(
    entities = [
        Product::class,
        FinanceMovement::class
    ],
    version = 3, // SUBIMOS VERSION
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qtengo_db"
                )
                    .fallbackToDestructiveMigration() // BORRA BD SI CAMBIA
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}