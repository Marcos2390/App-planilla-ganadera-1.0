package com.example.planillarural

import androidx.room.*

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animales ORDER BY nombre ASC")
    suspend fun obtenerTodos(): List<Animal>

    @Query("SELECT * FROM animales WHERE id = :animalId")
    suspend fun obtenerPorId(animalId: Int): Animal?

    @Query("SELECT * FROM animales WHERE nombre LIKE :query || '%' ORDER BY nombre ASC")
    suspend fun buscarPorNombre(query: String): List<Animal>

    @Query("""
        SELECT categoria, SUM(
            CASE
                WHEN UPPER(tipo) IN ('NACIMIENTO', 'COMPRA', 'REGISTRO INICIAL') THEN cantidad
                WHEN UPPER(tipo) IN ('MUERTE', 'VENTA') THEN -cantidad
                ELSE 0
            END
        ) as count
        FROM movimientos
        GROUP BY categoria
    """)
    suspend fun contarPorCategoria(): List<CategoryCount>

    // ¡CORRECCIÓN! Se separa en dos funciones para evitar el borrado al editar.
    @Insert
    suspend fun insertar(animal: Animal): Long

    @Update
    suspend fun actualizar(animal: Animal)

    @Delete
    suspend fun eliminar(animal: Animal)
}