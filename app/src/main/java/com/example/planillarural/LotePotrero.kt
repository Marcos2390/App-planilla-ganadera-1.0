package com.example.planillarural

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "lotes_potrero",
    foreignKeys = [ForeignKey(
        entity = Potrero::class,
        parentColumns = ["id"],
        childColumns = ["potreroId"],
        onDelete = ForeignKey.CASCADE // Si borro el potrero, se borran sus lotes
    )]
)
data class LotePotrero(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potreroId: Int,
    val especie: String,      // "Bovino" o "Ovino"
    val categoria: String,    // "Vaca", "Cordero"
    val cantidad: Int
)