package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// CORRECCIÓN: Cambiamos "PlanillaDB" por "AppDatabase" en el constructor.
// Ahora este ViewModel espera y trabaja con nuestra AppDatabase.
class RegistroViewModel(private val db: AppDatabase) : ViewModel() {

    fun registrarAnimal(animal: Animal) {
        viewModelScope.launch {
            // Usará el animalDao de la AppDatabase que ha recibido.
            db.animalDao().registrar(animal)
        }
    }
}