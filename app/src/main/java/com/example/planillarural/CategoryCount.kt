package com.example.planillarural

// Esta clase no es una tabla, solo sirve para almacenar el resultado
// de una consulta específica de conteo.
data class CategoryCount(
    val categoria: String,
    val count: Int
)