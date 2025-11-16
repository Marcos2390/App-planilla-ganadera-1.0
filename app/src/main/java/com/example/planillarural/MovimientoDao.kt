package com.example.planillarural

import androidx.room.*

@Dao
interface MovimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(mov: Movimiento)

    @Query("SELECT * FROM movimiento WHERE animalId = :idAnimal")
    suspend fun obtenerPorAnimal(idAnimal: Int): List<Movimiento>
}