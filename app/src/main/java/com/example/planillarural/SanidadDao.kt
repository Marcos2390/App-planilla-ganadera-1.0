package com.example.planillarural

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ¡CORRECCIÓN! Este archivo debe tener la anotación @Dao
@Dao
interface SanidadDao { // <-- La declaración es una "interface", no una "data class".

    // Define una función para insertar un registro de sanidad.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(sanidad: Sanidad)

    // Define una función para buscar todos los registros de sanidad de un animal específico.
    @Query("SELECT * FROM sanidad WHERE animalId = :animalId ORDER BY fecha DESC")
    suspend fun obtenerPorAnimal(animalId: Int): List<Sanidad>
}

