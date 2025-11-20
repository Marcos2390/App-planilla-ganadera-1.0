package com.example.planillarural

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    // RESTAURADO: Borrado en cascada activo para permitir eliminar animales.
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["animalId"])]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val animalId: Int?, 
    val tipo: String,
    val fecha: String,
    val cantidad: Int,
    val motivo: String,
    val categoria: String,
    val especie: String = "Bovino" // ¡RESTAURADO!
)
