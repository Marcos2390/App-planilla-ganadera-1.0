package com.example.planillarural

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anotaciones")
data class Anotacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,
    val titulo: String,       // Ej: "Venta de lana" o "Recordatorio vacunas"
    val descripcion: String,  // Detalle más largo
    val tipo: String,         // "Ingreso", "Gasto", "Nota"
    val monto: Double = 0.0,  // Cantidad de dinero (0 si es solo nota)
    val moneda: String = "ARS" // "ARS" (Pesos) o "USD" (Dólares)
)
