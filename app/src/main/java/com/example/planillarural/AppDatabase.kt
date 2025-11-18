package com.example.planillarural

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Animal::class, Sanidad::class, Movimiento::class, NacimientoPendiente::class], version = 9, exportSchema = false) // ¡VERSIÓN 9!
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun sanidadDao(): SanidadDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun nacimientoPendienteDao(): NacimientoPendienteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "planilla_rural_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
