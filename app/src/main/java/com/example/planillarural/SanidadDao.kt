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

    @Query("SELECT * FROM sanidad ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<Sanidad>

    // ¡NUEVO! Buscar tratamientos pendientes por fecha de próxima dosis
    @Query("SELECT * FROM sanidad WHERE fechaProximaDosis IS NOT NULL AND fechaProximaDosis = :fecha")
    suspend fun buscarPendientesPorFecha(fecha: String): List<Sanidad>
}