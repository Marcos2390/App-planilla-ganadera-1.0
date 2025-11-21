package com.example.planillarural

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PotreroDao {
    @Query("SELECT * FROM potreros")
    suspend fun obtenerTodos(): List<Potrero>
    
    @Query("SELECT * FROM potreros WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Potrero?

    @Insert
    suspend fun insertarPotrero(potrero: Potrero)

    @Query("DELETE FROM potreros WHERE id = :potreroId")
    suspend fun eliminarPotrero(potreroId: Int)

    // --- Lotes ---
    @Insert
    suspend fun insertarLote(lote: LotePotrero)

    @Query("SELECT * FROM lotes_potrero WHERE potreroId = :potreroId")
    suspend fun obtenerLotesDePotrero(potreroId: Int): List<LotePotrero>
    
    @Delete
    suspend fun eliminarLote(lote: LotePotrero)
}