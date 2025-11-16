// Archivo: app/src/main/java/com/example/planillarural/Animal.kt

package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animales")
data class Animal(
    @PrimaryKey(autoGenerate = true) // CORRECCIÓN: Definir 'id' como clave primaria autoincremental.
    val id: Int = 0, // Es buena práctica inicializar el ID para que no tengas que pasarlo al crear un nuevo animal.
    val nombre: String,
    val categoria: String,
    val raza: String,
    val fechaNac: String
)
