package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nacimientos_pendientes")
data class NacimientoPendiente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,
    val categoria: String,
    val cantidadTotal: Int,
    var cantidadAsignada: Int = 0 // Contador de cuántos ya hemos identificado
)
