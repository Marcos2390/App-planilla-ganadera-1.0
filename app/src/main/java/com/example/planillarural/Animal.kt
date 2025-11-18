package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val raza: String,
    val fechaNac: String,
    val informacionAdicional: String? = null,
    val color: String? = null // ¡NUEVO CAMPO PARA EL COLOR!
)
