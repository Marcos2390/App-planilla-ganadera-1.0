package com.example.planillarural

import androidx.room.*

@Dao
interface VentaPreventaDao {
    @Query("SELECT * FROM ventas_preventa ORDER BY id DESC")
    suspend fun obtenerTodas(): List<VentaPreventa>

    @Insert
    suspend fun insertar(venta: VentaPreventa)

    @Update
    suspend fun actualizar(venta: VentaPreventa)

    @Delete
    suspend fun eliminar(venta: VentaPreventa)
}
