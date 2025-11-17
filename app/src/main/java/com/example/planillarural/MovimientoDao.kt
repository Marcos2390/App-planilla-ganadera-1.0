package com.example.planillarural

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(movimiento: Movimiento)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<Movimiento>

    // ¡NUEVA FUNCIÓN! Obtiene todos los movimientos para un animal específico.
    @Query("SELECT * FROM movimientos WHERE animalId = :animalId ORDER BY fecha DESC")
    suspend fun obtenerPorAnimal(animalId: Int): List<Movimiento>

    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
