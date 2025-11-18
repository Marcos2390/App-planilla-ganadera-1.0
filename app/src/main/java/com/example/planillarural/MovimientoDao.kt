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

    @Query("SELECT * FROM movimientos WHERE animalId = :animalId ORDER BY fecha DESC")
    suspend fun obtenerPorAnimal(animalId: Int): List<Movimiento>

    // ¡NUEVA FUNCIÓN! Suma las cantidades de los movimientos de un tipo específico.
    @Query("SELECT SUM(cantidad) FROM movimientos WHERE tipo = :tipoMovimiento")
    suspend fun calcularTotalPorTipo(tipoMovimiento: String): Int?

    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
