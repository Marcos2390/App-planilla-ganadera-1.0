// Archivo: app/src/main/java/com/example/planillarural/Sanidad.kt

package com.example.planillarural

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sanidad",
    // CORRECCIÓN 1: Declarar la clave foránea que conecta 'animalId' con el 'id' de la tabla 'animales'.
    foreignKeys = [ForeignKey(
        entity = Animal::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.CASCADE // Opcional pero recomendado: Si borras un Animal, se borran sus registros de Sanidad.
    )],
    // CORRECCIÓN 2: Crear un índice en la columna de la clave foránea para optimizar las búsquedas.
    indices = [Index(value = ["animalId"])]
)
data class Sanidad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val animalId: Int, // Esta columna apunta al 'id' de un Animal.
    val fecha: String,
    val tratamiento: String,
    val producto: String,
    val dosis: String
)