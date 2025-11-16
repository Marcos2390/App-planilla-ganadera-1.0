// Contenido CORRECTO para RegistroViewModel.kt
package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistroViewModel(private val db: PlanillaDB) : ViewModel() {

    fun registrarAnimal(animal: Animal) {
        viewModelScope.launch {
            db.animalDao().registrar(animal)
        }
    }
}