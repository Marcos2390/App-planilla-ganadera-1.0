// Contenido CORRECTO para RegistroViewModelFactory.kt
package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegistroViewModelFactory(private val db: PlanillaDB) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
