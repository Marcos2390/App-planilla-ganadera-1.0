package com.example.planillarural

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnotacionDao {
    @Query("SELECT * FROM anotaciones ORDER BY fecha DESC")
    suspend fun obtenerTodas(): List<Anotacion>

    @Insert
    suspend fun insertar(anotacion: Anotacion)

    @Update
    suspend fun actualizar(anotacion: Anotacion)

    @Delete
    suspend fun eliminar(anotacion: Anotacion)
    
    // Para calcular totales (opcional pero útil)
    @Query("SELECT SUM(monto) FROM anotaciones WHERE tipo = 'Ingreso' AND moneda = :moneda")
    suspend fun totalIngresos(moneda: String): Double?

    @Query("SELECT SUM(monto) FROM anotaciones WHERE tipo = 'Gasto' AND moneda = :moneda")
    suspend fun totalGastos(moneda: String): Double?
}