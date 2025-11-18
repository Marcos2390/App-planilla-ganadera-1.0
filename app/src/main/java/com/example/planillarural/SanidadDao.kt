package com.example.planillarural

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SanidadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(sanidad: Sanidad)

    @Query("SELECT * FROM sanidad WHERE animalId = :animalId ORDER BY fecha DESC")
    suspend fun obtenerPorAnimal(animalId: Int): List<Sanidad>

    // ¡NUEVO! Obtener todos los registros para exportación
    @Query("SELECT * FROM sanidad ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<Sanidad>
}