package com.example.planillarural

import androidx.room.*

@Dao
interface NacimientoPendienteDao {
    @Insert
    suspend fun insertar(nacimiento: NacimientoPendiente)

    @Update
    suspend fun actualizar(nacimiento: NacimientoPendiente)

    @Query("SELECT * FROM nacimientos_pendientes WHERE cantidadAsignada < cantidadTotal ORDER BY fecha ASC")
    suspend fun obtenerTodosPendientes(): List<NacimientoPendiente>

    @Query("SELECT * FROM nacimientos_pendientes WHERE id = :id")
    suspend fun obtenerPorId(id: Int): NacimientoPendiente?
}