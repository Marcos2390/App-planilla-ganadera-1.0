package com.example.planillarural

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Animal::class, Sanidad::class], version = 1, exportSchema = false)
abstract class PlanillaDB : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun sanidadDao(): SanidadDao

    companion object {
        @Volatile
        private var INSTANCE: PlanillaDB? = null

        fun getDatabase(context: Context): PlanillaDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanillaDB::class.java,
                    "planilla_rural_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
