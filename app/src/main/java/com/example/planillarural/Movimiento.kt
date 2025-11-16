package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimiento")
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val animalId: Int,
    val tipo: String,     // Ej: "Entrada", "Salida"
    val fecha: String,
    val destino: String? = null
)