// Contenido CORRECTO para AnimalDao.kt
package com.example.planillarural

import androidx.room.*

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animales ORDER BY nombre ASC")
    suspend fun obtenerTodos(): List<Animal>

    @Query("SELECT * FROM animales WHERE id = :animalId")
    suspend fun obtenerPorId(animalId: Int): Animal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(animal: Animal)

    @Update
    suspend fun actualizar(animal: Animal)

    @Delete
    suspend fun eliminar(animal: Animal)
}