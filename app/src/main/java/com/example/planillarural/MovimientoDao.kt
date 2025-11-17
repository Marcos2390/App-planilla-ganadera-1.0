package com.example.planillarural

import androidx.room.Dao
import androidx.room.Delete // ¡NUEVO! Importar la anotación para eliminar.
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// @Dao le dice a Room que esta es una interfaz para acceder a datos.
@Dao
interface MovimientoDao {

    // @Insert le dice a Room qué hacer cuando se llama a la función 'registrar'.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrar(movimiento: Movimiento)

    // @Query permite hacer consultas personalizadas para obtener todos los movimientos.
    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<Movimiento>

    // ¡NUEVA FUNCIÓN!
    // @Delete le dice a Room que esta función se encarga de borrar un objeto de la tabla.
    @Delete
    suspend fun eliminar(movimiento: Movimiento)
}
