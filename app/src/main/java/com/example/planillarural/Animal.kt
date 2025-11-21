package com.example.planillarural

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "animales",
    foreignKeys = [ForeignKey(
        entity = Potrero::class,
        parentColumns = ["id"],
        childColumns = ["potreroId"],
        onDelete = ForeignKey.SET_NULL // Si borro el potrero, el animal queda sin asignar (no se borra)
    )],
    indices = [Index("potreroId")]
)
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val raza: String,
    val fechaNac: String,
    val informacionAdicional: String? = null,
    val color: String? = null,
    val especie: String = "Bovino",
    val status: String = "Activo",
    val potreroId: Int? = null // ¡NUEVO! Guarda dónde está el animal
)