package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// CORRECCIÓN: Cambiamos "PlanillaDB" por "AppDatabase" en el constructor.
// Ahora esta clase espera recibir una AppDatabase.
class RegistroViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            // Le pasaremos la AppDatabase al RegistroViewModel
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
