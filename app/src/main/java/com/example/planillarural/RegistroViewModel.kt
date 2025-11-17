package com.example.planillarural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistroViewModel(private val db: AppDatabase) : ViewModel() {

    fun registrarAnimal(animal: Animal) {
        viewModelScope.launch {
            db.animalDao().registrar(animal)
        }
    }

    // ¡NUEVA FUNCIÓN! Obtiene un animal por su ID.
    suspend fun obtenerAnimalPorId(animalId: Int): Animal? {
        return db.animalDao().obtenerPorId(animalId)
    }
}