package com.example.planillarural

import androidx.room.*

@Dao
interface AnimalDao {

    // Query para la lista principal (SOLO ACTIVOS)
    @Query("SELECT * FROM animales WHERE status = 'Activo' ORDER BY nombre ASC")
    suspend fun obtenerTodosActivos(): List<Animal>

    // ¡NUEVO! Query para la exportación (TODOS LOS ANIMALES)
    @Query("SELECT * FROM animales ORDER BY status ASC, nombre ASC")
    suspend fun obtenerTodosConBajas(): List<Animal>

    @Query("SELECT * FROM animales WHERE id = :animalId")
    suspend fun obtenerPorId(animalId: Int): Animal?

    @Query("SELECT * FROM animales WHERE nombre LIKE :query || '%' AND status = 'Activo' ORDER BY nombre ASC")
    suspend fun buscarPorNombre(query: String): List<Animal>

    // ... (el resto de las funciones no cambian) ...
    @Query("""
        SELECT categoria, SUM(
            CASE
                WHEN UPPER(tipo) IN ('NACIMIENTO', 'COMPRA', 'REGISTRO INICIAL') THEN cantidad
                WHEN UPPER(tipo) IN ('MUERTE', 'VENTA') THEN -cantidad
                ELSE 0
            END
        ) as count
        FROM movimientos
        WHERE especie = 'Bovino' 
        GROUP BY categoria
    """)
    suspend fun contarPorCategoria(): List<CategoryCount>

    @Insert
    suspend fun insertar(animal: Animal): Long

    @Update
    suspend fun actualizar(animal: Animal)

    @Delete
    suspend fun eliminar(animal: Animal)
}
