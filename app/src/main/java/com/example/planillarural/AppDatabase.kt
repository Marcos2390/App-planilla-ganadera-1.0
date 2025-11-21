package com.example.planillarural

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ¡VERSIÓN 18! Actualizada por campo potreroId en Animal
@Database(entities = [Animal::class, Sanidad::class, Movimiento::class, NacimientoPendiente::class, Anotacion::class, Potrero::class, LotePotrero::class], version = 18, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun sanidadDao(): SanidadDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun nacimientoPendienteDao(): NacimientoPendienteDao
    abstract fun anotacionDao(): AnotacionDao
    abstract fun potreroDao(): PotreroDao

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