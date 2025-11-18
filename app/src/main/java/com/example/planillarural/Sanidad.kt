package com.example.planillarural

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sanidad",
    // ¡CORRECCIÓN CLAVE! Se elimina onDelete = ForeignKey.CASCADE
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["id"],
        childColumns = ["animalId"]
    )],
    indices = [Index(value = ["animalId"])]
)
data class Sanidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val animalId: Int?,
    val fecha: String,
    val tratamiento: String,
    val producto: String,
    val dosis: String,
    val fechaProximaDosis: String? = null
)
