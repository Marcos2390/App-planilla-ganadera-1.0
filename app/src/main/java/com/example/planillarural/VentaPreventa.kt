package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas_preventa")
data class VentaPreventa(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caravana: String,
    val raza: String,
    val sexo: String,
    val kilos: Double,
    val precio: Double = 0.0, // ¡NUEVO!
    val fecha: String,
    val estado: String = "Pendiente" // Pendiente, Vendido
)
