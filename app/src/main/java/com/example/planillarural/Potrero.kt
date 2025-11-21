package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potreros")
data class Potrero(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,       // Ej: "El Bajo", "Loma Alta"
    val descripcion: String,  // Ej: "Pastura natural"
    val hectareas: Double     // Ej: 50.5
)